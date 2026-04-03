#!/usr/bin/env bash
set -euo pipefail

to_win_path() {
  if command -v cygpath >/dev/null 2>&1; then
    cygpath -w "$1"
  else
    printf '%s\n' "$1"
  fi
}

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"
mvn -f demo/javafx-login-app/pom.xml package
JAVA_FX_CONTROLS="$USERPROFILE/.m2/repository/org/openjfx/javafx-controls/21.0.2/javafx-controls-21.0.2-win.jar"
JAVA_FX_GRAPHICS="$USERPROFILE/.m2/repository/org/openjfx/javafx-graphics/21.0.2/javafx-graphics-21.0.2-win.jar"
MODULE_PATH="$(to_win_path "$ROOT/demo/javafx-login-app/target/classes");$(to_win_path "$JAVA_FX_CONTROLS");$(to_win_path "$JAVA_FX_GRAPHICS")"
java -p "$MODULE_PATH" -m dev.omniui.demo/dev.omniui.demo.LoginDemoApp "$@"
