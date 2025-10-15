#!/usr/bin/env bash
# start-backend.sh — deterministic startup for Podman + podman-compose with health checks
# Usage:
#   ./start-backend.sh                 # start in correct order
#   ./start-backend.sh --status        # show container status
#   ./start-backend.sh --logs <svc>    # tail logs for a service
#   ./start-backend.sh --down          # stop stack (preserve volumes)
#   ./start-backend.sh --clean         # stop + remove volumes (DANGEROUS)
#   ./start-backend.sh --only <svc>    # start a single service (still waits for its deps)
#
# Services (adjust if your compose file uses other names):
#   mysql, email-service, image-service, admin-service, blog-service, shop-service
#
set -Eeuo pipefail

# -------- Settings --------
export BUILDAH_FORMAT="${BUILDAH_FORMAT:-docker}"    # ensure healthchecks not ignored (OCI would ignore)
PROJECT_NAME="${COMPOSE_PROJECT_NAME:-perfect8}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"

RETRY_WAIT=3      # seconds between polls
RETRY_MAX=120     # ~6 min per service

ORDER=(
  "mysql"
  "email-service"
  "image-service"
  "admin-service"
  "blog-service"
  "shop-service"
)

# -------- Helpers --------
red()   { printf "\033[31m%s\033[0m\n" "$*"; }
green() { printf "\033[32m%s\033[0m\n" "$*"; }
yellow(){ printf "\033[33m%s\033[0m\n" "$*"; }
bold()  { printf "\033[1m%s\033[0m\n" "$*"; }

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { red "Missing command: $1"; exit 1; }
}

compose() {
  # Use podman-compose if available; fall back to 'podman compose' if present.
  if command -v podman-compose >/dev/null 2>&1; then
    podman-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" "$@"
  else
    # Podman v4 subcommand
    podman compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" "$@"
  fi
}

container_id_for() {
  local svc="$1"
  # Podman containers from compose are typically named "${PROJECT}_${svc}_1"
  podman ps -aq --filter "name=${PROJECT_NAME}_${svc}" | head -n1
}

health_status() {
  local cid="$1"
  # If Health object exists, return it; else return container State.Status (e.g., running, created, exited).
  podman inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$cid" 2>/dev/null || echo "unknown"
}

wait_healthy() {
  local svc="$1"
  local tries=0
  yellow "⏳ Waiting for '$svc' to become healthy (max $((RETRY_WAIT * RETRY_MAX))s)..."

  while (( tries < RETRY_MAX )); do
    local cid
    cid="$(container_id_for "$svc" || true)"
    if [[ -n "${cid:-}" ]]; then
      local st
      st="$(health_status "$cid")"
      if [[ "$st" == "healthy" ]]; then
        green "✅ $svc is HEALTHY"
        return 0
      fi
      # If no healthcheck is defined, consider 'running' as OK.
      if [[ "$st" == "running" ]]; then
        yellow "ℹ️  $svc has no healthcheck; container is RUNNING. Proceeding."
        return 0
      fi
      printf "."
    else
      printf "."
    fi
    sleep "$RETRY_WAIT"
    ((tries++))
  done

  echo
  red "❌ Timeout: $svc did not become healthy in time."
  podman logs --tail 100 "$(container_id_for "$svc" || true)" 2>/dev/null || true
  return 1
}

start_service() {
  local svc="$1"
  bold "▶ Starting: $svc"
  compose up -d "$svc"
  wait_healthy "$svc"
}

status_table() {
  podman ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | sed "1 s/^/NAME\tSTATUS\tPORTS\n/"
}

# -------- Dependency map (minimal) --------
# Define runtime deps so --only <svc> can still start prerequisites first.
# Adjust if your services require other dependencies.
deps_for() {
  local svc="$1"
  case "$svc" in
    mysql)               echo "" ;;
    email-service)       echo "mysql" ;;
    image-service)       echo "mysql email-service" ;;
    admin-service)       echo "mysql email-service image-service" ;;
    blog-service)        echo "mysql email-service image-service admin-service" ;;
    shop-service)        echo "mysql email-service image-service admin-service blog-service" ;;
    *) echo "" ;;
  esac
}

start_with_deps() {
  local svc="$1"
  local -A seen=()
  _walk() {
    local node="$1"
    if [[ -n "${seen[$node]:-}" ]]; then return; fi
    seen[$node]=1
    for d in $(deps_for "$node"); do _walk "$d"; done
    start_service "$node"
  }
  _walk "$svc"
}

# -------- Actions --------
do_start_all() {
  for svc in "${ORDER[@]}"; do
    start_with_deps "$svc"
  done
  bold "✔ All services attempted. Current status:"
  status_table
}

do_logs() {
  local svc="${1:-}"
  if [[ -z "$svc" ]]; then red "Usage: --logs <service>"; exit 1; fi
  local cid
  cid="$(container_id_for "$svc")"
  if [[ -z "$cid" ]]; then red "Service '$svc' not running."; exit 1; fi
  podman logs -f "$cid"
}

do_down() {
  bold "⏹ Stopping stack (keeping volumes)..."
  compose down
}

do_clean() {
  bold "🧹 Stopping stack and removing volumes (DANGEROUS)."
  compose down -v
  yellow "If you had named volumes, they are removed. DB data will be gone."
}

# -------- Main --------
require_cmd podman
if ! command -v podman-compose >/dev/null 2>&1 && ! podman compose version >/dev/null 2>&1; then
  red "Neither 'podman-compose' nor 'podman compose' found."; exit 1
fi

case "${1:-}" in
  "" )
    do_start_all
    ;;
  --status )
    status_table
    ;;
  --logs )
    shift; do_logs "${1:-}"
    ;;
  --only )
    shift
    svc="${1:-}"
    if [[ -z "$svc" ]]; then red "Usage: --only <service>"; exit 1; fi
    start_with_deps "$svc"
    ;;
  --down )
    do_down
    ;;
  --clean )
    do_clean
    ;;
  -h|--help )
    sed -n '1,120p' "$0"
    ;;
  * )
    red "Unknown option: $1"; exit 1
    ;;
esac
