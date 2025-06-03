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

echo "🧹 Dropping old schemas..."
docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOF
DROP SCHEMA IF EXISTS backend CASCADE;
DROP SCHEMA IF EXISTS auth CASCADE;
DROP SCHEMA IF EXISTS dek CASCADE;
DROP SCHEMA IF EXISTS kms CASCADE;
EOF

echo "📐 Creating isolated schemas..."
docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOF
-- Create roles if not exists
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${KMS_USER}') THEN
    CREATE ROLE ${KMS_USER} WITH LOGIN PASSWORD '${KMS_PASSWORD}';
  END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${BACKEND_USER}') THEN
    CREATE ROLE ${BACKEND_USER} WITH LOGIN PASSWORD '${BACKEND_PASSWORD}';
  END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DEK_USER}') THEN
    CREATE ROLE ${DEK_USER} WITH LOGIN PASSWORD '${DEK_PASSWORD}';
  END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${AUTH_USER}') THEN
    CREATE ROLE ${AUTH_USER} WITH LOGIN PASSWORD '${AUTH_PASSWORD}';
  END IF;
END
\$\$;

-- Create one schema per service
CREATE SCHEMA backend AUTHORIZATION ${BACKEND_USER};
CREATE SCHEMA auth AUTHORIZATION ${AUTH_USER};
CREATE SCHEMA dek AUTHORIZATION ${DEK_USER};
CREATE SCHEMA kms AUTHORIZATION ${KMS_USER};

-- Grant usage only on their own schemas
GRANT USAGE ON SCHEMA backend TO ${BACKEND_USER};
GRANT USAGE ON SCHEMA auth TO ${AUTH_USER};
GRANT USAGE ON SCHEMA dek TO ${DEK_USER};
GRANT USAGE ON SCHEMA kms TO ${KMS_USER};

-- Restrict access to other schemas
REVOKE ALL ON SCHEMA public FROM PUBLIC;

-- Optional: Set default search path per user
ALTER ROLE ${BACKEND_USER} SET search_path TO backend;
ALTER ROLE ${AUTH_USER} SET search_path TO auth;
ALTER ROLE ${DEK_USER} SET search_path TO dek;
ALTER ROLE ${KMS_USER} SET search_path TO kms;
EOF

echo "🔐 Schemas created. You can now run Liquibase in each Spring Boot app with its schema."
