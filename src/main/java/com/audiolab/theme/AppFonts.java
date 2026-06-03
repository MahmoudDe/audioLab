package com.audiolab.theme;

import javafx.scene.text.Font;

import java.util.Objects;

/** Loads bundled UI fonts used across the application. */
public final class AppFonts {

    public static final String FAMILY = "Noto Sans";

    public static void load() {
        loadFont("/fonts/NotoSans-Regular.ttf");
        loadFont("/fonts/NotoSans-Bold.ttf");
    }

    private static void loadFont(String resourcePath) {
        Font.loadFont(Objects.requireNonNull(
                AppFonts.class.getResourceAsStream(resourcePath),
                "Missing font resource: " + resourcePath), 14);
    }

    private AppFonts() {}
}
