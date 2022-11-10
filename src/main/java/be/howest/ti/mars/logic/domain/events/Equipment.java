package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class Equipment {
    private static final MarsH2Repository repo = Repositories.getH2Repo();

    private Equipment() {
    }

    public static SocketResponse getTypes(JsonObject data) {
        return new DataEventResponse("equipment-types", repo.getEquipmentTypes());
    }
}
