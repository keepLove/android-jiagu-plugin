package com.s.android.plugin.jiagu.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.s.android.plugin.jiagu.JiaGuTask
import com.s.android.plugin.jiagu.Logger
import com.s.android.plugin.jiagu.entity.FirUploadEntity
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.Icon
import net.dongliu.apk.parser.bean.IconFace
import okhttp3.*
import org.gradle.api.Project

import java.util.function.Consumer

class FirUploadUtils {

    private OkHttpClient okHttpClient = new OkHttpClient()
    private ApkFile mApkFile

    /**
     * firUpload
     */
    void firUpload(Project project) {
        FirUploadEntity mFirUploadEntity = project.jiagu.fir
        String firApiToken = mFirUploadEntity.firApiToken
        if (firApiToken == null || firApiToken.isEmpty()) {
            throw new NullPointerException("firApiToken can not be null.")
        }
        String firBundleId = mFirUploadEntity.firBundleId
        if (firBundleId == null || firBundleId.isEmpty()) {
            firBundleId = project.android.defaultConfig.applicationId
        }
        if (firBundleId == null || firBundleId.isEmpty()) {
            throw new NullPointerException("firBundleId can not be null.")
        }
        File uploadFile = mFirUploadEntity.apkFile
        try {
            project.android.applicationVariants.all { variant ->
                variant.outputs.all { output ->
                    if (project.tasks.findByName("${JiaGuTask.NAME + variant.name.capitalize()}") != null) {
                        if (mFirUploadEntity.versionCode == null) {
                            mFirUploadEntity.versionCode = variant.versionCode
                            project.jiagu.fir.versionCode = variant.versionCode
                        }
                        if (mFirUploadEntity.versionName == null) {
                            mFirUploadEntity.versionName = variant.versionName
                            project.jiagu.fir.versionName = variant.versionName
                        }
                        if (uploadFile == null || !uploadFile.exists()) {
                            uploadFile = output.outputFile
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        if (uploadFile == null || !uploadFile.exists()) {
            Logger.debug("not apk file.")
            return
        }
        if (project.jiagu.jiaguEnable) {
            String name = uploadFile.name.substring(0, uploadFile.name.lastIndexOf(".")) +
                    "_" + mFirUploadEntity.versionName.replace(".", "") + "_jiagu_sign.apk"
            File file = new File(project.jiagu.outputFileDir + "\\" + name)
            if (file.exists()) {
                uploadFile = file
            }
        }
        project.jiagu.fir.apkFile = uploadFile
        mApkFile = new ApkFile(uploadFile)
        if (mFirUploadEntity.appName == null || mFirUploadEntity.appName.isEmpty()) {
            project.jiagu.fir.appName = mApkFile.apkMeta.label
        }
        obtainCredentials(project, firApiToken, firBundleId)
    }

    /**
     * 获取上传凭证
     */
    private void obtainCredentials(Project project, String firApiToken, String firBundleId) {
        Logger.debug("obtain upload credentials...")
        FormBody.Builder formBodyBuild = new FormBody.Builder()
        formBodyBuild.add("type", "android")
        formBodyBuild.add("bundle_id", firBundleId)
        formBodyBuild.add("api_token", firApiToken)
        Request.Builder builder = new Request.Builder()
                .url("http://api.fir.im/apps")
                .post(formBodyBuild.build())
        Response response = okHttpClient.newCall(builder.build()).execute()
        if (response != null && response.code() == 201) {
            def string = response.body().string()
            if (project.jiagu.debug) {
                Logger.debug(string)
            }
            Logger.debug("obtain upload credentials:success")
            JsonObject jsonObject = new JsonParser().parse(string).asJsonObject.getAsJsonObject("cert")
            def binaryObject = jsonObject.getAsJsonObject("binary")
            def iconObject = jsonObject.getAsJsonObject("icon")
            firUploadApk(project, binaryObject.get("upload_url").asString, binaryObject.get("key").asString,
                    binaryObject.get("token").asString, jsonObject.get("prefix").asString)
            firUploadIcon(project, iconObject.get("upload_url").asString, iconObject.get("key").asString,
                    iconObject.get("token").asString)
        } else {
            Logger.debug("Unable to obtain upload credentials. $response")
        }
    }

    /**
     * 上传apk
     */
    private void firUploadApk(Project project, String url, String key, String token, String prefix) {
        FirUploadEntity mFirUploadEntity = project.jiagu.fir
        String versionCode = mFirUploadEntity.versionCode
        String versionName = mFirUploadEntity.versionName
        File uploadFile = mFirUploadEntity.apkFile
        Logger.debug("fir upload apk. ${uploadFile.path}")
        if (project.jiagu.debug) {
            Logger.debug("path: ${uploadFile.path}  " +
                    "\nurl:$url" +
                    "\nkey:$key" +
                    "\ntoken:$token" +
                    "\n" + "${prefix}name:" + mFirUploadEntity.appName +
                    "\n" + "${prefix}version:" + versionCode +
                    "\n" + "${prefix}build:" + versionName +
                    "\n" + "${prefix}changelog:" + mFirUploadEntity.firChangeLog
            )
        }
        MultipartBody.Builder bodybuilder = new MultipartBody.Builder()
        bodybuilder.setType(MultipartBody.FORM)
        bodybuilder.addFormDataPart("key", key)
        bodybuilder.addFormDataPart("token", token)
        bodybuilder.addFormDataPart("file", uploadFile.getName(), RequestBody.create(null, uploadFile))
        bodybuilder.addFormDataPart("${prefix}name", mFirUploadEntity.appName)
        bodybuilder.addFormDataPart("${prefix}version", versionCode)
        bodybuilder.addFormDataPart("${prefix}build", versionName)
        bodybuilder.addFormDataPart("${prefix}changelog", mFirUploadEntity.firChangeLog)
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(bodybuilder.build())
        Response response = okHttpClient.newCall(builder.build()).execute()
        if (response != null && response.body() != null && response.code() == 200) {
            def string = response.body().string()
            if (project.jiagu.debug) {
                Logger.debug(string)
            }
            def jsonObject = new JsonParser().parse(string).asJsonObject
            boolean isCompleted = jsonObject.get("is_completed").asBoolean
            String download_url = jsonObject.get("download_url").asString
            String release_id = jsonObject.get("release_id").asString
            Logger.debug("is_completed : $isCompleted")
            Logger.debug("download_url : $download_url")
            Logger.debug("release_id   : $release_id")
        } else {
            Logger.debug("upload apk failure. $response")
        }
    }

    /**
     * 上传icon
     */
    private void firUploadIcon(Project project, String url, String key, String token) {
        Icon icon = null
        mApkFile.getAllIcons().forEach(new Consumer<IconFace>() {
            @Override
            void accept(IconFace iconFace) {
                if (iconFace.file && iconFace instanceof Icon) {
                    icon = iconFace
                }
            }
        })
        Logger.debug("fir upload icon. ${icon.path}")
        if (project.jiagu.debug) {
            Logger.debug(icon.toString() + "\nurl:$url\nkey:${key}\ntoken:$token")
        }
        MultipartBody.Builder bodybuilder = new MultipartBody.Builder()
        bodybuilder.setType(MultipartBody.FORM)
        bodybuilder.addFormDataPart("key", key)
        bodybuilder.addFormDataPart("token", token)
        bodybuilder.addFormDataPart("file", icon.getPath(), RequestBody.create(null, icon.getData()))
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(bodybuilder.build())
        Response response = okHttpClient.newCall(builder.build()).execute()
        if (response != null && response.body() != null && response.code() == 200) {
            def string = response.body().string()
            if (project.jiagu.debug) {
                Logger.debug(string)
            }
            def jsonObject = new JsonParser().parse(string).asJsonObject
            boolean isCompleted = jsonObject.get("is_completed").asBoolean
            String download_url = jsonObject.get("download_url").asString
            Logger.debug("is_completed : $isCompleted")
            Logger.debug("download_url : $download_url")
        } else {
            Logger.debug("upload icon failure. $response")
        }
    }
}