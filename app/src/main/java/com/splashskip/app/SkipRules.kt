package com.splashskip.app

/** "跳过"规则的默认值、可设置的范围，以及把输入框文字整理成关键词的方法 */
object SkipRules {

    // 按钮上的字包含其中任意一个词就算"跳过"按钮（不分大小写）
    val DEFAULT_KEYWORDS = listOf("跳过", "跳过广告", "Skip")

    // 按钮上的字都很短，超过这个长度的多半是正文或说明，不当作按钮
    const val DEFAULT_MAX_LENGTH = 10
    const val MIN_LENGTH = 2
    const val MAX_LENGTH_LIMIT = 30

    // 切换到一个新 App 后，多少秒内才会去找并点"跳过"
    const val DEFAULT_WINDOW_SECONDS = 5
    const val MIN_SECONDS = 1
    const val MAX_SECONDS = 30

    /** 把输入框里的文字整理成关键词列表：每行一个，去掉首尾空格、空行和重复的（不分大小写） */
    fun parseKeywords(text: String): List<String> =
        text.lines().map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
}
