$ErrorActionPreference = "Stop"

$root = Join-Path $PSScriptRoot "target\omniui-login-demo\bin"
$agentJar = Join-Path $PSScriptRoot "..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar"
& (Join-Path $root "java.exe") "-javaagent:$agentJar" "-m" "dev.omniui.demo/dev.omniui.demo.LoginDemoApp" @args
