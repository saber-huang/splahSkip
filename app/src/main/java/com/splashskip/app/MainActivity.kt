package com.splashskip.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import java.text.NumberFormat

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var statusDot: View
    private lateinit var statusHint: TextView
    private lateinit var openSettingsButton: Button
    private lateinit var allowedText: TextView
    private lateinit var diagnosticsText: TextView
    private lateinit var countText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.status_text)
        statusDot = findViewById(R.id.status_dot)
        statusHint = findViewById(R.id.status_hint)
        openSettingsButton = findViewById(R.id.open_settings_button)
        allowedText = findViewById(R.id.allowed_text)
        diagnosticsText = findViewById(R.id.diagnostics_text)
        countText = findViewById(R.id.count_text)

        // 总开关：先显示保存的状态，再监听用户的操作（顺序不能反，否则初始化时会触发一次保存）
        val enableSwitch = findViewById<Switch>(R.id.enable_switch)
        enableSwitch.isChecked = SkipSettings.isEnabled(this)
        enableSwitch.setOnCheckedChangeListener { _, checked -> SkipSettings.setEnabled(this, checked) }
        // 点整张卡片也能切换开关
        findViewById<View>(R.id.enable_row).setOnClickListener { enableSwitch.toggle() }

        // 选择哪些 App 需要自动跳过
        findViewById<View>(R.id.pick_apps_button).setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        // 修改"跳过"的规则（关键词、生效时间、文字长度）
        findViewById<View>(R.id.rules_button).setOnClickListener {
            startActivity(Intent(this, RulesActivity::class.java))
        }

        // 诊断记录：排查"为什么没跳成功"用
        findViewById<View>(R.id.diagnostics_button).setOnClickListener {
            startActivity(Intent(this, DiagnosticsActivity::class.java))
        }

        // 使用说明与隐私
        findViewById<View>(R.id.help_button).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // 防止被系统关闭：能打开设置页就跳过去并提示怎么设置，打不开就弹出手动设置的说明
        findViewById<View>(R.id.keep_alive_button).setOnClickListener {
            val page = KeepAlive.openSettings(this)
            if (page == null) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.keep_alive_guide_title)
                    .setMessage(R.string.keep_alive_guide)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            } else {
                // 不同的页面里要做的事不一样，提示也不一样
                val hint = when (page) {
                    KeepAlive.Page.HUAWEI_STARTUP -> R.string.keep_alive_hint_startup
                    KeepAlive.Page.APP_DETAILS -> R.string.keep_alive_hint_details
                    KeepAlive.Page.BATTERY_LIST -> R.string.keep_alive_hint_battery
                }
                Toast.makeText(this, hint, Toast.LENGTH_LONG).show()
            }
        }

        // 打开系统的无障碍设置页，在里面找到 SplashSkip 并打开
        openSettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // 第一次打开 App：自动显示一次使用说明，之后从首页的"使用说明与隐私"进入
        if (!SkipSettings.isHelpShown(this)) {
            SkipSettings.setHelpShown(this)
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    // 每次回到这个界面（包括从别的页面返回），都刷新服务状态、已选 App 数和跳过次数
    override fun onResume() {
        super.onResume()
        showServiceStatus(SkipService.isRunning)

        val allowedCount = SkipSettings.allowedApps(this).size
        if (allowedCount == 0) {
            allowedText.setText(R.string.pick_apps_first)
            allowedText.setTextColor(getColor(R.color.warning)) // 一个都没选，用醒目的颜色提醒
        } else {
            allowedText.text = getString(R.string.apps_selected, allowedCount)
            allowedText.setTextColor(getColor(R.color.text_secondary))
        }

        // 诊断记录开着的时候用醒目的颜色提醒，用完记得关
        if (SkipSettings.isDiagnosticsEnabled(this)) {
            diagnosticsText.text = getString(R.string.diagnostics_summary_on, DiagnosticLog.count(this))
            diagnosticsText.setTextColor(getColor(R.color.warning))
        } else {
            diagnosticsText.setText(R.string.diagnostics_summary_off)
            diagnosticsText.setTextColor(getColor(R.color.text_secondary))
        }

        countText.text = NumberFormat.getIntegerInstance().format(SkipCounter.get(this))
    }

    /** 服务开着：绿色，按钮变成不抢眼的描边按钮；没开：橙色，按钮是醒目的实心按钮，引导用户去开 */
    private fun showServiceStatus(running: Boolean) {
        val color = getColor(if (running) R.color.success else R.color.warning)
        statusText.setText(if (running) R.string.service_on else R.string.service_off)
        statusText.setTextColor(color)
        statusDot.backgroundTintList = ColorStateList.valueOf(color)
        statusHint.setText(if (running) R.string.service_hint_on else R.string.service_hint_off)

        openSettingsButton.setText(if (running) R.string.open_settings_again else R.string.open_settings)
        openSettingsButton.setBackgroundResource(if (running) R.drawable.bg_button_secondary else R.drawable.bg_button_primary)
        openSettingsButton.setTextColor(getColor(if (running) R.color.accent_text else R.color.on_accent))
    }
}
