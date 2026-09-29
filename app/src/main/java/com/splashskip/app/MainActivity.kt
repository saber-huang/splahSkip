package com.splashskip.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var countText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.status_text)
        countText = findViewById(R.id.count_text)

        // 总开关：先显示保存的状态，再监听用户的操作（顺序不能反，否则初始化时会触发一次保存）
        findViewById<Switch>(R.id.enable_switch).apply {
            isChecked = SkipSettings.isEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, checked -> SkipSettings.setEnabled(this@MainActivity, checked) }
        }

        // 打开系统的无障碍设置页，在里面找到 SplashSkip 并打开
        findViewById<Button>(R.id.open_settings_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    // 每次回到这个界面，都刷新服务状态和跳过次数
    override fun onResume() {
        super.onResume()
        statusText.setText(if (SkipService.isRunning) R.string.service_on else R.string.service_off)
        countText.text = getString(R.string.skip_count, SkipCounter.get(this))
    }
}
