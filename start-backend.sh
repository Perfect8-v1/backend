#!/usr/bin/env bash
set -Eeuo pipefail

PROJECT_NAME="${COMPOSE_PROJECT_NAME:-perfect8}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"

RETRY_WAIT=3
RETRY_MAX=120

# Correct service names from docker-compose.yml
ORDER=(
  "adminDB"
  "blogDB"
  "emailDB"
  "imageDB"
  "shopDB"
  "email-service"
  "image-service"
  "admin-service"
  "blog-service"
  "shop-service"
)

red()   { printf "\033[31m%s\033[0m\n" "$*"; }
green() { printf "\033[32m%s\033[0m\n" "$*"; }
yellow(){ printf "\033[33m%s\033[0m\n" "$*"; }
bold()  { printf "\033[1m%s\033[0m\n" "$*"; }

require_cmd() { command -v "$1" >/dev/null 2>&1 || { red "Missing command: $1"; exit 1; }; }

compose() {
  docker compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" "$@"
}

list_services() { compose config --services 2>/dev/null | sort; }

preflight_validate_names() {
  local ok=1
  local defined="$(list_services | tr '\n' ' ')"
  for svc in "${ORDER[@]}"; do
    if ! list_services | grep -qx "$svc"; then
      red "Service '$svc' is not defined in $COMPOSE_FILE."
      yellow "Defined services are: $defined"
      ok=0
    fi
  done
  if [[ $ok -eq 0 ]]; then
    red "Fix ORDER[] to match compose service keys exactly."
    exit 1
  fi
}

container_id_for() {
  local svc="$1"
  docker ps -aq --filter "name=^${svc}$" | head -n1
}

health_status() {
  local cid="$1"
  docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$cid" 2>/dev/null || echo "unknown"
}

wait_healthy() {
  local svc="$1"
  local tries=0
  yellow "⏳ Waiting for '$svc' to become healthy (max $((RETRY_WAIT * RETRY_MAX))s)..."

  while (( tries < RETRY_MAX )); do
    local cid st
    cid="$(container_id_for "$svc" || true)"
    if [[ -n "${cid:-}" ]]; then
      st="$(health_status "$cid")"
      if [[ "$st" == "healthy" ]]; then green "✅ $svc is HEALTHY"; return 0; fi
      if [[ "$st" == "running" ]]; then yellow "ℹ️  $svc has no healthcheck; RUNNING. Proceeding."; return 0; fi
      printf "."
    else
      printf "."
    fi
    sleep "$RETRY_WAIT"; ((tries++))
  done
  echo; red "❌ Timeout: $svc not healthy in time."
  docker logs --tail 100 "$svc" 2>/dev/null || true
  return 1
}

start_service() { bold "▶ Starting: $1"; compose up -d "$1"; wait_healthy "$1"; }

status_table() { docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"; }

deps_for() {
  case "$1" in
    adminDB|blogDB|emailDB|imageDB|shopDB) echo "" ;;
    email-service) echo "emailDB" ;;
    image-service) echo "imageDB email-service" ;;
    admin-service) echo "adminDB email-service image-service" ;;
    blog-service) echo "blogDB email-service image-service admin-service" ;;
    shop-service) echo "shopDB email-service image-service admin-service blog-service" ;;
    *) echo "" ;;
  esac
}

start_with_deps() {
  local svc="$1"; declare -A seen=()
  _walk() { local node="$1"; [[ "${seen[$node]:-}" ]] && return; seen[$node]=1; for d in $(deps_for "$node"); do _walk "$d"; done; start_service "$node"; }
  _walk "$svc"
}

do_start_all() {
  preflight_validate_names
  for svc in "${ORDER[@]}"; do start_with_deps "$svc"; done
  bold "✓ All services attempted. Current status:"
  status_table
}

do_logs() {
  local svc="${1:-}"; [[ -z "$svc" ]] && { red "Usage: --logs <service>"; exit 1; }
  docker logs -f "$svc"
}

do_down() { bold "⏹ Stopping stack (keeping volumes)..."; compose down; }
do_clean(){ bold "🧹 Stopping stack and removing volumes."; compose down -v; yellow "DB volumes removed."; }

require_cmd docker

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
  red "Docker daemon is not running. Please start Docker Desktop first."
  exit 1
fi

case "${1:-}" in
  "" ) do_start_all ;;
  --status ) status_table ;;
  --logs ) shift; do_logs "${1:-}" ;;
  --only ) shift; svc="${1:-}"; [[ -z "$svc" ]] && { red "Usage: --only <service>"; exit 1; }; preflight_validate_names; start_with_deps "$svc" ;;
  --down ) do_down ;;
  --clean ) do_clean ;;
  -h|--help ) echo "Usage: $0 [--status|--logs <service>|--only <service>|--down|--clean]" ;;
  * ) red "Unknown option: $1" ; exit 1 ;;
esac
