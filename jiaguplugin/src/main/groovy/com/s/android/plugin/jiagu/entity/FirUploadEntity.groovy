package com.s.android.plugin.jiagu.entity

class FirUploadEntity {

    String firBundleId = null // 默认获取applicationId
    String firApiToken = null // fir api token  必填
    String appName = null // App Name
    String firChangeLog = null // 更新日志
    File apkFile = null // apk fill
    String versionCode = null
    String versionName = null

    @Override
    String toString() {
        return "FirUploadEntity{" +
                "firBundleId='" + firBundleId + '\'' +
                ", firApiToken='" + firApiToken + '\'' +
                ", appName='" + appName + '\'' +
                ", firChangeLog='" + firChangeLog + '\'' +
                '}'
    }

}