# VoxForge — Real-Time Voice Changer & Floating HUD Studio

VoxForge is a Jetpack Compose Android application featuring a real-time PCM16 digital signal processing (DSP) voice changer, a draggable floating game HUD overlay, studio audio recording/playback, and pre-configured GitHub Actions workflows for building signed Debug APKs.

## Key Systems

- **Access Gate & Session Persistence (`DataStore`)**: Animated crimson constellation particle network login screen with credential saving (`Save Login`) and persisted preset/target app preferences.
- **Real-Time Voice DSP Engine (`AudioRecord` + `AudioTrack`)**: Pure-Kotlin 44.1 kHz PCM16 DSP pipeline supporting overlap-add (OLA) pitch shifting (`0.50x`–`2.00x`), tone shaping, LFO tremolo, ring modulation, and feedback echo across 8 presets (`Normal`, `Cute Girl`, `Small Girl`, `Grandma`, `Grandpa`, `Motu Titan`, `Cyber Bot`, `Phantom`).
- **Floating HUD Overlay Service (`VoiceOverlayService`)**: Foreground microphone service (`foregroundServiceType="microphone"`) rendering a draggable floating bubble and expandable preset switcher panel via `WindowManager`, plus an interactive in-app HUD sandbox.
- **Studio Clip Recorder & Audition**: Records raw WAV samples to app-private storage and plays them back through the live DSP chain so presets can be switched mid-playback.
- **GitHub Actions CI/CD (`.github/workflows/`)**:
  - `build-debug-apk.yml`: Sets up JDK 21, Android SDK, and Gradle 9.3.1, prepares `.env` and `debug.keystore`, runs Robolectric unit tests, builds `app-debug.apk`, and uploads it as a GitHub Actions artifact.
  - `android-ci.yml`: Runs unit tests (`:app:testDebugUnitTest`) and Android Lint (`:app:lintDebug`) on pushes and pull requests.

## Local Build Commands

```bash
gradle :app:testDebugUnitTest --no-daemon
gradle :app:assembleDebug --no-daemon
```
