package be.howest.ti.mars.logic.domain;

import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class Utils {
    private Utils() {
    }

    private static <T> T getOrThrow(JsonObject data, String key, Function<JsonObject, T> getter) {
        if (data.containsKey(key)) {
            return getter.apply(data);
        } else {
            throw new IllegalArgumentException("Missing key: " + key);
        }
    }

    public static String getOrThrowString(JsonObject data, String key) {
        return Utils.getOrThrow(data, key, d -> d.getString(key));
    }

    public static int getOrThrowInt(JsonObject data, String key) {
        return Utils.getOrThrow(data, key, d -> d.getInteger(key));
    }
}
