package com.splashskip.app

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

/** 修改"跳过"的规则：按钮上的关键词、生效多少秒、文字最长几个字。保存后服务下一次扫描就会用新规则 */
class RulesActivity : Activity() {

    private lateinit var keywordsEdit: EditText
    private lateinit var windowEdit: EditText
    private lateinit var lengthEdit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)
        keywordsEdit = findViewById(R.id.keywords_edit)
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

    /** 把现在生效的规则填进输入框 */
    private fun showCurrentRules() {
        keywordsEdit.setText(SkipSettings.keywords(this).joinToString("\n"))
        windowEdit.setText(SkipSettings.windowSeconds(this).toString())
        lengthEdit.setText(SkipSettings.maxLength(this).toString())
    }

    /** 检查填的内容，没问题才保存；有问题就提示，不保存 */
    private fun save() {
        val keywords = SkipRules.parseKeywords(keywordsEdit.text.toString())
        val seconds = windowEdit.text.toString().trim().toIntOrNull()
        val length = lengthEdit.text.toString().trim().toIntOrNull()

        if (keywords.isEmpty()) {
            toast(getString(R.string.rules_err_keywords))
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

        SkipSettings.setRules(this, keywords, length, seconds)
        toast(getString(R.string.rules_saved))
        finish()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
