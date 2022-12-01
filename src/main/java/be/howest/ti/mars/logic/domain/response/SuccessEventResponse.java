package be.howest.ti.mars.logic.domain.response;

import java.util.Map;

public class SuccessEventResponse extends EventResponse {
    public SuccessEventResponse(String event) {
        this(event, true);
    }

    public SuccessEventResponse(String type, boolean success) {
        super(type, makeSuccessResponse(success));
    }

    private static Map<String, Object> makeSuccessResponse(boolean success) {
        return Map.of("success", success);
    }
}
