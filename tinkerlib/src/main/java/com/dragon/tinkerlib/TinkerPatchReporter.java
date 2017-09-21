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

import android.content.Context;
import android.content.Intent;

import com.tencent.tinker.lib.reporter.DefaultPatchReporter;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.SharePatchInfo;

import java.io.File;
import java.util.List;

/**
 * Description:Tinker在修复或者升级补丁时的一些回调
 *
 * @author: guoyongping
 * @date: 2017/1/3 下午4:01
 */
public class TinkerPatchReporter extends DefaultPatchReporter {

    private static final String TAG = "Tinker.TinkerPatchReporter";

    public TinkerPatchReporter(Context context) {
        super(context);
    }

    /**
     * Patch进程启动时的回调
     *
     * @param intent
     */
    @Override
    public void onPatchServiceStart(Intent intent) {
        super.onPatchServiceStart(intent);
        TinkerReport.onApplyPatchServiceStart();
        TinkerLog.i(TAG, "patch进程启动.intent==" + intent);
    }

    /**
     * 对合成的dex文件提前进行dexopt时出现异常
     *
     * @param patchFile
     * @param dexFiles
     * @param t
     */
    @Override
    public void onPatchDexOptFail(File patchFile, List<File> dexFiles, Throwable t) {
        super.onPatchDexOptFail(patchFile, dexFiles, t);
        TinkerReport.onApplyDexOptFail(t);
    }

    /**
     * 在补丁合成过程捕捉到异常
     *
     * @param patchFile 补丁文件
     * @param e         异常信息
     */
    @Override
    public void onPatchException(File patchFile, Throwable e) {
        super.onPatchException(patchFile, e);
        TinkerReport.onApplyCrash(e);
    }

    @Override
    public void onPatchInfoCorrupted(File patchFile, String oldVersion, String newVersion) {
        super.onPatchInfoCorrupted(patchFile, oldVersion, newVersion);
        TinkerReport.onApplyInfoCorrupted();
    }

    @Override
    public void onPatchPackageCheckFail(File patchFile, int errorCode) {
        super.onPatchPackageCheckFail(patchFile, errorCode);
        TinkerReport.onApplyPackageCheckFail(errorCode);
    }

    /**
     * 补丁合成结果回调
     *
     * @param patchFile 补丁文件
     * @param success   合成结果
     * @param cost      合成时长
     */
    @Override
    public void onPatchResult(File patchFile, boolean success, long cost) {
        super.onPatchResult(patchFile, success, cost);
        TinkerReport.onApplied(cost, success);
    }

    /**
     * 从补丁包与原始安装包中合成某种类型的文件出错
     *
     * @param patchFile 补丁文件
     * @param extractTo 原始文件
     * @param filename  文件名
     * @param fileType  文件类型
     */
    @Override
    public void onPatchTypeExtractFail(File patchFile, File extractTo, String filename, int fileType) {
        super.onPatchTypeExtractFail(patchFile, extractTo, filename, fileType);
        TinkerReport.onApplyExtractFail(fileType);
    }

    @Override
    public void onPatchVersionCheckFail(File patchFile, SharePatchInfo oldPatchInfo, String patchFileVersion) {
        super.onPatchVersionCheckFail(patchFile, oldPatchInfo, patchFileVersion);
        TinkerReport.onApplyVersionCheckFail();
    }
}
