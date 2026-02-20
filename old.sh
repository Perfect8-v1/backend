#!/bin/bash
# Server - bygg och deploy
# Version: 1.1 - Med felhantering

cd ~/backend

echo "=== GIT PULL ==="
git pull origin sop
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ GIT PULL MISSLYCKADES!"
    echo ""
    echo "Trolig orsak: Lokala ändringar blockerar."
    echo ""
    echo "Alternativ:"
    echo "  1. git stash && bash new.sh"
    echo "  2. git checkout -- <fil>"
    echo ""
    exit 1
fi

echo ""
echo "=== MAVEN BUILD ==="
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo ""
    echo "❌ MAVEN BUILD MISSLYCKADES!"
    exit 1
fi

echo ""
echo "=== COPY JARS ==="
bash copy-jars*.sh

echo ""
echo "=== DOCKER REBUILD ==="
docker compose down
docker compose build --no-cache
docker compose up -d

echo ""
echo "=== VÄNTAR 180 SEK ==="
sleep 180

echo ""
echo "=== STATUS ==="
docker compose ps -a

echo ""
echo "✅ DEPLOY KLAR!"
