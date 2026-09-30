package com.splashskip.app

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import java.text.Collator

/** 选择哪些 App 需要自动跳过开屏广告：列出手机上的 App，点一下勾选或取消，立刻保存 */
class AppPickerActivity : Activity() {

    private class AppItem(val label: String, val icon: Drawable, val packageName: String)

    private val items = mutableListOf<AppItem>() // 列表里显示的 App
    private val selected = mutableSetOf<String>() // 已勾选的 App（包名）
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)
        actionBar?.setDisplayHomeAsUpEnabled(true) // 标题栏左边的返回箭头

        val list = findViewById<ListView>(R.id.app_list)
        val emptyText = findViewById<TextView>(R.id.empty_text)
        list.emptyView = emptyText // 列表是空的时候显示"正在加载…"或"没有读取到…"

        selected.addAll(SkipSettings.allowedApps(this))
        adapter = AppAdapter()
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ ->
            toggle(items[position].packageName)
            adapter.notifyDataSetChanged()
        }

        // 读取所有 App 的名字和图标要一会儿，放到后台做，做完再显示
        val chosen = selected.toSet()
        Thread {
            val apps = loadApps(chosen)
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                items.addAll(apps)
                if (apps.isEmpty()) emptyText.setText(R.string.no_apps)
                adapter.notifyDataSetChanged()
            }
        }.start()
    }

    /** 点标题栏左边的返回箭头 = 返回上一页 */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /** 勾选或取消，并马上存进手机 */
    private fun toggle(packageName: String) {
        if (!selected.add(packageName)) selected.remove(packageName)
        SkipSettings.setAllowedApps(this, selected)
    }

    /** 读出手机上桌面能看到的所有 App（不含本 App）：已勾选的排前面，其余按名字排序 */
    @Suppress("DEPRECATION")
    private fun loadApps(chosen: Set<String>): List<AppItem> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val collator = Collator.getInstance()
        return packageManager.queryIntentActivities(launcherIntent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { AppItem(it.loadLabel(packageManager).toString(), it.loadIcon(packageManager), it.activityInfo.packageName) }
            .sortedWith(compareBy<AppItem> { it.packageName !in chosen }.thenComparator { a, b -> collator.compare(a.label, b.label) })
    }

    private inner class AppAdapter : BaseAdapter() {
        override fun getCount() = items.size
        override fun getItem(position: Int): Any = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_app, parent, false)
            val item = items[position]
            view.findViewById<ImageView>(R.id.app_icon).setImageDrawable(item.icon)
            view.findViewById<TextView>(R.id.app_name).text = item.label
            view.findViewById<CheckBox>(R.id.app_check).isChecked = item.packageName in selected
            return view
        }
    }
}
