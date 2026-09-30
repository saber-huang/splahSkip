package com.splashskip.app

/**
 * 只保留最近几条诊断记录：新记录加在最后，超过 max 条就丢掉最老的。
 * 只管"记录列表"本身，不碰文件和 Android，所以可以单独测试。
 */
class RecordStore(private val max: Int, private val separator: String, private val maxRecordChars: Int) {

    private val records = ArrayDeque<String>() // 最老的在前面

    val size: Int get() = records.size

    /** 开始一条新记录 */
    fun start(header: String) {
        records.addLast(header)
        while (records.size > max) records.removeFirst()
    }

    /** 往最新的一条记录后面加内容；这条记录已经太长就不再加，只在末尾提醒一次 */
    fun appendToNewest(text: String) {
        val current = records.removeLastOrNull() ?: return
        records.addLast(
            when {
                current.endsWith(TRUNCATED_NOTE) -> current
                current.length + text.length > maxRecordChars -> current + "\n" + TRUNCATED_NOTE
                else -> current + "\n" + text
            }
        )
    }

    fun newest(): String? = records.lastOrNull()

    /** 全部记录，最新的在最前面 */
    fun allNewestFirst(): String = records.reversed().joinToString("\n\n")

    fun clear() = records.clear()

    /** 存进文件时的样子；读回来用 load */
    fun serialize(): String = records.joinToString(separator)

    fun load(serialized: String) {
        records.clear()
        if (serialized.isBlank()) return
        serialized.split(separator).takeLast(max).forEach { records.addLast(it) }
    }

    companion object {
        const val TRUNCATED_NOTE = "（这条记录太长，后面的内容没有保存）"
    }
}
