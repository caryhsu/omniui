$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-login-demo\bin"
& "$root\java.exe" -m dev.omniui.demo.login/dev.omniui.demo.login.LoginApp @args
