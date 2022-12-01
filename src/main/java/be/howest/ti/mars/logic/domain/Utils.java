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

        String argumentExceptionMessage;

        if (data.containsKey(key)) {
            try {
                return getter.apply(data);
            } catch (ClassCastException e) {
                argumentExceptionMessage = String.format("Invalid type for key '%s' in data", key);
            }
        } else {
            argumentExceptionMessage = String.format("Missing key '%s' in data", key);
        }

        throw new IllegalArgumentException(argumentExceptionMessage);
    }

    public static String getOrThrowString(JsonObject data, String key) {
        return Utils.getOrThrow(data, key, d -> d.getString(key));
    }

    public static int getOrThrowInt(JsonObject data, String key) {
        return Utils.getOrThrow(data, key, d -> d.getInteger(key));
    }
}
