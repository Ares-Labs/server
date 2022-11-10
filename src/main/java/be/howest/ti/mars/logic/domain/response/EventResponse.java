package be.howest.ti.mars.logic.domain.response;

import be.howest.ti.mars.web.bridge.BasicMessage;

import java.util.HashMap;
import java.util.Map;

public class EventResponse extends BasicMessage {
    private final String type;
    private String channel;
    private final Map<String, Object> data;

    public EventResponse(String type, Map<String, Object> data) {
        this(type, data, null);
    }

    public EventResponse(String type, Map<String, Object> data, String channel) {
        this.type = type;
        this.channel = channel;
        this.data = new HashMap<>(data);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, Object> getJsonData() {
        return data;
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override
    public void setRequestIdentifier(String requestIdentifier) {
        data.put("requestIdentifier", requestIdentifier);
    }
}
