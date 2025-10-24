CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    password_hash VARCHAR(60) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    pesel VARCHAR(11),
    role ENUM('PATIENT', 'DOCTOR', 'RECEPTIONIST') NOT NULL
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (appointment_date, appointment_time)
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE visits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    notes TEXT,
    completed_by_doctor_id BIGINT NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE RESTRICT,
    FOREIGN KEY (completed_by_doctor_id) REFERENCES users(id) ON DELETE RESTRICT
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(20) NOT NULL,
    entity_type VARCHAR(30),
    entity_id BIGINT,
    old_values JSON,
    new_values JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) DEFAULT CHARSET=utf8mb4;
