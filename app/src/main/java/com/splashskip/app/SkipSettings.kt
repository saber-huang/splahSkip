package com.splashskip.app

import android.content.Context
import android.content.SharedPreferences

/** 用户设置，存在手机里，关掉 App 也不会丢 */
object SkipSettings {

    private const val PREFS_NAME = "splashskip"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_ALLOWED_APPS = "allowed_apps"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 总开关：是否启用自动跳过，第一次使用时默认开启 */
    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    /** 白名单：只对这些 App（用包名表示）自动跳过，一个都没选时对所有 App 都不生效 */
    fun allowedApps(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_ALLOWED_APPS, null)?.toSet() ?: emptySet()

    fun setAllowedApps(context: Context, apps: Set<String>) {
        prefs(context).edit().putStringSet(KEY_ALLOWED_APPS, HashSet(apps)).apply()
    }

    /** 这个 App 在不在白名单里（服务每来一个事件都会问一次，所以直接查，不复制） */
    fun isAllowed(context: Context, packageName: String): Boolean =
        prefs(context).getStringSet(KEY_ALLOWED_APPS, null)?.contains(packageName) == true
}
