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

Run with hot reload using:

```bash
make dev
make init-db MODE=development  # Creates tables, roles, and seeds dev data
```

- Backend/Auth run with `spring-boot:run`
- Frontend runs `npm run dev`
- KMS runs with `uvicorn --reload`
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
- NGINX serves static frontend + reverse proxies `/api`, `/auth`, `/kms`

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

| Command                         | Description                     |
| ------------------------------- | ------------------------------- |
| `make dev`                      | Start development environment   |
| `make prod`                     | Start production environment    |
| `make init-db`                  | Init roles + tables (prod mode) |
| `make init-db MODE=development` | Init roles + seed data (dev)    |
| `make dev-down`                 | Stop dev environment            |
| `make prod-down`                | Stop prod environment           |
| `make clean`                    | Stop and remove all volumes     |

---

## 📄 License

This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).
