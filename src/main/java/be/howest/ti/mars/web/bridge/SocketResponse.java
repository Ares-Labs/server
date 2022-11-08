package be.howest.ti.mars.web.bridge;

import io.vertx.core.json.JsonObject;

public interface SocketResponse {
    JsonObject toMessage();
}
