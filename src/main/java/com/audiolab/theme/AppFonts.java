package com.audiolab.theme;

import javafx.scene.text.Font;

import java.util.Objects;

public final class AppFonts {

    public static void load() {
        load("/fonts/NotoSans-Regular.ttf");
        load("/fonts/NotoSans-Bold.ttf");
    }

    private static void load(String path) {
        Font.loadFont(Objects.requireNonNull(
                AppFonts.class.getResourceAsStream(path), "Missing font: " + path), 14);
    }

    private AppFonts() {}
}
