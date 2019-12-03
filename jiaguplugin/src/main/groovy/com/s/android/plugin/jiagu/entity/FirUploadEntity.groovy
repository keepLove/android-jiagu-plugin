package com.s.android.plugin.jiagu.entity

class FirUploadEntity {

    String firBundleId = null // 默认获取applicationId
    String firApiToken = null // fir api token  必填
    String appName = null // App Name
    String firChangeLog = null // 更新日志
    File apkFile = null // apk fill
    File iconFile = null // icon fill
    String versionCode = null
    String versionName = null

    String iconName
    byte[] iconData

    @Override
    String toString() {
        return "FirUploadEntity{" +
                "firBundleId='" + firBundleId + '\'' +
                ", firApiToken='" + firApiToken + '\'' +
                ", appName='" + appName + '\'' +
                ", firChangeLog='" + firChangeLog + '\'' +
                ", apkFile=" + apkFile +
                ", iconFile=" + iconFile +
                ", versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", iconName='" + iconName + '\'' +
                ", iconData=" + Arrays.toString(iconData) +
                '}'
    }
}