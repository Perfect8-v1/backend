#!/bin/bash
# Perfect8 Backend Startup Script
# Optimerat för Podman på Alpine Linux

set -e

# Hantera --clean flagga
CLEAN_START=false
if [ "$1" == "--clean" ]; then
    CLEAN_START=true
    echo "⚠️  CLEAN START MODE"
    echo "   Detta raderar ALL data (databas + bilder)!"
    echo ""
    read -p "   Är du säker? (yes/no): " CONFIRM
    if [ "$CONFIRM" != "yes" ]; then
        echo "❌ Avbruten"
        exit 0
    fi
fi

echo "🚀 Perfect8 Backend Startup"
echo "=========================="

# Exportera Docker format för healthchecks
export BUILDAH_FORMAT=docker
echo "✅ BUILDAH_FORMAT=docker (för healthchecks)"

# Kontrollera att Podman är installerat
if ! command -v podman &> /dev/null; then
    echo "❌ Podman hittades inte!"
    echo "   Installera Podman först: apk add podman"
    exit 1
fi

echo "✅ Använder: Podman"

# Kontrollera att podman-compose finns
if ! command -v podman-compose &> /dev/null; then
    echo "❌ podman-compose hittades inte!"
    echo "   Installera: pip install podman-compose"
    exit 1
fi

# Steg 1: Verifiera att vi är i rätt mapp
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ docker-compose.yml hittades inte!"
    echo "   Kör detta skript från ~/perfect8 mappen"
    exit 1
fi

if [ ! -f ".env" ]; then
    echo "⚠️  .env fil saknas! Skapar från .env.example..."
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo "   ⚠️  VIKTIGT: Redigera .env med riktiga lösenord!"
        echo "   nano .env"
        exit 1
    else
        echo "❌ Ingen .env.example hittades!"
        exit 1
    fi
fi

echo "✅ Konfigurationsfiler OK"

# Steg 2: Städa gamla containers (om de finns)
echo ""
echo "🧹 Steg 1: Städar gamla containers..."
echo "--------------------------------------"
podman-compose down 2>/dev/null || true
podman pod rm -f pod_perfect8 2>/dev/null || true

# Om --clean, ta bort volymer
if [ "$CLEAN_START" = true ]; then
    echo ""
    echo "🗑️  Tar bort alla volymer (databas + bilder)..."
    podman volume rm perfect8_db_data 2>/dev/null || true
    podman volume rm perfect8_image_storage 2>/dev/null || true
    echo "✅ Volymer borttagna - börjar från scratch"
fi

echo "✅ Städning klar"

# Steg 3: Bygga alla Docker images
echo ""
echo "🐳 Steg 2: Bygger Docker images..."
echo "-----------------------------------"
echo "   (Detta kan ta 5-10 minuter första gången)"

podman-compose build --no-cache

if [ $? -eq 0 ]; then
    echo "✅ Alla Docker images byggda!"
else
    echo "❌ Build misslyckades!"
    exit 1
fi

# Steg 4: Starta alla services
echo ""
echo "🎯 Steg 3: Startar alla services..."
echo "------------------------------------"

podman-compose up -d

# Vänta på att services ska starta
echo ""
echo "⏳ Väntar på att services ska bli redo..."
sleep 15

# Steg 5: Visa status
echo ""
echo "🔍 Steg 4: Verifierar services..."
echo "----------------------------------"

podman ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "=================================="
echo "🎉 Perfect8 Backend är igång!"
echo "=================================="
echo ""
echo "🔗 Service-URLer:"
echo "   Admin:    http://localhost:8083"
echo "   Blog:     http://localhost:8080"
echo "   Email:    http://localhost:8084"
echo "   Image:    http://localhost:8081"
echo "   Shop:     http://localhost:8082"
echo "   Database: localhost:3306"
echo ""
echo "💡 Användbara kommandon:"
echo "   podman-compose logs -f          # Följ alla loggar"
echo "   podman-compose logs -f shop-service  # Följ en service"
echo "   podman ps                       # Lista containers"
echo "   ./stop-backend.sh               # Stoppa allt"
echo "   ./start-backend.sh --clean      # Starta från scratch (raderar data!)"
echo ""
echo "📊 Kolla status:"
echo "   curl http://localhost:8083/actuator/health  # Admin service"
echo "   curl http://localhost:8082/actuator/health  # Shop service"
echo ""