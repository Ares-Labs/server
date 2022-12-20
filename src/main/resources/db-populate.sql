INSERT INTO `tiers` (name, price)
VALUES ('Basic', 59.99),
       ('Premium', 99.99),
       ('Optimum', 149.99);

INSERT INTO `equipment_types` (name)
VALUES ('Camera'),
       ('Batch'),
       ('Drone');

-- Sample data:

-- Create new user
INSERT INTO users
VALUES ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e82', 'John Doe'),
       ('b66e49ae-55f3-11ed-a877-6f6036c8577a', 'Jane Doe'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 'Frankenstein'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e84', 'Mia Johnson'),
       ('b66e49ae-55f3-11ed-a877-6f6036c8577c', 'Adam Johnson'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8c', 'Emma Williams'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e85', 'Liam Williams'),
       ('b66e49ae-55f3-11ed-a877-6f6036c8577d', 'Olivia Brown'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8d', 'Noah Brown'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e86', 'Ava Jones'),
       ('b66e49ae-55f3-11ed-a877-6f6036c8577e', 'James Jones'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8e', 'Isabella Taylor'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e87', 'Oliver Taylor'),
       ('b66e49ae-55f3-11ed-a877-6f6036c8577f', 'Sophia Davis'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8f', 'William Davis'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e88', 'Charlotte Anderson'),
       ('b66e49ae-55f3-11ed-a877-6f6036c85780', 'Jacob Anderson'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f80', 'Abigail Martinez'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e89', 'Michael Martinez'),
       ('b66e49ae-55f3-11ed-a877-6f6036c85781', 'Emily Thompson'),
       ('bedb80fc-55f3-11ed-b681-07688aa63f81', 'Benjamin Thompson'),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e90', 'Elizabeth Jackson'),
       ('b66e49ae-55f3-11ed-a877-6f6036c85782', 'Daniel Jackson');

-- Create properties
INSERT INTO properties (location, x, y, width, height, tier, status, description)
VALUES ('wallstreet', 10, 10, 1200, 50, 3, 'PAID', 'Amazing wallstreet'),
       ('howest', 10, 60, 500, 500, 2, 'PENDING', 'Howest Brugge'),
       ('brussels', -1000, -20, 1000, 100, 3, 'REMOVED', 'Brussels (Belgium)');

-- Install equipment
INSERT INTO installed_equipment (description, property_id, type)
VALUES ('east camera', 1, 1),
       ('west camera', 1, 1),
       ('entry batch', 2, 2),
       ('central camera', 2, 1),
       ('drone', 1, 3),
       ('drone', 2, 3);

-- Set Frankenstein as the owner of wallstreet and howest
INSERT INTO user_properties (user_id, property_id)
VALUES ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 1),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 2);

-- Add John and Jane as white-listed for each property
INSERT INTO property_whitelists (user_id, property_id)
VALUES ('b66e49ae-55f3-11ed-a877-6f6036c8577a', 1),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e82', 2);

-- Add some alerts
INSERT INTO alerts (user_id, property_id)
VALUES ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 1),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 2),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e82', 2);

-- Add some scans
INSERT INTO scans (user_id, property_id, camera_id)
VALUES ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e82', 1, 1),
       ('9a0fbbc6-55f3-11ed-82ca-9313c9a89e82', 1, 2),
       ('bedb80fc-55f3-11ed-b681-07688aa63f8a', 2, 4);

INSERT INTO scans (timestamp, property_id, user_id, camera_id)
VALUES ('2022-11-4 00:00:00', 1, 'bedb80fc-55f3-11ed-b681-07688aa63f8a', 1);