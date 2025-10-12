#!/bin/bash

echo "=========================================="
echo "Perfect8 Backend Deployment - Docker Format"
echo "=========================================="

# Check if podman-compose is installed
if ! command -v podman-compose &> /dev/null; then
    echo "ERROR: podman-compose is not installed!"
    echo "Install with: pip3 install podman-compose"
    exit 1
fi

# Build images in Docker format (fixes healthcheck warnings)
echo ""
echo "Step 1: Building Docker images (Docker format)..."
echo "This will take a few minutes..."
podman-compose build --format=docker

if [ $? -ne 0 ]; then
    echo "ERROR: Image build failed!"
    exit 1
fi

echo ""
echo "Step 2: Starting all services..."
podman-compose up -d

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start services!"
    exit 1
fi

echo ""
echo "Step 3: Waiting for services to initialize (60 seconds)..."
sleep 60

echo ""
echo "Step 4: Service Status:"
echo "=========================================="
podman ps -a

echo ""
echo "=========================================="
echo "Perfect8 Backend Started!"
echo "=========================================="
echo ""
echo "Service URLs:"
echo "  Admin:    http://localhost:8081"
echo "  Blog:     http://localhost:8082"
echo "  Email:    http://localhost:8083"
echo "  Image:    http://localhost:8084"
echo "  Shop:     http://localhost:8085"
echo "  Database: localhost:3306"
echo ""
echo "Health checks:"
echo "  curl http://localhost:8081/actuator/health"
echo "  curl http://localhost:8082/actuator/health"
echo "  curl http://localhost:8083/actuator/health"
echo "  curl http://localhost:8084/actuator/health"
echo "  curl http://localhost:8085/actuator/health"
echo ""
echo "View logs:"
echo "  podman logs -f perfect8-admin"
echo "  podman logs -f perfect8-shop"
echo ""