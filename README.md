# AudioLab

JavaFX desktop application for the Multimedia Systems practical assignment — audio file compression with DPCM, Delta Modulation, and Adaptive Delta Modulation.

## Quick start

```bash
./mvnw javafx:run
```

Or run `com.audiolab.Launcher` from your IDE (Java 17+).

| Shortcut | Action |
|----------|--------|
| `Ctrl/⌘ + O` | Open WAV |
| `Ctrl/⌘ + S` | Save (.audc or .wav) |
| `Shift + Ctrl/⌘ + R` | Reset to original |

## Features

- Load WAV via file chooser or drag-and-drop
- Preview playback before/after compression
- Auto-display metadata (size, duration, sample rate, channels, bit rate, encoding)
- Compress with DPCM, Delta Modulation, or Adaptive Delta Modulation
- Decompress and play reconstructed audio
- Configurable settings (sample rate, quantization levels, step sizes)
- Real-time progress bar and live charts (compression ratio + processing speed)
- Cancel mid-operation
- Reset to original
- Post-compression report
- Save compressed `.audc` or decompressed `.wav`

## Tech stack

- Java 17, JavaFX 21, Maven
- Gson JSON i18n (English / Arabic)
- `javax.sound.sampled` for WAV I/O and playback

## Smoke test

```bash
./mvnw compile exec:java -Dexec.mainClass=com.audiolab.SmokeTest
```
