package com.audiolab.model;

import com.audiolab.service.compression.AudcContainer;

import javax.sound.sampled.AudioFormat;

import java.io.File;

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
    public static AudioMetadata fromAudc(File file, AudcContainer.Header header, long containerSizeBytes) {
        float sampleRate = header.settings().getTargetSampleRate();
        int channels = header.channels();
        int bitDepth = header.bitDepth();
        int frames = header.sampleCount() / Math.max(1, channels);
        double duration = frames / sampleRate;
        int bytesPerFrame = channels * (bitDepth / 8);
        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                bitDepth,
                channels,
                bytesPerFrame,
                sampleRate,
                false);
        return new AudioMetadata(
                file.getName(),
                file.getAbsolutePath(),
                containerSizeBytes,
                duration,
                sampleRate,
                channels,
                bitDepth,
                (int) (sampleRate * channels * bitDepth),
                "AUDC / " + header.algorithm().name(),
                format);
    }

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

    public AudioMetadata forPlayback(short[] samples, float playbackSampleRate) {
        int frames = samples.length / Math.max(1, channels);
        double duration = frames / playbackSampleRate;
        int bytesPerFrame = channels * (bitDepth / 8);
        AudioFormat playbackFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                playbackSampleRate,
                bitDepth,
                channels,
                bytesPerFrame,
                playbackSampleRate,
                false);
        int playbackBitRate = (int) (playbackSampleRate * channels * bitDepth);
        return new AudioMetadata(
                fileName,
                filePath,
                fileSizeBytes,
                duration,
                playbackSampleRate,
                channels,
                bitDepth,
                playbackBitRate,
                encodingType,
                playbackFormat);
    }
}
