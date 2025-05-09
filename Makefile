SHELL := /bin/bash

# Environment setup
ENV_FILE=.env

# ---------------------------
# 🧪 Development Targets
# ---------------------------
dev: ## Build and start dev environment (with hot reload)
	@echo "🔧 Starting development environment..."
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d

dev-up: ## Start dev environment (with hot reload)
	@echo "🔧 Starting development environment..."
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d

dev-down: ## Stop dev environment
	docker compose -f docker-compose.yml -f docker-compose.dev.yml down -v

dev-init: ## Start dev environment and initialize DB roles
	make dev
	make init-db MODE=development

# ---------------------------
# 🚀 Production Targets
# ---------------------------
prod: ## Build and start production environment with static frontend
	@echo "🚀 Starting production containers..."
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
	docker compose down -v

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