package com.ivor.ivormusic.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import com.ivor.ivormusic.MainActivity
import com.ivor.ivormusic.data.ExperiencePreferences
import kotlin.math.abs
import kotlin.math.roundToInt

/** Optional, draggable music pill drawn over other apps after overlay permission is granted. */
class DynamicIslandService : Service() {
    private lateinit var windowManager: WindowManager
    private var islandView: TextView? = null
    private var islandParams: WindowManager.LayoutParams? = null
    private var receiverRegistered = false
    private var currentTitle = ""
    private var currentArtist = ""
    private var isPlaying = false
    private var downX = 0f
    private var downY = 0f
    private var startX = 0
    private var startY = 0
    private var moved = false

    private val playbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_PLAYBACK_STATE) return
            currentTitle = intent.getStringExtra(EXTRA_TITLE).orEmpty()
            currentArtist = intent.getStringExtra(EXTRA_ARTIST).orEmpty()
            isPlaying = intent.getBooleanExtra(EXTRA_PLAYING, false)
            updateText()
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val filter = IntentFilter(ACTION_PLAYBACK_STATE)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(playbackReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(playbackReceiver, filter)
        }
        receiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_HIDE -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_REFRESH -> refreshAppearance()
            else -> ensureOverlay()
        }
        return START_STICKY
    }

    private fun ensureOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        if (islandView != null) {
            refreshAppearance()
            return
        }
        val preferences = ExperiencePreferences(this)
        val view = TextView(this).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setPadding(dp(16), dp(8), dp(16), dp(8))
            elevation = dp(8).toFloat()
        }
        val params = WindowManager.LayoutParams(
            dp(preferences.dynamicIslandWidthDp.value),
            dp(preferences.dynamicIslandHeightDp.value),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(12)
        }
        view.setOnTouchListener { _, event ->
            val currentParams = islandParams ?: return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = currentParams.x
                    startY = currentParams.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (abs(dx) > dp(5) || abs(dy) > dp(5)) moved = true
                    if (moved) {
                        currentParams.x = startX + dx.roundToInt()
                        currentParams.y = (startY + dy.roundToInt()).coerceAtLeast(0)
                        runCatching { windowManager.updateViewLayout(view, currentParams) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) openApp()
                    true
                }
                else -> true
            }
        }
        islandView = view
        islandParams = params
        runCatching {
            windowManager.addView(view, params)
            refreshAppearance()
        }.onFailure {
            islandView = null
            islandParams = null
            stopSelf()
        }
    }

    private fun refreshAppearance() {
        if (islandView == null) {
            ensureOverlay()
            return
        }
        val preferences = ExperiencePreferences(this)
        val params = islandParams ?: return
        params.width = dp(preferences.dynamicIslandWidthDp.value)
        params.height = dp(preferences.dynamicIslandHeightDp.value)
        runCatching { windowManager.updateViewLayout(islandView, params) }
        val colors = when (preferences.dynamicIslandTheme.value) {
            "violet" -> intArrayOf(Color.rgb(43, 24, 76), Color.rgb(18, 14, 36))
            "ocean" -> intArrayOf(Color.rgb(12, 67, 92), Color.rgb(10, 25, 47))
            else -> intArrayOf(Color.argb(232, 40, 43, 54), Color.argb(220, 18, 20, 29))
        }
        islandView?.background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors).apply {
            cornerRadius = dp(preferences.dynamicIslandHeightDp.value).toFloat()
            setStroke(dp(1), Color.argb(90, 255, 255, 255))
        }
        updateText()
    }

    private fun updateText() {
        val title = currentTitle.ifBlank { getString(com.ivor.ivormusic.R.string.app_name) }
        val detail = currentArtist.takeIf { it.isNotBlank() }
        val prefix = if (isPlaying) "♫ " else "Ⅱ "
        islandView?.text = prefix + title + if (detail != null) "  ·  " + detail else ""
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        runCatching { startActivity(intent) }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    override fun onDestroy() {
        if (receiverRegistered) runCatching { unregisterReceiver(playbackReceiver) }
        islandView?.let { view -> runCatching { windowManager.removeView(view) } }
        islandView = null
        islandParams = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_SHOW = "com.ivor.ivormusic.action.DYNAMIC_ISLAND_SHOW"
        const val ACTION_HIDE = "com.ivor.ivormusic.action.DYNAMIC_ISLAND_HIDE"
        const val ACTION_REFRESH = "com.ivor.ivormusic.action.DYNAMIC_ISLAND_REFRESH"
        const val ACTION_PLAYBACK_STATE = "com.ivor.ivormusic.action.DYNAMIC_ISLAND_PLAYBACK"
        const val EXTRA_TITLE = "title"
        const val EXTRA_ARTIST = "artist"
        const val EXTRA_PLAYING = "playing"
    }
}
