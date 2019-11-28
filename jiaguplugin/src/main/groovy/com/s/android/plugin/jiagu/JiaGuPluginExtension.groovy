package com.s.android.plugin.jiagu

import com.s.android.plugin.jiagu.entity.FirUploadEntity
import org.gradle.api.Action
import org.gradle.api.Project

class JiaGuPluginExtension {

    boolean debug = false // 调试模式开关
    boolean enable = true // 插件开关
    boolean debugOn = false // 是否在debug时开启插件
    boolean jiaguEnable = true // 是否加固
    boolean firEnable = false // 是否上传至fir
    String jiaGuDir = null //加固文件地址 必填
    String username = null //用户名 必填
    String password = null//密码 必填
    File storeFile = null //签名文件 默认获取android.buildTypes.release.signingConfig.storeFile
    String storePassword = null //签名密码 默认获取android.buildTypes.release.signingConfig.storePassword
    String keyAlias = null //别名 默认获取android.buildTypes.release.signingConfig.keyAlias
    String keyPassword = null ///别名密码 默认获取android.buildTypes.release.signingConfig.keyPassword
    File channelFile = null //指向通道备注文件.txt 默认null
    String inputFilePath = null //输入apk路径 默认获取applicationVariants.outputs.outputFile
    String outputFileDir = null //输出apk文件地址 默认输出${project.buildDir.getAbsolutePath()}\jiagu
    String config = null //扩展配置 默认配置-crashlog -x86 -analyse

    FirUploadEntity fir = new FirUploadEntity()

    //创建内部Extension，名称为方法名 fir
    void fir(Action<FirUploadEntity> action) {
        action.execute(fir)
    }

    JiaGuPluginExtension(Project project) {
    }

    @Override
    String toString() {
        return "JiaGuPluginExtension{" +
                "debug=" + debug +
                ", enable=" + enable +
                ", jiaguEnable=" + jiaguEnable +
                ", firEnable=" + firEnable +
                ", firBundleId='" + firBundleId + '\'' +
                ", firApiToken='" + firApiToken + '\'' +
                ", appName='" + appName + '\'' +
                ", firChangeLog='" + firChangeLog + '\'' +
                ", jiaGuDir='" + jiaGuDir + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", storeFile=" + storeFile +
                ", storePassword='" + storePassword + '\'' +
                ", keyAlias='" + keyAlias + '\'' +
                ", keyPassword='" + keyPassword + '\'' +
                ", channelFile=" + channelFile +
                ", inputFilePath='" + inputFilePath + '\'' +
                ", outputFileDir='" + outputFileDir + '\'' +
                ", config='" + config + '\'' +
                ", fir=" + fir +
                '}'
    }
}