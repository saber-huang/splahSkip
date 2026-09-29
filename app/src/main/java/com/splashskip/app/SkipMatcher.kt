package com.splashskip.app

/** 判断一段文字是不是"跳过"按钮上的字 */
object SkipMatcher {

    // 本 App 的名字里也有 Skip，安装弹窗等地方会显示它，但它不是跳过按钮
    private const val APP_NAME = "SplashSkip"

    /** 文字不超过 maxLength 个字，并且包含 keywords 里任意一个词（不分大小写）才算 */
    fun isSkipText(text: CharSequence?, keywords: List<String>, maxLength: Int): Boolean {
        val s = text?.toString()?.trim() ?: return false
        if (s.isEmpty() || s.length > maxLength || s.contains(APP_NAME, ignoreCase = true)) return false
        return keywords.any { it.isNotEmpty() && s.contains(it, ignoreCase = true) }
    }
}
