package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.domain.events.Users;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class MarsH2RepositoryTest {
    private static final String URL = "jdbc:h2:./db-06";

    @BeforeEach
    void setupTestSuite() {
        Repositories.shutdown();
        JsonObject dbProperties = new JsonObject(Map.of("url",URL,
                "username", "",
                "password", "",
                "webconsole.port", 9000 ));
        Repositories.configure(dbProperties);
    }

    private <T, C> void assertInstanceOf(T instance, Class<C> cls) {
        if (!cls.isInstance(instance)) {
            throw new AssertionError("Expected instance of " + cls.getName() + " but got " + instance.getClass().getName());
        }
    }

    @Test
    void getUser() {
        JsonObject data = new JsonObject();
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertInstanceOf(Users.getUser(data), DataEventResponse.class);
    }

    @Test
    void getProperties() {
        JsonObject data = new JsonObject();
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertInstanceOf(Users.getProperties(data), DataEventResponse.class);
    }
}
