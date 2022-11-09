package be.howest.ti.mars.logic.domain;

import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class EventHandler {
    private static final EventHandler INSTANCE = new EventHandler();
    private final Map<String, MessageHandler> messageHandlers = new HashMap<>();

    private EventHandler() {
    }

    public static EventHandler getInstance() {
        return INSTANCE;
    }

    public void addEventHandler(String event, MessageHandler handler) {
        messageHandlers.put(event, handler);
    }

    private <T> void errorIfNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public SocketResponse handleIncomingEvent(JsonObject message) {
        String type = message.getString("type");
        errorIfNull(type, "No type specified in message");

        MessageHandler handler = messageHandlers.get(type);
        errorIfNull(handler, "Unknown event type: " + type);

        JsonObject data;
        try {
            data = message.getJsonObject("data");
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Data is not a JsonObject");
        }
        errorIfNull(data, "No data specified in message");

        return handler.apply(data);
    }
}
