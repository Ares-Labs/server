package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.logic.domain.response.SuccessEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Properties {
    private static final MarsH2Repository repo = Repositories.getH2Repo();

    private static final String DESCRIPTION = "description";
    private static final String CLIENT_ID = "clientId";
    private static final String PROPERTY_ID = "propertyId";
    private static final String USER_ID = "userId";
    private static final String EQUIPMENT_ID = "equipmentId";
    private static final String LIMIT = "limit";
    private static final String SEARCH = "search";
    private static final String OFFSET = "offset";

    private Properties() {
    }

    public static SocketResponse addProperty(JsonObject data) {
        String location = Utils.getOrThrowString(data, "location");
        int tier = Utils.getOrThrowInt(data, "tier");
        int x = Utils.getOrThrowInt(data, "x");
        int y = Utils.getOrThrowInt(data, "y");
        String description = Utils.getOrThrowString(data, DESCRIPTION);
        String clientId = Utils.getOrThrowString(data, CLIENT_ID);
        String status = "PENDING";

        repo.insertProperty(clientId, location, tier, x, y, status, description);
        Subscriptions.emit("events.property-added", data);
        return new SuccessEventResponse("Property added");
    }

    public static SocketResponse removeProperty(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        repo.removeProperty(propertyId);
        return new SuccessEventResponse("remove-property");
    }

    public static SocketResponse getProperty(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        return new DataEventResponse("get-property", repo.getProperty(propertyId));
    }

    public static SocketResponse changePropertyStatus(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        String status = Utils.getOrThrowString(data, "status");
        repo.changePropertyStatus(propertyId, status);
        Subscriptions.emit("events.property-status-change", data);
        return new SuccessEventResponse("change-property-status");
    }

    public static SocketResponse changePropertySize(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        int width = Utils.getOrThrowInt(data, "width");
        int height = Utils.getOrThrowInt(data, "height");

        repo.changePropertySize(propertyId, width, height);
        Subscriptions.emit(
                "events.property-size-changed",
                new JsonObject()
                        .put(PROPERTY_ID, propertyId)
                        .put("width", width)
                        .put("height", height)
        );
        return new SuccessEventResponse("change-property-size");
    }

    public static SocketResponse getPendingProperties(JsonObject data) {
        return new DataEventResponse("get-pending-properties", repo.getPendingProperties());
    }

    public static SocketResponse getAllowedUsers(JsonObject data) {
        String propertyId = Utils.getOrThrowString(data, PROPERTY_ID);
        return new DataEventResponse("get-allowed-users", repo.getAllowedUsers(propertyId));
    }

    public static SocketResponse addAllowedUser(JsonObject data) {
        String propertyId = Utils.getOrThrowString(data, PROPERTY_ID);
        String userId = Utils.getOrThrowString(data, USER_ID);
        repo.addAllowedUser(propertyId, userId);
        return new SuccessEventResponse("add-allowed-user");
    }

    public static SocketResponse removeAllowedUser(JsonObject data) {
        String propertyId = Utils.getOrThrowString(data, PROPERTY_ID);
        String userId = Utils.getOrThrowString(data, USER_ID);
        repo.removeAllowedUser(propertyId, userId);
        return new SuccessEventResponse("remove-allowed-user");
    }

    public static SocketResponse getAlerts(JsonObject data) {
        String propertyId = Utils.getOrThrowString(data, PROPERTY_ID);
        return new DataEventResponse("get-alerts", repo.getAlerts(propertyId));
    }

    /// Should emit `events.alerts`
    public static SocketResponse addAlert(JsonObject data) {
        String propertyId = Utils.getOrThrowString(data, PROPERTY_ID);
        String userId = Utils.getOrThrowString(data, USER_ID);

        repo.addAlert(propertyId, userId);
        Subscriptions.emit("events.alerts", new JsonObject().put(PROPERTY_ID, propertyId).put("user", userId));

        return new SuccessEventResponse("add-alert");
    }

    public static SocketResponse getWeeklyVisitors(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        return new DataEventResponse("get-weekly-visitors", repo.getWeeklyVisitors(propertyId));
    }

    /// Should emit `events.visits`
    public static SocketResponse addVisitor(JsonObject data) {
        String userId = Utils.getOrThrowString(data, USER_ID);
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        int cameraId = Utils.getOrThrowInt(data, "cameraId");

        repo.addVisitor(userId, propertyId, cameraId);
        Subscriptions.emit("events.visits", new JsonObject().put(PROPERTY_ID, propertyId).put(CLIENT_ID, userId));

        return new SuccessEventResponse("add-visitor");
    }

    public static SocketResponse getCrimesInArea(JsonObject data) {
        JsonObject result = new JsonObject();
        JsonObject day1 = new JsonObject();
        JsonObject day2 = new JsonObject();
        JsonObject day3 = new JsonObject();

        String count = "count";

        day1.put("day", 1);
        day1.put(count, 2);
        day2.put("day", 2);
        day2.put(count, 1);
        day3.put("day", 3);
        day3.put(count, 4);

        result.put("crimes", new JsonArray().add(day1).add(day2).add(day3));

        return new DataEventResponse("get-crimes-in-area", result);
    }

    /// Should emit `events.crimes`
    public static SocketResponse addCrime(JsonObject data) {
        // Some SQL magic here
        Subscriptions.emit("events.crimes", new JsonObject().put(PROPERTY_ID, 1).put(CLIENT_ID, 1));
        return new SuccessEventResponse("add-crime");
    }

    public static SocketResponse getScannedVisitors(JsonObject data) {
        // Get scans for specific property
        String propertyId = Utils.getOrThrowString(data, PROPERTY_ID);
        String from = Utils.getOrThrowString(data, "from");
        String to = Utils.getOrThrowString(data, "to");
        return new DataEventResponse("get-scanned-visitors", repo.getScannedVisitors(propertyId, from, to));
    }

    public static SocketResponse getAuthEntries(JsonObject data) {
        // Get entries of authorizations for specific property
        String propertyId = Utils.getOrThrowString(data, PROPERTY_ID);
        return new DataEventResponse("get-auth-entries", repo.getAuthEntries(propertyId));
    }

    /// Should emit `events.auth-entries`
    public static SocketResponse addAuthEntry(JsonObject data) {
        // Update entries when someone enters a property
        String propertyId = Utils.getOrThrowString(data, PROPERTY_ID);
        String userId = Utils.getOrThrowString(data, USER_ID);
        repo.addAuthEntry(propertyId, userId);
        return new SuccessEventResponse("add-auth-entry");
    }

    /// Should emit `events.property-equipment-change`
    public static SocketResponse addEquipmentProperty(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        int equipmentType = Utils.getOrThrowInt(data, "equipmentType");
        String description = Utils.getOrThrowString(data, DESCRIPTION);

        int id = repo.addEquipmentProperty(propertyId, equipmentType, description);
        Subscriptions.emit(
                "events.property-equipment-change",
                new JsonObject()
                        .put(PROPERTY_ID, propertyId)
                        .put(EQUIPMENT_ID, id)
                        .put("equipmentType", equipmentType)
                        .put(DESCRIPTION, description)
        );

        return new DataEventResponse("add-equipment", new JsonObject().put("id", id));
    }

    /// Should emit `events.property-equipment-change`
    public static SocketResponse removeEquipmentProperty(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        int equipmentId = Utils.getOrThrowInt(data, EQUIPMENT_ID);

        repo.removeEquipmentProperty(propertyId, equipmentId);
        Subscriptions.emit(
                "events.property-equipment-change",
                new JsonObject()
                        .put(PROPERTY_ID, propertyId)
                        .put(EQUIPMENT_ID, equipmentId)
        );

        return new SuccessEventResponse("remove-equipment");
    }

    public static SocketResponse getEquipmentProperty(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        return new DataEventResponse("get-equipment", repo.getEquipmentProperty(propertyId));
    }

    /// Should emit `events.requested-remove-property`
    public static SocketResponse requestRemoveProperty(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        repo.requestRemoveProperty(propertyId);
        Subscriptions.emit("events.requested-remove-property", new JsonObject().put(PROPERTY_ID, propertyId));
        return new SuccessEventResponse("request-remove-property");
    }

    public static SocketResponse getRequestedRemoveProperties(JsonObject data) {
        return new DataEventResponse("get-requested-remove-properties", repo.getRequestedRemoveProperties());
    }

    public static SocketResponse approveRemoveProperty(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        repo.approveRemoveProperty(propertyId);

        Subscriptions.emit("events.approved-remove-property", new JsonObject().put(PROPERTY_ID, propertyId));
        return new SuccessEventResponse("approve-remove-property");
    }

    public static SocketResponse changePropertyTier(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        int tier = Utils.getOrThrowInt(data, "tier");

        repo.changePropertyTier(propertyId, tier);
        Subscriptions.emit("events.property-tier-changed", new JsonObject().put(PROPERTY_ID, propertyId).put("tier", tier));
        return new SuccessEventResponse("change-property-tier");
    }

    public static SocketResponse getProperties(JsonObject data) {
        String search = Utils.getOrDefaultString(data, SEARCH, "");
        int offset = Utils.getOrDefaultInt(data, OFFSET, 0);
        int limit = Utils.getOrDefaultInt(data, LIMIT, 10);

        return new DataEventResponse("get-properties", repo.getProperties(limit, offset, search));
    }

    public static SocketResponse searchPendingProperties(JsonObject data) {
        String search = Utils.getOrDefaultString(data, SEARCH, "");
        int offset = Utils.getOrDefaultInt(data, OFFSET, 0);
        int limit = Utils.getOrDefaultInt(data, LIMIT, 10);

        return new DataEventResponse("search-pending-properties", repo.searchPendingProperties(search, limit, offset));
    }

    public static SocketResponse searchRemovalProperties(JsonObject data) {
        String search = Utils.getOrDefaultString(data, SEARCH, "");
        int offset = Utils.getOrDefaultInt(data, OFFSET, 0);
        int limit = Utils.getOrDefaultInt(data, LIMIT, 10);

        return new DataEventResponse("search-removal-properties", repo.searchRemovalProperties(search, limit, offset));
    }

    public static SocketResponse getPropertyDetailed(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        return new DataEventResponse("get-property-detailed", repo.getPropertyDetailed(propertyId));
    }

    public static SocketResponse changePropertyCoordinates(JsonObject data) {
        int propertyId = Utils.getOrThrowInt(data, PROPERTY_ID);
        int x = Utils.getOrThrowInt(data, "x");
        int y = Utils.getOrThrowInt(data, "y");

        repo.changePropertyCoordinates(propertyId, x, y);
        Subscriptions.emit("events.property-coordinates-changed", new JsonObject().put(PROPERTY_ID, propertyId).put("x", x).put("y", y));
        return new SuccessEventResponse("change-property-location");
    }
}
