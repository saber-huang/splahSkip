package com.splashskip.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * 帮用户打开系统里"防止 App 被杀掉"的设置页。
 * 各家手机的页面不一样，所以按顺序一个个试，第一个能打开的就停下。
 */
object KeepAlive {

    private const val TAG = "SplashSkip"

    // 华为"应用启动管理"页面在不同系统版本里的名字，从新到旧依次尝试
    private const val HUAWEI_MANAGER = "com.huawei.systemmanager"
    private val HUAWEI_PAGES = listOf(
        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity",
        "com.huawei.systemmanager.optimize.process.ProtectActivity",
    )

    /** 打开设置页，成功返回 true；所有页面都打不开返回 false，由调用方显示手动设置的说明 */
    fun openSettings(activity: Activity): Boolean {
        for (intent in candidateIntents(activity)) {
            try {
                activity.startActivity(intent)
                Log.i(TAG, "已打开设置页：$intent")
                return true
            } catch (e: Exception) {
                // 这个页面在这台手机上没有，或者不允许打开，试下一个
                Log.i(TAG, "打不开设置页：$intent（${e.javaClass.simpleName}）")
            }
        }
        return false
    }

    private fun candidateIntents(context: Context): List<Intent> {
        val intents = mutableListOf<Intent>()

        val brand = Build.MANUFACTURER.lowercase()
        if (brand.contains("huawei") || brand.contains("honor")) {
            HUAWEI_PAGES.forEach { intents += Intent().setClassName(HUAWEI_MANAGER, it) }
        }

        // 所有手机通用：电池优化列表，以及本 App 的应用信息页
        intents += Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        intents += Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        return intents
    }
}
