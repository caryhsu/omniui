$ErrorActionPreference = "Stop"

$root = Join-Path $PSScriptRoot "target\omniui-login-demo\bin"
& (Join-Path $root "java.exe") "-m" "dev.omniui.demo/dev.omniui.demo.LoginDemoApp" @args
