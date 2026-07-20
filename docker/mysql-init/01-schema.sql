-- Demo hotel schema used by the DB-layer tests (accessed via the SSH tunnel).
CREATE TABLE room (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    type VARCHAR(32) NOT NULL,
    price DECIMAL(8, 2) NOT NULL
);

CREATE TABLE booking (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_id INT NOT NULL,
    guest_first_name VARCHAR(64) NOT NULL,
    guest_last_name VARCHAR(64) NOT NULL,
    checkin DATE NOT NULL,
    checkout DATE NOT NULL,
    deposit_paid BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES room (id)
);

INSERT INTO room (name, type, price) VALUES
    ('101', 'single', 80.00),
    ('102', 'double', 120.00),
    ('201', 'double', 130.00),
    ('202', 'suite', 250.00),
    ('301', 'family', 180.00);

INSERT INTO booking (room_id, guest_first_name, guest_last_name, checkin, checkout, deposit_paid) VALUES
    (1, 'Alice', 'Wilson', '2026-01-05', '2026-01-08', TRUE),
    (2, 'Bob', 'Taylor', '2026-01-10', '2026-01-12', FALSE),
    (3, 'Carol', 'Nguyen', '2026-02-01', '2026-02-05', TRUE),
    (4, 'David', 'Smith', '2026-02-14', '2026-02-16', TRUE),
    (5, 'Erin', 'Johnson', '2026-03-01', '2026-03-07', FALSE),
    (1, 'Frank', 'Miller', '2026-03-10', '2026-03-11', TRUE),
    (2, 'Grace', 'Lee', '2026-04-02', '2026-04-06', TRUE),
    (3, 'Henry', 'Brown', '2026-04-20', '2026-04-22', FALSE),
    (4, 'Iris', 'Davis', '2026-05-01', '2026-05-03', TRUE),
    (5, 'Jack', 'Wilson', '2026-05-15', '2026-05-20', TRUE);
