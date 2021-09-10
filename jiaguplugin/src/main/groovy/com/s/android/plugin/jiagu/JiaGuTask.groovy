package com.s.android.plugin.jiagu

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.s.android.plugin.jiagu.utils.JiaguUtils
import com.s.android.plugin.jiagu.utils.ProcessUtils
import com.s.android.plugin.jiagu.utils.Utils
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class JiaGuTask extends DefaultTask {

    private JiaGuPluginExtension jiaGuExtension
    private AppExtension android

    @Input
    String currentFlavorName

    @Input
    String currentBuildType

    JiaGuTask() {
        group = "JiaGu"
        description = "360 jiagu plugin"
        android = project.extensions.getByType(AppExtension.class)
        jiaGuExtension = project.extensions.getByType(JiaGuPluginExtension.class)
    }

    /**
     * 插件开始
     */
    @TaskAction
    void start() {
        checkParams()
        ProcessUtils.charsetName = jiaGuExtension.charsetName
        ProcessUtils.debug = jiaGuExtension.debug
        Logger.debug("-----start-----")
        def applicationVariants = android.applicationVariants
        for (ApplicationVariant applicationVariant : applicationVariants) {
            if (applicationVariant.flavorName == currentFlavorName &&
                    applicationVariant.buildType.name == currentBuildType) {
                applicationVariant.outputs.all(new Action<BaseVariantOutput>() {
                    @Override
                    void execute(BaseVariantOutput baseVariantOutput) {
                        jiagu(applicationVariant, baseVariantOutput)
                    }
                })
                break
            }
        }
        Logger.debug("------end------")
    }

    private void jiagu(ApplicationVariant variant, BaseVariantOutput output) {
        String fileName = output.outputFile.name
        if (output.outputFile == null || !output.outputFile.exists()) {
            Logger.debug("未找到${fileName}文件")
            return
        }
        String apkName = fileName.substring(0, fileName.indexOf(".apk"))
        String versionName = variant.versionName.replace(".", "")
        String jiaGuName = "${apkName}_${versionName}_jiagu_sign.apk"
        File file = new File(jiaGuExtension.outputFileDir, jiaGuName)
        if (file.exists()) {
            Logger.debug("${fileName}已经加固了")
            return
        }
        JiaguUtils.jiagu(jiaGuExtension, output)
    }

    private void checkParams() {
        if (jiaGuExtension.signingConfig == null) {
            jiaGuExtension.signingConfig = android.signingConfigs.findByName("release")
        }
        if (Utils.isEmpty(jiaGuExtension.outputFileDir)) {
            jiaGuExtension.outputFileDir = "${project.buildDir.getAbsolutePath()}\\jiagu"
        }
        def outputFile = new File(jiaGuExtension.outputFileDir)
        if (!outputFile.exists()) {
            outputFile.mkdirs()
        }
        jiaGuExtension.checkParams()
    }

}