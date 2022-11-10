package be.howest.ti.mars.logic.domain.response;

import io.vertx.core.json.JsonObject;


public class DataEventResponse extends EventResponse {
    public DataEventResponse(String type, JsonObject data) {
        super(type, data.getMap());
    }
}
