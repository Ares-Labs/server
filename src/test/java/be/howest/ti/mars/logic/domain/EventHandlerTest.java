package be.howest.ti.mars.logic.domain;

import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.logic.domain.response.EventResponse;
import be.howest.ti.mars.logic.domain.response.SuccessEventResponse;
import be.howest.ti.mars.logic.exceptions.RepositoryException;
import be.howest.ti.mars.web.bridge.BasicMessage;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EventHandlerTest {
    private static final String validType = "my-event-type";
    private static final EventHandler eh = EventHandler.getInstance();

    private <T> void assertExceptionContains(Class<T> ex, String contains, Runnable task) {
        try {
            task.run();
            Assertions.fail("Expected exception " + ex.getName() + " to be thrown");
        } catch (Exception e) {
            Assertions.assertEquals(ex, e.getClass(), "Expected exception " + ex.getName() + " to be thrown, but got " + e.getClass().getName());
            Assertions.assertTrue(e.getMessage().contains(contains), "Expected exception message to contain " + contains + " but got " + e.getMessage());
        }
    }

    private <T> void assertDoesNotThrowWithExceptionContains(Class<T> ex, String contains, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            if (e.getClass() == ex) {
                Assertions.assertFalse(e.getMessage().contains(contains), "Exception message should not contain " + contains);
            }
        }
    }

    @BeforeAll
    static void setUp() {
        eh.addEventHandler(validType, (m) -> new SuccessEventResponse(validType));
    }

    @Test
    void incomingEventEmptyMessage() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eh.handleIncomingEvent(null));
    }

    @Test
    void incomingEventWithNoType() {
        JsonObject message = new JsonObject();

        Assertions.assertThrows(IllegalArgumentException.class, () -> eh.handleIncomingEvent(message));
    }

    @Test
    void incomingEventWithUnknownType() {
        JsonObject message = new JsonObject();
        message.put("type", "unknown");

        Assertions.assertThrows(IllegalArgumentException.class, () -> eh.handleIncomingEvent(message));
    }

    @Test
    void incomingEventWithValidType() {
        JsonObject message = new JsonObject();
        message.put("type", validType);

        assertDoesNotThrowWithExceptionContains(IllegalArgumentException.class, "Unknown event type", () -> eh.handleIncomingEvent(message));
    }

    @Test
    void incomingEventWithValidTypeButNoData() {
        JsonObject message = new JsonObject();
        message.put("type", validType);

        assertExceptionContains(IllegalArgumentException.class, "No data specified in message", () -> eh.handleIncomingEvent(message));
    }

    @Test
    void incomingEventWithValidTypeButDataNotJsonObject() {
        JsonObject message = new JsonObject();
        message.put("type", validType);
        message.put("data", "not a json object");

        assertExceptionContains(IllegalArgumentException.class, "Data is not a JsonObject", () -> eh.handleIncomingEvent(message));
    }

    @Test
    void incomingEventWithValidTypeAndValidData() {
        JsonObject message = new JsonObject();
        message.put("type", validType);
        message.put("data", new JsonObject());

        assertDoesNotThrowWithExceptionContains(IllegalArgumentException.class, "No data specified in message", () -> eh.handleIncomingEvent(message));
        assertDoesNotThrowWithExceptionContains(IllegalArgumentException.class, "Data is not a JsonObject", () -> eh.handleIncomingEvent(message));
    }

    @Test
    void incomingEventValidWithRequestIdentifier() {
        JsonObject message = new JsonObject();
        message.put("type", "check-request-identifier");
        JsonObject data = new JsonObject();
        data.put("requestIdentifier", "my-request-identifier");
        message.put("data", data);

        eh.addEventHandler("check-request-identifier", (m) -> {
            // Check that we can not use the request identifier within the event handler
            Assertions.assertNull(m.getString("requestIdentifier"), "Request identifier should not be available within the event handler");
            return new SuccessEventResponse("check-request-identifier");
        });

        BasicMessage res = (BasicMessage) eh.handleIncomingEvent(message);
        Assertions.assertEquals("my-request-identifier", res.getJsonData().get("requestIdentifier"), "Expected the same request identifier in the response");
    }

    @Test
    void incomingEventValidWithRepositoryException() {
        JsonObject message = new JsonObject();
        message.put("type", "check-repository-exception");
        message.put("data", new JsonObject());

        eh.addEventHandler("check-repository-exception", (m) -> {
            throw new RepositoryException("my-repository-exception");
        });

        BasicMessage res = (BasicMessage) eh.handleIncomingEvent(message);
        if (!(res instanceof ErrorEventResponse)) {
            Assertions.fail("Response should be an ErrorEventResponse");
        }
    }
}
