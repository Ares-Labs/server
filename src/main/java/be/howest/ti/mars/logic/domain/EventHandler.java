package be.howest.ti.mars.logic.domain;

import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

public class EventHandler {
    private static final EventHandler INSTANCE = new EventHandler();

    private EventHandler() {
    }

    public static EventHandler getInstance() {
        return INSTANCE;
    }

    public SocketResponse handleIncomingEvent(JsonObject message) {
        String type = message.getString("type");

        if (type == null) {
            return new MessageEventResponse("error", "No type specified");
        }

        // TODO: Properly handle each request type
        return new MessageEventResponse("message", "Hello, World!");
    }
}
