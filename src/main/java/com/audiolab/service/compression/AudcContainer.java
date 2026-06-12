package com.audiolab.service.compression;

import com.audiolab.model.CompressionAlgorithm;
import com.audiolab.model.CompressionSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
public final class AudcContainer {

    private static final byte[] MAGIC = "AUDC".getBytes(StandardCharsets.US_ASCII);
    private static final int VERSION = 1;

    private AudcContainer() {}

    public record Header(
            CompressionAlgorithm algorithm,
            CompressionSettings settings,
            int channels,
            int bitDepth,
            int sampleCount,
            short[] seedSamples
    ) {}

    public static byte[] pack(Header header, byte[] payload) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        dos.write(MAGIC);
        dos.writeByte(VERSION);
        dos.writeByte(header.algorithm().ordinal());
        dos.writeInt(header.settings().getTargetSampleRate());
        dos.writeInt(header.channels());
        dos.writeInt(header.bitDepth());
        dos.writeInt(header.sampleCount());
        dos.writeInt(header.settings().getQuantizationLevels());
        dos.writeInt(header.settings().getStepSize());
        dos.writeInt(header.settings().getInitialStepSize());
        dos.writeInt(header.settings().getMinStepSize());
        dos.writeInt(header.settings().getMaxStepSize());
        dos.writeDouble(header.settings().getAdaptationFactor());
        for (short seed : header.seedSamples()) {
            dos.writeShort(seed);
        }
        dos.writeInt(payload.length);
        dos.write(payload);
        dos.flush();
        return out.toByteArray();
    }

    public static Parsed parse(byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        byte[] magic = in.readNBytes(4);
        if (!Arrays.equals(magic, MAGIC)) {
            throw new IOException("Invalid AUDC magic");
        }
        int version = in.readUnsignedByte();
        if (version != VERSION) {
            throw new IOException("Unsupported AUDC version: " + version);
        }
        CompressionAlgorithm algorithm = CompressionAlgorithm.values()[in.readUnsignedByte()];
        CompressionSettings settings = new CompressionSettings();
        settings.setAlgorithm(algorithm);
        settings.setTargetSampleRate(in.readInt());
        int channels = in.readInt();
        int bitDepth = in.readInt();
        int sampleCount = in.readInt();
        settings.setQuantizationLevels(in.readInt());
        settings.setStepSize(in.readInt());
        settings.setInitialStepSize(in.readInt());
        settings.setMinStepSize(in.readInt());
        settings.setMaxStepSize(in.readInt());
        settings.setAdaptationFactor(in.readDouble());
        short[] seed = new short[channels];
        for (int i = 0; i < channels; i++) {
            seed[i] = in.readShort();
        }
        int payloadLen = in.readInt();
        byte[] payload = in.readNBytes(payloadLen);
        Header header = new Header(algorithm, settings, channels, bitDepth, sampleCount, seed);
        return new Parsed(header, payload);
    }

    public record Parsed(Header header, byte[] payload) {}
}
