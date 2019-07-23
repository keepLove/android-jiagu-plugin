package com.s.android.plugin.jiagu

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class JiaGuTask extends DefaultTask {

    static final String NAME = "sJiaGu"

    private String commandJiaGu
    private String commandExt = ""
    private JiaGuPluginExtension jiaGuPluginExtension
    private static boolean debug = false

    JiaGuTask() {
        group = "JiaGu"
        description = "360 jiagu plugin"
    }

    /**
     * 加固登录
     */
    private String login() {
        return exec(commandJiaGu + " -login ${jiaGuPluginExtension.username} ${jiaGuPluginExtension.password}")
    }

    /**
     * 导入签名信息
     */
    private String importSign() {
        try {
            if (jiaGuPluginExtension.storeFile == null || !jiaGuPluginExtension.storeFile.exists()) {
                jiaGuPluginExtension.storeFile = project.android.buildTypes.release.signingConfig.storeFile
                jiaGuPluginExtension.storePassword = project.android.buildTypes.release.signingConfig.storePassword
                jiaGuPluginExtension.keyAlias = project.android.buildTypes.release.signingConfig.keyAlias
                jiaGuPluginExtension.keyPassword = project.android.buildTypes.release.signingConfig.keyPassword
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        if (jiaGuPluginExtension.storeFile != null && jiaGuPluginExtension.storeFile.exists()) {
            commandExt += " -autosign "
            return exec(commandJiaGu + " -importsign ${jiaGuPluginExtension.storeFile.getAbsolutePath()}" +
                    " ${jiaGuPluginExtension.storePassword}  ${jiaGuPluginExtension.keyAlias}  ${jiaGuPluginExtension.keyPassword}")
        }
        return "未导入签名信息"
    }

    /**
     * 导入渠道信息
     */
    private String importMulPkg() {
        if (jiaGuPluginExtension.channelFile != null && jiaGuPluginExtension.channelFile.exists()) {
            commandExt += " -automulpkg "
            return exec(commandJiaGu + " -importmulpkg ${jiaGuPluginExtension.channelFile}")
        }
        return "未导入渠道信息"
    }

    /**
     * 配置加固服务
     */
    private String setConfig() {
        if (jiaGuPluginExtension.config == null || jiaGuPluginExtension.config.isEmpty()) {
            // 选择崩溃日志服务、支持x86架构设备、选择数据分析服务
            jiaGuPluginExtension.config = "-crashlog -x86 -analyse"
        }
        // 配置加固服务
        return exec(commandJiaGu + " -config ${jiaGuPluginExtension.config}")
    }

    /**
     * 加固
     */
    private String jiaguStart() {
        if (jiaGuPluginExtension.inputFilePath == null || jiaGuPluginExtension.inputFilePath.isEmpty()) {
            String outputFilePath = ""
            String taskName = getName()
            try {
                project.android.applicationVariants.all { variant ->
                    variant.outputs.all { output ->
                        if (taskName.contains(variant.name.capitalize())) {
                            outputFilePath = output.outputFile.getAbsolutePath()
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            }
            jiaGuPluginExtension.inputFilePath = outputFilePath
        }
        if (jiaGuPluginExtension.outputFileDir == null || jiaGuPluginExtension.outputFileDir.isEmpty()) {
            jiaGuPluginExtension.outputFileDir = "${project.buildDir.getAbsolutePath()}\\jiagu"
        }
        def outputFile = new File(jiaGuPluginExtension.outputFileDir)
        if (!outputFile.exists()) {
            outputFile.mkdirs()
        }
        // 应用加固
        String cmd = commandJiaGu + " -jiagu ${jiaGuPluginExtension.inputFilePath} ${jiaGuPluginExtension.outputFileDir}"
        return exec(cmd + commandExt)
    }

    /**
     * 开始加固
     */
    @TaskAction
    void start() {
        jiaGuPluginExtension = project.extensions.findByName(JiaGuPlugin.EXTENSION_NAME) as JiaGuPluginExtension
        debug = jiaGuPluginExtension.debug
        if (!jiaGuPluginExtension.enable) {
            Logger.debug("enable: false")
            return
        }
        if (jiaGuPluginExtension.jiaGuDir == null || jiaGuPluginExtension.jiaGuDir.isEmpty()) {
            throw new NullPointerException("jiaGuDir 必填")
        }
        if (jiaGuPluginExtension.username == null || jiaGuPluginExtension.username.isEmpty()) {
            throw new NullPointerException("username 必填")
        }
        if (jiaGuPluginExtension.password == null || jiaGuPluginExtension.password.isEmpty()) {
            throw new NullPointerException("password 必填")
        }
        def jiaguDirFile = new File(jiaGuPluginExtension.jiaGuDir)
        if (jiaguDirFile == null || !jiaguDirFile.exists()) {
            throw new NullPointerException("jiaGuDir 不存在")
        }
        def jiaguJarFile = new File("${jiaGuPluginExtension.jiaGuDir}\\jiagu.jar")
        if (jiaguJarFile == null || !jiaguJarFile.exists()) {
            throw new NullPointerException("jiagu.jar 不存在")
        }
        commandJiaGu = "${jiaGuPluginExtension.jiaGuDir}\\java\\bin\\java -jar ${jiaGuPluginExtension.jiaGuDir}\\jiagu.jar "
        Logger.debug("-----start-----")
        // 登录
        String result = login()
        if (result.contains("success")) {
            Logger.debug("login success")
            // 导入签名keystore信息
            result = importSign()
            if (result.contains("succeed")) {
                result = "导入签名 succeed"
            }
            Logger.debug(result)
            // 导入渠道信息
            Logger.debug(importMulPkg())
            // 配置加固服务
            result = setConfig()
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
            result = jiaguStart()
            if (result.contains("任务完成_已签名")) {
                result = "任务完成_已签名"
            }
            Logger.debug(result)
            Logger.debug("输出目录：${jiaGuPluginExtension.outputFileDir}")
        } else {
            Logger.debug(result)
            throw new RuntimeException("登录失败")
        }
        Logger.debug("-----end-----")
    }

    /**
     * 执行命令行
     *
     * @param command 命令
     * @return 结果
     */
    static String exec(String command) throws InterruptedException {
        String returnString = ""
        Runtime runTime = Runtime.getRuntime()
        if (runTime == null) {
            Logger.debug("Create runtime failure!")
        }
        try {
            if (debug) {
                Logger.debug(command)
            }
            Process pro = runTime.exec(command)
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream(), "GBK"))
            String line
            while ((line = input.readLine()) != null) {
                returnString = returnString + line + "\n"
            }
            input.close()
            pro.destroy()
        } catch (IOException ex) {
            ex.printStackTrace()
        }
        if (debug) {
            Logger.debug(returnString)
        }
        return returnString
    }
}