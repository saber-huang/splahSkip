package com.splashskip.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsTest {

    // ---------- 按 ID 匹配 ----------
    private val ids = listOf("skip", "close")

    @Test fun idMatchesEntryNameCaseInsensitively() {
        assertTrue(SkipMatcher.isSkipId("com.xx.app:id/tt_splash_skip_btn", ids))
        assertTrue(SkipMatcher.isSkipId("com.xx.app:id/IV_CLOSE", ids))
        assertTrue(SkipMatcher.isSkipId("com.xx.app:id/closeButton", ids))
    }

    @Test fun idIgnoresThePackageNamePart() {
        assertFalse("包名里带 skip 不算", SkipMatcher.isSkipId("com.skip.close:id/title", ids))
    }

    @Test fun idNeverMatchesWhenEmptyOrNoSlashOrNoKeywords() {
        assertFalse(SkipMatcher.isSkipId(null, ids))
        assertFalse(SkipMatcher.isSkipId("", ids))
        assertFalse(SkipMatcher.isSkipId("skip_button_without_slash", ids))
        assertFalse(SkipMatcher.isSkipId("com.a:id/skip", emptyList()))
        assertFalse("空关键词不能匹配所有", SkipMatcher.isSkipId("com.a:id/skip", listOf("")))
    }

    // ---------- 文字整理 ----------
    @Test fun cleanHandlesEmptyNewlinesAndLongText() {
        assertEquals("-", DiagnosticText.clean(null))
        assertEquals("-", DiagnosticText.clean("   "))
        assertEquals("a b c", DiagnosticText.clean("a\nb\nc"))
        assertEquals("跳过", DiagnosticText.clean("  跳过  "))
        val long = "字".repeat(100)
        assertEquals("字".repeat(DiagnosticText.MAX_TEXT) + "…", DiagnosticText.clean(long))
    }

    @Test fun cleanRemovesTheControlCharUsedAsSeparator() {
        assertFalse(DiagnosticText.clean("x\u0001y").contains('\u0001'))
    }

    // ---------- 一行元素 ----------
    @Test fun lineForATypicalCloseIcon() {
        val line = DiagnosticText.formatLine(3, "android.widget.ImageView", "com.a.b:id/iv_close", null, "关闭",
            900, 80, 1000, 180, clickable = true, hidden = false, hit = false)
        assertEquals("d3 ImageView id=iv_close text=\"-\" desc=\"关闭\" [900,80][1000,180] 100x100 点击=是", line)
    }

    @Test fun lineMarksRuleHitsWithAStar() {
        val line = DiagnosticText.formatLine(1, "TextView", null, "跳过 3", null, 0, 0, 10, 10, clickable = false, hidden = false, hit = true)
        assertTrue(line.startsWith("★ d1 TextView id=- text=\"跳过 3\""))
        assertTrue(line.endsWith("点击=否"))
    }

    @Test fun editableFieldsNeverRevealTheirText() {
        val line = DiagnosticText.formatLine(2, "android.widget.EditText", "com.a:id/pwd", "我的密码123", "请输入密码",
            0, 0, 500, 100, clickable = true, hidden = true, hit = false)
        assertFalse(line.contains("我的密码123"))
        assertFalse(line.contains("请输入密码"))
        assertTrue(line.contains("text=\"<已隐藏>\""))
        assertTrue(line.contains("desc=\"<已隐藏>\""))
    }

    @Test fun hiddenFieldWithNoTextStillShowsDash() {
        val line = DiagnosticText.formatLine(2, "EditText", null, null, null, 0, 0, 1, 1, clickable = true, hidden = true, hit = false)
        assertTrue(line.contains("text=\"-\""))
    }

    @Test fun headerListsTheThingsWeNeedToWriteRules() {
        val h = DiagnosticText.formatHeader("2026-09-30 17:20:11", "有道词典", "com.youdao.dict", "com.youdao.dict.SplashActivity",
            "HUAWEI ALN-AL00，Android 12", "屏幕 1200x2640 像素，密度 3.0", listOf("跳过", "Skip"), emptyList(), 5, 10)
        listOf("有道词典", "com.youdao.dict", "SplashActivity", "Android 12", "1200x2640", "[跳过, Skip]", "ID 关键词=[]", "生效 5 秒").forEach {
            assertTrue("头部缺少：$it", h.contains(it))
        }
    }

    // ---------- 只保留最近几条 ----------
    private fun store(max: Int = 5, maxChars: Int = 1000) = RecordStore(max, "\n\u0001\n", maxChars)

    @Test fun keepsOnlyTheLatestFiveRecords() {
        val s = store()
        (1..7).forEach { s.start("记录$it") }
        assertEquals(5, s.size)
        assertEquals("记录7", s.newest())
        val all = s.allNewestFirst()
        assertFalse(all.contains("记录1")); assertFalse(all.contains("记录2"))
        assertTrue(all.indexOf("记录7") < all.indexOf("记录3"))
    }

    @Test fun appendsGoToTheNewestRecordOnly() {
        val s = store()
        s.start("A"); s.start("B")
        s.appendToNewest("快照1"); s.appendToNewest("快照2")
        assertEquals("B\n快照1\n快照2", s.newest())
        assertTrue(s.allNewestFirst().contains("A"))
        assertFalse(s.allNewestFirst().substringAfter("B\n快照1\n快照2").contains("快照"))
    }

    @Test fun appendWithoutARecordDoesNothing() {
        val s = store(); s.appendToNewest("孤零零的内容")
        assertEquals(0, s.size); assertNull(s.newest())
    }

    @Test fun recordStopsGrowingPastTheLimitAndSaysSo() {
        val s = store(maxChars = 30)
        s.start("头部")
        s.appendToNewest("x".repeat(20))
        s.appendToNewest("y".repeat(20)) // 会超
        s.appendToNewest("z".repeat(2))  // 已经被截断，之后都不再加
        val n = s.newest()!!
        assertTrue(n.endsWith(RecordStore.TRUNCATED_NOTE))
        assertFalse(n.contains("y")); assertFalse(n.contains("z"))
        assertEquals("只提醒一次", 1, Regex(Regex.escape(RecordStore.TRUNCATED_NOTE)).findAll(n).count())
    }

    @Test fun surviveSaveAndLoad() {
        val s = store()
        s.start("记录一\n里面有换行"); s.appendToNewest("快照"); s.start("记录二")
        val saved = s.serialize()
        val t = store(); t.load(saved)
        assertEquals(2, t.size)
        assertEquals("记录二", t.newest())
        assertEquals(s.allNewestFirst(), t.allNewestFirst())
        assertEquals(saved, t.serialize())
    }

    @Test fun loadHandlesEmptyAndTooManyRecords() {
        val t = store(); t.load(""); assertEquals(0, t.size)
        t.load((1..9).joinToString("\n\u0001\n") { "r$it" })
        assertEquals(5, t.size); assertEquals("r9", t.newest())
    }

    @Test fun clearEmptiesEverything() {
        val s = store(); s.start("A"); s.clear()
        assertEquals(0, s.size); assertEquals("", s.allNewestFirst())
    }
}
