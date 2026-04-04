#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/target/omniui-core-demo/bin"
AGENT_JAR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)/java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar"
"$ROOT/java" "-javaagent:$AGENT_JAR=port=48100" -m dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp "$@"
