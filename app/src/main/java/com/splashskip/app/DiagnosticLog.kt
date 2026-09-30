package com.splashskip.app

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * 诊断记录：最近 5 次的记录，存在这台手机 App 自己的文件里（别的 App 读不到），不联网。
 * 记录列表本身的规则在 RecordStore 里，这里只负责读写文件。
 */
object DiagnosticLog {

    private const val TAG = "SplashSkip"
    private const val FILE_NAME = "diagnostics.txt"
    private const val MAX_RECORDS = 5
    private const val MAX_RECORD_CHARS = 60_000 // 一条记录最多这么多字，免得复制和粘贴时太大
    private const val SEPARATOR = "\n\u0001\n" // 记录之间的分隔；文字里的控制字符已经被 DiagnosticText 清掉了

    private val store = RecordStore(MAX_RECORDS, SEPARATOR, MAX_RECORD_CHARS)
    private var loaded = false

    @Synchronized
    fun startRecord(context: Context, header: String) {
        load(context)
        store.start(header)
        save(context)
    }

    @Synchronized
    fun append(context: Context, text: String) {
        load(context)
        store.appendToNewest(text)
        save(context)
    }

    @Synchronized
    fun newest(context: Context): String? {
        load(context)
        return store.newest()
    }

    @Synchronized
    fun all(context: Context): String {
        load(context)
        return store.allNewestFirst()
    }

    @Synchronized
    fun count(context: Context): Int {
        load(context)
        return store.size
    }

    @Synchronized
    fun clear(context: Context) {
        store.clear()
        loaded = true
        save(context)
    }

    fun maxRecords() = MAX_RECORDS

    /** 第一次用到时从文件里读回来（App 被系统杀掉重启后，内存里的记录没了，文件里还在） */
    private fun load(context: Context) {
        if (loaded) return
        loaded = true
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) store.load(file.readText(Charsets.UTF_8))
        } catch (e: IOException) {
            Log.w(TAG, "读取诊断记录失败：${e.javaClass.simpleName}")
        }
    }

    private fun save(context: Context) {
        try {
            File(context.filesDir, FILE_NAME).writeText(store.serialize(), Charsets.UTF_8)
        } catch (e: IOException) {
            Log.w(TAG, "保存诊断记录失败：${e.javaClass.simpleName}")
        }
    }
}
