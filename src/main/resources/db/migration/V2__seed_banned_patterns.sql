-- V2__seed_banned_patterns.sql
-- Policy Seeds - baseline safety controls
-- Notes:
--  - Prefer format-based patterns (REGEX) for PII to reduce false positives.
--  - Keep keyword list minimal; expand through admin UI as needed.
--  - Inserts are idempotent via ON CONFLICT DO NOTHING.

-- ================
-- PII / Doxxing
-- ================
INSERT INTO banned_patterns(pattern, type, enabled, severity, notes)
VALUES
    -- Email addresses
    ('(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}', 'REGEX', true, 'MEDIUM', 'Email address pattern'),

    -- TR mobile phone (basic)
    ('(?i)(\+?90\s?)?(5\d{2})[\s-]?\d{3}[\s-]?\d{2}[\s-]?\d{2}', 'REGEX', true, 'MEDIUM', 'TR phone number pattern'),

    -- IBAN TR (basic)
    ('(?i)\bTR\d{2}\s?\d{4}\s?\d{4}\s?\d{4}\s?\d{4}\s?\d{2}\b', 'REGEX', true, 'HIGH', 'TR IBAN pattern'),

    -- Credit card-like sequences (13-19 digits, tolerant to spaces/dashes)
    ('\b(?:\d[ -]*?){13,19}\b', 'REGEX', true, 'HIGH', 'Card-number-like sequence')
    ON CONFLICT (pattern) DO NOTHING;

-- ================
-- Self-harm signals (minimal keyword set)
-- ================
INSERT INTO banned_patterns(pattern, type, enabled, severity, notes)
VALUES
    ('intihar', 'KEYWORD', true, 'HIGH', 'Self-harm indicator'),
    ('kendime zarar', 'KEYWORD', true, 'HIGH', 'Self-harm indicator')
    ON CONFLICT (pattern) DO NOTHING;

-- ================
-- Harm / illegal instruction intent (minimal keyword set)
-- ================
INSERT INTO banned_patterns(pattern, type, enabled, severity, notes)
VALUES
    ('bomba yap', 'KEYWORD', true, 'HIGH', 'Violence/illegal instruction intent'),
    ('silah yap', 'KEYWORD', true, 'HIGH', 'Violence/illegal instruction intent')
    ON CONFLICT (pattern) DO NOTHING;
