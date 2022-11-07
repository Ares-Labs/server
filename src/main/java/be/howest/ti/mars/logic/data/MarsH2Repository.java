package be.howest.ti.mars.logic.data;

import be.howest.ti.mars.logic.exceptions.RepositoryException;
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
}
