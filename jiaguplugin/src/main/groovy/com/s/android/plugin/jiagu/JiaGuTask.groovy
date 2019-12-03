package com.s.android.plugin.jiagu

import com.android.build.gradle.api.ApplicationVariant
import com.s.android.plugin.jiagu.entity.FirUploadEntity
import com.s.android.plugin.jiagu.utils.FirUploadUtils
import com.s.android.plugin.jiagu.utils.JiaguUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class JiaGuTask extends DefaultTask {

    static final String NAME = "sJiaGu"

    private JiaGuPluginExtension jiaGuPluginExtension

    @Input
    ApplicationVariant variant

    JiaGuTask() {
        group = "JiaGu"
        description = "360 jiagu plugin and upload apk to fir.im"
    }

    /**
     * 插件开始
     */
    @TaskAction
    void start() {
        jiaGuPluginExtension = project.extensions.findByName(JiaGuPlugin.EXTENSION_NAME) as JiaGuPluginExtension
        if (!jiaGuPluginExtension.enable) {
            Logger.debug("enable: false")
            return
        }
        if (jiaGuPluginExtension.jiaguEnable) {
            checkJiaguEntity(variant)
            JiaguUtils.debug = jiaGuPluginExtension.debug
            JiaguUtils.jiagu(jiaGuPluginExtension)
        }
        if (jiaGuPluginExtension.firEnable) {
            Logger.debug("-----start----- fir upload")
            FirUploadUtils.debug = jiaGuPluginExtension.debug
            FirUploadUtils.firUpload(getFirUploadEntity(variant))
            Logger.debug("------end------ fir upload")
        }
    }

    private FirUploadEntity getFirUploadEntity(ApplicationVariant variant) {
        if (!project.jiagu.firEnable) {
            return null
        }
        FirUploadEntity mFirUploadEntity = project.jiagu.fir
        if (mFirUploadEntity == null) {
            Logger.debug("firEnable is true.")
            return null
        }
        String firApiToken = mFirUploadEntity.firApiToken
        if (firApiToken == null || firApiToken.isEmpty()) {
            Logger.debug("firApiToken can not be null.")
            return null
        }
        if (mFirUploadEntity.versionCode == null) {
            mFirUploadEntity.versionCode = variant.versionCode
        }
        if (mFirUploadEntity.versionName == null) {
            mFirUploadEntity.versionName = variant.versionName
        }
        File uploadFile = mFirUploadEntity.apkFile
        if (uploadFile == null || !uploadFile.exists()) {
            uploadFile = variant.outputs[0].outputFile
        }
        if (project.jiagu.jiaguEnable) {
            String name = uploadFile.name.substring(0, uploadFile.name.lastIndexOf(".")) +
                    "_" + mFirUploadEntity.versionName.replace(".", "") + "_jiagu_sign.apk"
            File file = new File(project.jiagu.outputFileDir + "\\" + name)
//            if (file.exists()) {
            uploadFile = file
//            }
        }
        mFirUploadEntity.apkFile = uploadFile
        String firBundleId = mFirUploadEntity.firBundleId
        if (firBundleId == null || firBundleId.isEmpty()) {
            firBundleId = variant.applicationId
            if (firBundleId == null || firBundleId.isEmpty()) {
                firBundleId = project.android.defaultConfig.applicationId
            }
        }
        if (firBundleId == null || firBundleId.isEmpty()) {
            Logger.debug("firBundleId can not be null.")
            return null
        }
        mFirUploadEntity.firBundleId = firBundleId
        return mFirUploadEntity
    }

    private void checkJiaguEntity(ApplicationVariant variant) {
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
        try {
            if (jiaGuPluginExtension.storeFile == null || !jiaGuPluginExtension.storeFile.exists()) {
                jiaGuPluginExtension.storeFile = variant.buildType.signingConfig.storeFile
                jiaGuPluginExtension.storePassword = variant.buildType.signingConfig.storePassword
                jiaGuPluginExtension.keyAlias = variant.buildType.signingConfig.keyAlias
                jiaGuPluginExtension.keyPassword = variant.buildType.signingConfig.keyPassword
            }
            if (jiaGuPluginExtension.config == null || jiaGuPluginExtension.config.isEmpty()) {
                // 选择崩溃日志服务、支持x86架构设备、选择数据分析服务
                jiaGuPluginExtension.config = "-crashlog -x86 -analyse"
            }
            if (jiaGuPluginExtension.inputFilePath == null || jiaGuPluginExtension.inputFilePath.isEmpty()) {
                jiaGuPluginExtension.inputFilePath = variant.outputs[0].outputFile.getAbsolutePath()
            }
            if (jiaGuPluginExtension.outputFileDir == null || jiaGuPluginExtension.outputFileDir.isEmpty()) {
                jiaGuPluginExtension.outputFileDir = "${project.buildDir.getAbsolutePath()}\\jiagu"
            }
            def outputFile = new File(jiaGuPluginExtension.outputFileDir)
            if (!outputFile.exists()) {
                outputFile.mkdirs()
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

}