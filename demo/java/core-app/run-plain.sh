#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/target/omniui-core-demo/bin"
"$ROOT/java" -m dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp "$@"
