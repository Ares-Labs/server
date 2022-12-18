package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class Equipment {
    private static final MarsH2Repository repo = Repositories.getH2Repo();

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

        JsonObject response = new JsonObject();
        response.put("droneId", droneId);

        return new DataEventResponse("dispatch-drone", response);
    }

    public static SocketResponse getDispatchedDrones(JsonObject data) {
        String search = Utils.getOrDefaultString(data, "search", "");
        int limit = Utils.getOrDefaultInt(data, "limit", 10);
        int offset = Utils.getOrDefaultInt(data, "offset", 0);

        return new DataEventResponse("dispatched-drones", repo.getDispatchedDrones(limit, offset, search));

    }
}
