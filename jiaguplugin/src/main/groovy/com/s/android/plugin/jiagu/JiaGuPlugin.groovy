package com.s.android.plugin.jiagu

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.s.android.plugin.jiagu.utils.Utils
import org.gradle.api.*

class JiaGuPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'jiagu'

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new GradleException("无法在非android application插件中使用360加固")
        }
        project.extensions.create(EXTENSION_NAME, JiaGuPluginExtension.class)
        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project resource) {
                def jiaGuExtension = resource.extensions.getByType(JiaGuPluginExtension.class)
                def android = project.extensions.getByType(AppExtension.class)
                android.applicationVariants.all(new Action<ApplicationVariant>() {
                    @Override
                    void execute(ApplicationVariant variant) {
                        if (jiaGuExtension.signingConfig == null) {
                            jiaGuExtension.signingConfig = variant.signingConfig
                        }
                        addJiaGuTask(resource, variant.flavorName, variant.buildType.name)
                    }
                })
            }
        })
    }

    /**
     * 添加jiaGuTask
     */
    private static void addJiaGuTask(
            Project project,
            String flavorName,
            String buildType
    ) {
        // 添加task到assembleRelease之后
        addAssembleTask(project, flavorName, buildType)
        // 依赖assembleRelease,assembleDebug添加task任务
        addDependsOnTask(project, flavorName, buildType)
    }

    /**
     * 添加task到assembleRelease之后
     */
    private static void addAssembleTask(
            Project project,
            String flavorName,
            String buildType
    ) {
//        def name = Utils.capitalize(flavorName)
//        def type = Utils.capitalize(buildType)
        def jiaGuTask = createTask(project, flavorName, buildType, "Apk")
//        Task assembleTask = project.tasks["assemble${name}${type}"]
//        assembleTask.finalizedBy(jiaGuTask)
    }

    /**
     * 创建加固task
     */
    private static void addDependsOnTask(
            Project project,
            String flavorName,
            String buildType
    ) {
        def name = Utils.capitalize(flavorName)
        def type = Utils.capitalize(buildType)
        def jiaGuTask = createTask(project, flavorName, buildType, "Assemble")
        jiaGuTask.dependsOn("assemble${name}${type}")
    }

    /**
     * 创建task
     */
    private static Task createTask(Project project, String flavorName, String buildType, String prefix) {
        def name = Utils.capitalize(flavorName)
        def type = Utils.capitalize(buildType)
        return project.tasks.create("jiaGu${prefix}${name}${type}", JiaGuTask.class) {
            currentFlavorName = flavorName
            currentBuildType = buildType
        }
    }

}