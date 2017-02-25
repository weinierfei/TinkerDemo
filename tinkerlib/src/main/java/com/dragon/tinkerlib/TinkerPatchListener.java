/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dragon.tinkerlib;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerLoadResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;

/**
 * Description:过滤Tinker收到的补丁包的修复、升级请求
 *
 * @author: guoyongping
 * @date: 2017/1/3 下午4:17
 */
public class TinkerPatchListener extends DefaultPatchListener {

    private static final String TAG = "Tinker.TinkerPatchListener";

    protected static final long NEW_PATCH_RESTRICTION_SPACE_SIZE_MIN = 60 * 1024 * 1024;

    private final int maxMemory;

    public TinkerPatchListener(Context context) {
        super(context);
        maxMemory = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        TinkerLog.i(TAG, "application maxMemory:" + maxMemory);
    }

    /**
     * 检查是否要唤起:patch进程去尝试补丁合成
     * <p>
     * 若检查成功，我们会调用TinkerPatchService.runPatchService唤起:patch进程，去尝试完成补丁合成操作。反之，会回调检验失败的接口。
     * 事实上，你只需要复写patchCheck函数即可。若检查失败，会在LoadReporter的onLoadPatchListenerReceiveFail中回调
     *
     * @param path 补丁路径
     * @return
     */
    @Override
    public int patchCheck(String path) {
        File patchFile = new File(path);
        TinkerLog.i(TAG, "receive a patch file: %s, file size:%d", path, SharePatchFileUtil.getFileOrDirectorySize
                (patchFile));
        int returnCode = super.patchCheck(path);

        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            returnCode = TinkerUtils.checkForPatchRecover(NEW_PATCH_RESTRICTION_SPACE_SIZE_MIN, maxMemory);
        }

        if (returnCode == ShareConstants.ERROR_PATCH_OK) {
            String patchMd5 = SharePatchFileUtil.getMD5(patchFile);
            SharedPreferences sp = context.getSharedPreferences(ShareConstants.TINKER_SHARE_PREFERENCE_CONFIG,
                    Context.MODE_MULTI_PROCESS);
            //optional, only disable this patch file with md5
            int fastCrashCount = sp.getInt(patchMd5, 0);
            if (fastCrashCount >= TinkerUncaughtExceptionHandler.MAX_CRASH_COUNT) {
                returnCode = TinkerUtils.ERROR_PATCH_CRASH_LIMIT;
            } else {
                //for upgrade patch, version must be not the same
                //for repair patch, we won't has the tinker load flag
                Tinker tinker = Tinker.with(context);

                if (tinker.isTinkerLoaded()) {
                    TinkerLoadResult tinkerLoadResult = tinker.getTinkerLoadResultIfPresent();
                    if (tinkerLoadResult != null) {
                        String currentVersion = tinkerLoadResult.currentVersion;
                        if (patchMd5.equals(currentVersion)) {
                            returnCode = TinkerUtils.ERROR_PATCH_ALREADY_APPLY;
                        }
                    }
                }
            }
            //check whether retry so many times
            if (returnCode == ShareConstants.ERROR_PATCH_OK) {
                returnCode = UpgradePatchRetry.getInstance(context).onPatchListenerCheck(patchMd5) ? ShareConstants
                        .ERROR_PATCH_OK : TinkerUtils.ERROR_PATCH_RETRY_COUNT_LIMIT;
            }
        }

        TinkerReport.onTryApply(returnCode == ShareConstants.ERROR_PATCH_OK);
        return returnCode;
    }
}
