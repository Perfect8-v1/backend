#!/usr/bin/env bash

echo "=========================================="
echo "Stopping Perfect8 Backend Services"
echo "=========================================="

PROJECT_NAME="${COMPOSE_PROJECT_NAME:-perfect8}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed!"
    exit 1
fi

# Check if Docker daemon is running
if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker daemon is not running. Please start Docker Desktop."
    exit 1
fi

echo ""
echo "Stopping all services..."
docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" down

if [ $? -ne 0 ]; then
    echo "WARNING: Some services may not have stopped cleanly"
fi

echo ""
echo "Verifying all containers stopped:"
echo "=========================================="
docker ps -a --filter "name=^(adminDB|blogDB|emailDB|imageDB|shopDB|admin-service|blog-service|email-service|image-service|shop-service)$"

RUNNING=$(docker ps -q --filter "name=^(adminDB|blogDB|emailDB|imageDB|shopDB|admin-service|blog-service|email-service|image-service|shop-service)$")

if [ -z "$RUNNING" ]; then
    echo ""
    echo "✅ All Perfect8 containers stopped successfully!"
else
    echo ""
    echo "⚠️  Some containers are still running. To force stop:"
    echo "  docker stop adminDB blogDB emailDB imageDB shopDB admin-service blog-service email-service image-service shop-service"
    echo "  docker rm adminDB blogDB emailDB imageDB shopDB admin-service blog-service email-service image-service shop-service"
fi

echo ""
echo "=========================================="
echo "Perfect8 Backend Stopped"
echo "=========================================="
