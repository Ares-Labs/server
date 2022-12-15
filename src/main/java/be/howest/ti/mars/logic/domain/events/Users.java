package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.data.MarsH2Repository;
import be.howest.ti.mars.logic.data.Repositories;
import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class Users {
    private static final MarsH2Repository repo = Repositories.getH2Repo();

    private Users() {
    }

    public static SocketResponse getUser(JsonObject data) {
        String userId = Utils.getOrThrowString(data, "userId");

        return new DataEventResponse("get-user", repo.getUser(userId));
    }

    public static SocketResponse getProperties(JsonObject data) {
        String userId = Utils.getOrThrowString(data, "userId");

        return new DataEventResponse("get-properties", repo.getProperties(userId));
    }

    public static SocketResponse getUsers(JsonObject data) {
        int limit = Utils.getOrDefaultInt(data, "limit", 10);
        int offset = Utils.getOrDefaultInt(data, "offset", 0);
        String search = Utils.getOrDefaultString(data, "search", "");

        return new DataEventResponse("get-users", repo.getUsers(limit, offset, search));
    }
}
