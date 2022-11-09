package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class Properties {
    private Properties() {
    }

    public static SocketResponse addProperty(JsonObject data) {
        try {
            MarsH2Repository repo = Repositories.getH2Repo();

            String location = Utils.getOrThrow(data, "location");
            String name = Utils.getOrThrow(data, "name");
            repo.insertProperty();
        }
        return new ErrorEventResponse("Not implemented yet");
    }

    public static SocketResponse removeProperty(JsonObject data) {
        return new ErrorEventResponse("Not implemented yet");
    }

    public static SocketResponse getProperty(JsonObject data) {
        return new ErrorEventResponse("Not implemented yet");
    }
}
