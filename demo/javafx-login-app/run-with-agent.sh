#!/usr/bin/env bash
set -euo pipefail

to_win_path() {
  if command -v cygpath >/dev/null 2>&1; then
    cygpath -w "$1"
  else
    printf '%s\n' "$1"
  fi
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$SCRIPT_DIR/target/omniui-login-demo/bin"
AGENT_JAR="$SCRIPT_DIR/../../java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar"
"$ROOT/java" "-javaagent:$(to_win_path "$AGENT_JAR")" -m dev.omniui.demo/dev.omniui.demo.LoginDemoApp "$@"
