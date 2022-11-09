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
<<<<<<< HEAD
    id       INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
    location VARCHAR(64)                        NOT NULL,
    x        INTEGER,
    y        INTEGER,
    width    INTEGER,
    height   INTEGER,

    tier     INTEGER                            NOT NULL,
=======
    id          INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
    location    VARCHAR(64)                        NOT NULL,
    x           INTEGER,
    y           INTEGER,
    width       INTEGER,
    height      INTEGER,

    tier        INTEGER                            NOT NULL,
    description VARCHAR(256),
>>>>>>> c9982d3 (:construction: WIP)

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
<<<<<<< HEAD
    user_id           VARCHAR(36) NOT NULL,
    property_id INTEGER NOT NULL,
=======
    user_id     VARCHAR(36) NOT NULL,
    property_id INTEGER     NOT NULL,
>>>>>>> c9982d3 (:construction: WIP)

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

<<<<<<< HEAD
    property_id INTEGER  NOT NULL,
    type              INTEGER      NOT NULL,
=======
    property_id INTEGER      NOT NULL,
    type        INTEGER      NOT NULL,
>>>>>>> c9982d3 (:construction: WIP)

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

<<<<<<< HEAD
    user_id           VARCHAR(36) NOT NULL,
    property_id INTEGER NOT NULL,
=======
    user_id     VARCHAR(36) NOT NULL,
    property_id INTEGER     NOT NULL,
>>>>>>> c9982d3 (:construction: WIP)

    FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
