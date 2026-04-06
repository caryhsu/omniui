#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/target/omniui-login-demo/bin"
"$ROOT/java" -m dev.omniui.demo.login/dev.omniui.demo.login.LoginApp "$@"
