package be.howest.ti.mars.web.bridge;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

public abstract class BasicMessage implements SocketResponse {
    public String getType() {
        throw new NotImplementedException("getType method not implemented");
    }

    public Map<String, Object> getJsonData() {
        throw new NotImplementedException("getJsonData method not implemented");
    }

    @Override
    public JsonObject toMessage() {
        return new JsonObject(Map.of(
                "type", getType(),
                "data", getJsonData()
        ));
    }
}
