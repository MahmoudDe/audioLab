package com.audiolab.service;

import com.audiolab.model.AudioMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/** Writes compressed .audc files and decompressed WAV exports. */
public final class AudioExportService {

    private final AudioIOService ioService = new AudioIOService();

    public void saveCompressed(File file, byte[] containerBytes) throws IOException {
        Files.write(file.toPath(), containerBytes);
    }

    public void saveDecompressedWav(File file, short[] samples, AudioMetadata metadata) throws IOException {
        ioService.writeWav(file, samples, metadata.audioFormat());
    }
}
