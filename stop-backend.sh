#!/bin/bash
# Perfect8 Backend Stop Script
# Stoppar alla services säkert

echo "🛑 Perfect8 Backend Shutdown"
echo "============================"

# Kontrollera att vi är i rätt mapp
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ docker-compose.yml hittades inte!"
    echo "   Kör detta skript från ~/perfect8 mappen"
    exit 1
fi

# Stoppa alla services
echo "Stoppar alla services..."
podman-compose down

# Ta bort pod (om den finns)
podman pod rm -f pod_perfect8 2>/dev/null || true

# Visa status
echo ""
echo "Status efter stopp:"
podman ps -a

echo ""
echo "✅ Perfect8 Backend stoppad!"
echo ""
echo "💡 För att starta igen:"
echo "   ./start-backend.sh"
echo ""
echo "🗑️  För att ta bort ALLT (inklusive data):"
echo "   podman-compose down -v"
echo "   (Detta raderar databas och bilder!)"
echo ""