package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.logic.exceptions.RepositoryException;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class Properties {
    private Properties() {
    }

    public static SocketResponse addProperty(JsonObject data) {
        try {
            MarsH2Repository repo = Repositories.getH2Repo();

            String location = Utils.getOrThrowString(data, "location");
            int tier = Utils.getOrThrowInt(data, "tier");
            String description = Utils.getOrThrowString(data, "description");
            // TODO: Ability to return the property ID
            repo.insertProperty(location, tier, description);
        } catch (RepositoryException ex) {}
        return new ErrorEventResponse("Not implemented yet");
    }

    public static SocketResponse removeProperty(JsonObject data) {
        return new ErrorEventResponse("Not implemented yet");
    }

    public static SocketResponse getProperty(JsonObject data) {
        return new ErrorEventResponse("Not implemented yet");
    }

    public static SocketResponse getAllowedUsers(JsonObject data) {
        try {
            MarsH2Repository repo = Repositories.getH2Repo();

            String propertyId = Utils.getOrThrowString(data, "propertyId");
            return new DataEventResponse("getAllowedUsers", repo.getAllowedUsers(propertyId));
        } catch (RepositoryException ex) {
            return new ErrorEventResponse(ex.getMessage());
        }
    }
}
