package be.howest.ti.mars.logic.domain.response;

import io.vertx.core.json.JsonObject;

import java.util.Map;

public class DataEventResponse extends EventResponse {
    public DataEventResponse(String type, String data) {
        super(type, Map.of("data", data));
    }

    public DataEventResponse(String type, String data, String id) {
        super(type, Map.of("data", data, "id", id));
    }

    public DataEventResponse(String type, JsonObject data) {
        this(type, data.encode());
    }

    public DataEventResponse(String type, JsonObject data, String id) {
        this(type, data.encode(), id);
    }
}
