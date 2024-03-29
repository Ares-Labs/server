package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.logic.domain.response.StatusMessageEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class Equipment {
    private static final MarsH2Repository repo = Repositories.getH2Repo();
    private static final String DRONE_ID = "droneId";

    private Equipment() {
    }

    public static SocketResponse getTypes(JsonObject data) {
        return new DataEventResponse("equipment-types", repo.getEquipmentTypes());
    }

    public static SocketResponse dispatchDrone(JsonObject data) {
        int propertyId = data.getInteger("propertyId");
        List<Integer> drones = repo.getFreeDrones(propertyId);

        if (drones.isEmpty()) {
            return new ErrorEventResponse("No drones available");
        }

        int droneId = drones.get(0);
        repo.dispatchDrone(droneId);

        Subscriptions.emit("events.drone-dispatched", new JsonObject().put(DRONE_ID, droneId));

        JsonObject response = new JsonObject();
        response.put(DRONE_ID, droneId);

        return new DataEventResponse("dispatch-drone", response);
    }

    public static SocketResponse getDispatchedDrones(JsonObject data) {
        String search = Utils.getOrDefaultString(data, "search", "");
        int limit = Utils.getOrDefaultInt(data, "limit", 10);
        int offset = Utils.getOrDefaultInt(data, "offset", 0);

        return new DataEventResponse("dispatched-drones", repo.getDispatchedDrones(limit, offset, search));

    }

    public static SocketResponse recallDrone(JsonObject data) {
        int droneId = data.getInteger(DRONE_ID);
        repo.recallDrone(droneId);

        Subscriptions.emit("events.drone-recalled", new JsonObject().put(DRONE_ID, droneId));

        return new StatusMessageEventResponse("recall-drone");
    }

    public static SocketResponse getFreeDrones(JsonObject data) {
        int propertyId = data.getInteger("propertyId");
        List<Integer> drones = repo.getFreeDrones(propertyId);

        JsonObject response = new JsonObject();
        response.put("drones", new JsonArray(drones));

        return new DataEventResponse("get-free-drones", response);
    }
}
