-- V3__seed_users.sql
-- Demo Users (BCrypt)
-- Purpose:
--  - Provide deterministic accounts for local development & Swagger testing
--  - Passwords are BCrypt-hashed (compatible with Spring Security BCryptPasswordEncoder)

INSERT INTO users(email, password_hash, role)
VALUES
    ('admin@local', '$2b$10$UDqTrFojh6U4O.wjyTZxl.8LrvvFSol1pkJDGdj69TvpqvZZ1Qujy', 'ADMIN'),
    ('user@local',  '$2b$10$6JkWYC7W1.8rHJv0vXX2Ne4YAsFE/nD0341IqRw8rtR5b01gBstkS', 'USER')
    ON CONFLICT (email) DO NOTHING;
