package com.audiolab.service;

import com.audiolab.model.AudioMetadata;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Loads WAV audio and converts to 16-bit PCM samples. */
public final class AudioIOService {

    public record LoadedAudio(AudioMetadata metadata, short[] samples) {}

    public LoadedAudio load(File file) throws IOException, UnsupportedAudioFileException {
        try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(file)) {
            AudioFormat sourceFormat = sourceStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false
            );

            AudioInputStream pcmStream = sourceFormat.matches(targetFormat)
                    ? sourceStream
                    : AudioSystem.getAudioInputStream(targetFormat, sourceStream);

            byte[] pcmBytes = pcmStream.readAllBytes();
            short[] samples = bytesToShorts(pcmBytes);
            long frameCount = samples.length / targetFormat.getChannels();
            AudioMetadata metadata = AudioMetadata.fromFile(file, targetFormat, frameCount);
            return new LoadedAudio(metadata, samples);
        }
    }

    public byte[] samplesToPcmBytes(short[] samples) {
        ByteBuffer buffer = ByteBuffer.allocate(samples.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (short sample : samples) {
            buffer.putShort(sample);
        }
        return buffer.array();
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

    private static short[] bytesToShorts(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int count = bytes.length / 2;
        short[] samples = new short[count];
        for (int i = 0; i < count; i++) {
            samples[i] = buffer.getShort();
        }
        return samples;
    }
}
