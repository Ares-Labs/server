package be.howest.ti.mars.logic.domain;

import be.howest.ti.mars.web.bridge.BasicMessage;

import java.util.Map;

public class EventResponse extends BasicMessage {
    private final String type;
    private final Map<String, String> data;

    public EventResponse(String type, Map<String, String> data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, String> getJsonData() {
        return data;
    }
}
