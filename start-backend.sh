#!/usr/bin/env bash
set -Eeuo pipefail

export BUILDAH_FORMAT="${BUILDAH_FORMAT:-docker}"
PROJECT_NAME="${COMPOSE_PROJECT_NAME:-perfect8}"
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"

RETRY_WAIT=3
RETRY_MAX=120

# Use the exact service names from docker-compose.yml. It's 'mysql', not 'mariadb'.
ORDER=(
  "mysql"
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
  if command -v podman-compose >/dev/null 2>&1; then
    podman-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" "$@"
  else
    podman compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" "$@"
  fi
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
  podman ps -aq --filter "name=${PROJECT_NAME}_${svc}" | head -n1
}

health_status() {
  local cid="$1"
  podman inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$cid" 2>/dev/null || echo "unknown"
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
  podman logs --tail 100 "$(container_id_for "$svc" || true)" 2>/dev/null || true
  return 1
}

start_service() { bold "▶ Starting: $1"; compose up -d "$1"; wait_healthy "$1"; }

status_table() { podman ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"; }

deps_for() {
  case "$1" in
    mysql) echo "" ;;
    email-service) echo "mysql" ;;
    image-service) echo "mysql email-service" ;;
    admin-service) echo "mysql email-service image-service" ;;
    blog-service) echo "mysql email-service image-service admin-service" ;;
    shop-service) echo "mysql email-service image-service admin-service blog-service" ;;
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
  bold "✔ All services attempted. Current status:"
  status_table
}

do_logs() {
  local svc="${1:-}"; [[ -z "$svc" ]] && { red "Usage: --logs <service>"; exit 1; }
  local cid; cid="$(container_id_for "$svc")"
  [[ -z "$cid" ]] && { red "Service '$svc' not running."; exit 1; }
  podman logs -f "$cid"
}

do_down() { bold "⏹ Stopping stack (keeping volumes)..."; compose down; }
do_clean(){ bold "🧹 Stopping stack and removing volumes."; compose down -v; yellow "DB volume removed."; }

require_cmd podman
if ! command -v podman-compose >/dev/null 2>&1 && ! podman compose version >/dev/null 2>&1; then
  red "Neither 'podman-compose' nor 'podman compose' found."; exit 1
fi

case "${1:-}" in
  "" ) do_start_all ;;
  --status ) status_table ;;
  --logs ) shift; do_logs "${1:-}" ;;
  --only ) shift; svc="${1:-}"; [[ -z "$svc" ]] && { red "Usage: --only <service>"; exit 1; }; preflight_validate_names; start_with_deps "$svc" ;;
  --down ) do_down ;;
  --clean ) do_clean ;;
  -h|--help ) sed -n '1,200p' "$0" ;;
  * ) red "Unknown option: $1" ; exit 1 ;;
esac
