-- V1__init.sql
-- Chatbot Platform - Initial Schema (PostgreSQL)
-- Purpose:
--  - Create core tables for authentication, conversations, messages, and policy patterns
--  - Enforce integrity (FKs, checks, uniqueness)
--  - Add essential indexes for common query paths

-- UUID generation (pgcrypto provides gen_random_uuid())
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- USERS
-- =========================
CREATE TABLE IF NOT EXISTS users (
                                     id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    role          VARCHAR(32) NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_role_chk CHECK (role IN ('USER', 'ADMIN'))
    );

-- =========================
-- CONVERSATIONS
-- =========================
CREATE TABLE IF NOT EXISTS conversations (
                                             id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL,
    title      TEXT NOT NULL DEFAULT 'New chat',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT conversations_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_conversations_user_id
    ON conversations(user_id);

-- =========================
-- MESSAGES
-- =========================
CREATE TABLE IF NOT EXISTS messages (
                                        id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL,
    role          VARCHAR(32) NOT NULL,
    content       TEXT NOT NULL,
    blocked       BOOLEAN NOT NULL DEFAULT false,
    blocked_reason TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT messages_conversation_id_fkey
    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
    ON DELETE CASCADE,

    CONSTRAINT messages_role_chk
    CHECK (role IN ('USER', 'ASSISTANT'))
    );

-- Most frequent query: fetch messages by conversation ordered by time
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created
    ON messages(conversation_id, created_at);

-- =========================
-- BANNED / POLICY PATTERNS
-- =========================
CREATE TABLE IF NOT EXISTS banned_patterns (
                                               id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern    TEXT NOT NULL,
    type       VARCHAR(32) NOT NULL,         -- KEYWORD / REGEX
    enabled    BOOLEAN NOT NULL DEFAULT true,
    severity   VARCHAR(32) NOT NULL DEFAULT 'MEDIUM', -- LOW / MEDIUM / HIGH
    notes      TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT banned_patterns_type_chk
    CHECK (type IN ('KEYWORD', 'REGEX')),

    CONSTRAINT banned_patterns_severity_chk
    CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH')),

    CONSTRAINT banned_patterns_pattern_key UNIQUE (pattern)
    );

CREATE INDEX IF NOT EXISTS idx_banned_patterns_enabled
    ON banned_patterns(enabled);

CREATE INDEX IF NOT EXISTS idx_banned_patterns_type
    ON banned_patterns(type);
