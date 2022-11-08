package be.howest.ti.mars.logic.domain;

import be.howest.ti.mars.web.bridge.SocketResponse;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public interface MessageHandler extends Function<JsonObject, SocketResponse> {
}
