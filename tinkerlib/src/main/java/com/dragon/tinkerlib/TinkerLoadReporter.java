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
import android.os.Looper;
import android.os.MessageQueue;

import com.tencent.tinker.lib.reporter.DefaultLoadReporter;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.UpgradePatchRetry;
import com.tencent.tinker.loader.shareutil.ShareConstants;

import java.io.File;

/**
 * Description:Tinker在加载补丁时的一些回调
 * 1.回调运行在加载的进程(patch进程)，它有可能是各个不一样的进程。我们可以通过tinker.isMainProcess
 * 或者tinker.isPatchProcess知道当前是否是主进程，patch补丁合成进程。
 * 2.回调发生的时机是我们调用installTinker之后，某些进程可能并不需要installTinker。
 *
 * @author: guoyongping
 * @date: 2017/1/3 下午2:29
 */
public class TinkerLoadReporter extends DefaultLoadReporter {

    private final static String TAG = "Tinker.TinkerLoadReporter";

    public TinkerLoadReporter(Context context) {
        super(context);
    }

    /**
     * 补丁校验不通过回调 (此处可上报服务器)
     *
     * @param patchFile 补丁文件路径
     * @param errorCode 错误码
     *                  errorCode           desc
     *                  -1         当前tinkerFlag为不可用状态。
     *                  -2         输入的临时补丁包文件不存在。
     *                  -3         当前:patch补丁合成进程正在运行。
     *                  -4         不能在:patch补丁合成进程，发起补丁的合成请求。
     */
    @Override
    public void onLoadPatchListenerReceiveFail(final File patchFile, int errorCode) {
        super.onLoadPatchListenerReceiveFail(patchFile, errorCode);
        TinkerReport.onTryApplyFail(errorCode);
        TinkerLog.e(TAG, "补丁校验失败  :patchFile==" + patchFile + "      errorCode=" + errorCode);
    }

    /**
     * 无论加载失败或者成功都会回调的接口，它返回了本次加载所用的时间、返回码等信息
     *
     * @param patchDirectory 补丁文件
     * @param loadCode       返回码
     * @param cost           加载时长
     */
    @Override
    public void onLoadResult(File patchDirectory, int loadCode, final long cost) {
        super.onLoadResult(patchDirectory, loadCode, cost);
        TinkerLog.i(TAG, "onLoadResult回调 :rstCode==" + loadCode + "     cost==" + cost);
        switch (loadCode) {
            case ShareConstants.ERROR_LOAD_OK:
                TinkerReport.onLoaded(cost);
                break;
            default:
                break;
        }
        Looper.getMainLooper().myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                if (UpgradePatchRetry.getInstance(context).onPatchRetryLoad()){
                    TinkerReport.onReportRetryPatch();
                }
                return false;
            }
        });
    }


    /**
     * 加载过程中捕获的异常
     * 默认:卸载补丁
     * errorCode           desc
     * -1           没有捕获到的java crash
     * -2           在加载dex过程中捕获到的crash
     * -3           在加载res过程中捕获到的crash
     * -4           没有捕获到的非java crash,这个是补丁机制的安全模式
     *
     * @param e         异常信息
     * @param errorCode 错误码
     */
    @Override
    public void onLoadException(Throwable e, int errorCode) {
        super.onLoadException(e, errorCode);
        TinkerReport.onLoadException(e, errorCode);
    }

    /**
     * md5不一致回调
     * 部分文件的md5与meta中定义的不一致。默认我们为了安全考虑，依然会清空补丁。
     *
     * @param file     补丁文件
     * @param fileType 补丁类型
     */
    @Override
    public void onLoadFileMd5Mismatch(File file, int fileType) {
        super.onLoadFileMd5Mismatch(file, fileType);
        TinkerReport.onLoadFileMisMatch(fileType);
    }

    /**
     * 在加载过程中，发现部分文件丢失的回调(dex opt的重试生成)
     * [默认若是dex，dex优化文件或者lib文件丢失，我们将尝试从补丁包去修复这些丢失的文件。若补丁包或者版本文件丢失，将卸载补丁包。]
     *
     * @param file        补丁文件
     * @param fileType    补丁类型
     * @param isDirectory 是否文件夹
     */
    @Override
    public void onLoadFileNotFound(File file, int fileType, boolean isDirectory) {
        super.onLoadFileNotFound(file, fileType, isDirectory);
        TinkerReport.onLoadFileNotFound(fileType);
    }

    /**
     * 加载过程中补丁包检查失败回调,可通过错误码区分
     * errorCode              desc
     * -1            签名校验失败
     * -2            找不到"assets/package_meta.txt"文件
     * -3            assets/dex_meta.txt信息损坏
     * -4            assets/so_meta.txt信息损坏
     * -5            找不到基准apk AndroidManifest中的TINKER_ID
     * -6            找不到补丁中"assets/package_meta.txt"中的TINKER_ID
     * -7            基准版本与补丁定义的TINKER_ID不相等
     * -8            assets/res_meta.txt信息损坏
     * -9            tinkerFlag不支持补丁中的某些类型的更改，例如补丁中存在资源更新，但是使用者指定不支持资源类型更新。
     *
     * @param patchFile 补丁路径
     * @param errorCode 错误码
     */
    @Override
    public void onLoadPackageCheckFail(File patchFile, int errorCode) {
        super.onLoadPackageCheckFail(patchFile, errorCode);
        TinkerReport.onLoadPackageCheckFail(errorCode);
        TinkerLog.e(TAG, "LoadPackageCheckFail.errorCode==" + errorCode);
    }

    /**
     * 补丁包版本管理文件(patch.info)损坏回调
     * [默认:卸载补丁包]
     *
     * @param oldVersion    旧版本号
     * @param newVersion    新版本号
     * @param patchInfoFile 补丁版本管理文件
     */
    @Override
    public void onLoadPatchInfoCorrupted(String oldVersion, String newVersion, File patchInfoFile) {
        super.onLoadPatchInfoCorrupted(oldVersion, newVersion, patchInfoFile);
        TinkerReport.onLoadInfoCorrupted();
    }

    @Override
    public void onLoadInterpret(int type, Throwable e) {
        super.onLoadInterpret(type, e);
        TinkerReport.onLoadInterpretReport(type, e);
    }

    /**
     * 补丁版本升级的回调  (只会在主进程调用)
     *
     * @param oldVersion         旧版本号
     * @param newVersion         新版本号
     * @param patchDirectoryFile 补丁文件
     * @param currentPatchName   当前补丁名称
     */
    @Override
    public void onLoadPatchVersionChanged(String oldVersion, String newVersion, File patchDirectoryFile, String
            currentPatchName) {
        super.onLoadPatchVersionChanged(oldVersion, newVersion, patchDirectoryFile, currentPatchName);
    }

}
