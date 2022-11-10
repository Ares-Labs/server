package be.howest.ti.mars.logic.domain;

import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.logic.exceptions.RepositoryException;
import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventHandler {
    private static final EventHandler INSTANCE = new EventHandler();
    private static final Logger LOGGER = Logger.getLogger(EventHandler.class.getName());
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
        LOGGER.log(Level.INFO, "Incoming event: {0}", message);

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

        String requestIdentifier = data.getString("requestIdentifier");

        if (requestIdentifier != null) {
            data.remove("requestIdentifier");
        }

        SocketResponse response;
        try {
            response = handler.apply(data);
        } catch (RepositoryException e) {
            response = new ErrorEventResponse(e.getMessage());
        }

        if (requestIdentifier != null) {
            response.setRequestIdentifier(requestIdentifier);
        }

        return response;
    }
}
