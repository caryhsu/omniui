$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-input-demo\bin"
& (Join-Path $root "java.exe") "-m" "dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp" @args
