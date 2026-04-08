#!/usr/bin/env bash
set -euo pipefail
ROOT="$(dirname "$0")/target/omniui-user-search-demo/bin"
AGENT="$(realpath "$(dirname "$0")/../../../java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar")"
"$ROOT/java" "-javaagent:$AGENT=port=48109" -m dev.omniui.demo.usersearch/dev.omniui.demo.usersearch.UserSearchApp "$@"
