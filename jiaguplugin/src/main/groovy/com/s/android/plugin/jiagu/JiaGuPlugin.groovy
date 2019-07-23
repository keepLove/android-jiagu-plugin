package com.s.android.plugin.jiagu

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class JiaGuPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'jiagu'

    @Override
    void apply(Project project) {
        // 接收外部参数
        project.extensions.create(EXTENSION_NAME, JiaGuPluginExtension.class, project)
        // 取得外部参数
        if (project.android.hasProperty("applicationVariants")) { // For android application.
            project.android.applicationVariants.all { variant ->
                // 禁止插件
                if (false == project.jiagu.enable) {
                    Logger.debug("enable: false")
                    return
                }
                String variantName = variant.name.capitalize()
                // debug打包，并且开启了debug , 或者release打包
                if ((variantName.contains("Debug") && project.jiagu.debug) ||
                        variantName.contains("Release")) {
                    Task jiaGuTask = project.tasks.create("${JiaGuTask.NAME}${variantName}", JiaGuTask.class)
                    project.tasks["assemble${variantName}"].dependsOn(jiaGuTask)
                }
            }
        }
    }

}