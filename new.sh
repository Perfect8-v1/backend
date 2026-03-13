#!/bin/bash
# Server - bygg och deploy
# Version: 1.3 - Regenererar SSL-certifikat från certbot

cd ~/backend

echo "=== GIT PULL ==="
git pull origin main
if [ $? -ne 0 ]; then
  echo ""
  echo "❌ GIT PULL MISSLYCKADES!"
  echo ""
  echo "Trolig orsak: Lokala ändringar blockerar."
  echo "Alternativ:"
  echo "  1. git stash && bash new.sh"
  echo "  2. git checkout -- <fil>"
  exit 1
fi

echo ""
echo "=== SSL KEYSTORE (certbot → keystore.p12) ==="
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/p8.rantila.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/p8.rantila.com/privkey.pem \
  -out ~/backend/api-gateway/keystore.p12 \
  -name p8 \
  -passout pass:changeit
if [ $? -ne 0 ]; then
  echo "❌ SSL KEYSTORE MISSLYCKADES!"
  exit 1
fi
echo "✅ SSL keystore OK"

echo ""
echo "=== MAVEN BUILD ==="
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
  echo "❌ MAVEN BUILD MISSLYCKADES!"
  exit 1
fi
echo "✅ Maven build OK"

echo ""
echo "=== COPY JARS ==="
bash copy-jars.sh

echo ""
echo "=== DOCKER REBUILD ==="
docker compose down
docker compose build
docker compose up -d

echo ""
echo "=== VÄNTAR 90 SEK ==="
sleep 90

echo ""
echo "=== STATUS ==="
docker compose ps -a

echo ""
echo "✅ DEPLOY KLAR!"
