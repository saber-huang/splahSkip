package com.splashskip.app

/** 判断一段文字是不是"跳过"按钮上的字 */
object SkipMatcher {

    // 包含这些词之一就算（不分大小写），想加新词直接往里加
    private val KEYWORDS = listOf("跳过", "跳过广告", "Skip")

    // 按钮上的字都很短，超过这个长度的多半是正文或说明
    private const val MAX_LENGTH = 10

    // 本 App 的名字里也有 Skip，安装弹窗等地方会显示它，但它不是跳过按钮
    private const val APP_NAME = "SplashSkip"

    fun isSkipText(text: CharSequence?): Boolean {
        val s = text?.toString()?.trim() ?: return false
        if (s.isEmpty() || s.length > MAX_LENGTH || s.contains(APP_NAME, ignoreCase = true)) return false
        return KEYWORDS.any { s.contains(it, ignoreCase = true) }
    }
}
