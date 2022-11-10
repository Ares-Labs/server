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
import java.util.logging.Level;
import java.util.logging.Logger;

enum Queries {
    SQL_ADD_PROPERTY("INSERT INTO properties (location,  tier, x, y, width, height, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"),
    SQL_REMOVE_PROPERTY("DELETE FROM properties WHERE id = ?;"),
    SQL_GET_PROPERTY("SELECT * FROM properties WHERE id = ?;"),
    SQL_GET_PROPERTY_ALLOWED_USERS("SELECT * FROM users WHERE id IN (SELECT user_id FROM property_whitelists WHERE property_id = ?)"),
    SQL_ADD_PROPERTY_WHITELIST("INSERT INTO property_whitelists (property_id, user_id) VALUES (?, ?)"),
    SQL_REMOVE_PROPERTY_WHITELIST("DELETE FROM property_whitelists WHERE property_id = ? AND user_id = ?"),
    SQL_CHANGE_PROPERTY_STATUS("UPDATE properties SET status = ? WHERE id = ?;"),
    SQL_ADD_AUTH_ENTRY("INSERT INTO authorizations (property_id, user_id) VALUES (?, ?);"),
    SQL_GET_AUTH_ENTRIES("SELECT * FROM authorizations WHERE property_id = ?"),
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

    public void insertProperty(String clientId, String location, int tier, int x, int y, int width, int height, String status, String description) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_PROPERTY.getQuery(), Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, location);
            stmt.setInt(2, tier);
            stmt.setInt(3, x);
            stmt.setInt(4, y);
            stmt.setInt(5, width);
            stmt.setInt(6, height);
            stmt.setString(7, status);
            stmt.setString(8, description);

            stmt.executeUpdate();

            int returnedId = stmt.getGeneratedKeys().getInt(1);

            // Add property to users properties
            try (PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO user_properties (user_id, property_id) VALUES (?, ?);")) {
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
                    return new JsonObject().put("id", rs.getInt("id")).put("location", rs.getString("location")).put("description", rs.getString("description")).put("tier", rs.getInt("tier")).put("x", rs.getInt("x")).put("y", rs.getInt("y")).put("width", rs.getInt("width")).put("height", rs.getInt("height"));
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

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new RepositoryException("No property found with location: " + location);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not remove property", ex);
            throw new RepositoryException("Could not remove property");
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

    public boolean addAllowedUser(String propertyId, String userId) {
        // Add a user to the whitelist of a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_PROPERTY_WHITELIST.getQuery())) {
            stmt.setString(1, propertyId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not add allowed user.", ex);
            throw new RepositoryException("Could not add allowed user.");
        }
    }

    public boolean removeAllowedUser(String propertyId, String userId) {
        // Remove a user from the whitelist of a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_REMOVE_PROPERTY_WHITELIST.getQuery())) {
            stmt.setString(1, propertyId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not remove allowed user.", ex);
            throw new RepositoryException("Could not remove allowed user.");
        }
    }

    public boolean changePropertyStatus(int id, String status) {
        // Change the status of a property
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_CHANGE_PROPERTY_STATUS.getQuery())) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not change property status.", ex);
            throw new RepositoryException("Could not change property status.");
        }
    }

    public boolean addAuthEntry(String propertyId, String userId) {
        // Add an entry to the auth table
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(Queries.SQL_ADD_AUTH_ENTRY.getQuery())) {
            stmt.setString(1, propertyId);
            stmt.setString(2, userId);
            stmt.executeUpdate();
            return true;
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
}
