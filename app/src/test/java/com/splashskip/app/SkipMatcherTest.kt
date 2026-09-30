package com.splashskip.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkipMatcherTest {

    private fun defaults(text: String?) =
        SkipMatcher.isSkipText(text, SkipRules.DEFAULT_KEYWORDS, SkipRules.DEFAULT_MAX_LENGTH)

    // ---- 默认规则：行为必须和改动前完全一样 ----

    @Test
    fun defaultsMatchSkipButtons() {
        listOf("跳过", "跳过广告", "跳过 5", "5s 跳过", "  跳过  ", "Skip", "SKIP 3", "skip ad").forEach {
            assertTrue("应该匹配：$it", defaults(it))
        }
    }

    @Test
    fun defaultsIgnoreOtherText() {
        listOf(null, "", "   ", "关闭", "进入首页", "点击这里可以跳过开屏广告哦", "SplashSkip").forEach {
            assertFalse("不应该匹配：$it", defaults(it))
        }
    }

    @Test
    fun tenCharactersIsTheDefaultLimit() {
        assertTrue(defaults("跳过" + "x".repeat(8)))
        assertFalse(defaults("跳过" + "x".repeat(9)))
    }

    // ---- 自定义规则 ----

    @Test
    fun customKeywordsReplaceTheDefaults() {
        val custom = listOf("关闭广告", "close")
        assertTrue(SkipMatcher.isSkipText("关闭广告", custom, 10))
        assertTrue(SkipMatcher.isSkipText("CLOSE", custom, 10))
        assertFalse("默认词不再生效", SkipMatcher.isSkipText("跳过", custom, 10))
    }

    @Test
    fun customLengthLimitIsUsed() {
        assertFalse(SkipMatcher.isSkipText("跳过广告吧", SkipRules.DEFAULT_KEYWORDS, 4))
        assertTrue(SkipMatcher.isSkipText("跳过广告吧", SkipRules.DEFAULT_KEYWORDS, 5))
    }

    @Test
    fun emptyKeywordNeverMatchesEverything() {
        assertFalse(SkipMatcher.isSkipText("随便什么文字", listOf(""), 10))
        assertFalse(SkipMatcher.isSkipText("随便什么文字", emptyList(), 10))
    }

    @Test
    fun ownAppNameIsStillExcludedEvenIfSkipIsAKeyword() {
        assertFalse(SkipMatcher.isSkipText("SplashSkip", listOf("Skip"), 20))
    }

    // ---- 输入框文字整理成关键词 ----

    @Test
    fun parseKeywordsTrimsDropsBlankLinesAndDuplicates() {
        assertEquals(listOf("跳过", "Skip", "关闭"), SkipRules.parseKeywords("  跳过 \n\n Skip\nskip\n\n关闭  \n   \n跳过"))
    }

    @Test
    fun parseKeywordsHandlesWindowsLineEndingsAndEmptyInput() {
        assertEquals(listOf("跳过", "Skip"), SkipRules.parseKeywords("跳过\r\nSkip\r\n"))
        assertEquals(emptyList<String>(), SkipRules.parseKeywords(""))
        assertEquals(emptyList<String>(), SkipRules.parseKeywords(" \n  \n"))
    }

    @Test
    fun keywordsSurviveTheSaveAndLoadRoundTrip() {
        // SkipSettings 保存时用换行连起来、读取时再拆开，这里模拟这一来一回
        val original = listOf("跳过", "跳过广告", "Skip", "关闭广告")
        assertEquals(original, SkipRules.parseKeywords(original.joinToString("\n")))
    }

    @Test
    fun defaultsAreInsideTheAllowedRanges() {
        assertTrue(SkipRules.DEFAULT_WINDOW_SECONDS in SkipRules.MIN_SECONDS..SkipRules.MAX_SECONDS)
        assertTrue(SkipRules.DEFAULT_MAX_LENGTH in SkipRules.MIN_LENGTH..SkipRules.MAX_LENGTH_LIMIT)
        assertTrue(SkipRules.DEFAULT_KEYWORDS.isNotEmpty())
    }
}
