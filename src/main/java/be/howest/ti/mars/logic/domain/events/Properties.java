package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.logic.domain.response.StatusMessageEventResponse;
import be.howest.ti.mars.logic.exceptions.RepositoryException;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class Properties {
    private Properties() {
    }

    public static SocketResponse addProperty(JsonObject data) {
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

        return new StatusMessageEventResponse("Property added");
    }

    public static SocketResponse removeProperty(JsonObject data) {
        MarsH2Repository repo = Repositories.getH2Repo();

        int id = Utils.getOrThrowInt(data, "id");
        repo.removeProperty(id);

        return new StatusMessageEventResponse("Property removed");
    }

    public static SocketResponse getProperty(JsonObject data) {
        MarsH2Repository repo = Repositories.getH2Repo();

        int id = Utils.getOrThrowInt(data, "id");
        return new DataEventResponse("get-property", repo.getProperty(id));
    }

    public static SocketResponse getAllowedUsers(JsonObject data) {
        MarsH2Repository repo = Repositories.getH2Repo();

        String propertyId = Utils.getOrThrowString(data, "propertyId");
        return new DataEventResponse("get-allowed-users", repo.getAllowedUsers(propertyId));
    }

    public static SocketResponse addAllowedUser(JsonObject data) {
        MarsH2Repository repo = Repositories.getH2Repo();

        String propertyId = Utils.getOrThrowString(data, "propertyId");
        String userId = Utils.getOrThrowString(data, "userId");
        boolean success = repo.addAllowedUser(propertyId, userId);
        return new DataEventResponse("add-allowed-user", new JsonObject().put("success", success));
    }

    public static SocketResponse removeAllowedUser(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    public static SocketResponse getAlerts(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    /// Should emit `events.alerts`
    public static SocketResponse addAlert(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    public static SocketResponse getWeeklyVisitors(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    /// Should emit `events.visits`
    public static SocketResponse addVisitor(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    public static SocketResponse getCrimesInArea(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    /// Should emit `events.crimes`
    public static SocketResponse addCrime(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    public static SocketResponse getScannedVisitors(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    /// Should emit `events.scanned`
    public static SocketResponse addScannedVisitor(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    public static SocketResponse getAuthEntries(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    /// Should emit `events.auth-entries`
    public static SocketResponse addAuthEntry(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }
}
