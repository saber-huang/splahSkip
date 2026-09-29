package com.splashskip.app

import android.content.Context

/** 记录一共跳过了多少次，存在手机里，关掉 App 也不会丢 */
object SkipCounter {

    private const val PREFS_NAME = "splashskip"
    private const val KEY_COUNT = "skip_count"

    fun get(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_COUNT, 0)

    fun increment(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_COUNT, prefs.getInt(KEY_COUNT, 0) + 1).apply()
    }
}
