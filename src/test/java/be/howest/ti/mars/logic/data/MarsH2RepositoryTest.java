package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.domain.events.Equipment;
import be.howest.ti.mars.logic.domain.events.Properties;
import be.howest.ti.mars.logic.domain.events.Subscriptions;
import be.howest.ti.mars.logic.domain.events.Users;
import be.howest.ti.mars.logic.domain.response.DataEventResponse;
import be.howest.ti.mars.logic.domain.response.SuccessEventResponse;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class MarsH2RepositoryTest {
    private static final String URL = "jdbc:h2:./db-06";

    @BeforeEach
    void setupTestSuite() {
        Repositories.shutdown();
        JsonObject dbProperties = new JsonObject(Map.of("url", URL,
                "username", "",
                "password", "",
                "webconsole.port", 9000));
        Repositories.configure(dbProperties);

        Vertx vertx = Vertx.vertx();
        EventBus bus = vertx.eventBus();
        Subscriptions.setBus(bus);
    }

    private <T, C> void assertInstanceOf(T instance, Class<C> cls) {
        if (!cls.isInstance(instance)) {
            throw new AssertionError("Expected instance of " + cls.getName() + " but got " + instance.getClass().getName());
        }
    }

    private <T> void assertDataEventResponse(T instance) {
        assertInstanceOf(instance, DataEventResponse.class);
    }

    private <T> void assertSuccessEventResponse(T instance) {
        assertInstanceOf(instance, SuccessEventResponse.class);
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

    @Test
    void addProperty() {
        JsonObject data = new JsonObject();
        data.put("location", "Test");
        data.put("tier", 1);
        data.put("x", 1);
        data.put("y", 1);
        data.put("description", "Test");
        data.put("clientId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertSuccessEventResponse(Properties.addProperty(data));
    }

    @Test
    void removeProperty() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertSuccessEventResponse(Properties.removeProperty(data));
    }

    @Test
    void getProperty() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertDataEventResponse(Properties.getProperty(data));
    }

    @Test
    void changePropertyStatus() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("status", "Test");

        assertSuccessEventResponse(Properties.changePropertyStatus(data));
    }

    @Test
    void changePropertySize() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("width", 100);
        data.put("height", 100);

        assertSuccessEventResponse(Properties.changePropertySize(data));
    }

    @Test
    void getPendingProperties() {
        JsonObject data = new JsonObject();

        assertDataEventResponse(Properties.getPendingProperties(data));
    }

    @Test
    void getAllowedUsers() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertDataEventResponse(Properties.getAllowedUsers(data));
    }

    @Test
    void addAllowedUser() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertSuccessEventResponse(Properties.addAllowedUser(data));
    }

    @Test
    void removeAllowedUser() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertSuccessEventResponse(Properties.removeAllowedUser(data));
    }

    @Test
    void getAlerts() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertDataEventResponse(Properties.getAlerts(data));
    }

    @Test
    void addAlert() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertSuccessEventResponse(Properties.addAlert(data));
    }

    @Test
    void getWeeklyVisitors() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertDataEventResponse(Properties.getWeeklyVisitors(data));
    }

    @Test
    void addVisitor() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("cameraId", 1);
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertSuccessEventResponse(Properties.addVisitor(data));
    }

    @Test
    void getCrimesInArea() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertDataEventResponse(Properties.getCrimesInArea(data));
    }

    @Test
    void addCrime() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("clientId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertSuccessEventResponse(Properties.addCrime(data));
    }

    @Test
    void getScannedVisitors() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("from", "1000000000");
        data.put("to", "1669898648");

        assertDataEventResponse(Properties.getScannedVisitors(data));
    }

    @Test
    void addAuthEntry() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertSuccessEventResponse(Properties.addAuthEntry(data));
    }

    @Test
    void getAuthEntries() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("userId", "9a0fbbc6-55f3-11ed-82ca-9313c9a89e82");

        assertDataEventResponse(Properties.getAuthEntries(data));
    }

    @Test
    void addEquipmentProperty() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("equipmentType", 1);
        data.put("description", "Test");

        assertDataEventResponse(Properties.addEquipmentProperty(data));
    }

    @Test
    void removeEquipmentProperty() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("equipmentId", 1);

        assertSuccessEventResponse(Properties.removeEquipmentProperty(data));
    }

    @Test
    void getEquipmentProperty() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertDataEventResponse(Properties.getEquipmentProperty(data));
    }

    @Test
    void requestRemoveProperty() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertSuccessEventResponse(Properties.requestRemoveProperty(data));
    }

    @Test
    void getRequestedRemoveProperties() {
        JsonObject data = new JsonObject();

        assertDataEventResponse(Properties.getRequestedRemoveProperties(data));
    }

    @Test
    void approveRemoveProperty() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);

        assertSuccessEventResponse(Properties.approveRemoveProperty(data));
    }

    @Test
    void changePropertyTier() {
        JsonObject data = new JsonObject();
        data.put("propertyId", 1);
        data.put("tier", 1);

        assertSuccessEventResponse(Properties.changePropertyTier(data));
    }
}
