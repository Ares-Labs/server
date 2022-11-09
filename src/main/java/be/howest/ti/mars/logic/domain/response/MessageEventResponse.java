package be.howest.ti.mars.logic.domain.response;

import java.util.Map;

public class MessageEventResponse extends EventResponse {
    public MessageEventResponse(String type, String message) {
        super(type, Map.of("message", message));
    }

    public MessageEventResponse(String type, String message, String id) {
        super(type, Map.of("message", message, "id", id));
    }
}
