# 360加固插件 [![JiaguPlugin](https://jitpack.io/v/com.github.keepLove/android-jiagu-plugin.svg)](https://jitpack.io/#com.github.keepLove/android-jiagu-plugin)

## Dependency

#### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
    allprojects {
        repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

#### Step 2. Add the dependency

```
    dependencies {
        ...
        classpath 'com.github.keepLove:android-jiagu-plugin:TAG'
	}
```

#### Step 3. Add the pulgin

```
apply plugin: 'jiagu'

jiagu {
    debug = false // debug 模式
    jiaGuDir = "D:\\360jiagubao_windows_64\\jiagu" // 360加固文件根目录 必填
    username = "xxxxx" // 360加固用户名 必填
    password = "xxx" // 360加固密码 必填
    signingConfig = null // 签名文件，默认读取buildType中的signingConfig 或名为'release'的签名文件
    channelFile = null // 指向通道备注文件.txt 默认null
    outputFileDir = null // 输出apk文件地址 默认输出 build\jiagu
    config = null // 扩展配置
    charsetName = "GBK" // 控制台输出编码方式，默认GBK
}


```

**签名**

插件使用 buildTypes 里面的签名配置

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

或者使用名为release的签名配置

```
    signingConfigs {
        release {
            ...
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

## Use

在Android Studio的Gradle窗口app/Tasks中有一个jiagu目录。

jiaGuApk开头是直接加固当前已有apk，jiaGuAssemble开头是运行assembleRelease/Debug之后进行加固。

## [360加固助手](https://jiagu.360.cn/#/global/download)

