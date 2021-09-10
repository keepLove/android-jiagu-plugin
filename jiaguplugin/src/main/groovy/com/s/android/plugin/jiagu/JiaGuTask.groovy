package com.s.android.plugin.jiagu

import com.android.build.gradle.AppExtension
import com.s.android.plugin.jiagu.utils.ProcessUtils
import com.s.android.plugin.jiagu.utils.Utils
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
        Logger.debug("-----start----- buildType:${currentBuildType}  flavorName:${currentFlavorName}")
//        JiaguUtils.jiagu(jiaGuExtension)
//        Logger.debug("------end------ jiagu")
    }

    private void checkParams() {
        if (jiaGuExtension.signingConfig == null) {
            jiaGuExtension.signingConfig = android.signingConfigs.findByName("release")
        }
        if (Utils.isEmpty(jiaGuExtension.config)) {
            jiaGuExtension.config = "-crashlog -x86 -analyse"
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