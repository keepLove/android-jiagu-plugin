package com.s.android.plugin.jiagu.utils

import com.s.android.plugin.jiagu.JiaGuPluginExtension
import com.s.android.plugin.jiagu.Logger

class JiaguUtils {

    static boolean debug = false
    private static String commandJiaGu
    private static String commandExt = ""

    static void jiagu(JiaGuPluginExtension jiaGuPluginExtension) {
        ProcessUtils.debug = debug
        commandExt = ""
        commandJiaGu = "${jiaGuPluginExtension.jiaGuDir}\\java\\bin\\java -jar ${jiaGuPluginExtension.jiaGuDir}\\jiagu.jar "
        // 登录
        String result = login(jiaGuPluginExtension)
        if (result.contains("success")) {
            Logger.debug("login success")
            // 导入签名keystore信息
            result = importSign(jiaGuPluginExtension)
            if (result.contains("succeed")) {
                result = "导入签名 succeed"
            }
            Logger.debug(result)
            // 导入渠道信息
            Logger.debug(importMulPkg(jiaGuPluginExtension))
            // 配置加固服务
            result = setConfig(jiaGuPluginExtension)
            if (result.contains("config saving succeed.")) {
                def indexOf = result.indexOf("已选增强服务")
                if (indexOf > -1) {
                    result = result.substring(indexOf).trim()
                } else {
                    result = "已选增强服务：${jiaGuPluginExtension.config}"
                }
            }
            Logger.debug(result)
            Logger.debug("加固中........")
            // 加固
            result = jiaguStart(jiaGuPluginExtension)
            if (result.contains("任务完成_已签名")) {
                result = "任务完成_已签名"
            }
            Logger.debug(result)
            Logger.debug("输出目录：${jiaGuPluginExtension.outputFileDir}")
        } else {
            Logger.debug(result)
            throw new RuntimeException("登录失败")
        }
    }

    /**
     * 1.加固登录
     */
    private static String login(JiaGuPluginExtension jiaGuPluginExtension) {
        return ProcessUtils.exec(commandJiaGu + " -login ${jiaGuPluginExtension.username} ${jiaGuPluginExtension.password}")
    }

    /**
     * 2.导入签名信息
     */
    private static String importSign(JiaGuPluginExtension jiaGuPluginExtension) {
        if (jiaGuPluginExtension.storeFile != null && jiaGuPluginExtension.storeFile.exists()) {
            commandExt += " -autosign "
            return ProcessUtils.exec(commandJiaGu + " -importsign ${jiaGuPluginExtension.storeFile.getAbsolutePath()}" +
                    " ${jiaGuPluginExtension.storePassword}  ${jiaGuPluginExtension.keyAlias}  ${jiaGuPluginExtension.keyPassword}")
        }
        return "未导入签名信息"
    }

    /**
     * 3.导入渠道信息
     */
    private static String importMulPkg(JiaGuPluginExtension jiaGuPluginExtension) {
        if (jiaGuPluginExtension.channelFile != null && jiaGuPluginExtension.channelFile.exists()) {
            commandExt += " -automulpkg "
            return ProcessUtils.exec(commandJiaGu + " -importmulpkg ${jiaGuPluginExtension.channelFile}")
        }
        return "未导入渠道信息"
    }

    /**
     * 4.配置加固服务
     */
    private static String setConfig(JiaGuPluginExtension jiaGuPluginExtension) {
        // 配置加固服务
        return ProcessUtils.exec(commandJiaGu + " -config ${jiaGuPluginExtension.config}")
    }

    /**
     * 5.加固
     */
    private static String jiaguStart(JiaGuPluginExtension jiaGuPluginExtension) {
        // 应用加固
        String cmd = commandJiaGu + " -jiagu ${jiaGuPluginExtension.inputFilePath} ${jiaGuPluginExtension.outputFileDir}"
        return ProcessUtils.exec(cmd + commandExt)
    }
}