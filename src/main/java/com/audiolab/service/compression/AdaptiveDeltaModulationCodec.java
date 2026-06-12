package com.audiolab.service.compression;

import com.audiolab.model.CompressionSettings;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CancellationException;
public final class AdaptiveDeltaModulationCodec implements AudioCodec {

    @Override
    public byte[] encode(short[] samples, int channels, CompressionSettings settings,
                         ProgressCallback callback) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bitBuffer = 0;
        int bitCount = 0;
        int totalFrames = samples.length / channels;
        int[] integrator = new int[channels];
        int[] step = new int[channels];
        int[] lastBit = new int[channels];
        boolean[] hasLastBit = new boolean[channels];

        for (int ch = 0; ch < channels; ch++) {
            step[ch] = settings.getInitialStepSize();
            lastBit[ch] = 0;
            hasLastBit[ch] = false;
        }

        for (int frame = 0; frame < totalFrames; frame++) {
            if (frame % ProgressCallback.CHUNK_SIZE == 0 && callback.isCancelled()) {
                throw new CancellationException();
            }
            for (int ch = 0; ch < channels; ch++) {
                int idx = frame * channels + ch;
                short target = samples[idx];
                if (frame == 0) {
                    integrator[ch] = target;
                    continue;
                }
                int bit = target >= integrator[ch] ? 1 : 0;
                integrator[ch] += bit == 1 ? step[ch] : -step[ch];
                integrator[ch] = clamp16(integrator[ch]);

                if (hasLastBit[ch] && bit == lastBit[ch]) {
                    step[ch] = (int) Math.min(settings.getMaxStepSize(), step[ch] * settings.getAdaptationFactor());
                } else {
                    step[ch] = (int) Math.max(settings.getMinStepSize(), step[ch] / settings.getAdaptationFactor());
                }
                lastBit[ch] = bit;
                hasLastBit[ch] = true;

                bitBuffer = (bitBuffer << 1) | bit;
                bitCount++;
                if (bitCount == 8) {
                    out.write(bitBuffer);
                    bitBuffer = 0;
                    bitCount = 0;
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
        short[] output = new short[sampleCount];
        int totalFrames = sampleCount / channels;
        int[] integrator = new int[channels];
        int[] step = new int[channels];
        int[] lastBit = new int[channels];
        boolean[] hasLastBit = new boolean[channels];

        for (int ch = 0; ch < channels; ch++) {
            integrator[ch] = ch < seedSamples.length ? seedSamples[ch] : 0;
            step[ch] = settings.getInitialStepSize();
            hasLastBit[ch] = false;
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
                int bit = DeltaModulationCodec.readBit(payload, bitIndex++);
                integrator[ch] += bit == 1 ? step[ch] : -step[ch];
                integrator[ch] = clamp16(integrator[ch]);
                output[idx] = (short) integrator[ch];

                if (hasLastBit[ch] && bit == lastBit[ch]) {
                    step[ch] = (int) Math.min(settings.getMaxStepSize(), step[ch] * settings.getAdaptationFactor());
                } else {
                    step[ch] = (int) Math.max(settings.getMinStepSize(), step[ch] / settings.getAdaptationFactor());
                }
                lastBit[ch] = bit;
                hasLastBit[ch] = true;
            }
            if (frame % ProgressCallback.CHUNK_SIZE == 0) {
                callback.onProgress(frame * channels, sampleCount, bitIndex / 8);
            }
        }
        callback.onProgress(sampleCount, sampleCount, payload.length);
        return output;
    }

    private static int clamp16(int value) {
        return Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
    }
}
