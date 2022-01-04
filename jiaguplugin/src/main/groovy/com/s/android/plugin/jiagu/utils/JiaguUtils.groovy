package com.s.android.plugin.jiagu.utils

import com.android.build.gradle.api.BaseVariantOutput
import com.s.android.plugin.jiagu.entity.JiaGuPluginExtension

class JiaguUtils {

    private static String commandJiaGu
    private static String commandExt = ""

    static void jiagu(JiaGuPluginExtension jiaGuExtension, BaseVariantOutput variantOutput) {
        commandExt = ""
        commandJiaGu = "${jiaGuExtension.jiaGuDir}\\java\\bin\\java -jar ${jiaGuExtension.jiaGuDir}\\jiagu.jar "
        // 1，登录
        String result = login(jiaGuExtension)
        if (!result.contains("success")) {
            Logger.debug(result)
            throw new RuntimeException("360登录失败")
        }
        Logger.debug("360登录成功")
        // 2，导入签名keystore信息
        result = importSign(jiaGuExtension)
        if (!result.contains("succeed")) {
            Logger.debug(result)
            throw new RuntimeException("导入签名keystore失败")
        }
        Logger.debug("导入签名成功")
        // 3，导入渠道信息
        Logger.debug(importMulPkg(jiaGuExtension))
        // 4，配置加固服务
        result = setConfig(jiaGuExtension)
        if (result.contains("config saving succeed.")) {
            def indexOf = result.indexOf("已选增强服务")
            if (indexOf > -1) {
                result = result.substring(indexOf).trim()
            } else {
                result = "已选增强服务：${jiaGuExtension.config}"
            }
        }
        Logger.debug(result)
        Logger.debug("加固中... " + variantOutput.outputFile.path)
        // 5，加固
        result = jiaguStart(jiaGuExtension, variantOutput)
        if (result.contains("任务完成_已签名")) {
            result = "任务完成_已签名"
        }
        Logger.debug(result)
        Logger.debug("输出目录：${jiaGuExtension.outputFileDir}")
    }

    /**
     * 1.加固登录
     */
    private static String login(JiaGuPluginExtension jiaGuExtension) {
        return ProcessUtils.exec(commandJiaGu + " -login ${jiaGuExtension.username} ${jiaGuExtension.password}")
    }

    /**
     * 2.导入签名信息
     */
    private static String importSign(JiaGuPluginExtension jiaGuExtension) {
        if (jiaGuExtension.signingConfig != null) {
            commandExt += " -autosign "
            return ProcessUtils.exec(commandJiaGu + " -importsign ${jiaGuExtension.getSign()}")
        }
        return "未导入签名信息"
    }

    /**
     * 3.导入渠道信息
     */
    private static String importMulPkg(JiaGuPluginExtension jiaGuExtension) {
        if (jiaGuExtension.channelFile != null && jiaGuExtension.channelFile.exists()) {
            commandExt += " -automulpkg "
            return ProcessUtils.exec(commandJiaGu + " -importmulpkg ${jiaGuExtension.channelFile}")
        }
        return "未导入渠道信息"
    }

    /**
     * 4.配置加固服务
     */
    private static String setConfig(JiaGuPluginExtension jiaGuExtension) {
        // 配置加固服务
        return ProcessUtils.exec(commandJiaGu + " -config ${jiaGuExtension.config}")
    }

    /**
     * 5.加固
     */
    private static String jiaguStart(JiaGuPluginExtension jiaGuExtension, BaseVariantOutput variantOutput) {
        // 应用加固
        String cmd = commandJiaGu + " -jiagu ${variantOutput.outputFile.absolutePath} ${jiaGuExtension.outputFileDir}"
        return ProcessUtils.exec(cmd + commandExt)
    }
}