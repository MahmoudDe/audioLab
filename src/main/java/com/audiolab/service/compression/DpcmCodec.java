package com.audiolab.service.compression;

import com.audiolab.model.CompressionSettings;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CancellationException;

/** First-order DPCM with uniform quantization of sample differences. */
public final class DpcmCodec implements AudioCodec {

    @Override
    public byte[] encode(short[] samples, int channels, CompressionSettings settings,
                         ProgressCallback callback) {
        int levels = settings.getQuantizationLevels();
        double maxDiff = 65536.0;
        double step = maxDiff / levels;
        ByteArrayOutputStream out = new ByteArrayOutputStream(samples.length);
        int totalFrames = samples.length / channels;

        for (int frame = 0; frame < totalFrames; frame++) {
            if (frame % ProgressCallback.CHUNK_SIZE == 0 && callback.isCancelled()) {
                throw new CancellationException();
            }
            for (int ch = 0; ch < channels; ch++) {
                int idx = frame * channels + ch;
                short current = samples[idx];
                short previous = frame == 0 ? 0 : samples[(frame - 1) * channels + ch];
                int diff = current - previous;
                int quantized = quantize(diff, step, levels);
                out.write(quantized & 0xFF);
            }
            if (frame % ProgressCallback.CHUNK_SIZE == 0) {
                callback.onProgress(frame * channels, samples.length, out.size());
            }
        }
        callback.onProgress(samples.length, samples.length, out.size());
        return out.toByteArray();
    }

    @Override
    public short[] decode(byte[] payload, int channels, int sampleCount, short[] seedSamples,
                          CompressionSettings settings, ProgressCallback callback) {
        int levels = settings.getQuantizationLevels();
        double maxDiff = 65536.0;
        double step = maxDiff / levels;
        short[] output = new short[sampleCount];
        int totalFrames = sampleCount / channels;
        int payloadIndex = 0;

        for (int frame = 0; frame < totalFrames; frame++) {
            if (frame % ProgressCallback.CHUNK_SIZE == 0 && callback.isCancelled()) {
                throw new CancellationException();
            }
            for (int ch = 0; ch < channels; ch++) {
                int idx = frame * channels + ch;
                if (frame == 0) {
                    output[idx] = ch < seedSamples.length ? seedSamples[ch] : 0;
                    continue;
                }
                if (payloadIndex >= payload.length) {
                    break;
                }
                int quantized = payload[payloadIndex++] & 0xFF;
                int diff = dequantize(quantized, step, levels);
                int reconstructed = output[idx - channels] + diff;
                output[idx] = (short) clamp16(reconstructed);
            }
            if (frame % ProgressCallback.CHUNK_SIZE == 0) {
                callback.onProgress(frame * channels, sampleCount, payloadIndex);
            }
        }
        callback.onProgress(sampleCount, sampleCount, payload.length);
        return output;
    }

    private static int quantize(int diff, double step, int levels) {
        int index = (int) Math.round((diff + 32768.0) / step);
        return Math.max(0, Math.min(levels - 1, index));
    }

    private static int dequantize(int index, double step, int levels) {
        index = Math.max(0, Math.min(levels - 1, index));
        return (int) Math.round(index * step - 32768.0);
    }

    private static int clamp16(int value) {
        return Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
    }
}
