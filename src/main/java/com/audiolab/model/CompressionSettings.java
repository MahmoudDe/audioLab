package com.audiolab.model;

/** User-configurable compression parameters. */
public final class CompressionSettings {

    private CompressionAlgorithm algorithm = CompressionAlgorithm.DPCM;
    private int targetSampleRate = 44100;
    private int quantizationLevels = 64;
    private int stepSize = 512;
    private int initialStepSize = 512;
    private int minStepSize = 64;
    private int maxStepSize = 4096;
    private double adaptationFactor = 1.5;

    public CompressionAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(CompressionAlgorithm algorithm) {
        this.algorithm = algorithm != null ? algorithm : CompressionAlgorithm.DPCM;
    }

    public int getTargetSampleRate() {
        return targetSampleRate;
    }

    public void setTargetSampleRate(int targetSampleRate) {
        this.targetSampleRate = Math.max(8000, targetSampleRate);
    }

    public int getQuantizationLevels() {
        return quantizationLevels;
    }

    public void setQuantizationLevels(int quantizationLevels) {
        this.quantizationLevels = Math.max(4, Math.min(256, quantizationLevels));
    }

    public int getStepSize() {
        return stepSize;
    }

    public void setStepSize(int stepSize) {
        this.stepSize = Math.max(16, stepSize);
    }

    public int getInitialStepSize() {
        return initialStepSize;
    }

    public void setInitialStepSize(int initialStepSize) {
        this.initialStepSize = Math.max(16, initialStepSize);
    }

    public int getMinStepSize() {
        return minStepSize;
    }

    public void setMinStepSize(int minStepSize) {
        this.minStepSize = Math.max(8, minStepSize);
    }

    public int getMaxStepSize() {
        return maxStepSize;
    }

    public void setMaxStepSize(int maxStepSize) {
        this.maxStepSize = Math.max(64, maxStepSize);
    }

    public double getAdaptationFactor() {
        return adaptationFactor;
    }

    public void setAdaptationFactor(double adaptationFactor) {
        this.adaptationFactor = Math.max(1.1, Math.min(3.0, adaptationFactor));
    }

    public CompressionSettings copy() {
        CompressionSettings copy = new CompressionSettings();
        copy.algorithm = algorithm;
        copy.targetSampleRate = targetSampleRate;
        copy.quantizationLevels = quantizationLevels;
        copy.stepSize = stepSize;
        copy.initialStepSize = initialStepSize;
        copy.minStepSize = minStepSize;
        copy.maxStepSize = maxStepSize;
        copy.adaptationFactor = adaptationFactor;
        return copy;
    }

    public void applyDefaultsFromMetadata(AudioMetadata metadata) {
        if (metadata == null) {
            return;
        }
        targetSampleRate = (int) metadata.sampleRate();
    }

    public String summary() {
        return switch (algorithm) {
            case DPCM -> String.format("DPCM, rate=%d Hz, levels=%d",
                    targetSampleRate, quantizationLevels);
            case DELTA_MODULATION -> String.format("Delta Modulation, rate=%d Hz, step=%d",
                    targetSampleRate, stepSize);
            case ADAPTIVE_DELTA_MODULATION -> String.format(
                    "ADM, rate=%d Hz, step=%d..%d (init=%d, factor=%.1f)",
                    targetSampleRate, minStepSize, maxStepSize, initialStepSize, adaptationFactor);
        };
    }
}
