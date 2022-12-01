package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.domain.events.Equipment;
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

    private <T> void assertDataEventResponse(T instance) {
        assertInstanceOf(instance, DataEventResponse.class);
    }

    @Test
    void getUser() {
        JsonObject data = new JsonObject();
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertDataEventResponse(Users.getUser(data));
    }

    @Test
    void getProperties() {
        JsonObject data = new JsonObject();
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertDataEventResponse(Users.getProperties(data));
    }

    @Test
    void getEquipmentTypes() {
        JsonObject data = new JsonObject();

        assertDataEventResponse(Equipment.getTypes(data));
    }

    @Test
    void dispatchDrone() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertDataEventResponse(Equipment.dispatchDrone(data));
    }
}
