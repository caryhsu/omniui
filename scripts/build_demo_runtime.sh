#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "[1/2] Installing local Java agent..."
mvn -pl java-agent -am clean install

echo "[2/2] Building JavaFX demo runtime image..."
mvn -f demo/javafx-login-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink

echo
echo "Runtime image ready."
echo "Next steps:"
echo "  With agent: $ROOT/demo/javafx-login-app/run-with-agent.sh"
echo "  Plain app : $ROOT/demo/javafx-login-app/run-plain.sh"
