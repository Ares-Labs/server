package be.howest.ti.mars.web.bridge;

import io.vertx.core.json.JsonObject;

public interface SocketResponse {
    JsonObject toMessage();

    String getChannel();

    void setChannel(String channel);

    void setRequestIdentifier(String requestIdentifier);
}
