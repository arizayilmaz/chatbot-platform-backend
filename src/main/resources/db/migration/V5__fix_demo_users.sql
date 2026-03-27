-- V5__fix_demo_users.sql
-- Create valid email accounts for demo to pass @Email validation
-- admin@local.dev / admin123
-- user@local.dev / user123

INSERT INTO users(email, password_hash, role)
VALUES
    ('admin@local.dev', '$2b$10$UDqTrFojh6U4O.wjyTZxl.8LrvvFSol1pkJDGdj69TvpqvZZ1Qujy', 'ADMIN'),
    ('user@local.dev',  '$2b$10$6JkWYC7W1.8rHJv0vXX2Ne4YAsFE/nD0341IqRw8rtR5b01gBstkS', 'USER')
    ON CONFLICT (email) DO NOTHING;
