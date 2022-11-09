package be.howest.ti.mars.logic.domain;

import io.vertx.core.json.JsonObject;

public class Utils {
    public static String getOrThrow(JsonObject data, String key) {
        if (data.containsKey(key)) {
            return data.getString(key);
        } else {
            throw new IllegalArgumentException("Missing key on data: " + key);
        }
    }
}
