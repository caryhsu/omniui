#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/target/omniui-input-demo/bin"
"$ROOT/java" -m dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp "$@"
