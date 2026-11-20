package com.example.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class VoiceRecordingClip(
  val id: String,
  val name: String,
  val filePath: String,
  val durationMs: Long,
  val sizeBytes: Long,
  val createdAtLabel: String,
)

data class VoiceEngineState(
  val isLiveModulationActive: Boolean = false,
  val isOverlayActive: Boolean = false,
  val isRecording: Boolean = false,
  val playingClipId: String? = null,
  val activePreset: VoicePreset = VoicePreset.CUTE_GIRL,
  val dspConfig: VoiceDspConfig = VoicePreset.CUTE_GIRL.defaultConfig,
  val sampleRate: Int = 44100,
  val bufferSizeFrames: Int = 1024,
  val estimatedLatencyMs: Int = 23,
  val rmsLevel: Float = 0f,
  val waveformBars: List<Float> = List(20) { 0.08f },
  val recordedClips: List<VoiceRecordingClip> = emptyList(),
  val recordingSeconds: Int = 0,
  val statusMessage: String = "Engine standby",
  val errorMessage: String? = null,
)

object VoiceEngineManager {

  private const val SAMPLE_RATE = 44100
  private const val WAVEFORM_BARS = 20

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val processor = VoiceDspProcessor(SAMPLE_RATE)

  private val _state = MutableStateFlow(VoiceEngineState())
  val state: StateFlow<VoiceEngineState> = _state.asStateFlow()

  private var liveJob: Job? = null
  private var recordJob: Job? = null
  private var playbackJob: Job? = null

  fun initClipsDirectory(context: Context) {
    refreshRecordings(context)
  }

  fun selectPreset(preset: VoicePreset) {
    _state.update {
      it.copy(
        activePreset = preset,
        dspConfig = preset.defaultConfig,
        statusMessage = "Preset: ${preset.displayName}",
        errorMessage = null,
      )
    }
  }

  fun updatePitch(pitch: Float) {
    _state.update {
      it.copy(dspConfig = it.dspConfig.copy(pitchFactor = pitch.coerceIn(0.50f, 2.00f)))
    }
  }

  fun updateGain(gain: Float) {
    _state.update { it.copy(dspConfig = it.dspConfig.copy(gain = gain.coerceIn(0.50f, 2.50f))) }
  }

  fun updateEcho(echoMix: Float) {
    _state.update {
      it.copy(dspConfig = it.dspConfig.copy(echoMix = echoMix.coerceIn(0f, 0.75f)))
    }
  }

  fun updateRobotMix(robotMix: Float) {
    _state.update {
      it.copy(dspConfig = it.dspConfig.copy(robotMix = robotMix.coerceIn(0f, 1.0f)))
    }
  }

  fun resetCurrentPresetConfig() {
    _state.update { it.copy(dspConfig = it.activePreset.defaultConfig) }
  }

  fun setOverlayActive(active: Boolean) {
    _state.update { it.copy(isOverlayActive = active) }
  }

  fun clearError() {
    _state.update { it.copy(errorMessage = null) }
  }

  fun toggleLiveModulation(context: Context): Boolean {
    return if (_state.value.isLiveModulationActive) {
      stopLiveModulation()
      false
    } else {
      startLiveModulation(context)
    }
  }

  fun startLiveModulation(context: Context): Boolean {
    if (
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
        PackageManager.PERMISSION_GRANTED
    ) {
      _state.update {
        it.copy(
          isLiveModulationActive = false,
          errorMessage = "Microphone permission is required for live voice modulation.",
        )
      }
      return false
    }

    if (_state.value.isLiveModulationActive) return true
    stopPlayback()
    stopRecording(context)

    liveJob?.cancel()
    liveJob =
      scope.launch {
        val minRecBuf =
          AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
          )
        val minPlayBuf =
          AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
          )
        val frameShorts = 1024
        val recBufferBytes = maxOf(minRecBuf, frameShorts * 4)
        val playBufferBytes = maxOf(minPlayBuf, frameShorts * 4)
        val latencyMs = ((frameShorts * 1000f) / SAMPLE_RATE).roundToInt()

        var recorder: AudioRecord? = null
        var player: AudioTrack? = null

        try {
          recorder =
            AudioRecord(
              MediaRecorder.AudioSource.VOICE_COMMUNICATION,
              SAMPLE_RATE,
              AudioFormat.CHANNEL_IN_MONO,
              AudioFormat.ENCODING_PCM_16BIT,
              recBufferBytes,
            )
          if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            recorder =
              AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                recBufferBytes,
              )
          }

          player =
            AudioTrack.Builder()
              .setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_MEDIA)
                  .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                  .build()
              )
              .setAudioFormat(
                AudioFormat.Builder()
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setSampleRate(SAMPLE_RATE)
                  .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                  .build()
              )
              .setBufferSizeInBytes(playBufferBytes)
              .setTransferMode(AudioTrack.MODE_STREAM)
              .setSessionId(AudioManager.AUDIO_SESSION_ID_GENERATE)
              .build()

          if (
            recorder.state != AudioRecord.STATE_INITIALIZED ||
              player.state != AudioTrack.STATE_INITIALIZED
          ) {
            _state.update {
              it.copy(
                isLiveModulationActive = false,
                errorMessage = "Audio hardware failed to initialize.",
              )
            }
            return@launch
          }

          processor.reset()
          recorder.startRecording()
          player.play()

          _state.update {
            it.copy(
              isLiveModulationActive = true,
              bufferSizeFrames = frameShorts,
              estimatedLatencyMs = latencyMs,
              statusMessage = "Live DSP Active • ${_state.value.activePreset.displayName}",
              errorMessage = null,
            )
          }

          val inBuf = ShortArray(frameShorts)
          val outBuf = ShortArray(frameShorts)
          val waveBars = FloatArray(WAVEFORM_BARS)

          while (isActive) {
            val read = recorder.read(inBuf, 0, frameShorts)
            if (read > 0) {
              val rms = processor.processBuffer(inBuf, outBuf, read, _state.value.dspConfig, waveBars)
              player.write(outBuf, 0, read)
              _state.update { it.copy(rmsLevel = rms, waveformBars = waveBars.toList()) }
            }
          }
        } catch (se: SecurityException) {
          _state.update {
            it.copy(
              isLiveModulationActive = false,
              errorMessage = "Microphone access denied: ${se.localizedMessage ?: "Permission error"}",
            )
          }
        } catch (e: Exception) {
          _state.update {
            it.copy(
              isLiveModulationActive = false,
              errorMessage = "Audio engine error: ${e.localizedMessage ?: "Unknown"}",
            )
          }
        } finally {
          runCatching { recorder?.stop() }
          runCatching { recorder?.release() }
          runCatching { player?.stop() }
          runCatching { player?.release() }
          _state.update {
            it.copy(
              isLiveModulationActive = false,
              rmsLevel = 0f,
              waveformBars = List(WAVEFORM_BARS) { 0.08f },
              statusMessage = "Engine standby",
            )
          }
        }
      }
    return true
  }

  fun stopLiveModulation() {
    liveJob?.cancel()
    liveJob = null
    _state.update {
      it.copy(
        isLiveModulationActive = false,
        rmsLevel = 0f,
        waveformBars = List(WAVEFORM_BARS) { 0.08f },
        statusMessage = "Engine standby",
      )
    }
  }

  fun startRecording(context: Context) {
    if (
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
        PackageManager.PERMISSION_GRANTED
    ) {
      _state.update { it.copy(errorMessage = "Grant Microphone permission to record voice clips.") }
      return
    }
    if (_state.value.isRecording) return

    stopLiveModulation()
    stopPlayback()

    recordJob?.cancel()
    recordJob =
      scope.launch {
        val frameShorts = 1024
        val minRecBuf =
          AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
          )
        val recBufferBytes = maxOf(minRecBuf, frameShorts * 4)
        var recorder: AudioRecord? = null
        val pcmStream = ByteArrayOutputStream()
        val startMs = System.currentTimeMillis()

        try {
          recorder =
            AudioRecord(
              MediaRecorder.AudioSource.MIC,
              SAMPLE_RATE,
              AudioFormat.CHANNEL_IN_MONO,
              AudioFormat.ENCODING_PCM_16BIT,
              recBufferBytes,
            )
          if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            _state.update { it.copy(errorMessage = "Could not open microphone for recording.") }
            return@launch
          }

          recorder.startRecording()
          _state.update {
            it.copy(
              isRecording = true,
              recordingSeconds = 0,
              statusMessage = "Recording raw studio sample...",
              errorMessage = null,
            )
          }

          val inBuf = ShortArray(frameShorts)
          val outBuf = ShortArray(frameShorts)
          val waveBars = FloatArray(WAVEFORM_BARS)
          val byteBuf = ByteBuffer.allocate(frameShorts * 2).order(ByteOrder.LITTLE_ENDIAN)

          while (isActive) {
            val read = recorder.read(inBuf, 0, frameShorts)
            if (read > 0) {
              byteBuf.clear()
              for (i in 0 until read) {
                byteBuf.putShort(inBuf[i])
              }
              pcmStream.write(byteBuf.array(), 0, read * 2)

              val rms =
                processor.processBuffer(
                  inBuf,
                  outBuf,
                  read,
                  VoicePreset.NORMAL.defaultConfig,
                  waveBars,
                )
              val elapsedSec = ((System.currentTimeMillis() - startMs) / 1000L).toInt()
              _state.update {
                it.copy(
                  rmsLevel = rms,
                  waveformBars = waveBars.toList(),
                  recordingSeconds = elapsedSec,
                )
              }
              if (elapsedSec >= 30) break
            }
          }
        } catch (e: Exception) {
          _state.update {
            it.copy(errorMessage = "Recording failed: ${e.localizedMessage ?: "Error"}")
          }
        } finally {
          runCatching { recorder?.stop() }
          runCatching { recorder?.release() }

          val rawBytes = pcmStream.toByteArray()
          if (rawBytes.size > SAMPLE_RATE / 2) {
            saveWavFile(context, rawBytes)
          }
          _state.update {
            it.copy(
              isRecording = false,
              rmsLevel = 0f,
              waveformBars = List(WAVEFORM_BARS) { 0.08f },
              statusMessage = "Clip saved — tap Play to audition presets",
            )
          }
          refreshRecordings(context)
        }
      }
  }

  fun stopRecording(context: Context) {
    if (_state.value.isRecording) {
      recordJob?.cancel()
      recordJob = null
      refreshRecordings(context)
    }
  }

  fun playClipWithLiveDsp(clip: VoiceRecordingClip) {
    if (_state.value.playingClipId == clip.id) {
      stopPlayback()
      return
    }
    stopLiveModulation()
    playbackJob?.cancel()

    playbackJob =
      scope.launch {
        val file = File(clip.filePath)
        if (!file.exists() || file.length() <= 44L) {
          _state.update { it.copy(errorMessage = "Clip file is empty or missing.") }
          return@launch
        }

        val frameShorts = 1024
        val minPlayBuf =
          AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
          )
        val playBufBytes = maxOf(minPlayBuf, frameShorts * 4)

        var track: AudioTrack? = null
        try {
          track =
            AudioTrack.Builder()
              .setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_MEDIA)
                  .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                  .build()
              )
              .setAudioFormat(
                AudioFormat.Builder()
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setSampleRate(SAMPLE_RATE)
                  .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                  .build()
              )
              .setBufferSizeInBytes(playBufBytes)
              .setTransferMode(AudioTrack.MODE_STREAM)
              .build()

          processor.reset()
          track.play()
          _state.update {
            it.copy(
              playingClipId = clip.id,
              statusMessage = "Auditioning: ${clip.name} (${_state.value.activePreset.displayName})",
              errorMessage = null,
            )
          }

          FileInputStream(file).use { fis ->
            // Skip 44-byte WAV header
            val header = ByteArray(44)
            fis.read(header)

            val rawByteChunk = ByteArray(frameShorts * 2)
            val inShorts = ShortArray(frameShorts)
            val outShorts = ShortArray(frameShorts)
            val waveBars = FloatArray(WAVEFORM_BARS)

            while (isActive) {
              val bytesRead = fis.read(rawByteChunk)
              if (bytesRead <= 0) break
              val shortCount = bytesRead / 2
              ByteBuffer.wrap(rawByteChunk, 0, shortCount * 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
                .get(inShorts, 0, shortCount)

              val rms =
                processor.processBuffer(
                  inShorts,
                  outShorts,
                  shortCount,
                  _state.value.dspConfig,
                  waveBars,
                )
              track.write(outShorts, 0, shortCount)
              _state.update { it.copy(rmsLevel = rms, waveformBars = waveBars.toList()) }
            }
          }
        } catch (e: Exception) {
          _state.update {
            it.copy(errorMessage = "Playback error: ${e.localizedMessage ?: "Unknown"}")
          }
        } finally {
          runCatching { track?.stop() }
          runCatching { track?.release() }
          _state.update {
            it.copy(
              playingClipId = null,
              rmsLevel = 0f,
              waveformBars = List(WAVEFORM_BARS) { 0.08f },
              statusMessage = "Engine standby",
            )
          }
        }
      }
  }

  fun stopPlayback() {
    playbackJob?.cancel()
    playbackJob = null
    _state.update {
      it.copy(
        playingClipId = null,
        rmsLevel = 0f,
        waveformBars = List(WAVEFORM_BARS) { 0.08f },
      )
    }
  }

  fun deleteClip(context: Context, clip: VoiceRecordingClip) {
    if (_state.value.playingClipId == clip.id) {
      stopPlayback()
    }
    runCatching { File(clip.filePath).delete() }
    refreshRecordings(context)
  }

  private fun recordingsDir(context: Context): File {
    val dir = File(context.filesDir, "recordings")
    if (!dir.exists()) dir.mkdirs()
    return dir
  }

  private fun saveWavFile(context: Context, pcmBytes: ByteArray) {
    val stamp = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date())
    val file = File(recordingsDir(context), "vox_$stamp.wav")
    val totalDataLen = pcmBytes.size + 36
    val byteRate = SAMPLE_RATE * 2

    FileOutputStream(file).use { out ->
      val header =
        ByteBuffer.allocate(44)
          .order(ByteOrder.LITTLE_ENDIAN)
          .put("RIFF".toByteArray())
          .putInt(totalDataLen)
          .put("WAVE".toByteArray())
          .put("fmt ".toByteArray())
          .putInt(16) // PCM subchunk size
          .putShort(1) // AudioFormat = PCM
          .putShort(1) // Mono
          .putInt(SAMPLE_RATE)
          .putInt(byteRate)
          .putShort(2) // BlockAlign
          .putShort(16) // BitsPerSample
          .put("data".toByteArray())
          .putInt(pcmBytes.size)
          .array()
      out.write(header)
      out.write(pcmBytes)
    }
  }

  fun refreshRecordings(context: Context) {
    val dir = recordingsDir(context)
    val files =
      dir.listFiles { f -> f.isFile && f.extension.equals("wav", ignoreCase = true) }
        ?.sortedByDescending { it.lastModified() }
        ?: emptyList()

    val fmt = SimpleDateFormat("MMM dd • HH:mm", Locale.US)
    val clips =
      files.mapIndexed { idx, file ->
        val pcmBytes = (file.length() - 44L).coerceAtLeast(0L)
        val durationMs = (pcmBytes * 1000L) / (SAMPLE_RATE * 2L)
        VoiceRecordingClip(
          id = file.name,
          name = "Studio Clip #${files.size - idx}",
          filePath = file.absolutePath,
          durationMs = durationMs,
          sizeBytes = file.length(),
          createdAtLabel = fmt.format(Date(file.lastModified())),
        )
      }
    _state.update { it.copy(recordedClips = clips) }
  }
}
