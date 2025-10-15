#!/bin/bash

echo "=========================================="
echo "Stopping Perfect8 Backend Services"
echo "=========================================="

# Check if podman-compose is installed
if ! command -v podman-compose &> /dev/null; then
    echo "ERROR: podman-compose is not installed!"
    exit 1
fi

echo ""
echo "Stopping all services..."
podman-compose down

if [ $? -ne 0 ]; then
    echo "WARNING: Some services may not have stopped cleanly"
fi

echo ""
echo "Verifying all containers stopped:"
echo "=========================================="
podman ps -a | grep perfect8

if [ $? -ne 0 ]; then
    echo "All Perfect8 containers stopped successfully!"
else
    echo ""
    echo "Some containers are still running. To force stop:"
    echo "  podman stop \$(podman ps -q --filter name=perfect8)"
    echo "  podman rm \$(podman ps -aq --filter name=perfect8)"
fi

echo ""
echo "=========================================="
echo "Perfect8 Backend Stopped"
echo "=========================================="