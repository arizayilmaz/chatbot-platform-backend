-- Test user (MVP)
INSERT INTO users (id, email, password_hash, role)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           'test@local',
           'noop',
           'USER'
       )
    ON CONFLICT (email) DO NOTHING;
