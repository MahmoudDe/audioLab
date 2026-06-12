package com.audiolab.service;

import com.audiolab.model.AudioMetadata;

import java.io.File;
import java.io.IOException;

public final class AudioExportService {

    private final AudioIOService io = new AudioIOService();

    public void saveCompressed(File file, byte[] containerBytes) throws IOException {
        io.saveAudc(file, containerBytes);
    }

    public void saveDecompressedWav(File file, short[] samples, AudioMetadata metadata) throws IOException {
        io.writeWav(file, samples, metadata.audioFormat());
    }
}
