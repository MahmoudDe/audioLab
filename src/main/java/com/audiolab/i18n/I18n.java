package com.audiolab.i18n;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
public final class I18n {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static Map<String, String> strings = Map.of();

    private I18n() {}

    public static void load(Locale locale) {
        String lang = locale != null ? locale.getLanguage() : "en";
        Map<String, String> loaded = readBundle("strings_" + lang + ".json");
        if (loaded.isEmpty() && !"en".equals(lang)) {
            loaded = readBundle("strings_en.json");
        }
        strings = Collections.unmodifiableMap(new HashMap<>(loaded));
    }

    public static String get(String key) {
        return strings.getOrDefault(key, key);
    }

    public static String get(String key, Object... args) {
        return String.format(get(key), args);
    }

    private static Map<String, String> readBundle(String fileName) {
        String path = "/i18n/" + fileName;
        try (InputStream in = I18n.class.getResourceAsStream(path)) {
            if (in == null) {
                return Map.of();
            }
            Map<String, String> map = GSON.fromJson(
                    new InputStreamReader(in, StandardCharsets.UTF_8), MAP_TYPE);
            return Objects.requireNonNullElse(map, Map.of());
        } catch (Exception e) {
            return Map.of();
        }
    }
}
