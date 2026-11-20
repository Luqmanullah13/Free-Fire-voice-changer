package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DashboardTab
import com.example.ui.VoxForgeViewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MixerHudScreen
import com.example.ui.screens.StudioDashboardScreen
import com.example.ui.screens.SystemsCiScreen
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoxForgeTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { VoxForgeTheme { VoxForgeApp() } }
  }
}

@Composable
fun VoxForgeApp(viewModel: VoxForgeViewModel = viewModel()) {
  val context = LocalContext.current
  val sessionPrefs by viewModel.sessionPrefs.collectAsStateWithLifecycle()
  val engineState by viewModel.engineState.collectAsStateWithLifecycle()
  val permissions by viewModel.permissions.collectAsStateWithLifecycle()
  val isVerifyingLogin by viewModel.isVerifyingLogin.collectAsStateWithLifecycle()
  val loginError by viewModel.loginError.collectAsStateWithLifecycle()
  val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
  val launchableApps by viewModel.launchableApps.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  val micPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      viewModel.refreshPermissions()
      if (!granted) {
        Toast.makeText(
            context,
            context.getString(R.string.permission_mic_required),
            Toast.LENGTH_SHORT,
          )
          .show()
      }
    }

  val notificationPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      viewModel.refreshPermissions()
    }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }

  LaunchedEffect(engineState.errorMessage) {
    val msg = engineState.errorMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearEngineError()
    }
  }

  if (!sessionPrefs.isAuthenticated) {
    LoginScreen(
      prefs = sessionPrefs,
      isVerifying = isVerifyingLogin,
      loginError = loginError,
      onLoginClick = { key, pass, save -> viewModel.performLogin(key, pass, save) },
    )
    return
  }

  BackHandler(enabled = selectedTab != DashboardTab.STUDIO) {
    viewModel.selectTab(DashboardTab.STUDIO)
  }

  val requestMicAndToggleLive: () -> Unit = {
    if (!permissions.hasMicPermission) {
      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    } else {
      viewModel.toggleLiveVoice()
    }
  }

  val requestMicAndRecord: () -> Unit = {
    if (!permissions.hasMicPermission) {
      micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    } else {
      viewModel.startRecording()
    }
  }

  val requestNotificationPerm: () -> Unit = {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavRail = maxWidth >= 600.dp

    Scaffold(
      containerColor = ObsidianDeep,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(snackbarHostState) },
      bottomBar = {
        if (!useNavRail) {
          NavigationBar(containerColor = ObsidianSurface) {
            DashboardTab.entries.forEach { tab ->
              val selected = selectedTab == tab
              NavigationBarItem(
                selected = selected,
                onClick = { viewModel.selectTab(tab) },
                icon = {
                  Icon(
                    imageVector = tabIcon(tab, selected),
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = CrimsonPrimary,
                    indicatorColor = CrimsonContainer,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                  ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (useNavRail) {
          NavigationRail(containerColor = ObsidianSurface) {
            DashboardTab.entries.forEach { tab ->
              val selected = selectedTab == tab
              NavigationRailItem(
                selected = selected,
                onClick = { viewModel.selectTab(tab) },
                icon = {
                  Icon(
                    imageVector = tabIcon(tab, selected),
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                colors =
                  NavigationRailItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = CrimsonPrimary,
                    indicatorColor = CrimsonContainer,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                  ),
                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}"),
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
          when (selectedTab) {
            DashboardTab.STUDIO ->
              StudioDashboardScreen(
                prefs = sessionPrefs,
                engineState = engineState,
                permissions = permissions,
                launchableApps = launchableApps,
                onRequestMicPermission = {
                  micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onRequestNotificationPermission = requestNotificationPerm,
                onRequestOverlayPermission = { viewModel.openOverlayPermissionSettings() },
                onToggleLiveVoice = requestMicAndToggleLive,
                onToggleOverlayHud = {
                  if (!viewModel.toggleFloatingOverlay()) {
                    viewModel.openOverlayPermissionSettings()
                  }
                },
                onSelectPreset = { viewModel.selectPreset(it) },
                onStartRecording = requestMicAndRecord,
                onStopRecording = { viewModel.stopRecording() },
                onPlayOrStopClip = { viewModel.playOrStopClip(it) },
                onSelectTargetApp = { viewModel.setTargetApp(it) },
                onLaunchTargetWithHud = {
                  if (!viewModel.launchTargetAppWithHud()) {
                    Toast.makeText(
                        context,
                        "Select a target app first or check app availability.",
                        Toast.LENGTH_SHORT,
                      )
                      .show()
                  }
                },
                onOpenSoundSettings = { viewModel.openDeviceSoundSettings() },
                onLogout = { viewModel.logout() },
              )

            DashboardTab.MIXER ->
              MixerHudScreen(
                engineState = engineState,
                hasOverlayPermission = permissions.hasOverlayPermission,
                onSelectPreset = { viewModel.selectPreset(it) },
                onToggleLiveVoice = requestMicAndToggleLive,
                onToggleSystemOverlay = { viewModel.toggleFloatingOverlay() },
                onRequestOverlayPermission = { viewModel.openOverlayPermissionSettings() },
                onPitchChange = { viewModel.updatePitch(it) },
                onGainChange = { viewModel.updateGain(it) },
                onEchoChange = { viewModel.updateEcho(it) },
                onRobotChange = { viewModel.updateRobotMix(it) },
                onResetDefaults = { viewModel.resetPresetConfig() },
                onPlayOrStopClip = { viewModel.playOrStopClip(it) },
                onDeleteClip = { viewModel.deleteClip(it) },
              )

            DashboardTab.SYSTEMS ->
              SystemsCiScreen(
                engineState = engineState,
                permissions = permissions,
              )
          }
        }
      }
    }
  }
}

private fun tabIcon(tab: DashboardTab, selected: Boolean) =
  when (tab) {
    DashboardTab.STUDIO -> if (selected) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq
    DashboardTab.MIXER -> if (selected) Icons.Filled.Tune else Icons.Outlined.Tune
    DashboardTab.SYSTEMS -> if (selected) Icons.Filled.Build else Icons.Outlined.Build
  }
