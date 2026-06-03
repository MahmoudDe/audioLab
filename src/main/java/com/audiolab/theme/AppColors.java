package com.audiolab.theme;

import javafx.scene.paint.Color;

/** Design tokens for AudioLab (light theme). */
public final class AppColors {

    public static final Color BACKGROUND = Color.web("#f8fafc");
    public static final Color BACKGROUND_ALT = Color.web("#ffffff");
    public static final Color SURFACE = Color.web("#ffffff");
    public static final Color SURFACE_ELEVATED = Color.web("#f1f5f9");
    public static final Color CARD = Color.web("#ffffff");
    public static final Color CARD_HOVER = Color.web("#f1f5f9");
    public static final Color BORDER = Color.web("#e2e8f0");
    public static final Color BORDER_SUBTLE = Color.web("#f1f5f9");
    public static final Color TEXT_PRIMARY = Color.web("#0f172a");
    public static final Color TEXT_MUTED = Color.web("#64748b");
    public static final Color TEXT_DIM = Color.web("#94a3b8");
    public static final Color ACCENT = Color.web("#2563eb");
    public static final Color ACCENT_STRONG = Color.web("#1d4ed8");
    public static final Color ACCENT_SOFT = Color.web("#dbeafe");
    public static final Color SUCCESS = Color.web("#16a34a");
    public static final Color WARNING = Color.web("#d97706");
    public static final Color ERROR = Color.web("#dc2626");
    public static final Color WAVEFORM = Color.web("#2563eb");
    public static final Color WAVEFORM_BG = Color.web("#f1f5f9");

    private AppColors() {}

    public static String toHex(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
