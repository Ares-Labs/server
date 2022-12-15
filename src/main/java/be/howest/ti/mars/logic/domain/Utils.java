package be.howest.ti.mars.logic.domain;

import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class Utils {
    private Utils() {
    }

    protected static <T> T getOrThrow(JsonObject data, String key, Function<JsonObject, T> getter) {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        } else if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        if (data.containsKey(key)) {
            try {
                return getter.apply(data);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(String.format("Invalid type for key '%s' in data", key));
            }
        }

        throw new IllegalArgumentException(String.format("Missing key '%s' in data", key));
    }

    public static String getOrThrowString(JsonObject data, String key) {
        return Utils.getOrThrow(data, key, d -> d.getString(key));
    }

    public static int getOrThrowInt(JsonObject data, String key) {
        return Utils.getOrThrow(data, key, d -> d.getInteger(key));
    }

    protected static <T> T getOrDefault(JsonObject data, String key, Function<JsonObject, T> getter, T defaultValue) {
        try {
            return Utils.getOrThrow(data, key, getter);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public static String getOrDefaultString(JsonObject data, String key, String defaultValue) {
        return Utils.getOrDefault(data, key, d -> d.getString(key), defaultValue);
    }

    public static int getOrDefaultInt(JsonObject data, String key, int defaultValue) {
        return Utils.getOrDefault(data, key, d -> d.getInteger(key), defaultValue);
    }
}
