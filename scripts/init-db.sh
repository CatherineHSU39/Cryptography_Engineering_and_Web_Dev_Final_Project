#!/bin/bash
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Load environment variables
source .env
MODE=${1:-production}

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

echo "📐 Creating database schema from init-schema.sql..."
docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$SCRIPT_DIR/init-schema.sql"

echo "🔐 Creating roles and assigning table permissions..."
docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOF

-- Create KMS role if not exists
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${KMS_USER}') THEN
    CREATE ROLE kms WITH LOGIN PASSWORD '${KMS_PASSWORD}';
  END IF;
END
\$\$;

-- Create Backend role if not exists
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${BACKEND_USER}') THEN
    CREATE ROLE backend WITH LOGIN PASSWORD '${BACKEND_PASSWORD}';
  END IF;
END
\$\$;

-- Create Auth Server role if not exists
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${AUTH_USER}') THEN
    CREATE ROLE auth_server WITH LOGIN PASSWORD '${AUTH_PASSWORD}';
  END IF;
END
\$\$;

-- Grant access to placeholder tables
GRANT SELECT, INSERT, UPDATE, DELETE ON table_1, table_2 TO ${KMS_USER};
GRANT SELECT, INSERT, UPDATE, DELETE ON table_3, table_4 TO ${BACKEND_USER};
GRANT SELECT, INSERT, UPDATE, DELETE ON table_5, table_6 TO ${AUTH_USER};

EOF

if [ "$MODE" = "development" ]; then
  echo "📥 Seeding development data..."
  docker exec -i db psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$SCRIPT_DIR/seed-dev-data.sql"
fi

echo "🎉 Database initialization complete (${MODE} mode)."