package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.exceptions.RepositoryException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.h2.tools.Server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
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
                if (rs.next()) {
                    JsonObject property = new JsonObject();
                    property.put("id", rs.getInt("id"));
                    property.put("location", rs.getString("location"));
                    property.put("tier", rs.getInt("tier"));
                    property.put("x", rs.getInt("x"));
                    property.put("y", rs.getInt("y"));
                    property.put("width", rs.getInt("width"));
                    property.put("height", rs.getInt("height"));
                    property.put("status", rs.getString("status"));
                    property.put("description", rs.getString("description"));


                    return property;
                } else {
                    return null;
                }
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
                result.getJsonObject("allowedUsers").put(rs.getString("id"), rs.getString("full_name"));
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
                entry.put("user_id", rs.getString("user_id"));
                entry.put("timestamp", rs.getString("timestamp"));
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
            result.put("pendingProperties", new JsonArray());

            while (rs.next()) {
                result.getJsonArray("pendingProperties").add(
                        new JsonObject()
                                .put("id", rs.getInt("id"))
                                .put("location", rs.getString("location"))
                                .put("description", rs.getString("description"))
                                .put("tier", rs.getInt("tier"))
                                .put("x", rs.getInt("x"))
                                .put("y", rs.getInt("y"))
                                .put("width", rs.getInt("width"))
                                .put("height", rs.getInt("height")));
            }
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
                alert.put("userId", rs.getString("user_id"));
                alert.put("timestamp", rs.getString("timestamp"));
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
}
