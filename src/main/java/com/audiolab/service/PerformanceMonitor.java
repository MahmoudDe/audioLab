package com.audiolab.service;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/** Thread-safe metrics surfaced to JavaFX charts and progress bar. */
public final class PerformanceMonitor {

    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final ObservableList<XYChart.Data<Number, Number>> ratioSeries = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data<Number, Number>> speedSeries = FXCollections.observableArrayList();

    private long startNanos;
    private long lastUpdateNanos;
    private int lastProcessedSamples;
    private double peakSamplesPerSecond;
    private double totalSamplesProcessed;
    private long originalSizeBytes;

    public ReadOnlyDoubleProperty progressProperty() {
        return progress;
    }

    public ObservableList<XYChart.Data<Number, Number>> ratioSeries() {
        return ratioSeries;
    }

    public ObservableList<XYChart.Data<Number, Number>> speedSeries() {
        return speedSeries;
    }

    public void setOriginalSizeBytes(long bytes) {
        this.originalSizeBytes = bytes;
    }

    public void reset() {
        runOnFxThread(() -> {
            progress.set(0);
            ratioSeries.clear();
            speedSeries.clear();
        });
        startNanos = 0;
        lastUpdateNanos = 0;
        lastProcessedSamples = 0;
        peakSamplesPerSecond = 0;
        totalSamplesProcessed = 0;
    }

    public void start() {
        startNanos = System.nanoTime();
        lastUpdateNanos = startNanos;
        lastProcessedSamples = 0;
    }

    public void finish() {
        runOnFxThread(() -> progress.set(Math.min(1.0, progress.get())));
    }

    public void update(int processedSamples, int totalSamples, long compressedBytes) {
        if (totalSamples <= 0) {
            return;
        }
        long now = System.nanoTime();
        double elapsedSec = (now - startNanos) / 1_000_000_000.0;
        double fraction = Math.min(1.0, (double) processedSamples / totalSamples);

        double instantSpeed = 0;
        if (lastUpdateNanos > 0) {
            double deltaSec = (now - lastUpdateNanos) / 1_000_000_000.0;
            int deltaSamples = processedSamples - lastProcessedSamples;
            if (deltaSec > 0) {
                instantSpeed = deltaSamples / deltaSec;
                peakSamplesPerSecond = Math.max(peakSamplesPerSecond, instantSpeed);
            }
        }
        totalSamplesProcessed = processedSamples;
        lastUpdateNanos = now;
        lastProcessedSamples = processedSamples;

        double ratioPercent = 0;
        if (originalSizeBytes > 0 && compressedBytes > 0) {
            ratioPercent = (1.0 - (double) compressedBytes / originalSizeBytes) * 100.0;
        }

        final double ratioPoint = ratioPercent;
        final double speedPoint = instantSpeed;
        final double elapsedPoint = elapsedSec;
        final double progressPoint = fraction;

        runOnFxThread(() -> {
            progress.set(progressPoint);
            ratioSeries.add(new XYChart.Data<>(elapsedPoint, ratioPoint));
            speedSeries.add(new XYChart.Data<>(elapsedPoint, speedPoint));
            trimSeries(ratioSeries);
            trimSeries(speedSeries);
        });
    }

    public long elapsedMillis() {
        if (startNanos == 0) {
            return 0;
        }
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    public double getAverageSamplesPerSecond() {
        long elapsed = elapsedMillis();
        if (elapsed <= 0) {
            return 0;
        }
        return totalSamplesProcessed / (elapsed / 1000.0);
    }

    public double getPeakSamplesPerSecond() {
        return peakSamplesPerSecond;
    }

    private static void trimSeries(ObservableList<XYChart.Data<Number, Number>> series) {
        while (series.size() > 300) {
            series.remove(0);
        }
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        try {
            Platform.runLater(action);
        } catch (IllegalStateException ex) {
            action.run();
        }
    }
}
