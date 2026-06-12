package com.audiolab.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public final class AudioSession {

    private final ObjectProperty<File> sourceFile = new SimpleObjectProperty<>();
    private final ObjectProperty<AudioMetadata> metadata = new SimpleObjectProperty<>();
    private final ObjectProperty<CompressionSettings> settings = new SimpleObjectProperty<>(new CompressionSettings());
    private final ObjectProperty<CompressionReport> lastReport = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<ProcessingState> processingState =
            new ReadOnlyObjectWrapper<>(ProcessingState.IDLE);

    private short[] originalSamples = new short[0];
    private short[] workingSamples = new short[0];
    private byte[] compressedPayload = new byte[0];
    private byte[] containerBytes = new byte[0];
    private AudioMetadata playbackMetadata;

    public ObjectProperty<File> sourceFileProperty() {
        return sourceFile;
    }

    public ObjectProperty<AudioMetadata> metadataProperty() {
        return metadata;
    }

    public ObjectProperty<CompressionSettings> settingsProperty() {
        return settings;
    }

    public ObjectProperty<CompressionReport> lastReportProperty() {
        return lastReport;
    }

    public ReadOnlyObjectProperty<ProcessingState> processingStateProperty() {
        return processingState.getReadOnlyProperty();
    }

    public boolean hasAudio() {
        return originalSamples.length > 0 || containerBytes.length > 0;
    }

    public boolean hasOriginalAudio() {
        return originalSamples.length > 0;
    }

    public boolean canPlay() {
        return workingSamples.length > 0;
    }

    public short[] originalSamples() {
        return originalSamples;
    }

    public short[] workingSamples() {
        return workingSamples;
    }

    public byte[] compressedPayload() {
        return compressedPayload;
    }

    public byte[] containerBytes() {
        return containerBytes;
    }

    public Optional<File> sourceFile() {
        return Optional.ofNullable(sourceFile.get());
    }

    public Optional<AudioMetadata> metadata() {
        return Optional.ofNullable(metadata.get());
    }

    public CompressionSettings settings() {
        CompressionSettings current = settings.get();
        return current != null ? current : new CompressionSettings();
    }

    public ProcessingState processingState() {
        return processingState.get();
    }

    public void setProcessingState(ProcessingState state) {
        processingState.set(state != null ? state : ProcessingState.IDLE);
    }

    public void open(File file, AudioMetadata meta, short[] samples) {
        sourceFile.set(file);
        metadata.set(meta);
        originalSamples = Arrays.copyOf(samples, samples.length);
        workingSamples = Arrays.copyOf(samples, samples.length);
        compressedPayload = new byte[0];
        containerBytes = new byte[0];
        playbackMetadata = null;
        lastReport.set(null);
        processingState.set(ProcessingState.IDLE);

        CompressionSettings s = settings.get();
        if (s == null) {
            s = new CompressionSettings();
            settings.set(s);
        }
        s.applyDefaultsFromMetadata(meta);
    }

    public void openFromAudc(File file, AudioMetadata meta, byte[] container, byte[] payload,
                             CompressionSettings containerSettings) {
        sourceFile.set(file);
        metadata.set(meta);
        originalSamples = new short[0];
        workingSamples = new short[0];
        compressedPayload = payload != null ? Arrays.copyOf(payload, payload.length) : new byte[0];
        containerBytes = container != null ? Arrays.copyOf(container, container.length) : new byte[0];
        playbackMetadata = null;
        lastReport.set(null);
        processingState.set(ProcessingState.IDLE);
        settings.set(containerSettings != null ? containerSettings.copy() : new CompressionSettings());
    }

    public void setWorkingSamples(short[] samples) {
        workingSamples = Arrays.copyOf(samples, samples.length);
    }

    public void setPlaybackMetadata(AudioMetadata meta) {
        playbackMetadata = meta;
    }

    public AudioMetadata playbackMetadata() {
        if (playbackMetadata != null) {
            return playbackMetadata;
        }
        AudioMetadata source = metadata.get();
        if (source == null) {
            throw new IllegalStateException("No audio metadata");
        }
        return source.forPlayback(workingSamples, source.sampleRate());
    }

    public void setCompressed(byte[] payload, byte[] container) {
        compressedPayload = payload != null ? Arrays.copyOf(payload, payload.length) : new byte[0];
        containerBytes = container != null ? Arrays.copyOf(container, container.length) : new byte[0];
    }

    public void resetToOriginal() {
        if (!hasAudio()) {
            return;
        }
        if (hasOriginalAudio()) {
            workingSamples = Arrays.copyOf(originalSamples, originalSamples.length);
            compressedPayload = new byte[0];
            containerBytes = new byte[0];
        } else {
            workingSamples = new short[0];
        }
        playbackMetadata = null;
        lastReport.set(null);
        processingState.set(ProcessingState.IDLE);
    }

    public void clear() {
        sourceFile.set(null);
        metadata.set(null);
        originalSamples = new short[0];
        workingSamples = new short[0];
        compressedPayload = new byte[0];
        containerBytes = new byte[0];
        playbackMetadata = null;
        lastReport.set(null);
        processingState.set(ProcessingState.IDLE);
    }
}
