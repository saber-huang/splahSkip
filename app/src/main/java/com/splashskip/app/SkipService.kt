package com.splashskip.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.InputMethod

/**
 * 无障碍服务：每次切换到一个新 App，在前 5 秒内找"跳过"按钮，找到就点一下。
 * 在 Android Studio 的 Logcat 里搜 SplashSkip，可以看到它每一步做了什么。
 */
class SkipService : AccessibilityService() {

    // 当前在前台的 App、切换过来的时间、这次启动是否已经点过
    private var currentPackage: String? = null
    private var switchTime = 0L
    private var clicked = false

    // 键盘、下拉通知栏、系统弹窗只是盖在 App 上面，它们出现不算切换 App
    private var overlayPackages = emptySet<String>()

    // 桌面、系统设置和本 App 里也有带"跳过""Skip"的字（比如本 App 的名字），不在这些地方点
    private var ignoredPackages = emptySet<String>()

    private var lastLog = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayPackages = setOf("android", "com.android.systemui") + inputMethodPackages()
        ignoredPackages = setOf(packageName, "com.android.settings") + launcherPackages()
        isRunning = true
        log("无障碍服务已开启")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        val now = SystemClock.elapsedRealtime()

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            pkg != currentPackage && pkg !in overlayPackages
        ) {
            currentPackage = pkg
            switchTime = now
            clicked = false
            log("切换到 App：$pkg")
        }

        // 只处理：当前 App 的界面、这次还没点过、切换过来 5 秒以内
        if (pkg != currentPackage || pkg in ignoredPackages || clicked) return
        if (now - switchTime > SKIP_WINDOW_MS) return

        for (window in windows) {
            val root = window.root ?: continue
            if (root.packageName?.toString() == pkg && findAndClickSkip(root)) {
                clicked = true
                SkipCounter.increment(this)
                return
            }
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    /** 从外到内一层层检查界面上的元素，找到"跳过"并点成功就返回 true */
    private fun findAndClickSkip(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque(listOf(root))
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val node = queue.removeFirst()
            visited++
            val isSkip = SkipMatcher.isSkipText(node.text) || SkipMatcher.isSkipText(node.contentDescription)
            if (isSkip && node.isVisibleToUser && click(node)) return true
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        return false
    }

    /** 点"跳过"：先让按钮自己执行点击；不行的话，模拟手指点一下文字中间 */
    private fun click(node: AccessibilityNodeInfo): Boolean {
        log("找到候选：${describe(node)}")

        val button = findClickable(node)
        if (button != null && isButtonSized(button) &&
            button.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        ) {
            log("已点击（按钮点击）：${describe(button)}")
            return true
        }

        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        if (isButtonSized(node) && tap(bounds.exactCenterX(), bounds.exactCenterY())) {
            log("已点击（模拟手指）：位置=${bounds.toShortString()}")
            return true
        }

        log("没有点击：区域太大或位置不对，怕点到广告本身")
        return false
    }

    /** 文字本身常常不能点，能点的是外面包着它的按钮，所以最多往外找几层 */
    private fun findClickable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        var level = 0
        while (current != null && level <= MAX_PARENT_LEVELS) {
            if (current.isClickable) return current
            current = current.parent
            level++
        }
        return null
    }

    /** "跳过"按钮应该是小小一块：宽不超过屏幕一半，高不超过屏幕五分之一。太大的多半是整个广告，点了会打开广告 */
    private fun isButtonSized(node: AccessibilityNodeInfo): Boolean {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val screen = resources.displayMetrics
        return !bounds.isEmpty &&
            bounds.width() <= screen.widthPixels / 2 &&
            bounds.height() <= screen.heightPixels / 5
    }

    /** 模拟手指在 (x, y) 轻点一下 */
    private fun tap(x: Float, y: Float): Boolean {
        if (x < 0 || y < 0) return false
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, TAP_DURATION_MS)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    private fun describe(node: AccessibilityNodeInfo): String {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        val text = node.text ?: node.contentDescription
        return "文字=\"$text\" id=${node.viewIdResourceName} 位置=${bounds.toShortString()} 可点击=${node.isClickable}"
    }

    /** 打印到 Logcat；和上一条一模一样的就不重复打印 */
    private fun log(message: String) {
        if (message == lastLog) return
        lastLog = message
        Log.i(TAG, message)
    }

    @Suppress("DEPRECATION")
    private fun inputMethodPackages(): List<String> =
        packageManager.queryIntentServices(Intent(InputMethod.SERVICE_INTERFACE), 0)
            .map { it.serviceInfo.packageName }

    @Suppress("DEPRECATION")
    private fun launcherPackages(): List<String> =
        packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)
            .map { it.activityInfo.packageName }

    companion object {
        private const val TAG = "SplashSkip"
        private const val SKIP_WINDOW_MS = 5_000L // 切换 App 后多久内有效
        private const val MAX_NODES = 1000 // 每次最多检查多少个界面元素，防止界面太复杂时卡顿
        private const val MAX_PARENT_LEVELS = 3 // 从文字往外最多找几层可点击的按钮
        private const val TAP_DURATION_MS = 50L

        /** 服务是否正在运行，主界面用它显示状态 */
        var isRunning = false
            private set
    }
}
