$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-core-demo\bin"
& (Join-Path $root "java.exe") "-m" "dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp" @args
