package com.audiolab.service.compression;
public interface ProgressCallback {

    int CHUNK_SIZE = 4096;

    void onProgress(int processedSamples, int totalSamples, long compressedBytes);

    boolean isCancelled();

    static ProgressCallback noop() {
        return new ProgressCallback() {
            @Override
            public void onProgress(int processedSamples, int totalSamples, long compressedBytes) {}

            @Override
            public boolean isCancelled() {
                return false;
            }
        };
    }
}
