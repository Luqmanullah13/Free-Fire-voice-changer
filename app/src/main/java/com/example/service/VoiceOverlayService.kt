package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color as AndroidColor
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.VoiceEngineManager
import com.example.audio.VoicePreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground microphone & floating game HUD overlay service.
 *
 * Uses pure Android Views inside WindowManager for ultra-lightweight overlay rendering and declares
 * foregroundServiceType="microphone" for Android 14+ compliance.
 */
class VoiceOverlayService : Service() {

  companion object {
    const val CHANNEL_ID = "voxforge_dsp_channel"
    const val NOTIFICATION_ID = 4104
    const val ACTION_START_OVERLAY = "com.example.action.START_OVERLAY"
    const val ACTION_STOP_ALL = "com.example.action.STOP_ALL"

    fun startOverlay(context: Context) {
      val intent = Intent(context, VoiceOverlayService::class.java).apply {
        action = ACTION_START_OVERLAY
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stopOverlay(context: Context) {
      val intent = Intent(context, VoiceOverlayService::class.java).apply {
        action = ACTION_STOP_ALL
      }
      context.startService(intent)
    }
  }

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var windowManager: WindowManager? = null
  private var bubbleView: View? = null
  private var panelView: View? = null
  private var isPanelExpanded = false

  private var presetRows = mutableMapOf<VoicePreset, TextView>()
  private var headerStatusView: TextView? = null
  private var liveToggleBtn: TextView? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP_ALL) {
      VoiceEngineManager.stopLiveModulation()
      VoiceEngineManager.setOverlayActive(false)
      removeOverlayViews()
      stopForeground(STOP_FOREGROUND_REMOVE)
      stopSelf()
      return START_NOT_STICKY
    }

    val notification = buildNotification()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ServiceCompat.startForeground(
        this,
        NOTIFICATION_ID,
        notification,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else {
          0
        },
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }

    VoiceEngineManager.setOverlayActive(true)
    if (Settings.canDrawOverlays(this)) {
      if (bubbleView == null) {
        showFloatingBubble()
      }
    }

    serviceScope.launch {
      VoiceEngineManager.state.collect { state ->
        updateOverlayUi(state.activePreset, state.isLiveModulationActive)
      }
    }

    return START_STICKY
  }

  override fun onDestroy() {
    serviceScope.cancel()
    VoiceEngineManager.setOverlayActive(false)
    removeOverlayViews()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
        NotificationChannel(
            CHANNEL_ID,
            "VoxForge Voice DSP & HUD",
            NotificationManager.IMPORTANCE_LOW,
          )
          .apply { description = "Keeps real-time voice changer & floating HUD active." }
      val nm = getSystemService(NotificationManager::class.java)
      nm.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(): Notification {
    val openIntent =
      PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val stopIntent =
      PendingIntent.getService(
        this,
        1,
        Intent(this, VoiceOverlayService::class.java).apply { action = ACTION_STOP_ALL },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val activePreset = VoiceEngineManager.state.value.activePreset.displayName
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("VoxForge HUD Active")
      .setContentText("Preset: $activePreset • Tap to open Studio")
      .setContentIntent(openIntent)
      .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop HUD", stopIntent)
      .setOngoing(true)
      .build()
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun showFloatingBubble() {
    val wm = windowManager ?: return
    val params =
      WindowManager.LayoutParams(
          dp(54),
          dp(54),
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
          } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
          },
          WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
          PixelFormat.TRANSLUCENT,
        )
        .apply {
          gravity = Gravity.TOP or Gravity.START
          x = dp(18)
          y = dp(160)
        }

    val bubble =
      TextView(this).apply {
        text = "VF\nv1.4"
        setTextColor(AndroidColor.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        background =
          GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(AndroidColor.parseColor("#E6141824"))
            setStroke(dp(2), AndroidColor.parseColor("#FFE61E32"))
          }
      }

    var initialX = 0
    var initialY = 0
    var initialTouchX = 0f
    var initialTouchY = 0f
    var moved = false

    bubble.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          initialX = params.x
          initialY = params.y
          initialTouchX = event.rawX
          initialTouchY = event.rawY
          moved = false
          true
        }
        MotionEvent.ACTION_MOVE -> {
          val dx = (event.rawX - initialTouchX).toInt()
          val dy = (event.rawY - initialTouchY).toInt()
          if (kotlin.math.abs(dx) > 6 || kotlin.math.abs(dy) > 6) {
            moved = true
          }
          params.x = initialX + dx
          params.y = initialY + dy
          wm.updateViewLayout(bubble, params)
          true
        }
        MotionEvent.ACTION_UP -> {
          if (!moved) {
            toggleExpandedPanel(params.x, params.y)
          }
          true
        }
        else -> false
      }
    }

    bubbleView = bubble
    wm.addView(bubble, params)
  }

  private fun toggleExpandedPanel(anchorX: Int, anchorY: Int) {
    val wm = windowManager ?: return
    if (isPanelExpanded) {
      panelView?.let { runCatching { wm.removeView(it) } }
      panelView = null
      isPanelExpanded = false
    } else {
      showFloatingPanel(anchorX, anchorY)
      isPanelExpanded = true
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun showFloatingPanel(anchorX: Int, anchorY: Int) {
    val wm = windowManager ?: return
    val params =
      WindowManager.LayoutParams(
          dp(250),
          WindowManager.LayoutParams.WRAP_CONTENT,
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
          } else {
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
          },
          WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
          PixelFormat.TRANSLUCENT,
        )
        .apply {
          gravity = Gravity.TOP or Gravity.START
          x = (anchorX + dp(62)).coerceAtLeast(dp(12))
          y = anchorY.coerceAtLeast(dp(60))
        }

    val container =
      LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(10), dp(12), dp(12))
        background =
          GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(AndroidColor.parseColor("#EE12151F"))
            setStroke(dp(2), AndroidColor.parseColor("#FFE61E32"))
          }
      }

    // Draggable HUD header
    val headerRow =
      LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, 0, 0, dp(8))
      }

    val title =
      TextView(this).apply {
        text = "VOXFORGE VIP v1.4 | DSP"
        setTextColor(AndroidColor.parseColor("#FF00D2FF"))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        typeface = Typeface.DEFAULT_BOLD
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
      }

    val closeBtn =
      TextView(this).apply {
        text = "✕"
        setTextColor(AndroidColor.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        setPadding(dp(8), dp(2), dp(4), dp(2))
        setOnClickListener { toggleExpandedPanel(params.x, params.y) }
      }

    headerRow.addView(title)
    headerRow.addView(closeBtn)

    var initX = 0
    var initY = 0
    var touchX = 0f
    var touchY = 0f
    headerRow.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          initX = params.x
          initY = params.y
          touchX = event.rawX
          touchY = event.rawY
          true
        }
        MotionEvent.ACTION_MOVE -> {
          params.x = initX + (event.rawX - touchX).toInt()
          params.y = initY + (event.rawY - touchY).toInt()
          wm.updateViewLayout(container, params)
          true
        }
        else -> false
      }
    }
    container.addView(headerRow)

    // Live DSP Toggle pill
    val liveBtn =
      TextView(this).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setPadding(dp(10), dp(8), dp(10), dp(8))
        setOnClickListener { VoiceEngineManager.toggleLiveModulation(this@VoiceOverlayService) }
      }
    liveToggleBtn = liveBtn
    container.addView(
      liveBtn,
      LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        .apply { bottomMargin = dp(8) },
    )

    val statusText =
      TextView(this).apply {
        setTextColor(AndroidColor.parseColor("#FF9BA4B8"))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setPadding(0, 0, 0, dp(6))
      }
    headerStatusView = statusText
    container.addView(statusText)

    // Scrollable Presets list
    val scroll = ScrollView(this)
    val presetList =
      LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    presetRows.clear()

    VoicePreset.entries.forEach { preset ->
      val row =
        TextView(this).apply {
          text = "${preset.displayName}  [${preset.shortTag}]"
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
          setPadding(dp(10), dp(8), dp(10), dp(8))
          setOnClickListener { VoiceEngineManager.selectPreset(preset) }
        }
      presetRows[preset] = row
      presetList.addView(
        row,
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
          )
          .apply { bottomMargin = dp(5) },
      )
    }

    scroll.addView(presetList)
    container.addView(
      scroll,
      LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(220)),
    )

    panelView = container
    wm.addView(container, params)
    val state = VoiceEngineManager.state.value
    updateOverlayUi(state.activePreset, state.isLiveModulationActive)
  }

  private fun updateOverlayUi(activePreset: VoicePreset, isLive: Boolean) {
    liveToggleBtn?.apply {
      text = if (isLive) "LIVE VOICE DSP: ON" else "LIVE VOICE DSP: OFF"
      setTextColor(AndroidColor.WHITE)
      background =
        GradientDrawable().apply {
          cornerRadius = dp(10).toFloat()
          setColor(
            if (isLive) AndroidColor.parseColor("#FF10B981")
            else AndroidColor.parseColor("#FFE61E32")
          )
        }
    }

    headerStatusView?.text = "Active Voice: ${activePreset.displayName} (${activePreset.shortTag})"

    presetRows.forEach { (preset, view) ->
      val selected = preset == activePreset
      view.setTextColor(
        if (selected) AndroidColor.WHITE else AndroidColor.parseColor("#FFD1D5DB")
      )
      view.background =
        GradientDrawable().apply {
          cornerRadius = dp(8).toFloat()
          setColor(
            if (selected) AndroidColor.parseColor("#FF4A0B15")
            else AndroidColor.parseColor("#FF191D2A")
          )
          setStroke(
            dp(1),
            if (selected) AndroidColor.parseColor("#FFFF384C")
            else AndroidColor.parseColor("#FF2B3245"),
          )
        }
    }
  }

  private fun removeOverlayViews() {
    val wm = windowManager ?: return
    bubbleView?.let { runCatching { wm.removeView(it) } }
    panelView?.let { runCatching { wm.removeView(it) } }
    bubbleView = null
    panelView = null
    isPanelExpanded = false
    presetRows.clear()
  }

  private fun dp(value: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        resources.displayMetrics,
      )
      .toInt()
  }
}
