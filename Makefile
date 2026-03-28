.PHONY: help build test run dev dev-backend dev-frontend clean rollback status logs shell

# Default target
help:
	@echo "OsiedleDesk - Docker Commands"
	@echo ""
	@echo "Production:"
	@echo "  make run           - Build and start all services (postgres + backend + frontend)"
	@echo "  make build         - Build backend in Docker container"
	@echo "  make test          - Run backend tests in Docker container"
	@echo ""
	@echo "Development:"
	@echo "  make dev           - Start full dev environment (postgres + backend + frontend with hot reload)"
	@echo "  make dev-backend   - Start only postgres + backend dev server"
	@echo "  make dev-frontend  - Start only postgres + frontend dev server"
	@echo ""
	@echo "Utilities:"
	@echo "  make clean         - Stop all containers and remove volumes"
	@echo "  make rollback      - Rollback database to initial state"
	@echo "  make status        - Show status of running containers"
	@echo "  make logs          - Show logs from all containers"
	@echo "  make shell-backend - Open shell in backend dev container"
	@echo "  make shell-frontend- Open shell in frontend dev container"
	@echo ""
	@echo "All commands run inside Docker containers - no local Java/Node required"

# Build and start all services (production)
run:
	@echo "Building and starting all services..."
	docker compose up --build -d
	@echo "Services starting..."
	@echo "  Frontend: http://localhost:3000"
	@echo "  Backend:  http://localhost:8080"

# Build backend in Docker
build:
	@echo "Building backend in Docker container..."
	docker compose --profile build run --rm gradle-build

# Run backend tests in Docker
test:
	@echo "Running backend tests in Docker container..."
	docker compose --profile test run --rm gradle-test

# Start full dev environment
dev:
	@echo "Starting development environment..."
	docker compose up -d postgres
	@echo "Waiting for postgres..."
	@sleep 3
	docker compose -f docker-compose.yml -f docker-compose.override.yml up -d backend-dev frontend-dev
	@echo ""
	@echo "Development servers starting:"
	@echo "  Frontend (Vite): http://localhost:5173"
	@echo "  Backend (Spring): http://localhost:8080"
	@echo ""
	@echo "Use 'make logs' to view logs"

# Start only backend dev server
dev-backend:
	@echo "Starting backend development environment..."
	docker compose up -d postgres
	@sleep 3
	docker compose -f docker-compose.yml -f docker-compose.override.yml up -d backend-dev
	@echo "Backend starting on http://localhost:8080"

# Start only frontend dev server
dev-frontend:
	@echo "Starting frontend development environment..."
	docker compose up -d postgres
	@sleep 3
	docker compose -f docker-compose.yml -f docker-compose.override.yml up -d frontend-dev
	@echo "Frontend starting on http://localhost:5173"

# Stop all containers and clean up
clean:
	@echo "Stopping all containers and cleaning up..."
	docker compose -f docker-compose.yml -f docker-compose.override.yml down -v
	@echo "All containers stopped and volumes removed"

# Rollback database to initial state
rollback:
	@echo "Rolling back database to initial state..."
	docker compose --profile rollback run --rm db-rollback

# Show container status
status:
	docker compose -f docker-compose.yml -f docker-compose.override.yml ps

# Show logs
logs:
	docker compose -f docker-compose.yml -f docker-compose.override.yml logs -f

# Open shell in backend dev container
shell-backend:
	docker compose -f docker-compose.yml -f docker-compose.override.yml exec backend-dev /bin/bash

# Open shell in frontend dev container
shell-frontend:
	docker compose -f docker-compose.yml -f docker-compose.override.yml exec frontend-dev /bin/sh
