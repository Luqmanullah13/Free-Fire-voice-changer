package com.example.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure-Kotlin real-time PCM16 Voice DSP engine.
 *
 * Implements:
 * - Dual-tap Hann-windowed OLA time-domain pitch shifter (0.50x to 2.00x)
 * - Tone shaping filters (brightness boost, low-pass warmth, bass shelf)
 * - Ring modulation (Cyber Bot), tremolo LFO (Grandma), and feedback delay echo (Phantom)
 * - Waveform bar & RMS telemetry extraction
 */
class VoiceDspProcessor(private val sampleRate: Int = 44100) {

  private val pitchRingSize = 4096
  private val pitchRingMask = pitchRingSize - 1
  private val pitchRing = FloatArray(pitchRingSize)
  private var pitchWriteIdx = 0

  // Window size for dual-tap overlap-add pitch shifting (~23ms at 44.1kHz)
  private val windowSize = 1024
  private var phaseA = 0f

  // Feedback echo delay line (~180ms at 44.1kHz)
  private val echoDelaySamples = (sampleRate * 0.18f).toInt().coerceAtLeast(1024)
  private val echoBuffer = FloatArray(echoDelaySamples)
  private var echoIdx = 0

  // LFO phase accumulators
  private var ringPhase = 0.0
  private var tremoloPhase = 0.0

  // 1-pole filter states
  private var lpState = 0f
  private var hpPrevIn = 0f
  private var hpState = 0f

  fun reset() {
    pitchRing.fill(0f)
    echoBuffer.fill(0f)
    pitchWriteIdx = 0
    phaseA = 0f
    echoIdx = 0
    ringPhase = 0.0
    tremoloPhase = 0.0
    lpState = 0f
    hpPrevIn = 0f
    hpState = 0f
  }

  /**
   * Processes [input] PCM16 samples in-place into [output], returning normalized RMS (0f..1f) and
   * filling [waveformOut] (if provided) with normalized bar heights.
   */
  fun processBuffer(
    input: ShortArray,
    output: ShortArray,
    length: Int,
    config: VoiceDspConfig,
    waveformOut: FloatArray? = null,
  ): Float {
    val count = min(length, min(input.size, output.size))
    if (count <= 0) return 0f

    val pitch = config.pitchFactor.coerceIn(0.50f, 2.00f)
    val doPitchShift = abs(pitch - 1.0f) > 0.015f
    val pitchStep = 1.0f - pitch

    val ringPhaseInc = (2.0 * PI * config.robotFreqHz.toDouble()) / sampleRate.toDouble()
    val tremPhaseInc = (2.0 * PI * 7.2) / sampleRate.toDouble()

    var sumSquares = 0.0

    for (i in 0 until count) {
      val raw = input[i] / 32768f

      // Write into circular pitch ring
      pitchRing[pitchWriteIdx] = raw

      // 1. Pitch Shift (dual-tap Hann OLA)
      var sample =
        if (!doPitchShift) {
          raw
        } else {
          phaseA += pitchStep
          while (phaseA >= windowSize) phaseA -= windowSize
          while (phaseA < 0f) phaseA += windowSize

          var phaseB = phaseA + windowSize * 0.5f
          if (phaseB >= windowSize) phaseB -= windowSize

          val sA = readFractional(pitchWriteIdx - phaseA)
          val sB = readFractional(pitchWriteIdx - phaseB)

          val wA = hannWeight(phaseA / windowSize)
          val wB = hannWeight(phaseB / windowSize)
          sA * wA + sB * wB
        }

      pitchWriteIdx = (pitchWriteIdx + 1) and pitchRingMask

      // 2. Tone shaping
      sample = applyToneShape(sample, config.toneShape)

      // 3. Tremolo LFO (elder vocal vibrato)
      if (config.tremoloDepth > 0.01f) {
        val mod = 1f - config.tremoloDepth * (0.5f + 0.5f * sin(tremoloPhase).toFloat())
        sample *= mod
        tremoloPhase += tremPhaseInc
        if (tremoloPhase > 2.0 * PI) tremoloPhase -= 2.0 * PI
      }

      // 4. Ring Modulator (robotic synth)
      if (config.robotMix > 0.01f) {
        val carrier = sin(ringPhase).toFloat()
        val modulated = sample * carrier * 1.4f
        sample = sample * (1f - config.robotMix) + modulated * config.robotMix
        ringPhase += ringPhaseInc
        if (ringPhase > 2.0 * PI) ringPhase -= 2.0 * PI
      }

      // 5. Feedback Echo Delay
      if (config.echoMix > 0.01f) {
        val delayed = echoBuffer[echoIdx]
        val fed = (sample + delayed * 0.42f).coerceIn(-1f, 1f)
        echoBuffer[echoIdx] = fed
        sample = sample * (1f - config.echoMix * 0.4f) + delayed * config.echoMix
        echoIdx++
        if (echoIdx >= echoDelaySamples) echoIdx = 0
      }

      // 6. Gain + soft limiter
      val amplified = sample * config.gain
      val limited = softClip(amplified)

      sumSquares += (limited * limited).toDouble()
      output[i] = (limited * 32767f).toInt().coerceIn(-32768, 32767).toShort()
    }

    if (waveformOut != null && waveformOut.isNotEmpty()) {
      val bars = waveformOut.size
      val step = (count / bars).coerceAtLeast(1)
      for (b in 0 until bars) {
        val start = b * step
        val end = min(start + step, count)
        var peak = 0f
        for (j in start until end) {
          val v = abs(output[j] / 32768f)
          if (v > peak) peak = v
        }
        waveformOut[b] = peak.coerceIn(0.04f, 1f)
      }
    }

    return sqrt(sumSquares / count).toFloat().coerceIn(0f, 1f)
  }

  private fun readFractional(indexFloat: Float): Float {
    var idx = indexFloat
    while (idx < 0f) idx += pitchRingSize
    val i0 = idx.toInt() and pitchRingMask
    val i1 = (i0 + 1) and pitchRingMask
    val frac = idx - idx.toInt()
    return pitchRing[i0] * (1f - frac) + pitchRing[i1] * frac
  }

  private fun hannWeight(normPhase: Float): Float {
    return 0.5f * (1f - cos(2.0 * PI * normPhase).toFloat())
  }

  private fun applyToneShape(x: Float, shape: ToneShape): Float {
    // Update simple 1-pole low-pass and high-pass trackers
    lpState += 0.24f * (x - lpState)
    hpState = 0.86f * (hpState + x - hpPrevIn)
    hpPrevIn = x

    return when (shape) {
      ToneShape.NEUTRAL -> x
      ToneShape.BRIGHT_AIRY -> x * 0.72f + hpState * 0.45f
      ToneShape.CRISP_HIGH -> x * 0.58f + hpState * 0.65f
      ToneShape.WARM_TREMOLO -> lpState * 0.85f + x * 0.25f
      ToneShape.DEEP_RESONANT -> lpState * 0.95f + x * 0.22f
      ToneShape.HEAVY_BASS -> lpState * 1.15f + x * 0.18f
      ToneShape.METALLIC_SYNTH -> hpState * 0.75f + x * 0.35f
      ToneShape.ETHEREAL_ECHO -> lpState * 0.55f + hpState * 0.35f + x * 0.25f
    }
  }

  private fun softClip(x: Float): Float {
    return when {
      x > 0.92f -> 0.92f + (x - 0.92f) / (1f + abs(x - 0.92f) * 4f)
      x < -0.92f -> -0.92f + (x + 0.92f) / (1f + abs(x + 0.92f) * 4f)
      else -> x
    }.coerceIn(-0.99f, 0.99f)
  }
}
