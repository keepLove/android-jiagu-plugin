package com.s.android.plugin.jiagu.utils

class Utils {

    /**
     * 将字符串的首字母转大写
     */
    static String capitalize(String str) {
        if (isBlank(str)) return ""
        return str.substring(0, 1).toUpperCase() + str.substring(1)
    }

    static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0
    }

    static boolean isBlank(CharSequence s) {
        if (s == null) {
            return true
        } else {
            for (int i = 0; i < s.length(); ++i) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false
                }
            }
            return true
        }
    }

    static String readText(InputStreamReader reader) {
        StringWriter stringWriter = new StringWriter()
        long charsCopied = 0
        char[] buffer = new char[8 * 1024]
        int chars = reader.read(buffer)
        while (chars >= 0) {
            stringWriter.write(buffer, 0, chars)
            charsCopied += chars
            chars = reader.read(buffer)
        }
        return stringWriter.toString()
    }

}