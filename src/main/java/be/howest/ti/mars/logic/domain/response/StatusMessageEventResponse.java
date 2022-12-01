package be.howest.ti.mars.logic.domain.response;

public class StatusMessageEventResponse extends MessageEventResponse {
    public StatusMessageEventResponse(String message) {
        super("status", message);
    }
}
