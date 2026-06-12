package com.audiolab.model;
public enum CompressionAlgorithm {
    DPCM,
    DELTA_MODULATION,
    ADAPTIVE_DELTA_MODULATION;

    public String i18nKey() {
        return switch (this) {
            case DPCM -> "algorithm.dpcm";
            case DELTA_MODULATION -> "algorithm.deltaModulation";
            case ADAPTIVE_DELTA_MODULATION -> "algorithm.adaptiveDeltaModulation";
        };
    }
}
