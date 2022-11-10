package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.logic.domain.response.StatusMessageEventResponse;
import be.howest.ti.mars.logic.domain.response.SuccessEventResponse;
import be.howest.ti.mars.logic.exceptions.RepositoryException;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class Properties {
    private static final MarsH2Repository repo = Repositories.getH2Repo();

    private Properties() {
    }

    public static SocketResponse addProperty(JsonObject data) {
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
        int id = Utils.getOrThrowInt(data, "id");
        repo.removeProperty(id);
        return new StatusMessageEventResponse("Property removed");
    }

    public static SocketResponse getProperty(JsonObject data) {
        int id = Utils.getOrThrowInt(data, "id");
        return new DataEventResponse("get-property", repo.getProperty(id));
    }

    public static SocketResponse changePropertyStatus(JsonObject data) {
        int id = Utils.getOrThrowInt(data, "id");
        String status = Utils.getOrThrowString(data, "status");
        boolean success = repo.changePropertyStatus(id, status);
        return new SuccessEventResponse("change-property-status", success);
    }

    public static SocketResponse getPendingProperties(JsonObject data) {
        return new ErrorEventResponse("Not implemented");
    }

    public static SocketResponse getAllowedUsers(JsonObject data) {
        String propertyId = Utils.getOrThrowString(data, "propertyId");
        return new DataEventResponse("get-allowed-users", repo.getAllowedUsers(propertyId));
    }

    public static SocketResponse addAllowedUser(JsonObject data) {
        String propertyId = Utils.getOrThrowString(data, "propertyId");
        String userId = Utils.getOrThrowString(data, "userId");
        boolean success = repo.addAllowedUser(propertyId, userId);
        return new SuccessEventResponse("add-allowed-user", success);
    }

    public static SocketResponse removeAllowedUser(JsonObject data) {
        String propertyId = Utils.getOrThrowString(data, "propertyId");
        String userId = Utils.getOrThrowString(data, "userId");
        boolean success = repo.removeAllowedUser(propertyId, userId);
        return new SuccessEventResponse("remove-allowed-user", success);
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
