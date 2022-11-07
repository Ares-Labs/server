USE `ares_labs`;

INSERT INTO `tiers` (name, price)
VALUES ('Basic', 59.99),
       ('Premium', 99.99),
       ('Optimum', 149.99);

INSERT INTO `equipment_types` (name)
VALUES ('Camera'),
       ('Batch');

-- Sample data:

-- Create new user
INSERT INTO users
VALUES ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e82', 'John Doe'),
       ('b66e49ae-55f3-11ed-a877-6f6036c8577a', 'Jane Doe'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 'Frankenstein');

-- Create properties
INSERT INTO properties (location, x, y, width, height, tier)
VALUES ('wallstreet', 10, 10, 1200, 50, 3),
       ('howest', 10, 60, 500, 500, 2);

-- Install equipment
INSERT INTO installed_equipment (description, property_location, type)
VALUES ('east camera', 'wallstreet', 1),
       ('west camera', 'wallstreet', 1),
       ('entry batch', 'howest', 2),
       ('central camera', 'howest', 1);

-- Set Frankenstein as the owner of wallstreet and howest
INSERT INTO user_properties (user_id, property_location)
VALUES ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 'wallstreet'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 'howest');

-- Add John and Jane as white-listed for each property
INSERT INTO property_whitelists (user_id, property_location)
VALUES ('b66e49ae-55f3-11ed-a877-6f6036c8577a', 'wallstreet'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e82', 'howest');

-- Add some alerts
INSERT INTO alerts (user_id, property_location)
VALUES ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 'wallstreet'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 'howest'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e82', 'wallstreet');
