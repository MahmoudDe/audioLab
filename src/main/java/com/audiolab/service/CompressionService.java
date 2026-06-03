package com.audiolab.service;

import com.audiolab.model.AudioMetadata;
import com.audiolab.model.CompressionAlgorithm;
import com.audiolab.model.CompressionReport;
import com.audiolab.model.CompressionSettings;
import com.audiolab.model.ProcessingState;
import com.audiolab.model.AudioSession;
import com.audiolab.service.compression.AdaptiveDeltaModulationCodec;
import com.audiolab.service.compression.AudioCodec;
import com.audiolab.service.compression.AudcContainer;
import com.audiolab.service.compression.DeltaModulationCodec;
import com.audiolab.service.compression.DpcmCodec;
import com.audiolab.service.compression.ProgressCallback;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/** Orchestrates compression and decompression on a background thread. */
public final class CompressionService {

    private final Map<CompressionAlgorithm, AudioCodec> codecs = new EnumMap<>(CompressionAlgorithm.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audio-compression");
        t.setDaemon(true);
        return t;
    });

    private volatile Future<?> currentTask;
    private volatile boolean cancelRequested;

    public CompressionService() {
        codecs.put(CompressionAlgorithm.DPCM, new DpcmCodec());
        codecs.put(CompressionAlgorithm.DELTA_MODULATION, new DeltaModulationCodec());
        codecs.put(CompressionAlgorithm.ADAPTIVE_DELTA_MODULATION, new AdaptiveDeltaModulationCodec());
    }

    public void compress(AudioSession session, PerformanceMonitor monitor,
                         Consumer<CompressionReport> onSuccess, Consumer<Throwable> onError) {
        cancelCurrent();
        cancelRequested = false;
        session.setProcessingState(ProcessingState.COMPRESSING);
        monitor.reset();
        monitor.start();

        currentTask = executor.submit(() -> {
            try {
                AudioMetadata meta = session.metadata().orElseThrow();
                CompressionSettings settings = session.settings().copy();
                short[] input = downsample(session.originalSamples(), meta.channels(),
                        (int) meta.sampleRate(), settings.getTargetSampleRate());
                AudioCodec codec = codecs.get(settings.getAlgorithm());

                ProgressCallback callback = progressCallback(monitor, input.length);
                short[] seed = extractSeed(input, meta.channels());
                byte[] payload = codec.encode(input, meta.channels(), settings, callback);

                AudcContainer.Header header = new AudcContainer.Header(
                        settings.getAlgorithm(),
                        settings,
                        meta.channels(),
                        meta.bitDepth(),
                        input.length,
                        seed
                );
                byte[] container = AudcContainer.pack(header, payload);

                long elapsed = monitor.elapsedMillis();
                long originalSize = meta.fileSizeBytes();
                long compressedSize = container.length;
                double savings = originalSize > 0
                        ? (1.0 - (double) compressedSize / originalSize) * 100.0
                        : 0.0;

                CompressionReport report = new CompressionReport(
                        originalSize,
                        compressedSize,
                        savings,
                        elapsed,
                        settings.getAlgorithm(),
                        settings,
                        monitor.getAverageSamplesPerSecond(),
                        monitor.getPeakSamplesPerSecond()
                );

                session.setCompressed(payload, container);
                session.lastReportProperty().set(report);
                session.setProcessingState(ProcessingState.IDLE);
                monitor.finish();
                onSuccess.accept(report);
            } catch (CancellationException e) {
                session.setProcessingState(ProcessingState.CANCELLED);
                monitor.finish();
                onError.accept(e);
            } catch (Exception e) {
                session.setProcessingState(ProcessingState.IDLE);
                monitor.finish();
                onError.accept(e);
            }
        });
    }

    public void decompress(AudioSession session, PerformanceMonitor monitor,
                           Runnable onSuccess, Consumer<Throwable> onError) {
        cancelCurrent();
        cancelRequested = false;
        session.setProcessingState(ProcessingState.DECOMPRESSING);
        monitor.reset();
        monitor.start();

        currentTask = executor.submit(() -> {
            try {
                byte[] container = session.containerBytes();
                if (container.length == 0) {
                    throw new IllegalStateException("No compressed data");
                }
                AudcContainer.Parsed parsed = AudcContainer.parse(container);
                AudcContainer.Header header = parsed.header();
                AudioCodec codec = codecs.get(header.algorithm());

                ProgressCallback callback = progressCallback(monitor, header.sampleCount());
                short[] decoded = codec.decode(
                        parsed.payload(),
                        header.channels(),
                        header.sampleCount(),
                        header.seedSamples(),
                        header.settings(),
                        callback
                );
                session.setWorkingSamples(decoded);
                session.setProcessingState(ProcessingState.IDLE);
                monitor.finish();
                onSuccess.run();
            } catch (CancellationException e) {
                session.setProcessingState(ProcessingState.CANCELLED);
                monitor.finish();
                onError.accept(e);
            } catch (Exception e) {
                session.setProcessingState(ProcessingState.IDLE);
                monitor.finish();
                onError.accept(e);
            }
        });
    }

    public void decompressFromFile(byte[] containerBytes, AudioSession session,
                                   PerformanceMonitor monitor, Runnable onSuccess,
                                   Consumer<Throwable> onError) {
        session.setCompressed(new byte[0], containerBytes);
        decompress(session, monitor, onSuccess, onError);
    }

    public void cancel() {
        cancelRequested = true;
        Future<?> task = currentTask;
        if (task != null) {
            task.cancel(true);
        }
    }

    public void shutdown() {
        cancel();
        executor.shutdownNow();
    }

    private void cancelCurrent() {
        cancelRequested = true;
        Future<?> task = currentTask;
        if (task != null) {
            task.cancel(true);
        }
    }

    private ProgressCallback progressCallback(PerformanceMonitor monitor, int totalSamples) {
        return new ProgressCallback() {
            @Override
            public void onProgress(int processedSamples, int total, long compressedBytes) {
                monitor.update(processedSamples, totalSamples, compressedBytes);
            }

            @Override
            public boolean isCancelled() {
                return cancelRequested || Thread.currentThread().isInterrupted();
            }
        };
    }

    private static short[] extractSeed(short[] samples, int channels) {
        short[] seed = new short[channels];
        for (int ch = 0; ch < channels; ch++) {
            seed[ch] = samples[ch];
        }
        return seed;
    }

    static short[] downsample(short[] samples, int channels, int sourceRate, int targetRate) {
        if (targetRate >= sourceRate || sourceRate <= 0) {
            return Arrays.copyOf(samples, samples.length);
        }
        double ratio = (double) sourceRate / targetRate;
        int sourceFrames = samples.length / channels;
        int targetFrames = (int) Math.floor(sourceFrames / ratio);
        short[] output = new short[targetFrames * channels];
        for (int frame = 0; frame < targetFrames; frame++) {
            int sourceFrame = (int) Math.floor(frame * ratio);
            for (int ch = 0; ch < channels; ch++) {
                output[frame * channels + ch] = samples[sourceFrame * channels + ch];
            }
        }
        return output;
    }
}
