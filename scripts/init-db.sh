#!/bin/bash
set -e

# Load environment variables
source .env
ENVIRONMENT=${1:-production}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "⏳ Waiting for Postgres to become ready..."
timeout=30
elapsed=0
while ! docker exec db pg_isready -U "$POSTGRES_USER" > /dev/null 2>&1; do
  sleep 1
  elapsed=$((elapsed + 1))
  if [ "$elapsed" -ge "$timeout" ]; then
    echo "❌ PostgreSQL did not become ready within ${timeout} seconds."
    exit 1
  fi
done

echo "🧹 Dropping existing tables and views..."

docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOF
DROP VIEW IF EXISTS users_backend_view CASCADE;
DROP VIEW IF EXISTS users_kms_view CASCADE;

DROP TABLE IF EXISTS
  backend_audit_log,
  kms_audit_log,
  encrypted_deks,
  cmks,
  group_members,
  messages,
  groups,
  users,
  kms_users
CASCADE;
EOF

echo "🧹 Dropping existing tables and views..."

docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOF
DROP VIEW IF EXISTS users_backend_view CASCADE;

DROP TABLE IF EXISTS
  backend_audit_log,
  kms_audit_log,
  encrypted_deks,
  cmks,
  group_members,
  messages,
  groups,
  users
CASCADE;
EOF

echo "📐 Creating database schema from init-schema.sql..."
docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$SCRIPT_DIR/init-schema.sql"

echo "🔐 Creating roles and assigning precise permissions..."
docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOF

-- Create KMS role if not exists
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${KMS_USER}') THEN
    CREATE ROLE ${KMS_USER} WITH LOGIN PASSWORD '${KMS_PASSWORD}';
  END IF;
END
\$\$;

-- Create Backend role if not exists
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${BACKEND_USER}') THEN
    CREATE ROLE ${BACKEND_USER} WITH LOGIN PASSWORD '${BACKEND_PASSWORD}';
  END IF;
END
\$\$;

-- Create dek role if not exists
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DEK_USER}') THEN
    CREATE ROLE ${DEK_USER} WITH LOGIN PASSWORD '${DEK_PASSWORD}';
  END IF;
END
\$\$;

-- Create Auth Server role if not exists
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${AUTH_USER}') THEN
    CREATE ROLE ${AUTH_USER} WITH LOGIN PASSWORD '${AUTH_PASSWORD}';
  END IF;
END
\$\$;

-- Create safe view of user data for backend (no password, no TOTP secret)
CREATE OR REPLACE VIEW users_backend_view AS
SELECT id, username, fetch_new_at, created_at
FROM users;

-- Create safe view of user data for kms (no password, no TOTP secret)
CREATE OR REPLACE VIEW users_kms_view AS
SELECT id, user_pub_pem, created_at
FROM users;

-- Grant precise permissions
-- Backend
GRANT SELECT, INSERT, UPDATE, DELETE ON messages, groups, group_members TO ${BACKEND_USER};

-- Grant SELECT on the view to backend only
GRANT SELECT, UPDATE ON users_backend_view TO ${BACKEND_USER};

-- Grant SELECT, INSERT on the backend_audit_log
GRANT SELECT, INSERT on backend_audit_log TO ${BACKEND_USER};
GRANT USAGE, SELECT ON SEQUENCE backend_audit_log_id_seq TO ${BACKEND_USER};

-- Grant SELECT, INSERT, UPDATE on the dek
GRANT SELECT, INSERT, UPDATE on encrypted_deks TO ${DEK_USER};

-- Auth Server
GRANT SELECT, INSERT, UPDATE ON users, encrypted_deks TO ${AUTH_USER};
GRANT INSERT ON kms_audit_log TO ${AUTH_USER};

-- KMS
GRANT SELECT, INSERT, UPDATE ON encrypted_deks, cmks TO ${KMS_USER};
GRANT SELECT, INSERT ON kms_audit_log TO ${KMS_USER};

-- Grant SELECT on the view to kms only
GRANT SELECT, UPDATE ON users_kms_view TO ${KMS_USER};

EOF


# echo "🛡️ Enabling RLS policies..."
# docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOF

# -- Enable RLS
# ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

# -- RLS Policy: Only show messages if user is a member of the group
# CREATE POLICY user_group_messages_policy ON messages
# USING (
#   EXISTS (
#     SELECT 1 FROM group_members
#     WHERE group_members.group_id = messages.group_id
#       AND group_members.user_id = current_setting('app.current_user_id')::uuid
#   )
# );
# EOF

if [ "$ENVIRONMENT" = "development" ]; then
  echo "📥 Seeding development data from seed-dev-data.sql..."
  docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$SCRIPT_DIR/seed-dev-data.sql"
fi

echo "🎉 Database initialization complete (${ENVIRONMENT} mode)."