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

import android.os.Handler;
import android.os.Looper;

import com.tencent.tinker.lib.service.DefaultTinkerResultService;
import com.tencent.tinker.lib.service.PatchResult;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;

import java.io.File;

/**
 * Description:patch补丁合成进程将合成结果返回给主进程的类
 * 定义:当应用进入后台或者手机锁屏时重启应用,以便补丁尽快生效
 *
 * @author: guoyongping
 * @date: 2017/1/3 下午3:06
 */
public class TinkerResultService extends DefaultTinkerResultService {

    private static final String TAG = "Tinker.TinkerResultService";

    /**
     * 补丁加载结果
     *
     * @param result 结果集(序列化实体)
     *               isSuccess	        补丁合成操作是否成功。
     *               rawPatchFilePath	原始的补丁包路径。
     *               costTime	        本次补丁合成的耗时。
     *               e	                本次补丁合成是否出现异常，null为没有异常。
     *               patchVersion	    补丁文件的md5, 有可能为空@Nullable。
     */
    @Override
    public void onPatchResult(final PatchResult result) {
        if (result == null) {
            TinkerLog.e(TAG, "TinkerResultService received null result!!!!");
            return;
        }
        TinkerLog.i(TAG, "TinkerResultService receive result: %s", result.toString());

        //first, we want to kill the recover process
        TinkerServiceInternals.killTinkerPatchServiceProcess(getApplicationContext());

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (result.isSuccess) {
                    //补丁加载成功,重启进程生效 (此处上报给服务器)
                    TinkerLog.i(TAG, "patch success, please restart procss");
                    //PatchManager.getInstance().onApplySuccess(result.rawPatchFilePath);
                } else {
                    //补丁加载失败(上报给服务器)
                    TinkerLog.i(TAG, "patch fail, please check reason");
                    //PatchManager.getInstance().onApplyFailure(result.rawPatchFilePath,"");
                }
            }
        });
        // is success and newPatch, it is nice to delete the raw file, and restart at once
        // for old patch, you can't delete the patch file
        if (result.isSuccess) {
            deleteRawPatchFile(new File(result.rawPatchFilePath));

            //not like TinkerResultService, I want to restart just when I am at background!
            //if you have not install tinker this moment, you can use TinkerApplicationHelper api
            if (checkIfNeedKill(result)) {
                if (TinkerUtils.isBackground()) {
                    TinkerLog.i(TAG, "it is in background, just restart process");
                    restartProcess();
                } else {
                    //we can wait process at background, such as onAppBackground
                    //or we can restart when the screen off
                    TinkerLog.i(TAG, "tinker wait screen to restart process");
                    new TinkerUtils.ScreenState(getApplicationContext(), new TinkerUtils.ScreenState.IOnScreenOff() {
                        @Override
                        public void onScreenOff() {
                            restartProcess();
                        }
                    });
                }
            } else {
                TinkerLog.i(TAG, "I have already install the newly patch version!");
            }
        }
    }

    /**
     * you can restart your process through service or broadcast
     */
    private void restartProcess() {
        TinkerLog.i(TAG, "app is background now, i can kill quietly");
        //you can send service or broadcast intent to restart your process
        android.os.Process.killProcess(android.os.Process.myPid());
        //reset(getApplication());
    }
}
