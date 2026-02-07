.PHONY: help build test run dev clean rollback status logs shell

# Default target
help:
	@echo "Administrative Tool - Docker Commands"
	@echo ""
	@echo "Available commands:"
	@echo "  make build    - Build the application in Docker container"
	@echo "  make test     - Run all tests in Docker container"
	@echo "  make run      - Run the application in Docker container (production mode)"
	@echo "  make dev      - Start development environment with live reload"
	@echo "  make clean    - Stop all containers and remove volumes"
	@echo "  make rollback - Rollback database to initial state"
	@echo "  make status   - Show status of running containers"
	@echo "  make logs     - Show logs from all containers"
	@echo "  make shell    - Open a shell in the development container"
	@echo ""
	@echo "All commands run inside Docker containers - no local Java/Gradle execution"

# Build the application in Docker
build:
	@echo "Building application in Docker container..."
	docker-compose --profile build run --rm gradle-build
	@make rollback

# Run tests in Docker
test:
	@echo "Running tests in Docker container..."
	docker-compose --profile test run --rm gradle-test
	@make rollback

# Run the application (production mode)
run:
	@echo "Starting application in production mode..."
	docker-compose up --build -d
	@echo "Application starting... waiting for health check"
	@sleep 10
	@curl -f http://localhost:8080/actuator/health || echo "Health check failed - check logs with 'make logs'"

# Start development environment with live reload
dev:
	@echo "Starting development environment with live reload..."
	docker-compose up -d postgres
	@sleep 5
	docker-compose -f docker-compose.yml -f docker-compose.override.yml up -d app-dev
	@echo "Development server starting on http://localhost:8080"
	@echo "Use 'make logs' to view logs"

# Stop all containers and clean up
clean:
	@echo "Stopping all containers and cleaning up..."
	docker-compose -f docker-compose.yml -f docker-compose.override.yml down -v
	docker-compose down -v
	@make rollback

# Rollback database to initial state
rollback:
	@echo "Rolling back database to initial state..."
	docker-compose --profile rollback run --rm db-rollback

# Show container status
status:
	@echo "Container status:"
	docker-compose -f docker-compose.yml -f docker-compose.override.yml ps

# Show logs
logs:
	@echo "Container logs:"
	docker-compose -f docker-compose.yml -f docker-compose.override.yml logs -f

# Open shell in development container
shell:
	@echo "Opening shell in development container..."
	docker-compose -f docker-compose.yml -f docker-compose.override.yml run --rm app-dev /bin/bash
