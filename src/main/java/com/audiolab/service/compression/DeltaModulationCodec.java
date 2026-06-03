package com.audiolab.service.compression;

import com.audiolab.model.CompressionSettings;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CancellationException;

/** Fixed step-size delta modulation (1 bit per sample). */
public final class DeltaModulationCodec implements AudioCodec {

    @Override
    public byte[] encode(short[] samples, int channels, CompressionSettings settings,
                         ProgressCallback callback) {
        int step = settings.getStepSize();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bitBuffer = 0;
        int bitCount = 0;
        int totalFrames = samples.length / channels;
        int[] integrator = new int[channels];

        for (int frame = 0; frame < totalFrames; frame++) {
            if (frame % ProgressCallback.CHUNK_SIZE == 0 && callback.isCancelled()) {
                throw new CancellationException();
            }
            for (int ch = 0; ch < channels; ch++) {
                int idx = frame * channels + ch;
                short target = samples[idx];
                if (frame == 0) {
                    integrator[ch] = target;
                } else {
                    int bit = target >= integrator[ch] ? 1 : 0;
                    integrator[ch] += bit == 1 ? step : -step;
                    integrator[ch] = clamp16(integrator[ch]);
                    bitBuffer = (bitBuffer << 1) | bit;
                    bitCount++;
                    if (bitCount == 8) {
                        out.write(bitBuffer);
                        bitBuffer = 0;
                        bitCount = 0;
                    }
                }
            }
            if (frame % ProgressCallback.CHUNK_SIZE == 0) {
                callback.onProgress(frame * channels, samples.length, out.size());
            }
        }
        if (bitCount > 0) {
            bitBuffer <<= (8 - bitCount);
            out.write(bitBuffer);
        }
        callback.onProgress(samples.length, samples.length, out.size());
        return out.toByteArray();
    }

    @Override
    public short[] decode(byte[] payload, int channels, int sampleCount, short[] seedSamples,
                          CompressionSettings settings, ProgressCallback callback) {
        int step = settings.getStepSize();
        short[] output = new short[sampleCount];
        int totalFrames = sampleCount / channels;
        int[] integrator = new int[channels];
        for (int ch = 0; ch < channels; ch++) {
            integrator[ch] = ch < seedSamples.length ? seedSamples[ch] : 0;
        }

        int bitIndex = 0;
        for (int frame = 0; frame < totalFrames; frame++) {
            if (frame % ProgressCallback.CHUNK_SIZE == 0 && callback.isCancelled()) {
                throw new CancellationException();
            }
            for (int ch = 0; ch < channels; ch++) {
                int idx = frame * channels + ch;
                if (frame == 0) {
                    output[idx] = (short) integrator[ch];
                    continue;
                }
                int bit = readBit(payload, bitIndex++);
                integrator[ch] += bit == 1 ? step : -step;
                integrator[ch] = clamp16(integrator[ch]);
                output[idx] = (short) integrator[ch];
            }
            if (frame % ProgressCallback.CHUNK_SIZE == 0) {
                callback.onProgress(frame * channels, sampleCount, bitIndex / 8);
            }
        }
        callback.onProgress(sampleCount, sampleCount, payload.length);
        return output;
    }

    static int readBit(byte[] payload, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitOffset = 7 - (bitIndex % 8);
        if (byteIndex >= payload.length) {
            return 0;
        }
        return (payload[byteIndex] >> bitOffset) & 1;
    }

    private static int clamp16(int value) {
        return Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
    }
}
