package com.splashskip.app

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

/** 修改"跳过"的规则：按钮上的关键词、生效多少秒、文字最长几个字。保存后服务下一次扫描就会用新规则 */
class RulesActivity : Activity() {

    private lateinit var keywordsEdit: EditText
    private lateinit var idKeywordsEdit: EditText
    private lateinit var windowEdit: EditText
    private lateinit var lengthEdit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)
        actionBar?.setDisplayHomeAsUpEnabled(true) // 标题栏左边的返回箭头
        keywordsEdit = findViewById(R.id.keywords_edit)
        idKeywordsEdit = findViewById(R.id.id_keywords_edit)
        windowEdit = findViewById(R.id.window_edit)
        lengthEdit = findViewById(R.id.length_edit)
        showCurrentRules()

        findViewById<Button>(R.id.save_button).setOnClickListener { save() }
        findViewById<Button>(R.id.reset_button).setOnClickListener {
            SkipSettings.resetRules(this)
            showCurrentRules()
            toast(getString(R.string.rules_reset_done))
        }
    }

    /** 点标题栏左边的返回箭头 = 返回上一页 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** 把现在生效的规则填进输入框 */
    private fun showCurrentRules() {
        keywordsEdit.setText(SkipSettings.keywords(this).joinToString("\n"))
        idKeywordsEdit.setText(SkipSettings.idKeywords(this).joinToString("\n"))
        windowEdit.setText(SkipSettings.windowSeconds(this).toString())
        lengthEdit.setText(SkipSettings.maxLength(this).toString())
    }

    /** 检查填的内容，没问题才保存；有问题就提示，不保存 */
    private fun save() {
        val keywords = SkipRules.parseKeywords(keywordsEdit.text.toString())
        val idKeywords = SkipRules.parseKeywords(idKeywordsEdit.text.toString())
        val seconds = windowEdit.text.toString().trim().toIntOrNull()
        val length = lengthEdit.text.toString().trim().toIntOrNull()

        if (keywords.isEmpty()) {
            toast(getString(R.string.rules_err_keywords))
            return
        }
        // ID 关键词太短会匹配到一大堆按钮，容易误点，所以要求至少 3 个字符（可以不填）
        idKeywords.firstOrNull { it.length < SkipRules.MIN_ID_KEYWORD_LENGTH }?.let {
            toast(getString(R.string.rules_err_id, SkipRules.MIN_ID_KEYWORD_LENGTH, it))
            return
        }
        if (seconds == null || seconds !in SkipRules.MIN_SECONDS..SkipRules.MAX_SECONDS) {
            toast(getString(R.string.rules_err_window, SkipRules.MIN_SECONDS, SkipRules.MAX_SECONDS))
            return
        }
        if (length == null || length !in SkipRules.MIN_LENGTH..SkipRules.MAX_LENGTH_LIMIT) {
            toast(getString(R.string.rules_err_length, SkipRules.MIN_LENGTH, SkipRules.MAX_LENGTH_LIMIT))
            return
        }

        SkipSettings.setRules(this, keywords, idKeywords, length, seconds)
        toast(getString(R.string.rules_saved))
        finish()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
