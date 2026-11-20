package com.example.audio

import androidx.compose.ui.graphics.Color

enum class ToneShape {
  NEUTRAL,
  BRIGHT_AIRY,
  CRISP_HIGH,
  WARM_TREMOLO,
  DEEP_RESONANT,
  HEAVY_BASS,
  METALLIC_SYNTH,
  ETHEREAL_ECHO,
}

data class VoiceDspConfig(
  val pitchFactor: Float = 1.0f,
  val gain: Float = 1.15f,
  val echoMix: Float = 0.0f,
  val robotMix: Float = 0.0f,
  val robotFreqHz: Float = 45f,
  val tremoloDepth: Float = 0.0f,
  val toneShape: ToneShape = ToneShape.NEUTRAL,
)

enum class VoicePreset(
  val id: String,
  val displayName: String,
  val shortTag: String,
  val subtitle: String,
  val accentColor: Color,
  val defaultConfig: VoiceDspConfig,
) {
  NORMAL(
    id = "normal",
    displayName = "Normal",
    shortTag = "BYPASS",
    subtitle = "Natural studio mic",
    accentColor = Color(0xFF9BA4B8),
    defaultConfig = VoiceDspConfig(pitchFactor = 1.0f, gain = 1.05f, toneShape = ToneShape.NEUTRAL),
  ),
  CUTE_GIRL(
    id = "cute_girl",
    displayName = "Cute Girl",
    shortTag = "1.42x",
    subtitle = "Sweet high-formant tone",
    accentColor = Color(0xFFFF4D7D),
    defaultConfig =
      VoiceDspConfig(
        pitchFactor = 1.42f,
        gain = 1.20f,
        echoMix = 0.08f,
        toneShape = ToneShape.BRIGHT_AIRY,
      ),
  ),
  SMALL_GIRL(
    id = "small_girl",
    displayName = "Small Girl",
    shortTag = "1.68x",
    subtitle = "Playful bright soprano",
    accentColor = Color(0xFFFF85A1),
    defaultConfig =
      VoiceDspConfig(
        pitchFactor = 1.68f,
        gain = 1.25f,
        echoMix = 0.05f,
        toneShape = ToneShape.CRISP_HIGH,
      ),
  ),
  GRANDMA(
    id = "grandma",
    displayName = "Grandma",
    shortTag = "WOBBLE",
    subtitle = "Warm elder vibrato",
    accentColor = Color(0xFFF59E0B),
    defaultConfig =
      VoiceDspConfig(
        pitchFactor = 1.18f,
        gain = 1.20f,
        tremoloDepth = 0.28f,
        toneShape = ToneShape.WARM_TREMOLO,
      ),
  ),
  GRANDPA(
    id = "grandpa",
    displayName = "Grandpa",
    shortTag = "0.76x",
    subtitle = "Gravelly elder baritone",
    accentColor = Color(0xFF38BDF8),
    defaultConfig =
      VoiceDspConfig(
        pitchFactor = 0.76f,
        gain = 1.25f,
        tremoloDepth = 0.14f,
        toneShape = ToneShape.DEEP_RESONANT,
      ),
  ),
  MOTU(
    id = "motu",
    displayName = "Motu Titan",
    shortTag = "0.64x",
    subtitle = "Heavy chest sub-bass",
    accentColor = Color(0xFFE61E32),
    defaultConfig =
      VoiceDspConfig(
        pitchFactor = 0.64f,
        gain = 1.35f,
        echoMix = 0.12f,
        toneShape = ToneShape.HEAVY_BASS,
      ),
  ),
  CYBER_BOT(
    id = "cyber_bot",
    displayName = "Cyber Bot",
    shortTag = "SYNTH",
    subtitle = "Ring-modulated droid",
    accentColor = Color(0xFF00D2FF),
    defaultConfig =
      VoiceDspConfig(
        pitchFactor = 0.96f,
        gain = 1.20f,
        echoMix = 0.22f,
        robotMix = 0.68f,
        robotFreqHz = 52f,
        toneShape = ToneShape.METALLIC_SYNTH,
      ),
  ),
  GHOST(
    id = "ghost",
    displayName = "Phantom",
    shortTag = "REVERB",
    subtitle = "Dark echo chamber",
    accentColor = Color(0xFFA855F7),
    defaultConfig =
      VoiceDspConfig(
        pitchFactor = 0.82f,
        gain = 1.25f,
        echoMix = 0.52f,
        toneShape = ToneShape.ETHEREAL_ECHO,
      ),
  );

  companion object {
    fun fromId(id: String?): VoicePreset = entries.firstOrNull { it.id == id } ?: CUTE_GIRL
  }
}
