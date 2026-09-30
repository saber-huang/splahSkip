package com.splashskip.app

/** 判断一段文字是不是"跳过"按钮上的字 */
object SkipMatcher {

    // 本 App 的名字里也有 Skip，安装弹窗等地方会显示它，但它不是跳过按钮
    private const val APP_NAME = "SplashSkip"

    /**
     * 元素的 ID（形如 "com.xxx:id/iv_close"）里"/"后面的名字包含 keywords 里任意一个词（不分大小写）才算。
     * 只看"/"后面的名字，不看前面的包名，免得包名里碰巧带了 close、skip 就全部匹配上。
     */
    fun isSkipId(viewId: String?, keywords: List<String>): Boolean {
        val name = viewId?.substringAfter('/', "") ?: return false
        if (name.isEmpty()) return false
        return keywords.any { it.isNotEmpty() && name.contains(it, ignoreCase = true) }
    }

    /** 文字不超过 maxLength 个字，并且包含 keywords 里任意一个词（不分大小写）才算 */
    fun isSkipText(text: CharSequence?, keywords: List<String>, maxLength: Int): Boolean {
        val s = text?.toString()?.trim() ?: return false
        if (s.isEmpty() || s.length > maxLength || s.contains(APP_NAME, ignoreCase = true)) return false
        return keywords.any { it.isNotEmpty() && s.contains(it, ignoreCase = true) }
    }
}
