package com.splashskip.app

import android.content.Context

/** 用户设置，存在手机里，关掉 App 也不会丢 */
object SkipSettings {

    private const val PREFS_NAME = "splashskip"
    private const val KEY_ENABLED = "enabled"

    /** 总开关：是否启用自动跳过，第一次使用时默认开启 */
    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}
