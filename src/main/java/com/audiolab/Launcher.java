package com.audiolab;

import javafx.application.Application;

/** JVM entry point; delegates to JavaFX {@link AudioLabApplication}. */
public final class Launcher {

    public static void main(String[] args) {
        Application.launch(AudioLabApplication.class, args);
    }

    private Launcher() {}
}
