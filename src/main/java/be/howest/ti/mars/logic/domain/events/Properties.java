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
            int x = Utils.getOrThrowInt(data, "x");
            int y = Utils.getOrThrowInt(data, "y");
            int width = Utils.getOrThrowInt(data, "width");
            int height = Utils.getOrThrowInt(data, "height");
            String description = Utils.getOrThrowString(data, "description");
            String clientId = Utils.getOrThrowString(data, "clientId");
            String status = "PENDING";
            repo.insertProperty(clientId, location, tier, x, y, width, height, status, description);

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
            return new DataEventResponse("get-allowed-users", repo.getAllowedUsers(propertyId));
        } catch (RepositoryException ex) {
            return new ErrorEventResponse(ex.getMessage());
        }
    }

    public static SocketResponse addAllowedUser(JsonObject data) {
        try {
            MarsH2Repository repo = Repositories.getH2Repo();

            String propertyId = Utils.getOrThrowString(data, "propertyId");
            String userId = Utils.getOrThrowString(data, "userId");
            boolean success = repo.addAllowedUser(propertyId, userId);
            return new DataEventResponse("add-allowed-user", new JsonObject().put("success", success));
        } catch (RepositoryException ex) {
            return new ErrorEventResponse(ex.getMessage());
        }
    }
}
