package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class Properties {
    private Properties() {
    }

    public static SocketResponse addProperty(JsonObject data) {
        return new ErrorEventResponse("Not implemented yet");
    }

    public static SocketResponse removeProperty(JsonObject data) {
        return new ErrorEventResponse("Not implemented yet");
    }

    public static SocketResponse getAllowedUsers(JsonObject data) {
        Repositories.getH2Repo().getAllowedUsers(data.getString("propertyId"));
    }
}
