DROP DATABASE IF EXISTS `ares_labs`;
CREATE DATABASE IF NOT EXISTS `ares_labs`;

USE `ares_labs`;

CREATE TABLE IF NOT EXISTS `users`
(
    id        VARCHAR(36) NOT NULL PRIMARY KEY,
    full_name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS `tiers`
(
    id    INTEGER        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(16)    NOT NULL,
    price DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS `properties`
(
    location VARCHAR(64) NOT NULL PRIMARY KEY,
    x        INTEGER,
    y        INTEGER,
    width    INTEGER,
    height   INTEGER,

    tier     INTEGER     NOT NULL,

    FOREIGN KEY (tier) REFERENCES tiers (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `user_properties`
(
    user_id           VARCHAR(36) NOT NULL,
    property_location VARCHAR(64) NOT NULL,

    PRIMARY KEY (user_id, property_location),

    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (property_location) REFERENCES properties (location)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `property_whitelists`
(
    user_id           VARCHAR(36) NOT NULL,
    property_location VARCHAR(64) NOT NULL,

    PRIMARY KEY (property_location, user_id),
    FOREIGN KEY (property_location) REFERENCES properties (location)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `equipment_types`
(
    type INTEGER     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(16) NOT NULL
);

CREATE TABLE IF NOT EXISTS `installed_equipment`
(
    id                INTEGER      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    description       VARCHAR(255) NOT NULL,

    property_location VARCHAR(64)  NOT NULL,
    type              INTEGER      NOT NULL,

    FOREIGN KEY (property_location) REFERENCES properties (location)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (type) REFERENCES equipment_types (type)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `alerts`
(
    id                INTEGER     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    timestamp         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    user_id           VARCHAR(36) NOT NULL,
    property_location VARCHAR(64) NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (property_location) REFERENCES properties (location)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
