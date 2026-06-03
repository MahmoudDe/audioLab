package com.audiolab.theme;

import javafx.scene.paint.Color;

/** Design tokens for AudioLab (Tokyo Night–inspired). */
public final class AppColors {

    public static final Color BACKGROUND = Color.web("#0d1b2a");
    public static final Color BACKGROUND_ALT = Color.web("#1a2633");
    public static final Color SURFACE = Color.web("#263849");
    public static final Color SURFACE_ELEVATED = Color.web("#2f4a66");
    public static final Color CARD = Color.web("#3d5a80");
    public static final Color CARD_HOVER = Color.web("#4a6fa5");
    public static final Color BORDER = Color.web("#5a7fa6");
    public static final Color BORDER_SUBTLE = Color.web("#3d5a80");
    public static final Color TEXT_PRIMARY = Color.web("#e6f3ff");
    public static final Color TEXT_MUTED = Color.web("#a8d5ff");
    public static final Color TEXT_DIM = Color.web("#5a8ac4");
    public static final Color ACCENT = Color.web("#3b9eff");
    public static final Color ACCENT_STRONG = Color.web("#1e7fd9");
    public static final Color ACCENT_SOFT = Color.web("#61dafb");
    public static final Color SUCCESS = Color.web("#52b788");
    public static final Color WARNING = Color.web("#ffa630");
    public static final Color ERROR = Color.web("#ff6b6b");
    public static final Color WAVEFORM = Color.web("#3b9eff");
    public static final Color WAVEFORM_BG = Color.web("#1a2633");

    private AppColors() {}

    public static String toHex(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
