-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    encrypted_totp_secret VARCHAR NOT NULL,
    role VARCHAR NOT NULL CHECK (role IN ('user', 'admin', 'auditor')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL REFERENCES users(id),
    group_id UUID,
    encrypted_message BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Groups Table
CREATE TABLE IF NOT EXISTS groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- GroupMembers Table
CREATE TABLE IF NOT EXISTS group_members (
    group_id UUID NOT NULL REFERENCES groups(id),
    user_id UUID NOT NULL REFERENCES users(id),
    joined_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (group_id, user_id)
);

-- EncryptedDEKs Table
CREATE TABLE IF NOT EXISTS encrypted_deks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES users(id),
    cmk_version INTEGER NOT NULL CHECK (cmk_version >= 1),
    encrypted_dek BYTEA NOT NULL,
    purpose VARCHAR CHECK (purpose IN ('message', 'totp')),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- MessageDEKLinks Table
CREATE TABLE IF NOT EXISTS message_dek_links (
    message_id UUID NOT NULL REFERENCES messages(id),
    recipient_id UUID NOT NULL REFERENCES users(id),
    dek_id UUID NOT NULL REFERENCES encrypted_deks(id),
    PRIMARY KEY (message_id, recipient_id)
);

-- UserTOTPDEKLinks Table
CREATE TABLE IF NOT EXISTS user_totp_dek_links (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    dek_id UUID NOT NULL UNIQUE REFERENCES encrypted_deks(id)
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

-- AuditLog Table
CREATE TABLE IF NOT EXISTS audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR NOT NULL,
    target_id UUID,
    timestamp TIMESTAMP NOT NULL DEFAULT now(),
    hash VARCHAR NOT NULL,
    prev_hash VARCHAR NOT NULL
);