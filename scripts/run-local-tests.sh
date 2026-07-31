#!/usr/bin/env bash
set -Eeuo pipefail

project_name="${BOOKWRIGHT_COMPOSE_PROJECT:-bookwright-${USER:-local}-$$}"
compose=(docker compose -p "$project_name" -f docker/docker-compose.yml)

cleanup() {
  status=$?
  trap - EXIT
  "${compose[@]}" down -v || true
  exit "$status"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

"${compose[@]}" up -d --wait

published_port() {
  "${compose[@]}" port "$1" "$2" | tail -n 1 | awk -F: '{print $NF}'
}

ssh_port="$(published_port bastion 2222)"
api_port="$(published_port restful-booker 3001)"

if [[ -z "$ssh_port" || -z "$api_port" ]]; then
  echo "Could not discover dynamically published Docker ports" >&2
  exit 1
fi

echo "Local stand '$project_name' is healthy"
echo "  API: http://127.0.0.1:$api_port"
echo "  SSH: 127.0.0.1:$ssh_port"
echo "  DB tunnel: dynamically assigned by JSch"

./gradlew clean test \
  -DSTAND=local \
  -Dssh.port="$ssh_port" \
  -Dapi.base.url="http://127.0.0.1:$api_port" \
  "$@"
