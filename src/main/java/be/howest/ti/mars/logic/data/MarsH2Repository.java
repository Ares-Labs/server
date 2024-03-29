package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.exceptions.RepositoryException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.h2.tools.Server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

enum Queries {
    SQL_ADD_PROPERTY("INSERT INTO properties (location,  tier, x, y, status, description) VALUES (?, ?, ?, ?, ?, ?);"),
    SQL_REMOVE_PROPERTY("DELETE FROM properties WHERE id = ?;"),
    SQL_GET_PROPERTY("SELECT * FROM properties WHERE id = ?;"),
    SQL_GET_PROPERTY_ALLOWED_USERS("SELECT * FROM users WHERE id IN (SELECT user_id FROM property_whitelists WHERE property_id = ?)"),
    SQL_ADD_PROPERTY_WHITELIST("INSERT INTO property_whitelists (property_id, user_id) VALUES (?, ?)"),
    SQL_REMOVE_PROPERTY_WHITELIST("DELETE FROM property_whitelists WHERE property_id = ? AND user_id = ?"),
    SQL_CHANGE_PROPERTY_STATUS("UPDATE properties SET status = ? WHERE id = ?;"),
    SQL_ADD_AUTH_ENTRY("INSERT INTO authorizations (property_id, user_id) VALUES (?, ?);"),
    SQL_GET_AUTH_ENTRIES("SELECT * FROM authorizations WHERE property_id = ?"),
    SQL_GET_PENDING_PROPERTIES("SELECT * FROM properties WHERE status = 'PENDING';"),
    SQL_BIND_PROPERTY_TO_USER("INSERT INTO user_properties (user_id, property_id) VALUES (?, ?);"),
    SQL_CHANGE_PROPERTY_SIZE("UPDATE properties SET width = ?, height = ? WHERE id = ?;"),
    SQL_GET_ALERTS("SELECT * FROM alerts WHERE property_id = ?;"),
    SQL_ADD_ALERT("INSERT INTO alerts (property_id, user_id) VALUES (?, ?);"),
    SQL_GET_WEEKLY_VISITORS("SELECT COUNT(*) AS amount, DAY_OF_WEEK(timestamp) AS day_of_week, camera_id, (SELECT description FROM installed_equipment WHERE id = camera_id LIMIT 1) as camera FROM scans WHERE DAY_OF_YEAR(timestamp) > DAY_OF_YEAR(NOW()) - 7 AND property_id = ? GROUP BY camera_id, DAY_OF_WEEK(timestamp);"),
    SQL_GET_SCANNED_VISITORS("SELECT dayofweek(timestamp) AS day, count(*) AS count from scans where timestamp > ? AND timestamp < ? and property_id = ? GROUP BY day"),
    SQL_ADD_VISITOR("INSERT INTO scans (user_id, property_id, camera_id) VALUES (?, ?, ?);"),
    SQL_ADD_EQUIPMENT_PROPERTY("INSERT INTO installed_equipment (type, property_id, description) VALUES (?, ?, ?);"),
    SQL_GET_EQUIPMENT_PROPERTY("SELECT ie.id, ie.property_id, ie.type, et.name, ie.DESCRIPTION FROM installed_equipment ie JOIN equipment_types et ON ie.type = et.type WHERE ie.property_id = ?;"),
    SQL_REMOVE_EQUIPMENT_PROPERTY("DELETE FROM installed_equipment WHERE property_id = ? AND id = ?;"),
    SQL_GET_EQUIPMENT_TYPES("SELECT * FROM equipment_types;"),
    SQL_GET_USER("SELECT * FROM users WHERE id = ?;"),
    SQL_GET_USER_PROPERTIES("SELECT * FROM properties WHERE id IN (SELECT property_id FROM user_properties WHERE user_id = ?);"),
    SQL_REQUEST_REMOVE_PROPERTY("UPDATE properties SET status = 'REMOVED' WHERE id = ?;"),
    SQL_GET_REQUESTED_REMOVE_PROPERTIES("SELECT * FROM properties WHERE status = 'REMOVED';"),
    SQL_APPROVE_REMOVE_PROPERTY("DELETE FROM properties WHERE id = ? AND status = 'REMOVED';"),
    SQL_CHANGE_PROPERTY_TIER("UPDATE properties SET tier = ? WHERE id = ?;"),
    SQL_GET_USERS("SELECT * FROM users WHERE id ilike CONCAT('%', ?, '%') OR full_name ilike CONCAT('%', ?, '%') LIMIT ? OFFSET ?;"),
    SQL_GET_PROPERTIES("SELECT * FROM properties WHERE id ilike CONCAT('%', ?, '%') OR location ilike CONCAT('%', ?, '%') LIMIT ? OFFSET ?;"),
    SQL_GET_FREE_DRONES("SELECT * FROM installed_equipment WHERE type = (SELECT type FROM equipment_types WHERE name = 'Drone') AND property_id = ? AND id not IN (SELECT installed_id FROM dispatched_drones where returned_at is null);"),
    SQL_DISPATCH_DRONE("INSERT INTO dispatched_drones (installed_id) VALUES (?);"),
    SQL_GET_DISPATCHED_DRONES("SELECT * FROM installed_equipment WHERE id in (SELECT installed_id FROM dispatched_drones WHERE returned_at IS NULL)  AND (id ilike CONCAT('%', ?, '%') OR description ilike CONCAT('%', ?, '%')) LIMIT ? OFFSET ?;"),
    SQL_SEARCH_STATUS_PROPERTIES("SELECT * FROM properties WHERE status = ? AND (id ilike CONCAT('%', ?, '%') OR location ilike CONCAT('%', ?, '%')) LIMIT ? OFFSET ?;"),
    SQL_RECALL_DRONE("UPDATE dispatched_drones SET returned_at = NOW() WHERE installed_id = ?;"),
    // This is probably the most inefficient query ever written, but it works
    SQL_GET_PROPERTY_DETAILED("SELECT p.id AS property_id, p.location AS property_location, p.description AS property_description, p.x AS property_x, p.y AS property_y, p.width AS property_width, p.height AS property_height, p.status AS property_status, t.ID AS tier_id, t.name AS tier_name, u.id AS owner_id, u.full_name AS owner_full_name FROM properties p JOIN user_properties o ON p.id = o.property_id JOIN users u ON o.user_id = u.id JOIN tiers t ON p.tier = t.id WHERE p.id = ?;"),
    SQL_CHANGE_PROPERTY_COORDINATES("UPDATE properties SET x = ?, y = ? WHERE id = ?;"),
    ;

    private final String query;

    Queries(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return query;
    }
}

public class MarsH2Repository {
    private static final Logger LOGGER = Logger.getLogger(MarsH2Repository.class.getName());
    private final Server dbWebConsole;
    private final String username;
    private final String password;
    private final String url;

    private static final String LOCATION = "location";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String STATUS = "status";
    private static final String DESCRIPTION = "description";
    private static final String FULL_NAME = "full_name";
    private static final String USER_ID = "user_id";
    private static final String TIMESTAMP = "timestamp";
    private static final String EQUIPMENT = "equipment";
    private static final String COULD_NOT_GET_PROPERTIES_ERROR = "Could not get properties.";


    public MarsH2Repository(String url, String username, String password, int console) {
        try {
            this.username = username;
            this.password = password;
            this.url = url;
            this.dbWebConsole = Server.createWebServer("-ifNotExists", "-webPort", String.valueOf(console)).start();
            LOGGER.log(Level.INFO, "Database web console started on port: {0}", console);
            this.generateData();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "DB configuration failed", ex);
            throw new RepositoryException("Could not configure MarsH2repository");
        }
    }

    public void cleanUp() {
        if (dbWebConsole != null && dbWebConsole.isRunning(false)) dbWebConsole.stop();

        try {
            Files.deleteIfExists(Path.of("./db-06.mv.db"));
            Files.deleteIfExists(Path.of("./db-06.trace.db"));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Database cleanup failed.", e);
            throw new RepositoryException("Database cleanup failed.");
        }
    }

    public void generateData() {
        try {
            executeScript("db-create.sql");
            executeScript("db-populate.sql");
        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Execution of database scripts failed.", ex);
        }
    }

    private void executeScript(String fileName) throws IOException, SQLException {
        String createDbSql = readFile(fileName);
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(createDbSql)) {
            stmt.executeUpdate();
        }
    }

    private String readFile(String fileName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) throw new RepositoryException("Could not read file: " + fileName);

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private JsonObject makeProperty(ResultSet rs) throws SQLException {
        JsonObject property = new JsonObject();
        property.put("id", rs.getInt("id"));
        property.put(LOCATION, rs.getString(LOCATION));
        property.put("x", rs.getInt("x"));
        property.put("y", rs.getInt("y"));
        property.put(WIDTH, rs.getInt(WIDTH));
        property.put(HEIGHT, rs.getInt(HEIGHT));
        property.put(STATUS, rs.getString(STATUS));
        property.put("tier", rs.getInt("tier"));
        property.put(DESCRIPTION, rs.getString(DESCRIPTION));
        return property;
    }

    public void insertProperty(String clientId, String location, int tier, int x, int y, String status, String description) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_PROPERTY.getQuery(), Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, location);
            stmt.setInt(2, tier);
            stmt.setInt(3, x);
            stmt.setInt(4, y);
            stmt.setString(5, status);
            stmt.setString(6, description);

            stmt.executeUpdate();

            stmt.getGeneratedKeys().next();
            int returnedId = stmt.getGeneratedKeys().getInt(1);

            // Add property to users properties
            try (PreparedStatement stmt2 = conn.prepareStatement(Queries.SQL_BIND_PROPERTY_TO_USER.getQuery())) {
                stmt2.setString(1, clientId);
                stmt2.setInt(2, returnedId);
                stmt2.executeUpdate();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not insert property", ex);
            throw new RepositoryException("Could not insert property");
        }
    }

    public JsonObject getProperty(int locationId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_PROPERTY.getQuery())) {
            stmt.setInt(1, locationId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? makeProperty(rs) : null;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get property", ex);
            throw new RepositoryException("Could not get property");
        }
    }

    public void removeProperty(int location) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_REMOVE_PROPERTY.getQuery())) {
            stmt.setInt(1, location);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not remove property", ex);
            throw new RepositoryException("Could not remove property");
        }
    }

    public JsonObject getAllowedUsers(String propertyId) {
        // Get whitelisted users of a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_PROPERTY_ALLOWED_USERS.getQuery())) {
            stmt.setString(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            result.put("allowedUsers", new JsonObject());
            while (rs.next()) {
                result.getJsonObject("allowedUsers").put(rs.getString("id"), rs.getString(FULL_NAME));
            }
            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get allowed users.", ex);
            throw new RepositoryException("Could not get allowed users.");
        }

    }

    public void addAllowedUser(String propertyId, String userId) {
        // Add a user to the whitelist of a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_PROPERTY_WHITELIST.getQuery())) {
            stmt.setString(1, propertyId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not add allowed user.", ex);
            throw new RepositoryException("Could not add allowed user.");
        }
    }

    public void removeAllowedUser(String propertyId, String userId) {
        // Remove a user from the whitelist of a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_REMOVE_PROPERTY_WHITELIST.getQuery())) {
            stmt.setString(1, propertyId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not remove allowed user.", ex);
            throw new RepositoryException("Could not remove allowed user.");
        }
    }

    public void changePropertyStatus(int id, String status) {
        // Change the status of a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_CHANGE_PROPERTY_STATUS.getQuery())) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not change property status.", ex);
            throw new RepositoryException("Could not change property status.");
        }
    }

    public void addAuthEntry(String propertyId, String userId) {
        // Add an entry to the auth table
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_AUTH_ENTRY.getQuery())) {
            stmt.setString(1, propertyId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not add auth entry.", ex);
            throw new RepositoryException("Could not add auth entry.");
        }
    }

    public JsonObject getAuthEntries(String propertyId) {
        // Get all auth entries for a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_AUTH_ENTRIES.getQuery())) {
            stmt.setString(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            JsonArray result = new JsonArray();
            while (rs.next()) {
                JsonObject entry = new JsonObject();
                entry.put(USER_ID, rs.getString(USER_ID));
                entry.put(TIMESTAMP, rs.getString(TIMESTAMP));
                result.add(entry);
            }
            return new JsonObject().put("authEntries", result);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get auth entries.", ex);
            throw new RepositoryException("Could not get auth entries.");
        }
    }

    public JsonObject getPendingProperties() {
        // Get all pending properties
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_PENDING_PROPERTIES.getQuery())) {
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            JsonArray properties = new JsonArray();

            while (rs.next()) {
                properties.add(makeProperty(rs));
            }

            result.put("pendingProperties", properties);
            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get pending properties.", ex);
            throw new RepositoryException("Could not get pending properties.");
        }
    }

    public void changePropertySize(int id, int width, int height) {
        // Change the size of a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_CHANGE_PROPERTY_SIZE.getQuery())) {
            stmt.setInt(1, width);
            stmt.setInt(2, height);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not change property size.", ex);
            throw new RepositoryException("Could not change property size.");
        }
    }

    public JsonObject getAlerts(String propertyId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_ALERTS.getQuery())) {
            stmt.setString(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            JsonArray alerts = new JsonArray();

            while (rs.next()) {
                JsonObject alert = new JsonObject();
                alert.put("id", rs.getInt("id"));
                alert.put("userId", rs.getString(USER_ID));
                alert.put(TIMESTAMP, rs.getString(TIMESTAMP));
                alerts.add(alert);
            }

            result.put("alerts", alerts);
            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get alerts.", ex);
            throw new RepositoryException("Could not get alerts.");
        }
    }

    public void addAlert(String propertyId, String user) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_ALERT.getQuery())) {
            stmt.setString(1, propertyId);
            stmt.setString(2, user);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not add alert.", ex);
            throw new RepositoryException("Could not add alert.");
        }
    }

    public JsonObject getScannedVisitors(String propertyId, String from, String to) {
        // Get scanned visitors for a property between two dates
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_SCANNED_VISITORS.getQuery())) {
            stmt.setString(1, propertyId);
            stmt.setString(2, from);
            stmt.setString(3, to);
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            JsonArray visitors = new JsonArray();

            while (rs.next()) {
                JsonObject visitor = new JsonObject();
                visitor.put("day", rs.getString("day"));
                visitor.put("count", rs.getInt("count"));
                visitors.add(visitor);
            }

            result.put("visitors", visitors);
            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get scanned visitors.", ex);
            throw new RepositoryException("Could not get scanned visitors.");
        }
    }

    public JsonObject getWeeklyVisitors(int propertyId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_WEEKLY_VISITORS.getQuery())) {
            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();

            while (rs.next()) {
                String camera = rs.getString("camera");
                int cameraId = rs.getInt("camera_id");
                int day = rs.getInt("day_of_week");
                int count = rs.getInt("amount");

                if (!result.containsKey(String.valueOf(cameraId))) {
                    JsonObject content = new JsonObject();
                    content.put("data", new JsonObject());
                    content.put("name", camera);
                    result.put(String.valueOf(cameraId), content);
                }

                result.getJsonObject(String.valueOf(cameraId)).getJsonObject("data").put(String.valueOf(day), count);
            }

            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get weekly visitors.", ex);
            throw new RepositoryException("Could not get weekly visitors.");
        }
    }

    public void addVisitor(String userId, int propertyId, int cameraId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_VISITOR.getQuery())) {
            stmt.setString(1, userId);
            stmt.setInt(2, propertyId);
            stmt.setInt(3, cameraId);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not add visitor.", ex);
            throw new RepositoryException("Could not add visitor.");
        }
    }

    public int addEquipmentProperty(int propertyId, int equipmentType, String description) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_EQUIPMENT_PROPERTY.getQuery(), Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, equipmentType);
            stmt.setInt(2, propertyId);
            stmt.setString(3, description);

            stmt.executeUpdate();

            stmt.getGeneratedKeys().next();
            return stmt.getGeneratedKeys().getInt(1);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not add equipment property.", ex);
            throw new RepositoryException("Could not add equipment property.");
        }
    }

    public JsonObject getEquipmentProperty(int propertyId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_EQUIPMENT_PROPERTY.getQuery())) {
            stmt.setInt(1, propertyId);
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            JsonArray equipment = new JsonArray();

            while (rs.next()) {
                JsonObject equipmentProperty = new JsonObject();
                equipmentProperty.put("id", rs.getInt("id"));
                equipmentProperty.put("type", rs.getInt("type"));
                equipmentProperty.put(DESCRIPTION, rs.getString(DESCRIPTION));
                equipmentProperty.put("name", rs.getString("name"));
                equipment.add(equipmentProperty);
            }

            result.put(EQUIPMENT, equipment);
            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get equipment property.", ex);
            throw new RepositoryException("Could not get equipment property.");
        }
    }

    public void removeEquipmentProperty(int propertyId, int equipmentId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_REMOVE_EQUIPMENT_PROPERTY.getQuery())) {
            stmt.setInt(1, propertyId);
            stmt.setInt(2, equipmentId);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not remove equipment property.", ex);
            throw new RepositoryException("Could not remove equipment property.");
        }
    }

    public JsonObject getEquipmentTypes() {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_EQUIPMENT_TYPES.getQuery())) {
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            JsonArray equipmentTypes = new JsonArray();

            while (rs.next()) {
                JsonObject equipmentType = new JsonObject();
                equipmentType.put("type", rs.getInt("type"));
                equipmentType.put("name", rs.getString("name"));
                equipmentTypes.add(equipmentType);
            }

            result.put("equipmentTypes", equipmentTypes);
            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get equipment types.", ex);
            throw new RepositoryException("Could not get equipment types.");
        }
    }

    public JsonObject getUser(String userId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_USER.getQuery())) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();

            if (rs.next()) {
                result.put("id", rs.getString("id"));
                result.put("fullName", rs.getString(FULL_NAME));
            }

            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get user.", ex);
            throw new RepositoryException("Could not get user.");
        }
    }

    public JsonObject getProperties(String userId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_USER_PROPERTIES.getQuery())) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            JsonArray properties = new JsonArray();

            while (rs.next()) {
                properties.add(makeProperty(rs));
            }

            result.put("owner", userId);
            result.put("properties", properties);
            return result;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, COULD_NOT_GET_PROPERTIES_ERROR, ex);
            throw new RepositoryException(COULD_NOT_GET_PROPERTIES_ERROR);
        }
    }

    public void requestRemoveProperty(int propertyId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_REQUEST_REMOVE_PROPERTY.getQuery())) {
            stmt.setInt(1, propertyId);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not request remove property.", ex);
            throw new RepositoryException("Could not request remove property.");
        }
    }

    public JsonObject getRequestedRemoveProperties() {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_REQUESTED_REMOVE_PROPERTIES.getQuery())) {
            return getEntries(stmt);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get requested remove properties.", ex);
            throw new RepositoryException("Could not get requested remove properties.");
        }
    }

    public void approveRemoveProperty(int propertyId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_APPROVE_REMOVE_PROPERTY.getQuery())) {
            stmt.setInt(1, propertyId);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not approve remove property.", ex);
            throw new RepositoryException("Could not approve remove property.");
        }
    }

    public void changePropertyTier(int propertyId, int tier) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_CHANGE_PROPERTY_TIER.getQuery())) {
            stmt.setInt(1, tier);
            stmt.setInt(2, propertyId);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not change property tier.", ex);
            throw new RepositoryException("Could not change property tier.");
        }
    }

    public JsonObject getUsers(int limit, int offset, String search) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_USERS.getQuery())) {
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);

            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            JsonArray users = new JsonArray();

            while (rs.next()) {
                JsonObject user = new JsonObject();
                user.put("id", rs.getString("id"));
                user.put("fullName", rs.getString(FULL_NAME));
                users.add(user);
            }

            result.put("users", users);
            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not get users.", e);
            throw new RepositoryException("Could not get users.");
        }
    }

    public JsonObject getProperties(int limit, int offset, String search) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_PROPERTIES.getQuery())) {
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);

            return getEntries(stmt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, COULD_NOT_GET_PROPERTIES_ERROR, e);
            throw new RepositoryException(COULD_NOT_GET_PROPERTIES_ERROR);
        }
    }

    public List<Integer> getFreeDrones(int propertyId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_FREE_DRONES.getQuery())) {
            stmt.setInt(1, propertyId);

            ResultSet rs = stmt.executeQuery();
            List<Integer> result = new ArrayList<>();

            while (rs.next()) {
                result.add(rs.getInt("id"));
            }

            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not get free drones.", e);
            throw new RepositoryException("Could not get free drones.");
        }
    }

    public void dispatchDrone(int droneId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_DISPATCH_DRONE.getQuery())) {
            stmt.setInt(1, droneId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not dispatch drone.", e);
            throw new RepositoryException("Could not dispatch drone.");
        }
    }

    public JsonObject getDispatchedDrones(int limit, int offset, String search) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_DISPATCHED_DRONES.getQuery())) {
            stmt.setString(1, search);
            stmt.setString(2, search);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);

            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();
            JsonArray drones = new JsonArray();

            while (rs.next()) {
                JsonObject drone = new JsonObject();
                drone.put("id", rs.getInt("id"));
                drone.put(DESCRIPTION, rs.getString(DESCRIPTION));
                drone.put("propertyId", rs.getInt("property_id"));
                drones.add(drone);
            }

            result.put("drones", drones);
            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not get dispatched drones.", e);
            throw new RepositoryException("Could not get dispatched drones.");
        }
    }

    private JsonObject searchStatusProperties(String status, String search, int limit, int offset) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_SEARCH_STATUS_PROPERTIES.getQuery())) {
            stmt.setString(1, status);
            stmt.setString(2, search);
            stmt.setString(3, search);
            stmt.setInt(4, limit);
            stmt.setInt(5, offset);

            return getEntries(stmt);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not search properties.", e);
            throw new RepositoryException("Could not search properties.");
        }
    }

    private JsonObject getEntries(PreparedStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        JsonObject result = new JsonObject();
        JsonArray properties = new JsonArray();

        while (rs.next()) {
            properties.add(makeProperty(rs));
        }

        result.put("properties", properties);
        return result;
    }

    public JsonObject searchPendingProperties(String search, int limit, int offset) {
        return searchStatusProperties("PENDING", search, limit, offset);
    }

    public JsonObject searchRemovalProperties(String search, int limit, int offset) {
        return searchStatusProperties("REMOVED", search, limit, offset);
    }

    public JsonObject getPropertyDetailed(int propertyId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_GET_PROPERTY_DETAILED.getQuery())) {
            stmt.setInt(1, propertyId);

            ResultSet rs = stmt.executeQuery();
            JsonObject result = new JsonObject();

            if (rs.next()) {
                result.put("id", rs.getInt("property_id"));
                result.put(LOCATION, rs.getString("property_location"));
                result.put("x", rs.getInt("property_x"));
                result.put("y", rs.getInt("property_y"));
                result.put(WIDTH, rs.getInt("property_width"));
                result.put(HEIGHT, rs.getInt("property_height"));
                result.put(STATUS, rs.getString("property_status"));
                result.put(DESCRIPTION, rs.getString("property_description"));
                result.put("tier", rs.getString("tier_id"));
                result.put("tier_name", rs.getString("tier_name"));
                result.put("owner", rs.getString("owner_id"));
                result.put("owner_full_name", rs.getString("owner_full_name"));

                JsonObject equipment = getEquipmentProperty(propertyId);
                result.put(EQUIPMENT, equipment.getValue(EQUIPMENT));
            }

            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not get property detailed.", e);
            throw new RepositoryException("Could not get property detailed.");
        }
    }

    public void changePropertyCoordinates(int propertyId, int x, int y) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_CHANGE_PROPERTY_COORDINATES.getQuery())) {
            stmt.setInt(1, x);
            stmt.setInt(2, y);
            stmt.setInt(3, propertyId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not change property location.", e);
            throw new RepositoryException("Could not change property location.");
        }
    }

    public void recallDrone(int droneId) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_RECALL_DRONE.getQuery())) {
            stmt.setInt(1, droneId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not recall drone.", e);
            throw new RepositoryException("Could not recall drone.");
        }
    }
}
