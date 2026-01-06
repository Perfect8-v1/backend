#!/bin/bash
# Server - bygg och deploy

cd ~/backend
git pull origin scg
mvn clean package -DskipTests
bash copy-jars*.sh
docker compose down
docker compose build --no-cache
docker compose up -d
sleep 180
docker compose ps -a
