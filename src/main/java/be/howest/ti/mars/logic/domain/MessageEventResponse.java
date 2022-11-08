package be.howest.ti.mars.logic.domain;

import java.util.Map;

public class MessageEventResponse extends EventResponse {
    public MessageEventResponse(String type, String message) {
        super(type, Map.of("message", message));
    }
}
