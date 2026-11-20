package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.audio.VoiceEngineState
import com.example.ui.PermissionSnapshot
import com.example.ui.theme.AmberWarn
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

private val DEBUG_APK_WORKFLOW_SNIPPET =
  """
  name: Build Debug APK
  on: [push, pull_request, workflow_dispatch]
  jobs:
    build-debug-apk:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            distribution: temurin
            java-version: "21"
        - uses: android-actions/setup-android@v3
          with:
            packages: platform-tools platforms;android-36 build-tools;36.0.0
        - uses: gradle/actions/setup-gradle@v4
          with:
            gradle-version: "9.3.1"
        - run: gradle :app:testDebugUnitTest --no-daemon
        - run: gradle :app:assembleDebug --no-daemon
        - uses: actions/upload-artifact@v4
          with:
            name: voxforge-debug-apk
            path: app/build/outputs/apk/debug/*.apk
  """.trimIndent()

private val CI_VERIFY_WORKFLOW_SNIPPET =
  """
  name: Android CI Verification
  on:
    push:
      branches: [main, master, develop]
    pull_request:
      branches: [main, master]
  jobs:
    verify:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            distribution: temurin
            java-version: "21"
        - uses: android-actions/setup-android@v3
          with:
            packages: platform-tools platforms;android-36 build-tools;36.0.0
        - uses: gradle/actions/setup-gradle@v4
          with:
            gradle-version: "9.3.1"
        - run: gradle :app:testDebugUnitTest --no-daemon
        - run: gradle :app:lintDebug --no-daemon
  """.trimIndent()

@Composable
fun SystemsCiScreen(
  engineState: VoiceEngineState,
  permissions: PermissionSnapshot,
) {
  val context = LocalContext.current
  var selectedWorkflowIndex by remember { mutableIntStateOf(0) }

  val activeYaml =
    if (selectedWorkflowIndex == 0) DEBUG_APK_WORKFLOW_SNIPPET else CI_VERIFY_WORKFLOW_SNIPPET
  val activeFilePath =
    if (selectedWorkflowIndex == 0) ".github/workflows/build-debug-apk.yml"
    else ".github/workflows/android-ci.yml"

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
      // 1. Audio & Android Runtime Diagnostics
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(Icons.Filled.Memory, contentDescription = null, tint = CyberCyan)
            Text(
              text = "SYSTEM DIAGNOSTICS",
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary,
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            MetricCard(
              title = "Sample Rate",
              value = "${engineState.sampleRate} Hz",
              accent = CyberCyan,
              modifier = Modifier.weight(1f),
            )
            MetricCard(
              title = "Buffer Frame",
              value = "${engineState.bufferSizeFrames} smp",
              accent = EmeraldActive,
              modifier = Modifier.weight(1f),
            )
            MetricCard(
              title = "DSP Latency",
              value = "~${engineState.estimatedLatencyMs} ms",
              accent = CrimsonPrimary,
              modifier = Modifier.weight(1f),
            )
          }

          SystemStatusRow("Application ID", BuildConfig.APPLICATION_ID, CyberCyan)
          SystemStatusRow("Build Type", "${BuildConfig.BUILD_TYPE.uppercase()} v${BuildConfig.VERSION_NAME}", TextPrimary)
          SystemStatusRow("Android API", "SDK ${Build.VERSION.SDK_INT} (${Build.MODEL})", TextSecondary)
          SystemStatusRow(
            "Foreground Mic Service",
            if (engineState.isOverlayActive) "RUNNING (MICROPHONE)" else "IDLE",
            if (engineState.isOverlayActive) EmeraldActive else TextSecondary,
          )
          SystemStatusRow(
            "Permissions Matrix",
            "Mic=${permissions.hasMicPermission} • Overlay=${permissions.hasOverlayPermission}",
            if (permissions.hasMicPermission && permissions.hasOverlayPermission) EmeraldActive
            else AmberWarn,
          )
        }
      }

      // 2. GitHub Debug APK & CI/CD Workflows Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.5.dp, CrimsonPrimary.copy(alpha = 0.65f)),
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
              Icon(Icons.Filled.Build, contentDescription = null, tint = CrimsonPrimary)
              Text(
                text = "GITHUB DEBUG APK CI/CD",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
              )
            }
            Surface(shape = RoundedCornerShape(8.dp), color = CyberCyanContainer) {
              Text(
                text = "CONFIGURED",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }

          Text(
            text = "Production GitHub Actions pipelines are pre-bundled in your repository to test, lint, sign with debug.keystore, and upload app-debug.apk artifacts.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = selectedWorkflowIndex == 0,
              onClick = { selectedWorkflowIndex = 0 },
              leadingIcon = {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              label = { Text("build-debug-apk.yml") },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = CrimsonContainer,
                  selectedLabelColor = Color.White,
                  selectedLeadingIconColor = CrimsonPrimary,
                ),
              modifier = Modifier.testTag("workflow_tab_debug_apk"),
            )
            FilterChip(
              selected = selectedWorkflowIndex == 1,
              onClick = { selectedWorkflowIndex = 1 },
              leadingIcon = {
                Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              label = { Text("android-ci.yml") },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = CrimsonContainer,
                  selectedLabelColor = Color.White,
                  selectedLeadingIconColor = CrimsonPrimary,
                ),
              modifier = Modifier.testTag("workflow_tab_android_ci"),
            )
          }

          Text(
            text = activeFilePath,
            style = MaterialTheme.typography.labelMedium,
            color = CyberCyan,
          )

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = ObsidianDeep,
            border = BorderStroke(1.dp, OutlineSubtle),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = activeYaml,
              style = MaterialTheme.typography.labelMedium,
              color = TextPrimary,
              modifier =
                Modifier.fillMaxWidth()
                  .horizontalScroll(rememberScrollState())
                  .padding(12.dp),
            )
          }

          Button(
            onClick = {
              copyToClipboard(context, activeFilePath, activeYaml)
              Toast.makeText(context, "Copied $activeFilePath", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = CrimsonPrimary,
                contentColor = Color.White,
              ),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("copy_workflow_btn"),
          ) {
            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("COPY WORKFLOW YAML", style = MaterialTheme.typography.labelLarge)
          }
        }
      }

      // 3. Gradle Build & Test CLI Reference
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
        border = BorderStroke(1.dp, OutlineSubtle),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(Icons.Filled.Terminal, contentDescription = null, tint = EmeraldActive)
            Text(
              text = "GRADLE BUILD COMMANDS",
              style = MaterialTheme.typography.titleMedium,
              color = TextPrimary,
            )
          }
          CommandChip("gradle :app:assembleDebug", "Build signed Debug APK")
          CommandChip("gradle :app:testDebugUnitTest", "Run Robolectric & DSP unit tests")
          CommandChip("gradle :app:lintDebug", "Run Android Lint verification")
        }
      }
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  accent: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(ObsidianCardElevated)
        .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    Text(value, style = MaterialTheme.typography.titleSmall, color = accent)
  }
}

@Composable
private fun SystemStatusRow(label: String, value: String, valueColor: Color) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(ObsidianCardElevated)
        .padding(horizontal = 12.dp, vertical = 9.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    Text(value, style = MaterialTheme.typography.labelMedium, color = valueColor)
  }
}

@Composable
private fun CommandChip(cmd: String, desc: String) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(ObsidianDeep)
        .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Text(cmd, style = MaterialTheme.typography.labelMedium, color = EmeraldActive)
    Text(desc, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
  }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
