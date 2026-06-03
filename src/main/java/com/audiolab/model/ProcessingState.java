package com.audiolab.model;

/** Session processing lifecycle. */
public enum ProcessingState {
    IDLE,
    COMPRESSING,
    DECOMPRESSING,
    CANCELLED
}
