package com.splashskip.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var allowedText: TextView
    private lateinit var countText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.status_text)
        allowedText = findViewById(R.id.allowed_text)
        countText = findViewById(R.id.count_text)

        // 总开关：先显示保存的状态，再监听用户的操作（顺序不能反，否则初始化时会触发一次保存）
        findViewById<Switch>(R.id.enable_switch).apply {
            isChecked = SkipSettings.isEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, checked -> SkipSettings.setEnabled(this@MainActivity, checked) }
        }

        // 选择哪些 App 需要自动跳过
        findViewById<Button>(R.id.pick_apps_button).setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        // 防止被系统关闭：能打开设置页就跳过去并提示怎么设置，打不开就弹出手动设置的说明
        findViewById<Button>(R.id.keep_alive_button).setOnClickListener {
            if (KeepAlive.openSettings(this)) {
                Toast.makeText(this, R.string.keep_alive_toast, Toast.LENGTH_LONG).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle(R.string.keep_alive_guide_title)
                    .setMessage(R.string.keep_alive_guide)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }

        // 打开系统的无障碍设置页，在里面找到 SplashSkip 并打开
        findViewById<Button>(R.id.open_settings_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    // 每次回到这个界面（包括从选择页面返回），都刷新服务状态、已选 App 数和跳过次数
    override fun onResume() {
        super.onResume()
        statusText.setText(if (SkipService.isRunning) R.string.service_on else R.string.service_off)
        val allowedCount = SkipSettings.allowedApps(this).size
        allowedText.text =
            if (allowedCount == 0) getString(R.string.pick_apps_first) else getString(R.string.apps_selected, allowedCount)
        countText.text = getString(R.string.skip_count, SkipCounter.get(this))
    }
}
