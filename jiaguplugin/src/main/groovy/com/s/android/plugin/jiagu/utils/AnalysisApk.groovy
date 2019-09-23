package com.s.android.plugin.jiagu.utils

import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta

class AnalysisApk {

    static ApkMeta getAppInfo(File apkFile) {
        return new ApkFile(apkFile).apkMeta
    }

}