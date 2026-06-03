# AudioLab — Implementation Report

## Overview

AudioLab is a JavaFX desktop application that loads WAV audio, displays file metadata, previews playback, compresses audio using three assignment algorithms (DPCM, Delta Modulation, Adaptive Delta Modulation), monitors performance in real time, and saves compressed or decompressed output.

## Architecture

Layered design mirroring PixelLab:

- **UI** — FXML layout + `MainController` + `WaveformView`
- **Model** — `AudioSession`, `AudioMetadata`, `CompressionSettings`, `CompressionReport`
- **Services** — I/O, playback, compression orchestration, performance monitoring, export
- **Codecs** — `DpcmCodec`, `DeltaModulationCodec`, `AdaptiveDeltaModulationCodec`

Compressed output uses a custom `.audc` container that stores algorithm ID, settings, seed samples, and payload for round-trip decompression.

## Requirement mapping

| # | Requirement | Implementation |
|---|-------------|----------------|
| 1 | Load via GUI + drag-and-drop | File chooser + root pane drag-and-drop in `MainController` |
| 2 | Preview playback | `AudioPlaybackService` using `javax.sound.sampled.Clip` |
| 3 | Auto metadata | `AudioMetadata.fromFile()` populated on load |
| 4 | ≥3 compression algorithms | DPCM, Delta Modulation, ADM in `service/compression/` |
| 5 | Decompress | Inverse codecs + `CompressionService.decompress()` |
| 6 | Configurable settings | Sidebar spinners + algorithm combo bound to `CompressionSettings` |
| 7 | Real-time monitoring | `PerformanceMonitor` → progress bar + two `LineChart`s |
| 8 | Cancel | `CompressionService.cancel()` + cooperative `ProgressCallback` checks |
| 9 | Reset | `AudioSession.resetToOriginal()` |
| 10 | Post-compression report | `CompressionReport` displayed in sidebar |
| 11 | Save to disk | `AudioExportService` (.audc compressed, .wav decompressed) |

## Algorithms

### DPCM
First-order differential PCM: encode quantized differences between consecutive samples; decode by accumulating dequantized deltas from seed samples stored in the container header.

### Delta Modulation
1-bit encoding per sample using a fixed step integrator; payload is a packed bitstream.

### Adaptive Delta Modulation
Same as delta modulation but step size increases after consecutive same-sign bits and decreases on sign change, bounded by min/max step settings.

## Files of note

- `ui/MainController.java` — wires all UI events
- `service/CompressionService.java` — background compress/decompress with cancellation
- `service/compression/AudcContainer.java` — custom file format
- `resources/i18n/strings_en.json`, `strings_ar.json` — bilingual UI
