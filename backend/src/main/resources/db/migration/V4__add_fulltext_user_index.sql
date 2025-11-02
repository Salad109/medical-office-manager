ALTER TABLE users
    ADD FULLTEXT INDEX idx_users_fulltext (first_name, last_name);