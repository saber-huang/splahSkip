package com.splashskip.app

/** 诊断记录里每一行、每个开头的文字怎么写。只处理文字，不碰 Android，所以可以单独测试 */
object DiagnosticText {

    // 文字和描述最多留多少个字：开屏页上的文字都很短，留长了只会增加隐私风险和记录的大小
    const val MAX_TEXT = 40

    /** 整理一段文字：换行等控制字符换成空格，太长的截断；没有内容就写 "-" */
    fun clean(s: CharSequence?): String {
        val t = s?.toString()?.replace(Regex("[\\u0000-\\u001F]"), " ")?.trim().orEmpty()
        if (t.isEmpty()) return "-"
        return if (t.length > MAX_TEXT) t.take(MAX_TEXT) + "…" else t
    }

    /**
     * 一个界面元素写成一行。
     * hidden = 输入框、密码框：里面的文字可能是用户输入的，不记录，只写"已隐藏"。
     * hit = 这个元素符合当前的跳过规则，行首加 ★。
     */
    fun formatLine(
        depth: Int, className: String?, viewId: String?, text: CharSequence?, desc: CharSequence?,
        left: Int, top: Int, right: Int, bottom: Int, clickable: Boolean, hidden: Boolean, hit: Boolean,
    ): String {
        val cls = className?.substringAfterLast('.') ?: "?"
        val id = viewId?.substringAfter('/', viewId) ?: "-" // "com.x:id/iv_close" 只留 "iv_close"
        val shownText = if (hidden && !text.isNullOrEmpty()) "<已隐藏>" else clean(text)
        val shownDesc = if (hidden && !desc.isNullOrEmpty()) "<已隐藏>" else clean(desc)
        return (if (hit) "★ " else "") +
            "d$depth $cls id=$id text=\"$shownText\" desc=\"$shownDesc\" " +
            "[$left,$top][$right,$bottom] ${right - left}x${bottom - top} 点击=${if (clickable) "是" else "否"}"
    }

    /** 每条记录开头的说明：什么时候、哪个 App、哪个页面、什么手机、当时用的规则 */
    fun formatHeader(
        time: String, appLabel: String, packageName: String, activity: String?, device: String,
        screen: String, keywords: List<String>, idKeywords: List<String>, windowSeconds: Int, maxLength: Int,
    ): String = listOf(
        "========== 记录 $time ==========",
        "App：$appLabel（$packageName）",
        "页面：${activity ?: "?"}",
        "设备：$device；$screen",
        "规则：文字关键词=$keywords；ID 关键词=$idKeywords；生效 $windowSeconds 秒；文字最长 $maxLength 字",
        "说明：★ 表示这个元素符合当前规则；“点击”是系统报告的，有些自定义按钮不会报告成可点击",
    ).joinToString("\n")
}
