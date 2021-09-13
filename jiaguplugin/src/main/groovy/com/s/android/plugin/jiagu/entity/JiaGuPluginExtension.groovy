package com.s.android.plugin.jiagu.entity

import com.android.builder.model.SigningConfig
import com.s.android.plugin.jiagu.utils.Utils

class JiaGuPluginExtension {

    /**
     * debug 模式
     */
    boolean debug = false
    /**
     * 360加固文件根目录 必填
     */
    String jiaGuDir = null
    /**
     * 360加固用户名 必填
     */
    String username = null
    /**
     * 360加固密码 必填
     */
    String password = null
    /**
     * 签名文件，默认读取buildType中的signingConfig 或名为'release'的签名文件
     */
    SigningConfig signingConfig = null
    /**
     * 指向通道备注文件.txt 默认null
     */
    File channelFile = null
    /**
     * 输出apk文件地址 默认输出 build\jiagu
     */
    String outputFileDir = null
    /**
     * 扩展配置
     */
    String config = null
    /**
     * 控制台输出编码方式，默认GBK
     */
    String charsetName = "GBK"

    void checkParams() {
        if (Utils.isEmpty(jiaGuDir)) {
            throw new NullPointerException("360加固文件根目录不能为空")
        }
        def jiaguDirFile = new File(jiaGuDir)
        if (jiaguDirFile == null || !jiaguDirFile.exists()) {
            throw new NullPointerException("360加固文件不存在")
        }
        if (Utils.isEmpty(username)) {
            throw new NullPointerException("360加固用户名不能为空")
        }
        if (Utils.isEmpty(password)) {
            throw new NullPointerException("360加固密码不能为空")
        }
        if (signingConfig == null) {
            throw new NullPointerException("签名文件不存在")
        }
    }

    String getSign() {
        return "${signingConfig.storeFile} ${signingConfig.storePassword} ${signingConfig.keyAlias} ${signingConfig.keyPassword}"
    }

}