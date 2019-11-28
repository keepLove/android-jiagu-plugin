# JiaguPlugin [![](https://img.shields.io/bintray/v/shuaijianwen/android/jiaguplugin.svg)](https://jcenter.bintray.com/com/s/android/plugin/jiaguplugin/) [ ![Download](https://api.bintray.com/packages/shuaijianwen/android/jiaguplugin/images/download.svg?version=1.3.0) ](https://bintray.com/shuaijianwen/android/jiaguplugin/1.3.0/link)

### Description

360加固和自动上传fir.im 的Gradle插件，当执行assemble${variantName}时自动进行apk加固、上传。

如果需要加固，需要先下载[360加固助手](http://jiagu.360.cn/#/global/download)

### Adding to project

在项目的buid.gradle文件的dependencies（buildscript部分）中添加：
```
buildscript {
    repositories {
        ...
        jcenter()
    }
    dependencies {
        ...
        classpath 'com.s.android.plugin:jiaguplugin:latest.release'
    }
}
```

在module的buid.gradle文件的顶部添加：

```
...
apply plugin: 'jiagu'

jiagu {
    jiaGuDir = ""
    username = ""
    password = ""
    fir {
        firApiToken = ""
        firChangeLog = ""
    }
}
```
**其中jiaGuDir、username和password是必填的。**

**如果firEnable为true，firApiToken必填**

还可以设置其他属性，属性列表如下：

|       属性        |	    类型    |                          默认值                           |                    说 明                    |
|:-----------------:|:-------------:|:---------------------------------------------------------:|:---------------------------:|
|    debug          |    boolean    |     false                                                 |    调试模式开关，会打印更多log，自动加固        |
|    enable         |    boolean    |     true                                                  |    插件开关                   |
|    debugOn        |    boolean    |     false                                                 |    debug时是否启动插件                   |
|    jiaguEnable    |    boolean    |     true                                                  |    加固开关                   |
|    firEnable      |    boolean    |     false                                                 |    fir上传开关                   |
|    jiaGuDir       |    String     |     null                                                  |    360加固助手安装地址\jiagu 类似D:\360jiagubao_windows_64\jiagu         |
|    username       |    String     |     null                                                  |    360加固助手登录用户名                   |
|    password       |    String     |     null                                                  |    360加固助手登录密码                    |
|    storeFile      |    File       |     android.buildTypes.release.signingConfig.storeFile    |    签名文件（具体说明见下文“签名”）   |
|    storePassword  |    String     |     android.buildTypes.release.signingConfig.storePassword|    签名密码（具体说明见下文“签名”）   |
|    keyAlias       |    String     |     android.buildTypes.release.signingConfig.keyAlias     |    别名（具体说明见下文“签名”）   |
|    keyPassword    |    String     |     android.buildTypes.release.signingConfig.keyPassword  |    别名密码（具体说明见下文“签名”） |
|    channelFile    |    File       |     null                                                  |    多渠道打包设置，选择.txt文件，下载的jiagu包里有多渠道模板.txt  |
|    inputFilePath  |    String     |     applicationVariants.outputs.outputFile                |    打包的apk路径   |
|    outputFileDir  |    String     |     ${project.buildDir.getAbsolutePath()}\jiagu           |    加固后apk的输出路径，app\build\jiagu   |
|    config         |    String     |     -crashlog -x86 -analyse                               |    加固配置，默认选择崩溃日志服务、支持x86架构设备、选择数据分析服务   |
|    firApiToken    |    String     |     fir API Token                                         |    鼠标悬浮头像，出现API Token 按钮，点击   |
|    firChangeLog   |    String     |     null                                                  |    更新说明   |

**签名**

插件模式使用 buildTypes 里面的签名配置
```
android {
    ...
    buildTypes {
        release {
            ...
            signingConfig signingConfigs.release
        }
    }
}
```

**config**

这个是360加固助手的配置加固可选项，高级加固选项需要会员 [前往](http://jiagu.360.cn/#/global/vip/packages)
```
 ----------------------可选增强服务-------------------------------
         [-crashlog]                             【崩溃日志分析】
         [-x86]                                  【x86支持】
         [-analyse]                              【加固数据分析】
         [-nocert]                               【跳过签名校验】
 ----------------------高级加固选项-------------------------------
         [-vmp]                                  【全VMP保护】
         [-data]                                 【本地数据文件保护】
         [-assets]                               【资源文件保护】
         [-filecheck]                            【文件完整性校验】
         [-ptrace]                               【Ptrace防注入】
         [-so]                                   【SO文件保护】
         [-dex2c]                                【dex2C保护】
         [-string_obfus]                         【字符串加密】
         [-dex_shadow]                           【DexShadow】
         [-so_private]                           【SO防盗用】
```

**说明**

- 插件会在执行assemble${variantName}前生成sJiaGu${variantName}方法
- 插件会在Release编译打包的时候自动启动
- 如果debugOn=true，插件会在debug编译时自动启动


