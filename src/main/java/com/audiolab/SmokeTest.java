package com.audiolab;

import com.audiolab.model.CompressionAlgorithm;
import com.audiolab.model.CompressionReport;
import com.audiolab.model.CompressionSettings;
import com.audiolab.model.AudioSession;
import com.audiolab.service.AudioIOService;
import com.audiolab.service.CompressionService;
import com.audiolab.service.PerformanceMonitor;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Smoke test for load → compress → decompress round-trip. */
public final class SmokeTest {

    public static void main(String[] args) throws Exception {
        File wav = new File(args.length > 0 ? args[0] : "test.wav");
        AudioIOService io = new AudioIOService();
        AudioSession session = new AudioSession();
        var loaded = io.load(wav);
        session.open(wav, loaded.metadata(), loaded.samples());

        for (CompressionAlgorithm algorithm : CompressionAlgorithm.values()) {
            CompressionSettings settings = session.settings();
            settings.setAlgorithm(algorithm);
            session.settingsProperty().set(settings);

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<CompressionReport> reportRef = new AtomicReference<>();
            PerformanceMonitor monitor = new PerformanceMonitor();
            monitor.setOriginalSizeBytes(loaded.metadata().fileSizeBytes());
            CompressionService compression = new CompressionService();

            compression.compress(session, monitor, report -> {
                reportRef.set(report);
                latch.countDown();
            }, error -> {
                error.printStackTrace();
                latch.countDown();
            });
            if (!latch.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Compression timed out for " + algorithm);
            }
            CompressionReport report = reportRef.get();
            System.out.printf("%s: %s -> %s (%.1f%% saved)%n",
                    algorithm,
                    report.formattedOriginalSize(),
                    report.formattedCompressedSize(),
                    report.savingsPercent());

            CountDownLatch decodeLatch = new CountDownLatch(1);
            compression.decompress(session, monitor, decodeLatch::countDown, error -> {
                error.printStackTrace();
                decodeLatch.countDown();
            });
            if (!decodeLatch.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Decompress timed out for " + algorithm);
            }
            System.out.println("  decoded samples: " + session.workingSamples().length);
            compression.shutdown();
        }
        System.out.println("Smoke test passed.");
    }

    private SmokeTest() {}
}
