package com.s.android.plugin.jiagu

import org.gradle.api.Project

class JiaGuPluginExtension {

    boolean debug = false // 调试模式开关
    boolean enable = true // 插件开关
    String jiaGuDir //加固文件地址
    String username //用户名
    String password //密码
    File storeFile //签名文件 默认获取android.buildTypes.release.signingConfig.storeFile
    String storePassword//签名密码 android.buildTypes.release.signingConfig.storePassword
    String keyAlias//别名 android.buildTypes.release.signingConfig.keyAlias
    String keyPassword ///别名密码 android.buildTypes.release.signingConfig.keyPassword
    File channelFile //指向通道备注文件.txt
    String inputFilePath//输入apk路径 默认获取applicationVariants.outputs.outputFile
    String outputFileDir//输出apk文件地址 默认输出${project.buildDir.getAbsolutePath()}\jiagu
    String config//扩展配置 默认配置-crashlog -x86 -analyse
    JiaGuPluginExtension(Project project) {
    }
}