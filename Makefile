SHELL := /bin/bash

# Environment setup
ENV_FILE=.env
COMPOSE_BASE=docker-compose.yml
COMPOSE_DEV=docker-compose.dev.yml

COMPOSE_FRONTEND_DEV=docker-compose.frontend.dev.yml
COMPOSE_BACKEND_DEV=docker-compose.backend.dev.yml
COMPOSE_DEK_DEV=docker-compose.dek.dev.yml
COMPOSE_AUTH_DEV=docker-compose.auth.dev.yml
COMPOSE_KMS_DEV=docker-compose.kms.dev.yml

# ---------------------------
# 🧪 Development Targets
# ---------------------------
# Default dev: all in dev mode (legacy)
dev: ## Build and start dev environment (with hot reload)
	@echo "🔧 Starting development environment..."
	docker-compose -f $(COMPOSE_BASE) -f $(COMPOSE_DEV) up --build -d

# Per-service dev commands
dev-frontend:
	@echo "🔧 Starting frontend development environment..."
	docker-compose -f $(COMPOSE_BASE) -f $(COMPOSE_FRONTEND_DEV) up --build -d

dev-backend:
	@echo "🔧 Starting backend development environment..."
	docker-compose -f $(COMPOSE_BASE) -f $(COMPOSE_BACKEND_DEV) up --build -d

dev-dek:
	@echo "🔧 Starting dek development environment..."
	docker-compose -f $(COMPOSE_BASE) -f $(COMPOSE_DEK_DEV) up --build -d

dev-auth:
	@echo "🔧 Starting auth development environment..."
	docker-compose -f $(COMPOSE_BASE) -f $(COMPOSE_AUTH_DEV) up --build -d

dev-kms:
	@echo "🔧 Starting kms development environment..."
	docker-compose -f $(COMPOSE_BASE) -f $(COMPOSE_KMS_DEV) up --build -d

dev-down: ## Stop dev environment
	docker compose -f docker-compose.yml -f docker-compose.dev.yml down -v

dev-frontend-init: ## Start frontend dev environment and initialize DB roles
	make dev-frontend
	make init-db MODE=development

dev-backend-init: ## Start backend dev environment and initialize DB roles
	make dev-backend
	make init-db MODE=development

dev-dek-init: ## Start backend dev environment and initialize DB roles
	make dev-dek
	make init-db MODE=development

dev-auth-init: ## Start auth dev environment and initialize DB roles
	make dev-auth
	make init-db MODE=development

dev-kms-init: ## Start kms dev environment and initialize DB roles
	make dev-kms
	make init-db MODE=development

# ---------------------------
# 🚀 Production Targets
# ---------------------------
prod: ## Build and start production environment with static frontend
	@echo "🚀 Starting production containers..."
	make init-spring
	docker compose up --build -d

prod-up: ## Start production environment with static frontend
	@echo "🚀 Starting production containers..."
	docker compose up -d

prod-down: ## Stop production environment
	docker compose down -v

prod-init: ## Initialize DB in production mode (no seeding)
	make prod
	make init-db MODE=production

# ---------------------------
# 🧹 Utilities
# ---------------------------
logs: ## Show logs from all services
	docker compose logs -f

ps: ## Show running containers
	docker compose ps

clean: ## Stop all containers and remove volumes
	docker-compose -f $(COMPOSE_BASE) -f $(COMPOSE_DEV) down
	docker compose down -v


# ---------------------------
# 🔐 Spring Init
# ---------------------------
init-spring: ## Initialize Spring Boot applications
	@echo "🔧 Initializing backend Spring Boot application..."
	cd backend && ./mvnw clean install -Dmaven.test.skip=true

	@echo "🔧 Initializing auth Spring Boot application..."
	cd auth && ./mvnw clean install -Dmaven.test.skip=true

	@echo "🔧 Initializing dek Spring Boot application..."
	cd dek && ./mvnw clean install -Dmaven.test.skip=true

# ---------------------------
# 🔐 DB Init (Post-Start SQL Setup)
# ---------------------------
init-db: ## Initialize DB with optional mode (e.g., MODE=development)
	@bash -c 'source $(ENV_FILE) && ./scripts/init-db.sh $${MODE:-production}'

# ---------------------------
# 🆘 Help
# ---------------------------
help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?##' Makefile | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'