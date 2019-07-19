package com.s.android.plugin.jiagu

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class JiaGuPlugin implements Plugin<Project> {

    static final String EXTENSION_NAME = 'jiagu'

    @Override
    void apply(Project project) {
        project.extensions.create(EXTENSION_NAME, JiaGuPluginExtension.class, project)
        Task jiaGuTask = project.tasks.create(JiaGuTask.NAME, JiaGuTask.class)
        project.tasks.whenTaskAdded { Task theTask ->
            if (theTask.name == 'assembleRelease') {
                theTask.dependsOn(jiaGuTask) // 编译完apk之后再执行自定义task
            }
        }
    }
}