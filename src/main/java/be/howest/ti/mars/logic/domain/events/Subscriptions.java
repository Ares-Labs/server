package be.howest.ti.mars.logic.domain.events;

import be.howest.ti.mars.logic.domain.Utils;
import be.howest.ti.mars.logic.domain.response.StatusMessageEventResponse;
import be.howest.ti.mars.web.bridge.MarsRtcBridge;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Subscriptions {
    private static final Map<String, Set<String>> associations = new HashMap<>();
    private static EventBus bus = null;

    private Subscriptions() {
    }

    public static void setBus(EventBus bus) {
        Subscriptions.bus = bus;
    }

    public static void emit(String event, JsonObject data) {
        if (bus == null) {
            throw new IllegalStateException("Event bus not set");
        }

        if (associations.containsKey(event)) {
            Set<String> addresses = associations.get(event);
            JsonObject message = new JsonObject();
            message.put("type", event);
            message.put("data", data);

            addresses.forEach(address -> bus.publish(address, message));
        }
    }

    public static SocketResponse subscribe(JsonObject data) {
        String clientId = Utils.getOrThrowString(data, "clientId");
        String event = Utils.getOrThrowString(data, "id");

        String channel = MarsRtcBridge.formatAddress(MarsRtcBridge.OUTBOUND, clientId);

        associations.computeIfAbsent(event, k -> new HashSet<>()).add(channel);
        return new StatusMessageEventResponse("Subscribed to " + event);
    }
}
