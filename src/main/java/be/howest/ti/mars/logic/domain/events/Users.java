package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class Users {
    private static final MarsH2Repository repo = Repositories.getH2Repo();

    private Users() {
    }

    public static SocketResponse getUser(JsonObject data) {
        String userId = data.getString("userId");

        return new DataEventResponse("get-user", repo.getUser(userId));
    }
}
