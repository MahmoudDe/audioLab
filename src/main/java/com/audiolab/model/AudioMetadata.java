package com.audiolab.model;

import javax.sound.sampled.AudioFormat;

import java.io.File;

/** Metadata extracted from a loaded audio file. */
public record AudioMetadata(
        String fileName,
        String filePath,
        long fileSizeBytes,
        double durationSeconds,
        float sampleRate,
        int channels,
        int bitDepth,
        int bitRate,
        String encodingType,
        AudioFormat audioFormat
) {
    public static AudioMetadata fromFile(File file, AudioFormat format, long frameCount) {
        int channels = format.getChannels();
        int bitDepth = format.getSampleSizeInBits();
        float sampleRate = format.getSampleRate();
        double duration = frameCount / sampleRate;
        int bitRate = (int) (sampleRate * channels * bitDepth);
        String encoding = describeEncoding(format);

        return new AudioMetadata(
                file.getName(),
                file.getAbsolutePath(),
                file.length(),
                duration,
                sampleRate,
                channels,
                bitDepth,
                bitRate,
                encoding,
                format
        );
    }

    private static String describeEncoding(AudioFormat format) {
        AudioFormat.Encoding enc = format.getEncoding();
        if (AudioFormat.Encoding.PCM_SIGNED.equals(enc)) {
            return "PCM Signed";
        }
        if (AudioFormat.Encoding.PCM_UNSIGNED.equals(enc)) {
            return "PCM Unsigned";
        }
        if (AudioFormat.Encoding.ALAW.equals(enc)) {
            return "A-Law";
        }
        if (AudioFormat.Encoding.ULAW.equals(enc)) {
            return "μ-Law";
        }
        return enc != null ? enc.toString() : "Unknown";
    }

    public String formattedSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        }
        if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        }
        return String.format("%.2f MB", fileSizeBytes / (1024.0 * 1024.0));
    }

    public String formattedDuration() {
        int totalSeconds = (int) Math.round(durationSeconds);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
