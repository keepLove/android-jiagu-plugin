package com.s.android.plugin.jiagu.utils

class Logger {

    private static String TAG = "[JiaGuPlugin] "

    static void debug(String message) {
        if (message == null || message.isEmpty()) {
            return
        }
        print(TAG)
        println(message)
    }

    static void debug(String format, Object... args) {
        debug(formatString(format, args))
    }

    private static String formatString(String format, Object... args) {
        if (format == null) {
            return "null"
        } else {
            return args != null && args.length != 0 ? String.format(Locale.US, format, args) : format
        }
    }
}