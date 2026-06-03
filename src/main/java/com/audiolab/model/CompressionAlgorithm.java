package com.audiolab.model;

/** Supported compression algorithms for the assignment. */
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
