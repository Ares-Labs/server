package be.howest.ti.mars.logic.domain.Events;

import be.howest.ti.mars.logic.domain.events.Subscriptions;
import be.howest.ti.mars.logic.domain.response.StatusMessageEventResponse;
import be.howest.ti.mars.web.bridge.MarsRtcBridge;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

class SubscriptionsTest {
    private final Vertx vertx = Vertx.vertx();
    private EventBus bus;

    @BeforeEach
    void setup() {
        bus = vertx.eventBus();
        Subscriptions.setBus(bus);
    }

    @Test
    void subscribeMissingDataProperties() {
        JsonObject data = new JsonObject();

        Assertions.assertThrows(IllegalArgumentException.class, () -> Subscriptions.subscribe(data));
    }

    private void subscribe(String clientId, String subscription) {
        JsonObject data = new JsonObject();
        data.put("clientId", clientId);
        data.put("id", subscription);

        SocketResponse res = Subscriptions.subscribe(data);

        if (!(res instanceof StatusMessageEventResponse)) {
            Assertions.fail(String.format("Exception while subscription {} to {}, expected StatusMessageEventResponse response but got {}", clientId, subscription, res));
        }
    }

    @Test
    void subscribeSuccess() {
        subscribe("my-client", "my-event");
    }

    @Test
    void emitNoBus() {
        String event = "my-event";
        JsonObject data = new JsonObject();

        Subscriptions.setBus(null);
        Assertions.assertThrows(IllegalStateException.class, () -> Subscriptions.emit(event, data));
    }

    @Test
    void emit() {
        String event = "my-event";
        String[] clients = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        JsonObject expectedPayload = new JsonObject();
        expectedPayload.put("data", "my-payload");
        // We need to use a latch, as the event bus is asynchronous, so the consumer will not be called yet.
        CountDownLatch latch = new CountDownLatch(clients.length - 1);

        Arrays.stream(clients).forEach(id -> {
            subscribe(id, event);

            String channel = MarsRtcBridge.formatAddress(MarsRtcBridge.OUTBOUND, id);
            bus.consumer(channel, (Message<JsonObject> msg) -> {
                JsonObject actualMessage = msg.body().getJsonObject("data");

                String expectedData = expectedPayload.getString("data");
                String actualData = actualMessage.getString("data");

                Assertions.assertEquals(expectedData, actualData);
                latch.countDown();
            });
        });

        Subscriptions.emit(event, expectedPayload);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
