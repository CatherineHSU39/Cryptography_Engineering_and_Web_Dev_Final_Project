# Cipher Game Pro Max

A full-stack secure messaging platform with envelope encryption, role-based access control, and simulated KMS.

---

## 🧱 Project Structure

/
├── backend/ # Spring Boot backend (REST API)
├── auth/ # Spring Boot auth server with TOTP/JWT
├── kms/ # FastAPI-based simulated KMS
├── frontend/ # Vue 3 frontend using Vite (Multi-stage built)
├── nginx/ # NGINX config and prod reverse proxy
├── db/ # PostgreSQL init scripts
├── docker-compose.yml # Production configuration
└── docker-compose.dev.yml # Dev configuration

---

## 🚀 Getting Started

### 📦 1. Clone the repository

bash
git clone https://github.com/CatherineHSU39/Cryptography_Engineering_and_Web_Dev_Final_Project.git
cd Cryptography_Engineering_and_Web_Dev_Final_Project

### 🔑 2. Environment Setup

Create .env files as needed (e.g., for DB or JWT secrets).

---

## 🧪 Local Development (with hot reload)

> Uses docker-compose.dev.yml for dev commands and volume mounts.

1. Start all services with dev settings:

bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build

- Backend and auth run via ./mvnw spring-boot:run
- Frontend runs npm run dev using Node container
- KMS runs with FastAPI --reload
- Database persists via db-data volume

---

## 🚀 Production Build (static frontend)

1. Build static Vue app into /dist using multi-stage Dockerfile:

bash
docker compose up --build

- Static frontend files are built using frontend/Dockerfile and copied to nginx:/usr/share/nginx/html

2. NGINX will serve:
   - /dist as static frontend
   - /api, /auth, /kms as backend services

---

## ⚙️ Additional Notes

- frontend/ is not a running container in production — it's built into NGINX.
- docker-compose.dev.yml is committed to version control and shared across the team
- Use this file to define dev-only commands, mounts, and ports

---

## 🧹 Cleanup

bash
docker compose down -v

Removes containers and volumes.

---

## 📄 License

This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).
