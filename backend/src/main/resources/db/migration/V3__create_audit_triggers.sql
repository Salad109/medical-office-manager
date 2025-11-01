DELIMITER
//

-- Users table triggers
CREATE TRIGGER audit_users_insert
    AFTER INSERT
    ON users
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
    VALUES (@current_user_id, 'INSERT', 'USER', NEW.id, NULL,
            JSON_OBJECT(
                    'first_name', NEW.first_name,
                    'last_name', NEW.last_name,
                    'phone_number', NEW.phone_number,
                    'pesel', NEW.pesel,
                    'role', NEW.role
            ));
END//

CREATE TRIGGER audit_users_update
    AFTER UPDATE
    ON users
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
    VALUES (@current_user_id, 'UPDATE', 'USER', NEW.id,
            JSON_OBJECT(
                    'first_name', OLD.first_name,
                    'last_name', OLD.last_name,
                    'phone_number', OLD.phone_number,
                    'pesel', OLD.pesel,
                    'role', OLD.role
            ),
            JSON_OBJECT(
                    'first_name', NEW.first_name,
                    'last_name', NEW.last_name,
                    'phone_number', NEW.phone_number,
                    'pesel', NEW.pesel,
                    'role', NEW.role
            ));
END//

-- Appointments table triggers
CREATE TRIGGER audit_appointments_insert
    AFTER INSERT
    ON appointments
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
    VALUES (@current_user_id, 'INSERT', 'APPOINTMENT', NEW.id, NULL,
            JSON_OBJECT(
                    'patient_id', NEW.patient_id,
                    'appointment_date', NEW.appointment_date,
                    'appointment_time', NEW.appointment_time,
                    'status', NEW.status
            ));
END//

CREATE TRIGGER audit_appointments_update
    AFTER UPDATE
    ON appointments
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
    VALUES (@current_user_id, 'UPDATE', 'APPOINTMENT', NEW.id,
            JSON_OBJECT(
                    'patient_id', OLD.patient_id,
                    'appointment_date', OLD.appointment_date,
                    'appointment_time', OLD.appointment_time,
                    'status', OLD.status
            ),
            JSON_OBJECT(
                    'patient_id', NEW.patient_id,
                    'appointment_date', NEW.appointment_date,
                    'appointment_time', NEW.appointment_time,
                    'status', NEW.status
            ));
END//

CREATE TRIGGER audit_appointments_delete
    AFTER DELETE
    ON appointments
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
    VALUES (@current_user_id, 'DELETE', 'APPOINTMENT', OLD.id,
            JSON_OBJECT(
                    'patient_id', OLD.patient_id,
                    'appointment_date', OLD.appointment_date,
                    'appointment_time', OLD.appointment_time,
                    'status', OLD.status
            ), NULL);
END//

-- Visits table triggers
CREATE TRIGGER audit_visits_insert
    AFTER INSERT
    ON visits
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
    VALUES (@current_user_id, 'INSERT', 'VISIT', NEW.id, NULL,
            JSON_OBJECT(
                    'appointment_id', NEW.appointment_id,
                    'notes', NEW.notes,
                    'completed_by_doctor_id', NEW.completed_by_doctor_id
            ));
END//

CREATE TRIGGER audit_visits_update
    AFTER UPDATE
    ON visits
    FOR EACH ROW
BEGIN
    INSERT INTO audit_log (user_id, action, entity_type, entity_id, old_values, new_values)
    VALUES (@current_user_id, 'UPDATE', 'VISIT', NEW.id,
            JSON_OBJECT(
                    'appointment_id', OLD.appointment_id,
                    'notes', OLD.notes,
                    'completed_by_doctor_id', OLD.completed_by_doctor_id
            ),
            JSON_OBJECT(
                    'appointment_id', NEW.appointment_id,
                    'notes', NEW.notes,
                    'completed_by_doctor_id', NEW.completed_by_doctor_id
            ));
END//

DELIMITER;
