package com.splashskip.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/** 诊断记录：开关、"复制"和"清空"按钮，以及最近一次记录的预览 */
class DiagnosticsActivity : Activity() {

    private lateinit var countText: TextView
    private lateinit var previewText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)
        actionBar?.setDisplayHomeAsUpEnabled(true) // 标题栏左边的返回箭头
        countText = findViewById(R.id.record_count)
        previewText = findViewById(R.id.preview_text)

        // 开关：先显示保存的状态，再监听用户的操作
        val diagSwitch = findViewById<Switch>(R.id.diag_switch)
        diagSwitch.isChecked = SkipSettings.isDiagnosticsEnabled(this)
        diagSwitch.setOnCheckedChangeListener { _, checked -> SkipSettings.setDiagnosticsEnabled(this, checked) }
        findViewById<View>(R.id.diag_row).setOnClickListener { diagSwitch.toggle() } // 点整张卡片也能切换

        findViewById<Button>(R.id.copy_latest_button).setOnClickListener { copy(DiagnosticLog.newest(this)) }
        findViewById<Button>(R.id.copy_all_button).setOnClickListener { copy(DiagnosticLog.all(this)) }
        findViewById<Button>(R.id.clear_button).setOnClickListener {
            DiagnosticLog.clear(this)
            showRecords()
            toast(getString(R.string.diag_cleared))
        }
    }

    // 每次回到这个页面都刷新：去别的 App 测试的时候，记录是在后台增加的
    override fun onResume() {
        super.onResume()
        showRecords()
    }

    /** 点标题栏左边的返回箭头 = 返回上一页 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** 显示已有几条记录，以及最近一次的开头部分（太长的只预览前面一段，复制的仍然是完整内容） */
    private fun showRecords() {
        countText.text = getString(R.string.diag_count, DiagnosticLog.count(this), DiagnosticLog.maxRecords())
        val newest = DiagnosticLog.newest(this)
        previewText.text = when {
            newest == null -> getString(R.string.diag_empty)
            newest.length > PREVIEW_CHARS -> newest.take(PREVIEW_CHARS) + "\n" + getString(R.string.diag_preview_cut)
            else -> newest
        }
    }

    private fun copy(text: String?) {
        if (text.isNullOrEmpty()) {
            toast(getString(R.string.diag_nothing_to_copy))
            return
        }
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.diagnostics), text))
            toast(getString(R.string.diag_copied, text.length))
        } catch (e: Exception) {
            toast(getString(R.string.diag_copy_failed)) // 内容太大等原因，剪贴板不收
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    companion object {
        private const val PREVIEW_CHARS = 3000
    }
}
