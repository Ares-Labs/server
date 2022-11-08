package be.howest.ti.mars.logic.domain;

public class ErrorEventResponse extends MessageEventResponse {
    public ErrorEventResponse(String message) {
        super("error", message);
    }
}
