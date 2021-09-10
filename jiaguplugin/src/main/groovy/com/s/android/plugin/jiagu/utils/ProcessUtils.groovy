package com.s.android.plugin.jiagu.utils

import com.s.android.plugin.jiagu.Logger

class ProcessUtils {

    static boolean debug = false

    static String charsetName = "UTF-8"

    /**
     * 执行命令行
     *
     * @param command 命令
     * @return 结果
     */
    static String exec(String command) throws InterruptedException {
        String returnString = ""
        Runtime runTime = Runtime.getRuntime()
        if (runTime == null) {
            Logger.debug("Create runtime failure!")
        }
        try {
            if (debug) {
                Logger.debug(command)
            }
            Process pro = runTime.exec(command)
            BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream(), charsetName))
            String line
            while ((line = input.readLine()) != null) {
                returnString = returnString + line + "\n"
            }
            input.close()
            pro.destroy()
        } catch (IOException ex) {
            ex.printStackTrace()
        }
        if (debug) {
            Logger.debug(returnString)
        }
        return returnString
    }
}