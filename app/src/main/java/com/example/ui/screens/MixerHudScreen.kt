package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.audio.VoiceEngineState
import com.example.audio.VoicePreset
import com.example.audio.VoiceRecordingClip
import com.example.ui.theme.CrimsonContainer
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianCardElevated
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MixerHudScreen(
  engineState: VoiceEngineState,
  hasOverlayPermission: Boolean,
  onSelectPreset: (VoicePreset) -> Unit,
  onToggleLiveVoice: () -> Unit,
  onToggleSystemOverlay: () -> Unit,
  onRequestOverlayPermission: () -> Unit,
  onPitchChange: (Float) -> Unit,
  onGainChange: (Float) -> Unit,
  onEchoChange: (Float) -> Unit,
  onRobotChange: (Float) -> Unit,
  onResetDefaults: () -> Unit,
  onPlayOrStopClip: (VoiceRecordingClip) -> Unit,
  onDeleteClip: (VoiceRecordingClip) -> Unit,
) {
  var hudOffsetX by remember { mutableFloatStateOf(16f) }
  var hudOffsetY by remember { mutableFloatStateOf(16f) }

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
      // 1. Interactive In-App Floating HUD Preview Sandbox
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.45f)),
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
                text = "INTERACTIVE HUD PREVIEW",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
              )
              Text(
                text = "Drag the floating VIP v1.4 menu below or launch system overlay.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
              )
            }
            Button(
              onClick = {
                if (hasOverlayPermission) onToggleSystemOverlay()
                else onRequestOverlayPermission()
              },
              shape = RoundedCornerShape(10.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor =
                    if (engineState.isOverlayActive) CyberCyan else CrimsonPrimary,
                  contentColor = if (engineState.isOverlayActive) Color.Black else Color.White,
                ),
              modifier = Modifier.height(48.dp).testTag("mixer_launch_overlay_btn"),
            ) {
              Icon(Icons.Filled.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text =
                  if (engineState.isOverlayActive) "HIDE HUD"
                  else if (hasOverlayPermission) "SHOW HUD"
                  else "ALLOW HUD",
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }

          // Simulated Game Frame containing Draggable Floating HUD
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(ObsidianDeep)
                .padding(8.dp)
          ) {
            Text(
              text = "GAME VIEWPORT SANDBOX • DRAG HUD PANEL",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondary.copy(alpha = 0.45f),
              modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
            )

            // Draggable Floating Menu matching video's VIP overlay
            Card(
              modifier =
                Modifier.offset { IntOffset(hudOffsetX.roundToInt(), hudOffsetY.roundToInt()) }
                  .width(230.dp)
                  .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                      change.consume()
                      hudOffsetX = (hudOffsetX + dragAmount.x).coerceIn(0f, 360f)
                      hudOffsetY = (hudOffsetY + dragAmount.y).coerceIn(0f, 220f)
                    }
                  },
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xEB131722)),
              border = BorderStroke(1.5.dp, CrimsonPrimary),
              elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            ) {
              Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Icon(
                      Icons.Filled.DragIndicator,
                      contentDescription = "Drag HUD",
                      tint = CyberCyan,
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text = "VOXFORGE v1.4 | DSP",
                      style = MaterialTheme.typography.labelMedium,
                      color = CyberCyan,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                  Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp),
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    text = engineState.activePreset.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                  )
                  Switch(
                    checked = engineState.isLiveModulationActive,
                    onCheckedChange = { onToggleLiveVoice() },
                    colors =
                      SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldActive,
                      ),
                    modifier = Modifier.testTag("hud_preview_live_switch"),
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  listOf(VoicePreset.CUTE_GIRL, VoicePreset.GRANDPA, VoicePreset.MOTU).forEach { p ->
                    val active = engineState.activePreset == p
                    Surface(
                      onClick = { onSelectPreset(p) },
                      shape = RoundedCornerShape(8.dp),
                      color = if (active) CrimsonContainer else ObsidianCardElevated,
                      border = BorderStroke(1.dp, if (active) CrimsonPrimary else OutlineSubtle),
                      modifier = Modifier.weight(1f),
                    ) {
                      Text(
                        text = p.displayName.split(" ").first(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) Color.White else TextSecondary,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 2. Parametric DSP Mixer Card
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
            Text(
              text = "REAL-TIME DSP MIXER",
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary,
            )
            OutlinedButton(
              onClick = onResetDefaults,
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, OutlineSubtle),
              modifier = Modifier.height(48.dp).testTag("reset_dsp_btn"),
            ) {
              Icon(
                Icons.Filled.RestartAlt,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("RESET", style = MaterialTheme.typography.labelMedium)
            }
          }

          DspSliderRow(
            label = "Pitch Multiplier",
            valueLabel = String.format(Locale.US, "%.2fx", engineState.dspConfig.pitchFactor),
            value = engineState.dspConfig.pitchFactor,
            valueRange = 0.50f..2.00f,
            accent = CrimsonPrimary,
            onValueChange = onPitchChange,
            tag = "slider_pitch",
          )

          DspSliderRow(
            label = "Mic Output Gain",
            valueLabel = String.format(Locale.US, "%.2fx", engineState.dspConfig.gain),
            value = engineState.dspConfig.gain,
            valueRange = 0.50f..2.50f,
            accent = EmeraldActive,
            onValueChange = onGainChange,
            tag = "slider_gain",
          )

          DspSliderRow(
            label = "Echo Chamber Mix",
            valueLabel = "${(engineState.dspConfig.echoMix * 100).roundToInt()}%",
            value = engineState.dspConfig.echoMix,
            valueRange = 0f..0.75f,
            accent = CyberCyan,
            onValueChange = onEchoChange,
            tag = "slider_echo",
          )

          DspSliderRow(
            label = "Cyber Ring-Modulation",
            valueLabel = "${(engineState.dspConfig.robotMix * 100).roundToInt()}%",
            value = engineState.dspConfig.robotMix,
            valueRange = 0f..1.0f,
            accent = Color(0xFFA855F7),
            onValueChange = onRobotChange,
            tag = "slider_robot",
          )
        }
      }

      // 3. Saved Studio Recordings Library
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
            text = "STUDIO CLIPS (${engineState.recordedClips.size})",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
          )

          if (engineState.recordedClips.isEmpty()) {
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(ObsidianCardElevated)
                  .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = TextSecondary)
              Text(
                text = "No clips recorded yet. Record a sample in Studio to audition presets anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
              )
            }
          } else {
            engineState.recordedClips.forEach { clip ->
              val isPlaying = engineState.playingClipId == clip.id
              Row(
                modifier =
                  Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isPlaying) CyberCyanContainer else ObsidianCardElevated)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                IconButton(
                  onClick = { onPlayOrStopClip(clip) },
                  modifier = Modifier.size(48.dp).testTag("play_clip_${clip.id}"),
                ) {
                  Icon(
                    imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Stop playback" else "Play clip",
                    tint = if (isPlaying) CyberCyan else CrimsonPrimary,
                  )
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = clip.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = "${(clip.durationMs / 1000.0).let { String.format(Locale.US, "%.1fs", it) }} • ${clip.createdAtLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                  )
                }

                IconButton(
                  onClick = { onDeleteClip(clip) },
                  modifier = Modifier.size(48.dp).testTag("delete_clip_${clip.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete clip",
                    tint = TextSecondary,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DspSliderRow(
  label: String,
  valueLabel: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  accent: Color,
  onValueChange: (Float) -> Unit,
  tag: String,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
      Text(
        text = valueLabel,
        style = MaterialTheme.typography.labelMedium,
        color = accent,
        fontWeight = FontWeight.Bold,
      )
    }
    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = valueRange,
      colors =
        SliderDefaults.colors(
          thumbColor = accent,
          activeTrackColor = accent,
          inactiveTrackColor = OutlineSubtle,
        ),
      modifier = Modifier.fillMaxWidth().testTag(tag),
    )
  }
}
