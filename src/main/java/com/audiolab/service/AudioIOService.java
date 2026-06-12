package com.audiolab.service;

import com.audiolab.io.SupportedAudioFormats;
import com.audiolab.model.AudioMetadata;
import com.audiolab.service.compression.AudcContainer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

public final class AudioIOService {

    public record LoadedAudio(AudioMetadata metadata, short[] samples) {}

    public record LoadedAudc(AudioMetadata metadata, AudcContainer.Parsed parsed, byte[] containerBytes) {}

    public LoadedAudio load(File file) throws IOException, UnsupportedAudioFileException {
        if (!SupportedAudioFormats.isImportable(file)) {
            throw new UnsupportedAudioFileException("Unsupported file type: " + file.getName());
        }
        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(file)) {
            AudioFormat sourceFormat = sourceStream.getFormat();
            AudioFormat pcmFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false);

            AudioInputStream pcmStream = sourceFormat.matches(pcmFormat)
                    ? sourceStream
                    : AudioSystem.getAudioInputStream(pcmFormat, sourceStream);

            byte[] pcmBytes = pcmStream.readAllBytes();
            short[] samples = bytesToShorts(pcmBytes);
            long frameCount = samples.length / pcmFormat.getChannels();
            return new LoadedAudio(AudioMetadata.fromFile(file, pcmFormat, frameCount), samples);
        }
    }

    public LoadedAudc loadAudc(File file) throws IOException {
        byte[] containerBytes = Files.readAllBytes(file.toPath());
        AudcContainer.Parsed parsed = AudcContainer.parse(containerBytes);
        AudioMetadata metadata = AudioMetadata.fromAudc(file, parsed.header(), containerBytes.length);
        return new LoadedAudc(metadata, parsed, containerBytes);
    }

    public void saveAudc(File file, byte[] containerBytes) throws IOException {
        Files.write(file.toPath(), containerBytes);
    }

    public void writeWav(File file, short[] samples, AudioFormat format) throws IOException {
        byte[] pcm = samplesToPcmBytes(samples);
        try (AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(pcm),
                format,
                samples.length / format.getChannels())) {
            AudioSystem.write(ais, javax.sound.sampled.AudioFileFormat.Type.WAVE, file);
        }
    }

    public byte[] samplesToPcmBytes(short[] samples) {
        ByteBuffer buffer = ByteBuffer.allocate(samples.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (short sample : samples) {
            buffer.putShort(sample);
        }
        return buffer.array();
    }

    private static short[] bytesToShorts(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        short[] samples = new short[bytes.length / 2];
        for (int i = 0; i < samples.length; i++) {
            samples[i] = buffer.getShort();
        }
        return samples;
    }
}
