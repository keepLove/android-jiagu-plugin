package com.s.android.plugin.jiagu

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class JiaGuPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'jiagu'
    private Project project

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new RuntimeException(
                    "'com.android.application' plugin must be applied")
        }
        if (!project.android.hasProperty("applicationVariants")) {
            return
        }
        project.extensions.create(EXTENSION_NAME, JiaGuPluginExtension.class, project)
//        // 禁止插件
//        if (false == project.jiagu.enable) {
//            Logger.debug("enable: false")
//            return
//        }
        this.project = project
        project.afterEvaluate {
            project.android.applicationVariants.all { ApplicationVariant variant ->
                String variantName = variant.name.capitalize()
                // debug打包，并且开启了debug, 或者release打包
                if ((variantName.contains("Debug") && project.jiagu.debugOn) ||
                        variantName.contains("Release")) {
                    addTasks(variant, variantName)
                }
            }
        }
    }

    private void addTasks(ApplicationVariant applicationVariant, String variantName) {
        JiaGuTask jiaGuTask = project.tasks.create(name: "${JiaGuTask.NAME}${variantName}", type: JiaGuTask.class) {
            variant = applicationVariant
        }
        Task assembleTask = project.tasks["assemble${variantName}"]
        jiaGuTask.dependsOn assembleTask
        jiaGuTask.mustRunAfter assembleTask
        assembleTask.finalizedBy(jiaGuTask)
    }

}