package com.audiolab.service.compression;

import com.audiolab.model.CompressionSettings;

/** Encode/decode contract for audio compression algorithms. */
public interface AudioCodec {

    byte[] encode(short[] samples, int channels, CompressionSettings settings, ProgressCallback callback);

    short[] decode(byte[] payload, int channels, int sampleCount, short[] seedSamples,
                   CompressionSettings settings, ProgressCallback callback);
}
