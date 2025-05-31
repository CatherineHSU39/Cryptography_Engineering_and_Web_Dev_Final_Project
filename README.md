# Cipher Game Pro Max

A full-stack secure messaging platform with envelope encryption, role-based access control, and simulated KMS.

---

## 🧱 Project Structure

```
/
├── backend/                  # Spring Boot backend (REST API)
├── auth/                     # Spring Boot auth server with TOTP/JWT
├── kms/                      # FastAPI-based simulated KMS
├── frontend/                 # Vue 3 frontend using Vite (Multi-stage built)
├── nginx/                    # NGINX config and prod reverse proxy
├── db/                       # PostgreSQL init scripts
├── docker-compose.yml        # Production configuration
├── docker-compose.dev.yml    # Dev overrides (hot reload)
├── scripts/
│   ├── init-db.sh            # Role/table creation and optional seeding
│   ├── init-schema.sql       # Table definitions
│   └── seed-dev-data.sql     # Sample dev data (Alice, Bob, etc.)
└── Makefile                  # Common build/management tasks
```

---

## 🚀 Getting Started

### 📦 1. Clone the repository

```bash
git clone https://github.com/CatherineHSU39/Cryptography_Engineering_and_Web_Dev_Final_Project.git
cd Cryptography_Engineering_and_Web_Dev_Final_Project
```

### 🔑 2. Set up environment variables

Copy `.env.example` to `.env`, then fill in required database and role credentials.

---

## 🧪 Local Development

When first start or there's changes on spring services:

```bash
make init-spring
```

Run with hot reload using:

```bash
make dev
make init-db MODE=development  # Creates tables, roles, and seeds dev data
```

or

```bash
make dev-init
```

- Backend run with `make dev-backend-init`
- Auth run with `make dev-auth-init`
- Frontend runs `make dev-frontend-init`
- KMS runs with `make dev-kms-init`
- PostgreSQL is seeded with example data (Alice, Bob)

Stop the environment with:

```bash
make dev-down
```

---

## 🚀 Production Deployment

Build and launch the production stack:

```bash
make prod
make init-db                  # Defaults to production mode (no seed data)
```

- Vue app is built via multi-stage Dockerfile
- NGINX serves static frontend + reverse proxies `/app`, `/auth`, `/kms`

Stop the environment with:

```bash
make prod-down
```

---

## 🧹 Cleanup

To remove containers and volumes:

```bash
make clean
```

---

## ⚙️ Makefile Quick Reference

| Command                         | Description                                |
| ------------------------------- | ------------------------------------------ |
| `make init-spring`              | Compile spring files for later use         |
| `make dev`                      | Start development environment              |
| `make dev-frontend-init`        | Start development environment for frontend |
| `make dev-backend-init`         | Start development environment for backend  |
| `make dev-auth-init`            | Start development environment for auth     |
| `make dev-kms-init`             | Start development environment for kms      |
| `make prod`                     | Start production environment               |
| `make init-db`                  | Init roles + tables (prod mode)            |
| `make init-db MODE=development` | Init roles + seed data (dev)               |
| `make dev-down`                 | Stop dev environment                       |
| `make prod-down`                | Stop prod environment                      |
| `make clean`                    | Stop and remove all volumes                |

---

## 📄 License

This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).
