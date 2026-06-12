package com.audiolab.io;

import java.io.File;
import java.util.Locale;

public final class SupportedAudioFormats {

    private static final String[] IMPORT = {
            ".wav", ".mp3", ".flac", ".aiff", ".aif", ".aifc", ".au", ".snd"
    };
    private static final String AUDC = ".audc";

    private SupportedAudioFormats() {}

    public static boolean isImportable(File file) {
        return matches(file, IMPORT);
    }

    public static boolean isAudc(File file) {
        return matches(file, AUDC);
    }

    public static boolean isSupported(File file) {
        return isImportable(file) || isAudc(file);
    }

    public static String[] importGlobs() {
        return globsFor(IMPORT);
    }

    public static String[] allGlobs() {
        String[] all = new String[IMPORT.length + 1];
        System.arraycopy(IMPORT, 0, all, 0, IMPORT.length);
        all[IMPORT.length] = AUDC;
        return globsFor(all);
    }

    private static String[] globsFor(String... extensions) {
        String[] globs = new String[extensions.length * 2];
        int i = 0;
        for (String ext : extensions) {
            globs[i++] = "*" + ext;
            globs[i++] = "*" + ext.toUpperCase(Locale.ROOT);
        }
        return globs;
    }

    private static boolean matches(File file, String... extensions) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String ext : extensions) {
            if (name.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
