package com.s.android.plugin.jiagu.utils

class ProcessUtils {

    static boolean debug = false

    static String charsetName = "GBK"

    /**
     * 执行命令行
     *
     * @param command 命令
     * @return 结果
     */
    static String exec(String command, File dir) throws InterruptedException {
        String returnString = ""
        Runtime runTime = Runtime.getRuntime()
        if (runTime == null) {
            Logger.debug("Create runtime failure!")
        }
        try {
            if (debug) {
                Logger.debug("命令行执行语句 => " + command)
            }
            Process pro = runTime.exec("cmd /C " + command, null, dir)
            returnString = Utils.readText(new InputStreamReader(pro.getInputStream(), charsetName))
            pro.destroy()
        } catch (IOException ex) {
            ex.printStackTrace()
        }
        String tmp = """
################################################
#                                              #
#        ## #   #    ## ### ### ##  ###        #
#       # # #   #   # #  #  # # # #  #         #
#       ### #   #   ###  #  # # ##   #         #
#       # # ### ### # #  #  ### # # ###        #
#                                              #
# Obfuscation by Allatori Obfuscator v5.6 DEMO #
#                                              #
#           http://www.allatori.com            #
#                                              #
################################################
        """.trim()
        returnString = returnString.replace(tmp, "").trim()
        if (debug) {
            Logger.debug("命令行执行返回 => " + returnString)
        }
        return returnString
    }
}