package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.VoiceDspProcessor
import com.example.audio.VoicePreset
import kotlin.math.PI
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches VoxForge`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VoxForge", appName)
  }

  @Test
  fun `dsp processor transforms sine wave across all presets`() {
    val sampleRate = 44100
    val frameCount = 1024
    val input =
      ShortArray(frameCount) { i ->
        (sin(2.0 * PI * 220.0 * i / sampleRate) * 14000.0).toInt().toShort()
      }

    VoicePreset.entries.forEach { preset ->
      val processor = VoiceDspProcessor(sampleRate)
      val output = ShortArray(frameCount)
      val waveform = FloatArray(20)
      val rms = processor.processBuffer(input, output, frameCount, preset.defaultConfig, waveform)
      assertTrue("RMS should be positive for ${preset.displayName}", rms > 0.01f)
      assertTrue("Waveform bars should be populated for ${preset.displayName}", waveform.any { it > 0.05f })
    }
  }

  @Test
  fun `preset lookup falls back safely`() {
    assertEquals(VoicePreset.CUTE_GIRL, VoicePreset.fromId("unknown_id"))
    assertEquals(VoicePreset.MOTU, VoicePreset.fromId("motu"))
  }
}
