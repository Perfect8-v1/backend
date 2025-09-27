#!/bin/bash
# Perfect8 Backend Startup Script för Frontend-utvecklare

set -e

echo "🚀 Perfect8 Backend Startup"
echo "=========================="

# Kontrollera att Docker/Podman är installerat
if command -v docker &> /dev/null; then
    DOCKER_CMD="docker"
elif command -v podman &> /dev/null; then
    DOCKER_CMD="podman"
else
    echo "❌ Varken Docker eller Podman hittades!"
    echo "   Installera Docker Desktop eller Podman först."
    exit 1
fi

echo "✅ Använder: $DOCKER_CMD"

# Steg 1: Bygga JAR-filer
echo ""
echo "📦 Steg 1: Bygger Java-applikationer..."
echo "----------------------------------------"
mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then
    echo "✅ Maven build lyckades!"
else
    echo "❌ Maven build misslyckades!"
    exit 1
fi

# Steg 2: Bygga Docker images
echo ""
echo "🐳 Steg 2: Bygger Docker images..."
echo "-----------------------------------"

SERVICES=("admin-service" "blog-service" "email-service" "image-service" "shop-service")

for SERVICE in "${SERVICES[@]}"; do
    echo "   Bygger $SERVICE..."
    cd $SERVICE
    $DOCKER_CMD build -t localhost/perfect8-$SERVICE:latest . -q
    cd ..
done

echo "✅ Alla Docker images byggda!"

# Steg 3: Starta alla services
echo ""
echo "🎯 Steg 3: Startar alla services..."
echo "------------------------------------"

if [ "$DOCKER_CMD" == "docker" ]; then
    docker-compose up -d
else
    podman-compose up -d
fi

# Vänta på att services ska starta
echo ""
echo "⏳ Väntar på att services ska bli redo..."
sleep 10

# Steg 4: Verifiera att allt körs
echo ""
echo "🔍 Steg 4: Verifierar services..."
echo "----------------------------------"

PORTS=(8081 8082 8083 8084 8085 8080)
NAMES=("Admin" "Blog" "Email" "Image" "Shop" "Gateway")

for i in "${!PORTS[@]}"; do
    if curl -f -s http://localhost:${PORTS[$i]}/actuator/health > /dev/null 2>&1 || \
       curl -f -s http://localhost:${PORTS[$i]}/health > /dev/null 2>&1; then
        echo "✅ ${NAMES[$i]} Service: http://localhost:${PORTS[$i]} - ONLINE"
    else
        echo "⚠️  ${NAMES[$i]} Service: http://localhost:${PORTS[$i]} - STARTAR..."
    fi
done

echo ""
echo "=================================="
echo "🎉 Perfect8 Backend är igång!"
echo "=================================="
echo ""
echo "📋 API Gateway: http://localhost:8080"
echo "📚 API Docs: http://localhost:8080/api/docs"
echo ""
echo "🔗 Direkta service-URLer:"
echo "   Admin:  http://localhost:8081"
echo "   Blog:   http://localhost:8082"
echo "   Email:  http://localhost:8083"
echo "   Image:  http://localhost:8084"
echo "   Shop:   http://localhost:8085"
echo ""
echo "💡 Tips för frontend-utvecklare:"
echo "   - Använd http://localhost:8080/api/v1/* för alla API-anrop"
echo "   - JWT token krävs för skyddade endpoints"
echo "   - CORS är konfigurerat för localhost:3000, 5173, 4200, 8080"
echo ""
echo "🛑 För att stoppa alla services:"
echo "   ./stop-backend.sh"
echo ""