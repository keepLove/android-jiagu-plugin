package com.s.android.plugin.jiagu.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.s.android.plugin.jiagu.Logger
import com.s.android.plugin.jiagu.entity.FirUploadEntity
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.Icon
import net.dongliu.apk.parser.bean.IconFace
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor

import java.util.function.Consumer

class FirUploadUtils {

    private static OkHttpClient okHttpClient
    public static boolean debug

    static OkHttpClient getHttpClient() {
        if (okHttpClient == null) {
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
        return okHttpClient
    }

    /**
     * firUpload
     */
    static void firUpload(FirUploadEntity mFirUploadEntity) {
        if (mFirUploadEntity == null) {
            Logger.debug("FirUploadEntity is null.")
            return
        }
        File uploadFile = mFirUploadEntity.apkFile
        if (uploadFile == null || !uploadFile.exists()) {
            Logger.debug("not find apk file.")
            return
        }
        ApkFile mApkFile = new ApkFile(uploadFile)
        if (mFirUploadEntity.appName == null || mFirUploadEntity.appName.isEmpty()) {
            mFirUploadEntity.appName = mApkFile.apkMeta.label
        }
        if (mFirUploadEntity.iconFile == null || !mFirUploadEntity.iconFile.exists()) {
            mApkFile.getAllIcons().forEach(new Consumer<IconFace>() {
                @Override
                void accept(IconFace iconFace) {
                    if (iconFace.file && iconFace instanceof Icon) {
                        mFirUploadEntity.iconName = iconFace.getPath()
                        mFirUploadEntity.iconData = iconFace.getData()
                    }
                }
            })
        }
        if (debug) {
            Logger.debug("FirUploadEntity = $mFirUploadEntity")
        }
        obtainCredentials(mFirUploadEntity)
    }

    /**
     * 获取上传凭证
     */
    private static void obtainCredentials(FirUploadEntity mFirUploadEntity) {
        Logger.debug("obtain upload credentials...")
        FormBody.Builder formBodyBuild = new FormBody.Builder()
        formBodyBuild.add("type", "android")
        formBodyBuild.add("bundle_id", mFirUploadEntity.firBundleId)
        formBodyBuild.add("api_token", mFirUploadEntity.firApiToken)
        Request.Builder builder = new Request.Builder()
                .url("https://api.fir.im/apps")
                .post(formBodyBuild.build())
        Response response = getHttpClient().newCall(builder.build()).execute()
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
            firUploadIcon(mFirUploadEntity, iconObject.get("upload_url").asString, iconObject.get("key").asString,
                    iconObject.get("token").asString)
            String release_id = firUploadApk(mFirUploadEntity, binaryObject.get("upload_url").asString, binaryObject.get("key").asString,
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
    private static String firUploadApk(FirUploadEntity mFirUploadEntity, String url, String key, String token, String prefix) {
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
        Response response = getHttpClient().newCall(builder.build()).execute()
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
    private static void firUploadIcon(FirUploadEntity mFirUploadEntity, String url, String key, String token) {
        MultipartBody.Builder bodybuilder = new MultipartBody.Builder()
        bodybuilder.setType(MultipartBody.FORM)
        bodybuilder.addFormDataPart("key", key)
        bodybuilder.addFormDataPart("token", token)
        if (mFirUploadEntity.iconFile != null && mFirUploadEntity.iconFile.exists()) {
            Logger.debug("fir upload icon. ${mFirUploadEntity.iconFile.name}")
            bodybuilder.addFormDataPart("file", mFirUploadEntity.iconFile.name,
                    RequestBody.create(null, mFirUploadEntity.iconFile))
        } else {
            Logger.debug("fir upload icon. ${mFirUploadEntity.iconName}")
            bodybuilder.addFormDataPart("file", mFirUploadEntity.iconName,
                    RequestBody.create(null, mFirUploadEntity.iconData))
        }
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(bodybuilder.build())
        Response response = getHttpClient().newCall(builder.build()).execute()
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