package com.splashskip.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkipMatcherTest {

    @Test
    fun matchesSkipButtons() {
        listOf("跳过", "跳过广告", "跳过 5", "5s 跳过", "  跳过  ", "Skip", "SKIP 3", "skip ad").forEach {
            assertTrue("应该匹配：$it", SkipMatcher.isSkipText(it))
        }
    }

    @Test
    fun ignoresOtherText() {
        listOf(null, "", "   ", "关闭", "进入首页", "点击这里可以跳过开屏广告哦", "SplashSkip").forEach {
            assertFalse("不应该匹配：$it", SkipMatcher.isSkipText(it))
        }
    }

    @Test
    fun tenCharactersIsTheLimit() {
        assertTrue(SkipMatcher.isSkipText("跳过" + "x".repeat(8)))
        assertFalse(SkipMatcher.isSkipText("跳过" + "x".repeat(9)))
    }
}
