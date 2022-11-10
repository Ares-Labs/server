drop table if exists `user_properties`;
drop table if exists `property_whitelists`;
drop table if exists `alerts`;
drop table if exists `authorisations`;
drop table if exists `scans`;
drop table if exists `users`;
drop table if exists `installed_equipment`;
drop table if exists `properties`;
drop table if exists `tiers`;
drop table if exists `equipment_types`;

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
    id          INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
    location    VARCHAR(64)                        NOT NULL,
    x           INTEGER,
    y           INTEGER,
    width       INTEGER,
    height      INTEGER,
    status      VARCHAR(16)                        NOT NULL,
    tier        INTEGER                            NOT NULL,
    description VARCHAR(256),

    FOREIGN KEY (tier) REFERENCES tiers (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `user_properties`
(
    user_id     VARCHAR(36) NOT NULL,
    property_id INTEGER     NOT NULL,

    PRIMARY KEY (user_id, property_id),

    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `property_whitelists`
(
    user_id     VARCHAR(36) NOT NULL,
    property_id INTEGER     NOT NULL,

    PRIMARY KEY (user_id, property_id),
    FOREIGN KEY (property_id) REFERENCES properties (id)
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
    id          INTEGER      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,

    property_id INTEGER      NOT NULL,
    type        INTEGER      NOT NULL,

    FOREIGN KEY (property_id) REFERENCES properties (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (type) REFERENCES equipment_types (type)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `alerts`
(
    id          INTEGER     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    timestamp   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    user_id     VARCHAR(36) NOT NULL,
    property_id INTEGER     NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE IF NOT EXISTS `authorisations`
(
    timestamp   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    user_id     VARCHAR(36) NOT NULL,
    property_id INTEGER     NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

/* Get scans from all cameras */
CREATE TABLE IF NOT EXISTS `scans`
(
    timestamp   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    property_id INTEGER     NOT NULL,
    FOREIGN KEY (property_id) REFERENCES properties (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    user_id     VARCHAR(36) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    camera_id   INTEGER     NOT NULL,
    FOREIGN KEY (camera_id) REFERENCES installed_equipment (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);