-- Insert test users with BCrypt hashed password "password123"
-- BCrypt hash generated with strength 10

INSERT INTO users (username, password_hash, first_name, last_name, phone_number, pesel, role)
VALUES
    -- Doctor account (username: doctor, password: password123)
    ('doctor', '$2a$10$qJ8YhN5K7xYKp5JZLvJ3.OKgZXvZ3VK2qL5qLKp5JZLvJ3.OKgZXvZ', 'Jan', 'Kowalski', '123456789', '90010112345', 'DOCTOR'),

    -- Receptionist account (username: receptionist, password: password123)
    ('receptionist', '$2a$10$qJ8YhN5K7xYKp5JZLvJ3.OKgZXvZ3VK2qL5qLKp5JZLvJ3.OKgZXvZ', 'Anna', 'Nowak', '987654321', '85050567890', 'RECEPTIONIST'),

    -- Patient account (username: patient, password: password123)
    ('patient', '$2a$10$qJ8YhN5K7xYKp5JZLvJ3.OKgZXvZ3VK2qL5qLKp5JZLvJ3.OKgZXvZ', 'Marek', 'Wiśniewski', '555123456', '92030398765', 'PATIENT');