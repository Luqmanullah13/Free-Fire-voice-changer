package com.example.ui

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceEngineManager
import com.example.audio.VoiceEngineState
import com.example.audio.VoicePreset
import com.example.audio.VoiceRecordingClip
import com.example.data.PreferencesRepository
import com.example.data.UserSessionPrefs
import com.example.service.VoiceOverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LaunchableAppInfo(
  val packageName: String,
  val label: String,
)

data class PermissionSnapshot(
  val hasMicPermission: Boolean = false,
  val hasOverlayPermission: Boolean = false,
  val hasNotificationPermission: Boolean = true,
)

enum class DashboardTab(val label: String) {
  STUDIO("Studio"),
  MIXER("HUD & Mixer"),
  SYSTEMS("CI & Systems"),
}

class VoxForgeViewModel(application: Application) : AndroidViewModel(application) {

  private val prefsRepo = PreferencesRepository(application)

  val sessionPrefs: StateFlow<UserSessionPrefs> =
    prefsRepo.userSessionFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UserSessionPrefs(),
    )

  val engineState: StateFlow<VoiceEngineState> = VoiceEngineManager.state

  private val _permissions = MutableStateFlow(PermissionSnapshot())
  val permissions: StateFlow<PermissionSnapshot> = _permissions.asStateFlow()

  private val _isVerifyingLogin = MutableStateFlow(false)
  val isVerifyingLogin: StateFlow<Boolean> = _isVerifyingLogin.asStateFlow()

  private val _loginError = MutableStateFlow<String?>(null)
  val loginError: StateFlow<String?> = _loginError.asStateFlow()

  private val _selectedTab = MutableStateFlow(DashboardTab.STUDIO)
  val selectedTab: StateFlow<DashboardTab> = _selectedTab.asStateFlow()

  private val _launchableApps = MutableStateFlow<List<LaunchableAppInfo>>(emptyList())
  val launchableApps: StateFlow<List<LaunchableAppInfo>> = _launchableApps.asStateFlow()

  init {
    VoiceEngineManager.initClipsDirectory(application)
    refreshPermissions()
    loadLaunchableApps()
    viewModelScope.launch {
      prefsRepo.userSessionFlow.collect { prefs ->
        val preset = VoicePreset.fromId(prefs.selectedPresetId)
        if (VoiceEngineManager.state.value.activePreset != preset &&
            !VoiceEngineManager.state.value.isLiveModulationActive
        ) {
          VoiceEngineManager.selectPreset(preset)
        }
      }
    }
  }

  fun selectTab(tab: DashboardTab) {
    _selectedTab.value = tab
  }

  fun refreshPermissions() {
    val ctx = getApplication<Application>()
    val mic =
      ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
    val overlay = Settings.canDrawOverlays(ctx)
    val notif =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    _permissions.value =
      PermissionSnapshot(
        hasMicPermission = mic,
        hasOverlayPermission = overlay,
        hasNotificationPermission = notif,
      )
  }

  fun performLogin(accessKey: String, passcode: String, saveLogin: Boolean) {
    val trimmedKey = accessKey.trim()
    val trimmedPass = passcode.trim()
    if (trimmedKey.isEmpty() || trimmedPass.isEmpty()) {
      _loginError.value = "Enter an access key and passcode (or tap Demo Fill)."
      return
    }
    _loginError.value = null
    viewModelScope.launch {
      _isVerifyingLogin.value = true
      delay(650)
      prefsRepo.saveLoginSession(trimmedKey, trimmedPass, saveLogin)
      _isVerifyingLogin.value = false
    }
  }

  fun logout() {
    val ctx = getApplication<Application>()
    VoiceEngineManager.stopLiveModulation()
    VoiceEngineManager.stopPlayback()
    VoiceOverlayService.stopOverlay(ctx)
    viewModelScope.launch { prefsRepo.logout() }
  }

  fun selectPreset(preset: VoicePreset) {
    VoiceEngineManager.selectPreset(preset)
    viewModelScope.launch { prefsRepo.saveSelectedPreset(preset.id) }
  }

  fun toggleLiveVoice(): Boolean {
    val ctx = getApplication<Application>()
    refreshPermissions()
    val active = VoiceEngineManager.toggleLiveModulation(ctx)
    if (active) {
      VoiceOverlayService.startOverlay(ctx)
    }
    return active
  }

  fun toggleFloatingOverlay(): Boolean {
    val ctx = getApplication<Application>()
    refreshPermissions()
    if (!Settings.canDrawOverlays(ctx)) {
      return false
    }
    if (VoiceEngineManager.state.value.isOverlayActive) {
      VoiceOverlayService.stopOverlay(ctx)
    } else {
      VoiceOverlayService.startOverlay(ctx)
    }
    return true
  }

  fun openOverlayPermissionSettings() {
    val ctx = getApplication<Application>()
    val intent =
      Intent(
          Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:${ctx.packageName}"),
        )
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { ctx.startActivity(intent) }
  }

  fun startRecording() {
    VoiceEngineManager.startRecording(getApplication())
  }

  fun stopRecording() {
    VoiceEngineManager.stopRecording(getApplication())
  }

  fun playOrStopClip(clip: VoiceRecordingClip) {
    VoiceEngineManager.playClipWithLiveDsp(clip)
  }

  fun deleteClip(clip: VoiceRecordingClip) {
    VoiceEngineManager.deleteClip(getApplication(), clip)
  }

  fun updatePitch(value: Float) = VoiceEngineManager.updatePitch(value)

  fun updateGain(value: Float) = VoiceEngineManager.updateGain(value)

  fun updateEcho(value: Float) = VoiceEngineManager.updateEcho(value)

  fun updateRobotMix(value: Float) = VoiceEngineManager.updateRobotMix(value)

  fun resetPresetConfig() = VoiceEngineManager.resetCurrentPresetConfig()

  fun clearEngineError() = VoiceEngineManager.clearError()

  fun setTargetApp(app: LaunchableAppInfo) {
    viewModelScope.launch { prefsRepo.saveTargetApp(app.packageName, app.label) }
  }

  fun launchTargetAppWithHud(): Boolean {
    val ctx = getApplication<Application>()
    val pkg = sessionPrefs.value.targetAppPackage
    if (Settings.canDrawOverlays(ctx)) {
      VoiceOverlayService.startOverlay(ctx)
    }
    if (pkg.isBlank()) return false
    val launchIntent = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return false
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching {
        ctx.startActivity(launchIntent)
        true
      }
      .getOrDefault(false)
  }

  fun openDeviceSoundSettings() {
    val ctx = getApplication<Application>()
    val intent = Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { ctx.startActivity(intent) }
  }

  private fun loadLaunchableApps() {
    viewModelScope.launch {
      val apps =
        withContext(Dispatchers.IO) {
          val ctx = getApplication<Application>()
          val pm = ctx.packageManager
          val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
          val resolved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
              @Suppress("DEPRECATION") pm.queryIntentActivities(mainIntent, 0)
            }
          resolved
            .mapNotNull { info ->
              val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
              if (pkg == ctx.packageName) return@mapNotNull null
              val label = info.loadLabel(pm).toString()
              LaunchableAppInfo(packageName = pkg, label = label)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
        }
      _launchableApps.value = apps
    }
  }
}
