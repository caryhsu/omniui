#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "[1/4] Installing local Java agent..."
mvn -pl java-agent -am clean install

echo "[2/4] Building core-app runtime image..."
mvn -f demo/java/core-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink

echo "[3/4] Building input-app runtime image..."
mvn -f demo/java/input-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink

echo "[4/4] Building advanced-app runtime image..."
mvn -f demo/java/advanced-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink

echo
echo "Runtime images ready."
echo "Next steps:"
echo "  core-app     with agent: $ROOT/demo/java/core-app/run-with-agent.sh"
echo "  core-app     plain app : $ROOT/demo/java/core-app/run-plain.sh"
echo "  input-app    with agent: $ROOT/demo/java/input-app/run-with-agent.sh"
echo "  input-app    plain app : $ROOT/demo/java/input-app/run-plain.sh"
echo "  advanced-app with agent: $ROOT/demo/java/advanced-app/run-with-agent.sh"
echo "  advanced-app plain app : $ROOT/demo/java/advanced-app/run-plain.sh"
