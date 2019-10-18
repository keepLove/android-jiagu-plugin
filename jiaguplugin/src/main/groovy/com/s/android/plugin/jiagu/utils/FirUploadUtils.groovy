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
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.Project

import java.util.function.Consumer

class FirUploadUtils {

    private OkHttpClient okHttpClient
    private ApkFile mApkFile
    private boolean debug

    FirUploadUtils() {
        def loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            void log(String message) {
                if (debug) {
                    Logger.debug(message)
                }
            }
        })
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(loggingInterceptor)
                .build()
    }

    /**
     * firUpload
     */
    void firUpload(Project project) {
        debug = project.jiagu.debug
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
                .url("https://api.fir.im/apps")
                .post(formBodyBuild.build())
        Response response = okHttpClient.newCall(builder.build()).execute()
        if (response != null && response.body() != null) {
            def string = response.body().string()
            if (response.code() != 201) {
                return
            }
            Logger.debug("obtain upload credentials:success")
            def parentJsonObject = new JsonParser().parse(string).asJsonObject
            JsonObject jsonObject = parentJsonObject.getAsJsonObject("cert")
            def binaryObject = jsonObject.getAsJsonObject("binary")
            def iconObject = jsonObject.getAsJsonObject("icon")
            firUploadIcon(iconObject.get("upload_url").asString, iconObject.get("key").asString,
                    iconObject.get("token").asString)
            String release_id = firUploadApk(project, binaryObject.get("upload_url").asString, binaryObject.get("key").asString,
                    binaryObject.get("token").asString, jsonObject.get("prefix").asString)
            if (release_id != null) {
                Logger.debug("download url : https://fir.im/${parentJsonObject.get("short").asString}?release_id=${release_id}")
            }
        } else {
            Logger.debug("Unable to obtain upload credentials. $response")
        }
    }

    /**
     * 上传apk
     */
    private String firUploadApk(Project project, String url, String key, String token, String prefix) {
        FirUploadEntity mFirUploadEntity = project.jiagu.fir
        String versionCode = mFirUploadEntity.versionCode
        String versionName = mFirUploadEntity.versionName
        File uploadFile = mFirUploadEntity.apkFile
        Logger.debug("fir upload apk. ${uploadFile.path}")
        MultipartBody.Builder bodybuilder = new MultipartBody.Builder()
        bodybuilder.setType(MultipartBody.FORM)
        bodybuilder.addFormDataPart("key", key)
        bodybuilder.addFormDataPart("token", token)
        bodybuilder.addFormDataPart("file", uploadFile.getName(), RequestBody.create(null, uploadFile))
        bodybuilder.addFormDataPart("${prefix}name", mFirUploadEntity.appName)
        bodybuilder.addFormDataPart("${prefix}version", versionName)
        bodybuilder.addFormDataPart("${prefix}build", versionCode)
        bodybuilder.addFormDataPart("${prefix}changelog", mFirUploadEntity.firChangeLog)
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(bodybuilder.build())
        Response response = okHttpClient.newCall(builder.build()).execute()
        if (response != null && response.body() != null && response.code() == 200) {
            def string = response.body().string()
            def jsonObject = new JsonParser().parse(string).asJsonObject
            boolean isCompleted = jsonObject.get("is_completed").asBoolean
            String download_url = jsonObject.get("download_url").asString
            String release_id = jsonObject.get("release_id").asString
            if (isCompleted) {
                Logger.debug("apk_url : $download_url")
                return release_id
            } else {
                Logger.debug("upload apk failure. $string")
            }
        } else {
            Logger.debug("upload apk failure. $response")
        }
        return null
    }

    /**
     * 上传icon
     */
    private void firUploadIcon(String url, String key, String token) {
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
            def jsonObject = new JsonParser().parse(string).asJsonObject
            boolean isCompleted = jsonObject.get("is_completed").asBoolean
            String download_url = jsonObject.get("download_url").asString
            if (isCompleted) {
                Logger.debug("upload icon success.  $download_url")
            } else {
                Logger.debug("upload icon failure.  $string")
            }
        } else {
            Logger.debug("upload icon failure. $response")
        }
    }
}