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

    /** 最终打开的是哪一种页面，界面根据它提示用户在页面里该做什么 */
    enum class Page { HUAWEI_STARTUP, APP_DETAILS, BATTERY_LIST }

    /** 打开设置页并返回打开的是哪种页面；所有页面都打不开返回 null，由调用方显示手动设置的说明 */
    fun openSettings(activity: Activity): Page? {
        for ((page, intent) in candidates(activity)) {
            try {
                activity.startActivity(intent)
                Log.i(TAG, "已打开设置页：$page $intent")
                return page
            } catch (e: Exception) {
                // 这个页面在这台手机上没有，或者不允许打开，试下一个
                Log.i(TAG, "打不开设置页：$page $intent（${e.javaClass.simpleName}）")
            }
        }
        return null
    }

    private fun candidates(context: Context): List<Pair<Page, Intent>> {
        val brand = Build.MANUFACTURER.lowercase()
        val isHuawei = brand.contains("huawei") || brand.contains("honor")

        val huaweiPages = if (isHuawei) {
            HUAWEI_PAGES.map { Page.HUAWEI_STARTUP to Intent().setClassName(HUAWEI_MANAGER, it) }
        } else {
            emptyList()
        }
        val appDetails = Page.APP_DETAILS to
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        val batteryList = Page.BATTERY_LIST to Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

        // 华为上，从应用信息页进"电池"能找到"应用启动管理"；通用的电池优化列表里往往根本没有本 App，所以放后面
        return huaweiPages + if (isHuawei) listOf(appDetails, batteryList) else listOf(batteryList, appDetails)
    }
}
