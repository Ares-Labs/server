package be.howest.ti.mars.web.bridge;

import be.howest.ti.mars.logic.domain.EventHandler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

/**
 * The RTC bridge is one of the class taught topics.
 * If you do not choose the RTC topic you don't have to do anything with this class.
 * Otherwise, you will need to expand this bridge with the websockets topics shown in the other modules.
 * <p>
 * The client-side starter project does not contain any teacher code about the RTC topic.
 * The rtc bridge is already initialized and configured in the WebServer.java.
 * No need to change the WebServer.java
 * <p>
 * The job of the "bridge" is to bridge between websockets events and Java (the controller).
 * Just like in the openapi bridge, keep business logic isolated in the package logic.
 */
public class MarsRtcBridge {
    private static final String EB_EVENT_TO_MARTIANS = "events.to.martians";
    private static final String EB_EVENT_FROM_MARTIANS = "events.from.martians";
    private SockJSHandler sockJSHandler;
    private EventBus eb;

    private void createSockJSHandler() {
        final PermittedOptions permittedOptions = new PermittedOptions().setAddressRegex("events\\..+");
        final SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(permittedOptions)
                .addOutboundPermitted(permittedOptions);

        sockJSHandler.bridge(options);
    }

    private void handleConsumerMessage(Message<String> msg) {
        JsonObject message = new JsonObject(msg.body());
        EventHandler eh = EventHandler.getInstance();
        SocketResponse response = eh.handleIncomingEvent(message);
        String res = response.toMessage().toString();
        eb.publish(EB_EVENT_TO_MARTIANS, res);
    }

    public SockJSHandler getSockJSHandler(Vertx vertx) {
        sockJSHandler = SockJSHandler.create(vertx);
        eb = vertx.eventBus();
        createSockJSHandler();

        eb.consumer(EB_EVENT_FROM_MARTIANS, this::handleConsumerMessage);

        return sockJSHandler;
    }
}
