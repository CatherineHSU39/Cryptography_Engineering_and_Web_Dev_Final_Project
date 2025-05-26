-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    encrypted_totp_secret VARCHAR,
    role VARCHAR NOT NULL CHECK (role IN ('USER', 'ADMIN', 'AUDITOR')),
    fetch_new_at TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Groups Table
CREATE TABLE IF NOT EXISTS groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL REFERENCES users(id),
    group_id UUID NOT NULL REFERENCES groups(id),
    encrypted_message VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- GroupMembers Table
CREATE TABLE IF NOT EXISTS group_members (
    group_id UUID NOT NULL REFERENCES groups(id),
    user_id UUID NOT NULL REFERENCES users(id),
    read_at TIMESTAMP NOT NULL DEFAULT now(),
    joined_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (group_id, user_id)
);

-- EncryptedDEKs Table
CREATE TABLE IF NOT EXISTS encrypted_deks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES users(id),
    entity_id UUID NOT NULL,
    cmk_version INTEGER NOT NULL CHECK (cmk_version >= 1),
    encrypted_dek BYTEA NOT NULL,
    purpose VARCHAR CHECK (purpose IN ('message', 'totp')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- CMKs Table
CREATE TABLE IF NOT EXISTS cmks (
    id UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    version INTEGER NOT NULL CHECK (version >= 1),
    key_material BYTEA NOT NULL,
    active BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (id, version)
);

-- AuditLog Tables
CREATE TABLE IF NOT EXISTS kms_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR NOT NULL,
    target_id UUID,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    hash VARCHAR NOT NULL,
    prev_hash VARCHAR NOT NULL
);

CREATE TABLE backend_audit_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    user_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,         -- e.g., 'CREATE', 'UPDATE', 'DELETE'
    entity_type VARCHAR(50) NOT NULL,    -- e.g., 'User', 'Group', etc.
    entity_id UUID NOT NULL,
    column_name VARCHAR(50) NOT NULL,
    old_value TEXT,                      -- optional: store old value
    new_value TEXT                       -- optional: store new value
);