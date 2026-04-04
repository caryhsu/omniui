#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/target/omniui-input-demo/bin"
AGENT_JAR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)/java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar"
"$ROOT/java" "-javaagent:$AGENT_JAR=port=48101" -m dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp "$@"
