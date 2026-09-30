package com.splashskip.app

import android.content.Context
import android.os.Build
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 把屏幕上的界面元素抄成文字，写进诊断记录。每一行怎么写在 DiagnosticText 里 */
object DiagnosticDump {

    private const val MAX_LINES = 120 // 一张快照最多写多少行
    private const val MAX_NODES = 1000 // 一张快照最多检查多少个元素，防止界面太复杂时卡顿
    private const val MAX_DEPTH = 40 // 界面层次太深就不再往里找

    /** 一条记录开头的说明 */
    @Suppress("DEPRECATION")
    fun header(
        context: Context, packageName: String, activity: String?,
        keywords: List<String>, idKeywords: List<String>, windowSeconds: Int, maxLength: Int,
    ): String {
        val pm = context.packageManager
        val label = try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) {
            packageName // 查不到名字就直接写包名
        }
        val m = context.resources.displayMetrics
        return DiagnosticText.formatHeader(
            time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
            appLabel = label, packageName = packageName, activity = activity,
            device = "${Build.MANUFACTURER} ${Build.MODEL}，Android ${Build.VERSION.RELEASE}",
            screen = "屏幕 ${m.widthPixels}x${m.heightPixels} 像素，密度 ${m.density}（1dp = ${m.density} 像素）",
            keywords = keywords, idKeywords = idKeywords, windowSeconds = windowSeconds, maxLength = maxLength,
        )
    }

    /** 给这个 App 当前的界面拍一张"快照"：把看得见的元素一行一个抄下来 */
    fun snapshot(
        windows: List<AccessibilityWindowInfo>, packageName: String, index: Int, elapsedMs: Long,
        keywords: List<String>, idKeywords: List<String>, maxLength: Int,
    ): String {
        val out = Collector(keywords, idKeywords, maxLength)
        for (window in windows) {
            val root = window.root ?: continue
            if (root.packageName?.toString() == packageName) collect(root, 0, out)
        }
        val seconds = String.format(Locale.US, "%.1f", elapsedMs / 1000.0)
        val more = if (out.truncated) "，元素太多，只记录了前面一部分" else ""
        return "--- 快照 #$index（切换后 $seconds 秒）：${out.lines.size} 个元素$more ---\n" +
            out.lines.joinToString("\n")
    }

    private class Collector(val keywords: List<String>, val idKeywords: List<String>, val maxLength: Int) {
        val lines = mutableListOf<String>()
        var visited = 0
        var truncated = false
    }

    private fun collect(node: AccessibilityNodeInfo, depth: Int, out: Collector) {
        if (!node.isVisibleToUser) return // 看不见的元素，它里面的也看不见，整个跳过
        if (out.visited++ >= MAX_NODES || depth > MAX_DEPTH) {
            out.truncated = true
            return
        }

        // 只记有信息的：能点的、有字的、有名字的、图片、最里面一层的（画出来的"×"常常是一个什么信息都没有的小方块）
        val informative = node.isClickable || !node.text.isNullOrEmpty() || !node.contentDescription.isNullOrEmpty() ||
            node.viewIdResourceName != null || node.className?.contains("Image") == true || node.childCount == 0
        if (informative) {
            if (out.lines.size >= MAX_LINES) {
                out.truncated = true
            } else {
                val bounds = Rect()
                node.getBoundsInScreen(bounds)
                val hit = SkipMatcher.isSkipText(node.text, out.keywords, out.maxLength) ||
                    SkipMatcher.isSkipText(node.contentDescription, out.keywords, out.maxLength) ||
                    SkipMatcher.isSkipId(node.viewIdResourceName, out.idKeywords)
                out.lines += DiagnosticText.formatLine(
                    depth, node.className?.toString(), node.viewIdResourceName, node.text, node.contentDescription,
                    bounds.left, bounds.top, bounds.right, bounds.bottom,
                    clickable = node.isClickable, hidden = node.isEditable || node.isPassword, hit = hit,
                )
            }
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collect(it, depth + 1, out) }
        }
    }
}
