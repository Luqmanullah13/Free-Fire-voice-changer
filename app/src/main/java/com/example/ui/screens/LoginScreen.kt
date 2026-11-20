package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.UserSessionPrefs
import com.example.ui.components.CrimsonConstellationBackground
import com.example.ui.theme.CrimsonBright
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.GateCardLight
import com.example.ui.theme.GateFieldBg
import com.example.ui.theme.GateTextDark

@Composable
fun LoginScreen(
  prefs: UserSessionPrefs,
  isVerifying: Boolean,
  loginError: String?,
  onLoginClick: (accessKey: String, passcode: String, saveLogin: Boolean) -> Unit,
) {
  var accessKey by rememberSaveable(prefs.accessKey) { mutableStateOf(prefs.accessKey) }
  var passcode by rememberSaveable(prefs.passcode) { mutableStateOf(prefs.passcode) }
  var saveLogin by rememberSaveable(prefs.saveLogin) { mutableStateOf(prefs.saveLogin) }
  var showPassword by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxSize()) {
    CrimsonConstellationBackground()

    Column(
      modifier =
        Modifier.fillMaxSize()
          .systemBarsPadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = GateCardLight),
        border = BorderStroke(2.5.dp, CrimsonPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.GraphicEq,
              contentDescription = null,
              tint = CrimsonPrimary,
              modifier = Modifier.size(26.dp),
            )
            Text(
              text = stringResource(R.string.login_title),
              style = MaterialTheme.typography.headlineMedium,
              color = CrimsonPrimary,
              fontWeight = FontWeight.Bold,
            )
            Icon(
              imageVector = Icons.Filled.GraphicEq,
              contentDescription = null,
              tint = CrimsonPrimary,
              modifier = Modifier.size(26.dp),
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = GateTextDark.copy(alpha = 0.68f),
            textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(18.dp))

          OutlinedTextField(
            value = accessKey,
            onValueChange = { accessKey = it },
            label = { Text(stringResource(R.string.login_key_label)) },
            leadingIcon = {
              Icon(Icons.Filled.Key, contentDescription = null, tint = CrimsonPrimary)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedTextColor = GateTextDark,
                unfocusedTextColor = GateTextDark,
                focusedContainerColor = GateFieldBg,
                unfocusedContainerColor = GateFieldBg,
                focusedBorderColor = CrimsonPrimary,
                unfocusedBorderColor = Color(0xFFD2D8E6),
                focusedLabelColor = CrimsonPrimary,
                unfocusedLabelColor = GateTextDark.copy(alpha = 0.65f),
              ),
            modifier = Modifier.fillMaxWidth().testTag("login_key_input"),
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = passcode,
            onValueChange = { passcode = it },
            label = { Text(stringResource(R.string.login_pass_label)) },
            leadingIcon = {
              Icon(Icons.Filled.Lock, contentDescription = null, tint = CrimsonPrimary)
            },
            trailingIcon = {
              IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                  imageVector =
                    if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                  contentDescription = if (showPassword) "Hide passcode" else "Show passcode",
                  tint = GateTextDark.copy(alpha = 0.7f),
                )
              }
            },
            visualTransformation =
              if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedTextColor = GateTextDark,
                unfocusedTextColor = GateTextDark,
                focusedContainerColor = GateFieldBg,
                unfocusedContainerColor = GateFieldBg,
                focusedBorderColor = CrimsonPrimary,
                unfocusedBorderColor = Color(0xFFD2D8E6),
                focusedLabelColor = CrimsonPrimary,
                unfocusedLabelColor = GateTextDark.copy(alpha = 0.65f),
              ),
            modifier = Modifier.fillMaxWidth().testTag("login_passcode_input"),
          )

          if (!loginError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = loginError,
              style = MaterialTheme.typography.bodyMedium,
              color = CrimsonBright,
              textAlign = TextAlign.Center,
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = stringResource(R.string.login_save_label),
                style = MaterialTheme.typography.titleMedium,
                color = GateTextDark,
              )
              Switch(
                checked = saveLogin,
                onCheckedChange = { saveLogin = it },
                colors =
                  SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = CrimsonPrimary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFBDC5D6),
                  ),
                modifier = Modifier.testTag("save_login_switch"),
              )
            }

            Button(
              onClick = { onLoginClick(accessKey, passcode, saveLogin) },
              enabled = !isVerifying,
              shape = RoundedCornerShape(12.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = CrimsonPrimary,
                  contentColor = Color.White,
                ),
              modifier = Modifier.height(48.dp).testTag("login_submit_button"),
            ) {
              Text(
                text = stringResource(R.string.login_button),
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          AssistChip(
            onClick = {
              accessKey = "12345"
              passcode = "12345"
              saveLogin = true
            },
            label = { Text("Fill Demo Key (12345)") },
            colors =
              AssistChipDefaults.assistChipColors(
                containerColor = GateFieldBg,
                labelColor = GateTextDark,
              ),
            modifier = Modifier.testTag("demo_fill_chip"),
          )
        }
      }
    }

    if (isVerifying) {
      Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
      ) {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = GateCardLight),
          border = BorderStroke(2.dp, CrimsonPrimary),
          modifier = Modifier.padding(24.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            CircularProgressIndicator(color = CrimsonPrimary, modifier = Modifier.size(30.dp))
            Text(
              text = stringResource(R.string.login_verifying),
              style = MaterialTheme.typography.titleMedium,
              color = GateTextDark,
            )
          }
        }
      }
    }
  }
}
