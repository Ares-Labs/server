package be.howest.ti.mars.web.bridge;

import be.howest.ti.mars.logic.domain.EventHandler;
import be.howest.ti.mars.logic.domain.events.Equipment;
import be.howest.ti.mars.logic.domain.events.Properties;
import be.howest.ti.mars.logic.domain.events.Subscriptions;
import be.howest.ti.mars.logic.domain.events.Users;
import be.howest.ti.mars.logic.domain.response.ErrorEventResponse;
import be.howest.ti.mars.logic.domain.response.StatusMessageEventResponse;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static final String OUTBOUND = "events.to.martians";
    public static final String INBOUND = "events.from.martians";
    private static final Logger LOGGER = Logger.getLogger(MarsRtcBridge.class.getName());
    private SockJSHandler sockJSHandler;
    private EventBus eb;

    public static String formatAddress(String address, String id) {
        return String.format("%s.%s", address, id);
    }

    private void createSockJSHandler() {
        final PermittedOptions permittedOptions = new PermittedOptions().setAddressRegex("events\\..+");
        final SockJSBridgeOptions options = new SockJSBridgeOptions()
                .addInboundPermitted(permittedOptions)
                .addOutboundPermitted(permittedOptions);

        sockJSHandler.bridge(options);
    }

    private void handleConsumerMessage(String address, Message<String> msg) {
        EventHandler eh = EventHandler.getInstance();
        SocketResponse response;

        try {
            response = eh.handleIncomingEvent(new JsonObject(msg.body()));
        } catch (IllegalArgumentException e) {
            response = new ErrorEventResponse(e.getMessage());
        }

        if (response != null) {
            String out = response.getChannel() != null ? response.getChannel() : address;
            eb.publish(out, response.toMessage());
        }
    }

    private void handlePublicConsumerMessage(Message<String> msg) {
        handleConsumerMessage(OUTBOUND, msg);
    }

    public SockJSHandler getSockJSHandler(Vertx vertx) {
        sockJSHandler = SockJSHandler.create(vertx);
        eb = vertx.eventBus();
        Subscriptions.setBus(eb);
        createSockJSHandler();

        eb.consumer(INBOUND, this::handlePublicConsumerMessage);

        EventHandler eh = EventHandler.getInstance();

        eh.addEventHandler("session", this::addNewSession);
        eh.addEventHandler("subscribe", Subscriptions::subscribe);

        eh.addEventHandler("queries.get-user", Users::getUser);
        eh.addEventHandler("queries.get-users", Users::getUsers);
        eh.addEventHandler("queries.get-user-properties", Users::getProperties);
        eh.addEventHandler("queries.get-equipment-types", Equipment::getTypes);
        eh.addEventHandler("queries.dispatch-drone", Equipment::dispatchDrone);
        eh.addEventHandler("queries.get-dispatched-drones", Equipment::getDispatchedDrones);
        eh.addEventHandler("queries.get-free-drones", Equipment::getFreeDrones);
        eh.addEventHandler("queries.recall-drone", Equipment::recallDrone);

        eh.addEventHandler("queries.add-property", Properties::addProperty);
        eh.addEventHandler("queries.remove-property", Properties::removeProperty);
        eh.addEventHandler("queries.get-property", Properties::getProperty);
        eh.addEventHandler("queries.get-property-detailed", Properties::getPropertyDetailed);
        eh.addEventHandler("queries.get-properties", Properties::getProperties);
        eh.addEventHandler("queries.change-property-size", Properties::changePropertySize);
        eh.addEventHandler("queries.change-property-coordinates", Properties::changePropertyCoordinates);
        eh.addEventHandler("queries.change-property-status", Properties::changePropertyStatus);
        eh.addEventHandler("queries.get-pending-properties", Properties::getPendingProperties);
        eh.addEventHandler("queries.add-equipment-property", Properties::addEquipmentProperty);
        eh.addEventHandler("queries.remove-equipment-property", Properties::removeEquipmentProperty);
        eh.addEventHandler("queries.get-equipment-property", Properties::getEquipmentProperty);
        eh.addEventHandler("queries.change-property-tier", Properties::changePropertyTier);
        eh.addEventHandler("queries.search-pending-properties", Properties::searchPendingProperties);
        eh.addEventHandler("queries.search-removal-properties", Properties::searchRemovalProperties);

        eh.addEventHandler("queries.request-remove-property", Properties::requestRemoveProperty);
        eh.addEventHandler("queries.get-requested-remove-properties", Properties::getRequestedRemoveProperties);
        eh.addEventHandler("queries.approve-remove-property", Properties::approveRemoveProperty);

        eh.addEventHandler("queries.get-allowed-users", Properties::getAllowedUsers);
        eh.addEventHandler("queries.add-allowed-user", Properties::addAllowedUser);
        eh.addEventHandler("queries.remove-allowed-user", Properties::removeAllowedUser);

        eh.addEventHandler("queries.get-alerts", Properties::getAlerts);
        eh.addEventHandler("queries.add-alert", Properties::addAlert);

        eh.addEventHandler("queries.get-weekly-visitors", Properties::getWeeklyVisitors);
        eh.addEventHandler("queries.add-visitor", Properties::addVisitor);
        eh.addEventHandler("queries.get-scanned-visitors", Properties::getScannedVisitors);

        eh.addEventHandler("queries.get-crimes-in-area", Properties::getCrimesInArea);
        eh.addEventHandler("queries.add-crime", Properties::addCrime);

        eh.addEventHandler("queries.get-auth-entries", Properties::getAuthEntries);
        eh.addEventHandler("queries.add-auth-entry", Properties::addAuthEntry);

        return sockJSHandler;
    }

    private SocketResponse addNewSession(JsonObject data) {
        // Session event bus initialisation
        String id = data.getString("id");
        String out = formatAddress(OUTBOUND, id);
        eb.consumer(formatAddress(INBOUND, id), (Message<String> msg) -> handleConsumerMessage(out, msg));

        SocketResponse res = new StatusMessageEventResponse("created");
        res.setChannel(out);

        LOGGER.log(Level.INFO, "New session created: {0}", id);
        return res;
    }
}
