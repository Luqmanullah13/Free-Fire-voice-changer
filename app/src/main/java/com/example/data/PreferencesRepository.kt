package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voxforge_prefs")

data class UserSessionPrefs(
  val accessKey: String = "12345",
  val passcode: String = "12345",
  val saveLogin: Boolean = true,
  val isAuthenticated: Boolean = false,
  val selectedPresetId: String = "cute_girl",
  val targetAppPackage: String = "",
  val targetAppLabel: String = "",
  val showFpsBadge: Boolean = true,
)

class PreferencesRepository(private val context: Context) {

  private object Keys {
    val ACCESS_KEY = stringPreferencesKey("access_key")
    val PASSCODE = stringPreferencesKey("passcode")
    val SAVE_LOGIN = booleanPreferencesKey("save_login")
    val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
    val SELECTED_PRESET_ID = stringPreferencesKey("selected_preset_id")
    val TARGET_APP_PACKAGE = stringPreferencesKey("target_app_package")
    val TARGET_APP_LABEL = stringPreferencesKey("target_app_label")
    val SHOW_FPS_BADGE = booleanPreferencesKey("show_fps_badge")
  }

  val userSessionFlow: Flow<UserSessionPrefs> =
    context.dataStore.data.map { prefs ->
      UserSessionPrefs(
        accessKey = prefs[Keys.ACCESS_KEY] ?: "12345",
        passcode = prefs[Keys.PASSCODE] ?: "12345",
        saveLogin = prefs[Keys.SAVE_LOGIN] ?: true,
        isAuthenticated = prefs[Keys.IS_AUTHENTICATED] ?: false,
        selectedPresetId = prefs[Keys.SELECTED_PRESET_ID] ?: "cute_girl",
        targetAppPackage = prefs[Keys.TARGET_APP_PACKAGE] ?: "",
        targetAppLabel = prefs[Keys.TARGET_APP_LABEL] ?: "",
        showFpsBadge = prefs[Keys.SHOW_FPS_BADGE] ?: true,
      )
    }

  suspend fun saveLoginSession(accessKey: String, passcode: String, saveLogin: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[Keys.SAVE_LOGIN] = saveLogin
      prefs[Keys.IS_AUTHENTICATED] = true
      if (saveLogin) {
        prefs[Keys.ACCESS_KEY] = accessKey
        prefs[Keys.PASSCODE] = passcode
      } else {
        prefs.remove(Keys.ACCESS_KEY)
        prefs.remove(Keys.PASSCODE)
      }
    }
  }

  suspend fun logout() {
    context.dataStore.edit { prefs -> prefs[Keys.IS_AUTHENTICATED] = false }
  }

  suspend fun saveSelectedPreset(presetId: String) {
    context.dataStore.edit { prefs -> prefs[Keys.SELECTED_PRESET_ID] = presetId }
  }

  suspend fun saveTargetApp(packageName: String, label: String) {
    context.dataStore.edit { prefs ->
      prefs[Keys.TARGET_APP_PACKAGE] = packageName
      prefs[Keys.TARGET_APP_LABEL] = label
    }
  }

  suspend fun setShowFpsBadge(show: Boolean) {
    context.dataStore.edit { prefs -> prefs[Keys.SHOW_FPS_BADGE] = show }
  }
}
