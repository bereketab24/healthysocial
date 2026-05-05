.PHONY: build up down help

JAVA_21_HOME=/usr/lib/jvm/jdk-21.0.9-oracle-x64

help:
	@echo "Usage:"
	@echo "  make up      - Build the app and start all services"
	@echo "  make build   - Build the Spring Boot JAR"
	@echo "  make down    - Stop and remove all containers"

build:
	@echo "Building executable JAR (using Java 21)..."
	cd backend && JAVA_HOME=$(JAVA_21_HOME) ./gradlew bootJar

up: build
	@echo "Starting all services..."
	docker compose up -d
	@echo "Waiting for the application to start..."
	@echo "Swagger UI: http://localhost:8080/swagger-ui.html"
	@docker compose logs -f app

down:
	docker compose down
