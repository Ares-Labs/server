package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.exceptions.RepositoryException;
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

/* All queries:
-- Initial user registry
INSERT INTO users (id, full_name)
VALUES (?, ?);

-- Add a property
INSERT INTO properties (location, tier, x, y, width, height)
VALUES (?, ?, ?, ?, ?, ?);

-- Assign a property to a user
INSERT INTO user_properties (user_id, property_location)
VALUES (?, ?);

-- Add user to white list
INSERT INTO property_whitelists (user_id, property_location)
VALUES (?, ?);

-- Add equipment to a property
INSERT INTO installed_equipment (description, property_location, type)
VALUES (?, ?, ?);

-- Add an alert (this includes authorized users)
INSERT INTO alerts (user_id, property_location)
VALUES (?, ?);

-- Select a user's properties
SELECT *
FROM properties
WHERE location IN (SELECT property_location
                   FROM user_properties
                   WHERE user_id = ?);

-- Select whitelisted users of a property
SELECT *
FROM users
WHERE id IN (SELECT user_id
             FROM property_whitelists
             WHERE property_location = ?);

-- Select alerts from a user
SELECT *
FROM alerts
WHERE user_id = ?
ORDER BY timestamp DESC
LIMIT ?;

-- Select alerts and trespass from a property
SELECT a.id, a.timestamp, u.id, u.full_name
FROM alerts AS a
         JOIN users u ON a.user_id = u.id
WHERE property_location = ?
ORDER BY a.timestamp DESC
LIMIT ?;

-- Select alerts from users that are not whitelisted on a property
SELECT a.id, a.timestamp, u.id, u.full_name
FROM alerts AS a
         JOIN users u ON a.user_id = u.id
WHERE property_location = ?
  AND u.id NOT IN (SELECT w.user_id FROM property_whitelists AS w WHERE w.property_location = ?)
  AND u.id NOT IN (SELECT m.user_id FROM user_properties AS m WHERE m.property_location = ?)
ORDER BY a.timestamp DESC
LIMIT ?;

-- Get protected areas that you currently reside in
SELECT location, tier
FROM properties
WHERE x < ?
  AND x + width > ?
  AND y > ?
  AND y + height > ?;

-- Get all property locations
SELECT x, y, width, height
FROM properties;
 */

/*
This is only a starter class to use an H2 database.
In this start project there was no need for a Java interface MarsRepository.
Please always use interfaces when needed.

To make this class useful, please complete it with the topics seen in the module OOA & SD
 */

public class MarsH2Repository {
    public static final String SQL_ADD_PROPERTY = "INSERT INTO properties (location,  tier, description) VALUES (?, ?, ?);";
    public static final String SQL_REMOVE_PROPERTY = "DELETE FROM properties WHERE id = ?;";
    public static final String SQL_GET_PROPERTY = "SELECT * FROM properties WHERE id = ?;";
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
            this.dbWebConsole = Server.createWebServer(
                    "-ifNotExists",
                    "-webPort", String.valueOf(console)).start();
            LOGGER.log(Level.INFO, "Database web console started on port: {0}", console);
            this.generateData();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "DB configuration failed", ex);
            throw new RepositoryException("Could not configure MarsH2repository");
        }
    }

    public void insertProperty(String location, int tier, String description) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ADD_PROPERTY)) {
            stmt.setString(1, location);
            stmt.setInt(2, tier);
            stmt.setString(3, description);

            stmt.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not insert property", ex);
            throw new RepositoryException("Could not insert property");
        }
    }

    public JsonObject getProperty(String location) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_PROPERTY)) {
            stmt.setString(1, location);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new JsonObject()
                            .put("id", rs.getInt("id"))
                            .put("location", rs.getString("location"))
                            .put("description", rs.getString("description"))
                            .put("tier", rs.getInt("tier"))
                            .put("x", rs.getInt("x"))
                            .put("y", rs.getInt("y"))
                            .put("width", rs.getInt("width"))
                            .put("height", rs.getInt("height"));
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Could not get property", ex);
            throw new RepositoryException("Could not get property");
        }
    }

    public void removeProperty(String location) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_REMOVE_PROPERTY)) {
            stmt.setString(1, location);

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
        if (dbWebConsole != null && dbWebConsole.isRunning(false))
            dbWebConsole.stop();

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
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(createDbSql)
        ) {
            stmt.executeUpdate();
        }
    }

    private String readFile(String fileName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null)
            throw new RepositoryException("Could not read file: " + fileName);

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public JsonObject getAllowedUsers(String propertyId) {
        // Get whitelisted users of a property
        try (
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT * FROM users WHERE id IN (SELECT user_id FROM property_whitelists WHERE property_id = ?)"
                )
        ) {
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
}
