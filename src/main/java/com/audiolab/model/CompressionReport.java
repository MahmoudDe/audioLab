package com.audiolab.model;
public record CompressionReport(
        long originalSizeBytes,
        long compressedSizeBytes,
        double savingsPercent,
        long elapsedMillis,
        CompressionAlgorithm algorithm,
        CompressionSettings settings,
        double averageSamplesPerSecond,
        double peakSamplesPerSecond
) {
    public String formattedOriginalSize() {
        return formatBytes(originalSizeBytes);
    }

    public String formattedCompressedSize() {
        return formatBytes(compressedSizeBytes);
    }

    public String formattedElapsed() {
        if (elapsedMillis < 1000) {
            return elapsedMillis + " ms";
        }
        return String.format("%.2f s", elapsedMillis / 1000.0);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
