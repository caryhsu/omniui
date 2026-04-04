$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-advanced-demo\bin"
& (Join-Path $root "java.exe") "-m" "dev.omniui.demo.advanced/dev.omniui.demo.advanced.AdvancedDemoApp" @args
