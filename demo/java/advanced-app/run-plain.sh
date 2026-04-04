#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/target/omniui-advanced-demo/bin"
"$ROOT/java" -m dev.omniui.demo.advanced/dev.omniui.demo.advanced.AdvancedDemoApp "$@"
