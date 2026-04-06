#!/usr/bin/env bash
set -euo pipefail

to_win_path() {
  if command -v cygpath >/dev/null 2>&1; then
    cygpath -w "$1"
  else
    printf '%s\n' "$1"
  fi
}

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "$ROOT"
mvn -pl java-agent -am package
mvn -f demo/java/login-app/pom.xml package
JAVA_FX_CONTROLS="$USERPROFILE/.m2/repository/org/openjfx/javafx-controls/21.0.2/javafx-controls-21.0.2-win.jar"
JAVA_FX_GRAPHICS="$USERPROFILE/.m2/repository/org/openjfx/javafx-graphics/21.0.2/javafx-graphics-21.0.2-win.jar"
JAVA_FX_BASE="$USERPROFILE/.m2/repository/org/openjfx/javafx-base/21.0.2/javafx-base-21.0.2-win.jar"
GSON_JAR="$USERPROFILE/.m2/repository/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar"
AGENT_JAR="$ROOT/java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar"
MODULE_PATH="$(to_win_path "$ROOT/demo/java/login-app/target/classes");$(to_win_path "$ROOT/java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar");$(to_win_path "$JAVA_FX_CONTROLS");$(to_win_path "$JAVA_FX_GRAPHICS");$(to_win_path "$JAVA_FX_BASE");$(to_win_path "$GSON_JAR")"
java "-javaagent:$(to_win_path "$AGENT_JAR")=port=48108" -p "$MODULE_PATH" -m dev.omniui.demo.login/dev.omniui.demo.login.LoginApp "$@"
