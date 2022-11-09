package be.howest.ti.mars.logic.domain.response;

public class StatusMessageEventResponse extends MessageEventResponse {
    public StatusMessageEventResponse(String message) {
        super("status", message);
    }

    public StatusMessageEventResponse(String message, String id) {
        super("status", message, id);
    }
}
