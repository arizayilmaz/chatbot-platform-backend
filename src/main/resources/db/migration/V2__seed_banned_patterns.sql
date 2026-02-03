INSERT INTO banned_patterns(pattern, type, enabled, severity, notes)
VALUES
    ('küfür', 'KEYWORD', true, 'HIGH', 'örnek keyword');

INSERT INTO banned_patterns(pattern, type, enabled, severity, notes)
VALUES
    ('(?i)\b(küfür1|küfür2)\b', 'REGEX', true, 'HIGH', 'örnek regex');
