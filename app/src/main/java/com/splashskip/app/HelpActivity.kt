package com.splashskip.app

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView

/** 使用说明与隐私：怎么用、没跳过怎么办、隐私说明。文字都写在 strings.xml 里 */
class HelpActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        actionBar?.setDisplayHomeAsUpEnabled(true) // 标题栏左边的返回箭头
        findViewById<TextView>(R.id.version_text).text = getString(R.string.help_version, versionName())
    }

    /** 点标题栏左边的返回箭头 = 返回上一页 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** 版本号来自 app/build.gradle.kts 里的 versionName */
    @Suppress("DEPRECATION")
    private fun versionName(): String = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
    } catch (e: Exception) {
        "?"
    }
}
