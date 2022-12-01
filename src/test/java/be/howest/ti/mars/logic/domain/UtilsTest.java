package be.howest.ti.mars.logic.domain;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.function.Function;

class UtilsTest {
    /// Utility method to not test the type getter.
    private final Function<JsonObject, String> empty = v -> null;

    @Test
    void getOrThrowInvalidData() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Utils.getOrThrow(null, "key", empty));
    }

    @Test
    void getOrThrowInvalidKey() {
        JsonObject data = new JsonObject();

        Assertions.assertThrows(IllegalArgumentException.class, () -> Utils.getOrThrow(data, null, empty));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Utils.getOrThrow(data, "", empty));
    }

    @Test
    void getOrThrowMissingKey() {
        JsonObject data = new JsonObject();
        data.put("key", "value");

        Assertions.assertThrows(IllegalArgumentException.class, () -> Utils.getOrThrow(data, "missing", empty));
        Assertions.assertDoesNotThrow(() -> Utils.getOrThrow(data, "key", empty));
    }

    @Test
    void getOrThrowString() {
        JsonObject data = new JsonObject();
        data.put("key", "value");

        Assertions.assertEquals("value", Utils.getOrThrowString( data, "key"));
    }

    @Test
    void getOrThrowInteger() {
        JsonObject data = new JsonObject();
        data.put("meaning of life", 42);

        Assertions.assertEquals(42, Utils.getOrThrowInt( data, "meaning of life"));
    }
}
