package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.audio.VoiceEngineState
import com.example.audio.VoicePreset
import com.example.audio.VoiceRecordingClip
import com.example.data.UserSessionPrefs
import com.example.ui.LaunchableAppInfo
import com.example.ui.PermissionSnapshot
import com.example.ui.components.LiveAudioWaveform
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioDashboardScreen(
  prefs: UserSessionPrefs,
  engineState: VoiceEngineState,
  permissions: PermissionSnapshot,
  launchableApps: List<LaunchableAppInfo>,
  onRequestMicPermission: () -> Unit,
  onRequestNotificationPermission: () -> Unit,
  onRequestOverlayPermission: () -> Unit,
  onToggleLiveVoice: () -> Unit,
  onToggleOverlayHud: () -> Unit,
  onSelectPreset: (VoicePreset) -> Unit,
  onStartRecording: () -> Unit,
  onStopRecording: () -> Unit,
  onPlayOrStopClip: (VoiceRecordingClip) -> Unit,
  onSelectTargetApp: (LaunchableAppInfo) -> Unit,
  onLaunchTargetWithHud: () -> Unit,
  onOpenSoundSettings: () -> Unit,
  onLogout: () -> Unit,
) {
  var showAppPicker by remember { mutableStateOf(false) }
  var showSetupGuide by remember { mutableStateOf(false) }

  Column(
    modifier =
      Modifier.fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // 1. Tactical Header Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.5.dp, CrimsonPrimary.copy(alpha = 0.65f)),
      ) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  listOf(CrimsonContainer.copy(alpha = 0.7f), ObsidianCard)
                )
              )
              .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier =
              Modifier.size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CrimsonPrimary),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.GraphicEq,
              contentDescription = "VoxForge Studio Emblem",
              tint = Color.White,
              modifier = Modifier.size(30.dp),
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "VOXFORGE HUD v1.4",
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary,
            )
            Text(
              text = "ID: ${prefs.accessKey.ifBlank { "STUDIO" }} • 44.1kHz PCM DSP",
              style = MaterialTheme.typography.labelMedium,
              color = CyberCyan,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = engineState.statusMessage,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          IconButton(
            onClick = onLogout,
            modifier = Modifier.size(48.dp).testTag("logout_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = "Lock Session",
              tint = TextSecondary,
            )
          }
        }
      }

      // 2. Master Controls + Live Waveform Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border =
          BorderStroke(
            1.5.dp,
            if (engineState.isLiveModulationActive) EmeraldActive else OutlineSubtle,
          ),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Box(
                modifier =
                  Modifier.size(10.dp)
                    .clip(CircleShape)
                    .background(
                      if (engineState.isLiveModulationActive) EmeraldActive else CrimsonPrimary
                    )
              )
              Text(
                text =
                  if (engineState.isLiveModulationActive)
                    stringResource(R.string.voice_changer_on)
                  else stringResource(R.string.voice_changer_off),
                style = MaterialTheme.typography.titleMedium,
                color =
                  if (engineState.isLiveModulationActive) EmeraldActive else TextPrimary,
              )
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = CyberCyanContainer,
            ) {
              Text(
                text = "${engineState.activePreset.displayName} • ${engineState.estimatedLatencyMs}ms",
                style = MaterialTheme.typography.labelMedium,
                color = CyberCyan,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              )
            }
          }

          LiveAudioWaveform(
            bars = engineState.waveformBars,
            isActive =
              engineState.isLiveModulationActive ||
                engineState.isRecording ||
                engineState.playingClipId != null,
            accentColor = engineState.activePreset.accentColor,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = onToggleLiveVoice,
              shape = RoundedCornerShape(14.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor =
                    if (engineState.isLiveModulationActive) EmeraldActive else CrimsonPrimary,
                  contentColor = if (engineState.isLiveModulationActive) Color.Black else Color.White,
                ),
              modifier = Modifier.weight(1f).height(50.dp).testTag("toggle_voice_changer_btn"),
            ) {
              Icon(
                imageVector = if (engineState.isLiveModulationActive) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text = if (engineState.isLiveModulationActive) "STOP DSP" else "START VOICE",
                style = MaterialTheme.typography.labelLarge,
              )
            }

            OutlinedButton(
              onClick = {
                if (!permissions.hasOverlayPermission) {
                  onRequestOverlayPermission()
                } else {
                  onToggleOverlayHud()
                }
              },
              shape = RoundedCornerShape(14.dp),
              border =
                BorderStroke(
                  1.5.dp,
                  if (engineState.isOverlayActive) CyberCyan else OutlineSubtle,
                ),
              colors =
                ButtonDefaults.outlinedButtonColors(
                  containerColor =
                    if (engineState.isOverlayActive) CyberCyanContainer else ObsidianCardElevated,
                  contentColor = if (engineState.isOverlayActive) CyberCyan else TextPrimary,
                ),
              modifier = Modifier.weight(1f).height(50.dp).testTag("toggle_floating_hud_btn"),
            ) {
              Icon(
                imageVector = Icons.Filled.Layers,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text =
                  if (engineState.isOverlayActive) "HUD [ON]"
                  else if (permissions.hasOverlayPermission) "HUD [OFF]"
                  else "GRANT HUD",
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }

          // Permission status chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            PermissionChip(
              label = if (permissions.hasMicPermission) "Mic Ready" else "Enable Mic",
              granted = permissions.hasMicPermission,
              icon = Icons.Filled.Mic,
              onClick = onRequestMicPermission,
              tag = "perm_mic_chip",
            )
            PermissionChip(
              label = if (permissions.hasOverlayPermission) "Overlay Ready" else "Enable Overlay",
              granted = permissions.hasOverlayPermission,
              icon = Icons.Filled.Layers,
              onClick = onRequestOverlayPermission,
              tag = "perm_overlay_chip",
            )
            PermissionChip(
              label =
                if (permissions.hasNotificationPermission) "Alerts Ready" else "Enable Alerts",
              granted = permissions.hasNotificationPermission,
              icon = Icons.Filled.Notifications,
              onClick = onRequestNotificationPermission,
              tag = "perm_notif_chip",
            )
          }
        }
      }

      // 3. Voice Presets Selector Grid
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.dp, OutlineSubtle),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "VOICE PRESETS",
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary,
            )
            Text(
              text = "${VoicePreset.entries.size} DSP Profiles",
              style = MaterialTheme.typography.labelMedium,
              color = TextSecondary,
            )
          }

          val rows = VoicePreset.entries.chunked(2)
          rows.forEach { pair ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              pair.forEach { preset ->
                PresetTile(
                  preset = preset,
                  isSelected = engineState.activePreset == preset,
                  onClick = { onSelectPreset(preset) },
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }
        }
      }

      // 4. Quick Voice Studio Recorder & Instant Audition
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.dp, OutlineSubtle),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "QUICK VOICE TESTER",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
              )
              Text(
                text = "Record a short clip and preview any voice preset cleanly.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
              )
            }
            if (engineState.isRecording) {
              Surface(shape = RoundedCornerShape(8.dp), color = CrimsonContainer) {
                Text(
                  text = "REC ${engineState.recordingSeconds}s",
                  style = MaterialTheme.typography.labelMedium,
                  color = CrimsonPrimary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = { if (engineState.isRecording) onStopRecording() else onStartRecording() },
              shape = RoundedCornerShape(12.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor =
                    if (engineState.isRecording) AmberWarn else ObsidianCardElevated,
                  contentColor = if (engineState.isRecording) Color.Black else TextPrimary,
                ),
              modifier = Modifier.weight(1f).height(48.dp).testTag("quick_record_btn"),
            ) {
              Icon(
                imageVector =
                  if (engineState.isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                contentDescription = null,
                tint = if (engineState.isRecording) Color.Black else CrimsonPrimary,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text = if (engineState.isRecording) "STOP REC" else "RECORD CLIP",
                style = MaterialTheme.typography.labelLarge,
              )
            }

            val latestClip = engineState.recordedClips.firstOrNull()
            val isPlayingLatest =
              latestClip != null && engineState.playingClipId == latestClip.id
            Button(
              onClick = { latestClip?.let(onPlayOrStopClip) },
              enabled = latestClip != null && !engineState.isRecording,
              shape = RoundedCornerShape(12.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor =
                    if (isPlayingLatest) CyberCyan else CrimsonPrimary,
                  contentColor = if (isPlayingLatest) Color.Black else Color.White,
                ),
              modifier = Modifier.weight(1f).height(48.dp).testTag("quick_play_btn"),
            ) {
              Icon(
                imageVector = if (isPlayingLatest) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text = if (isPlayingLatest) "STOP PLAY" else "PLAY PRESET",
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }
        }
      }

      // 5. Target Game / App Launcher Card (mirrors video's [OPEN] action card)
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.dp, OutlineSubtle),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier =
                Modifier.size(46.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(ObsidianCardElevated),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = "Target Game Launcher",
                tint = CyberCyan,
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "TARGET APP LAUNCHER",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
              )
              Text(
                text =
                  if (prefs.targetAppLabel.isNotBlank()) "Selected: ${prefs.targetAppLabel}"
                  else "Select any installed game or chat app",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = { showAppPicker = true },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, OutlineSubtle),
              modifier = Modifier.weight(1f).height(48.dp).testTag("pick_target_app_btn"),
            ) {
              Icon(Icons.Filled.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.size(6.dp))
              Text("PICK APP", style = MaterialTheme.typography.labelLarge)
            }

            Button(
              onClick = {
                if (prefs.targetAppPackage.isBlank()) {
                  showAppPicker = true
                } else {
                  onLaunchTargetWithHud()
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = CrimsonPrimary,
                  contentColor = Color.White,
                ),
              modifier = Modifier.weight(1f).height(48.dp).testTag("launch_target_app_btn"),
            ) {
              Icon(
                Icons.Filled.RocketLaunch,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text("OPEN + HUD", style = MaterialTheme.typography.labelLarge)
            }
          }
        }
      }

      // 6. Support, Audio Setup Guide & Disclaimer
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.dp, OutlineSubtle),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "AUDIO ROUTING & SUPPORT",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = { showSetupGuide = true },
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
              modifier = Modifier.weight(1f).height(48.dp).testTag("setup_guide_btn"),
            ) {
              Icon(
                Icons.Filled.HelpOutline,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text("SETUP GUIDE", color = CyberCyan, style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
              onClick = onOpenSoundSettings,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, OutlineSubtle),
              modifier = Modifier.weight(1f).height(48.dp).testTag("sound_settings_btn"),
            ) {
              Icon(
                Icons.Filled.Tune,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text("AUDIO SYS", color = TextPrimary, style = MaterialTheme.typography.labelLarge)
            }
          }

          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CrimsonContainer.copy(alpha = 0.45f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.WarningAmber,
              contentDescription = null,
              tint = AmberWarn,
              modifier = Modifier.size(20.dp),
            )
            Text(
              text = stringResource(R.string.disclaimer_notice),
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
            )
          }
        }
      }
    }
  }

  if (showAppPicker) {
    TargetAppPickerDialog(
      apps = launchableApps,
      selectedPackage = prefs.targetAppPackage,
      onSelect = { app ->
        onSelectTargetApp(app)
        showAppPicker = false
      },
      onDismiss = { showAppPicker = false },
    )
  }

  if (showSetupGuide) {
    AlertDialog(
      onDismissRequest = { showSetupGuide = false },
      containerColor = ObsidianCard,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(Icons.Filled.Headset, contentDescription = null, tint = CyberCyan)
          Text("Game & Voice Setup Guide", color = TextPrimary)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          GuideRow("1. Headphone Routing", "Plug in wired/USB headset before enabling Live DSP to prevent speaker-to-mic echo loops.")
          GuideRow("2. In-Game Mic Mode", "Inside your game Sound Settings, set Voice Chat to 'User / Push-to-Talk' or 'Team Mic' for clean capture.")
          GuideRow("3. Floating Overlay", "Grant Display Over Other Apps to pin the v1.4 bubble so you can switch presets mid-match.")
          GuideRow("4. Android Compatibility", "Standard Android apps cannot inject into another app's private HAL mic stream without system/root privileges; live headset monitoring & clip studio work on all stock Android devices.")
        }
      },
      confirmButton = {
        TextButton(onClick = { showSetupGuide = false }) {
          Text("GOT IT", color = CyberCyan)
        }
      },
    )
  }
}

@Composable
private fun GuideRow(title: String, detail: String) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = CrimsonPrimary)
    Text(detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
  }
}

@Composable
private fun PermissionChip(
  label: String,
  granted: Boolean,
  icon: ImageVector,
  onClick: () -> Unit,
  tag: String,
) {
  FilterChip(
    selected = granted,
    onClick = onClick,
    leadingIcon = {
      Icon(
        imageVector = if (granted) Icons.Filled.CheckCircle else icon,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    },
    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
    colors =
      FilterChipDefaults.filterChipColors(
        selectedContainerColor = EmeraldContainer,
        selectedLabelColor = EmeraldActive,
        selectedLeadingIconColor = EmeraldActive,
        containerColor = ObsidianCardElevated,
        labelColor = AmberWarn,
        iconColor = AmberWarn,
      ),
    modifier = Modifier.testTag(tag),
  )
}

@Composable
private fun PresetTile(
  preset: VoicePreset,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    onClick = onClick,
    modifier = modifier.heightIn(min = 78.dp).testTag("preset_tile_${preset.id}"),
    shape = RoundedCornerShape(14.dp),
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (isSelected) preset.accentColor.copy(alpha = 0.16f) else ObsidianCardElevated
      ),
    border =
      BorderStroke(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) preset.accentColor else OutlineSubtle,
      ),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Icon(
          imageVector = Icons.Filled.SettingsVoice,
          contentDescription = null,
          tint = preset.accentColor,
          modifier = Modifier.size(20.dp),
        )
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = preset.accentColor.copy(alpha = 0.18f),
        ) {
          Text(
            text = preset.shortTag,
            style = MaterialTheme.typography.labelSmall,
            color = preset.accentColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          )
        }
      }
      Text(
        text = preset.displayName,
        style = MaterialTheme.typography.titleSmall,
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = preset.subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = TextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun TargetAppPickerDialog(
  apps: List<LaunchableAppInfo>,
  selectedPackage: String,
  onSelect: (LaunchableAppInfo) -> Unit,
  onDismiss: () -> Unit,
) {
  var query by remember { mutableStateOf("") }
  val filtered =
    remember(apps, query) {
      if (query.isBlank()) apps
      else apps.filter { it.label.contains(query, ignoreCase = true) }
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = ObsidianCard,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text("Select Target App", color = TextPrimary)
        IconButton(onClick = onDismiss) {
          Icon(Icons.Filled.Close, contentDescription = "Close picker", tint = TextSecondary)
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text("Search installed apps...") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        if (filtered.isEmpty()) {
          Text(
            text = "No matching apps found on device.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(vertical = 12.dp),
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(filtered, key = { it.packageName }) { app ->
              val isCurrent = app.packageName == selectedPackage
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isCurrent) CrimsonContainer else ObsidianCardElevated)
                    .clickable { onSelect(app) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                if (isCurrent) {
                  Icon(Icons.Filled.Shield, contentDescription = "Selected", tint = CrimsonPrimary)
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("CLOSE", color = TextSecondary) }
    },
  )
}
