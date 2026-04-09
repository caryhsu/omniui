#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

APPS=(
  core-app input-app advanced-app login-app drag-app progress-app
  image-app color-app todo-app settings-app dynamic-fxml-app
  explorer-app user-search-app
)
TOTAL=$(( ${#APPS[@]} + 1 ))
STEP=0

STEP=$((STEP + 1))
echo "[$STEP/$TOTAL] Installing local Java agent..."
mvn -pl java-agent -am clean install

for app in "${APPS[@]}"; do
  STEP=$((STEP + 1))
  echo "[$STEP/$TOTAL] Building $app runtime image..."
  mvn -f "demo/java/$app/pom.xml" clean org.openjfx:javafx-maven-plugin:jlink
done

echo
echo "Runtime images ready."
echo "Next steps:"
for app in "${APPS[@]}"; do
  printf "  %-16s with agent: %s/demo/java/%s/run-with-agent.sh\n" "$app" "$ROOT" "$app"
  printf "  %-16s plain app : %s/demo/java/%s/run-plain.sh\n" "$app" "$ROOT" "$app"
done
