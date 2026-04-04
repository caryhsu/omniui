$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-input-demo\bin"
$agentJar = Join-Path $PSScriptRoot "..\..\..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar"
& (Join-Path $root "java.exe") "-javaagent:$agentJar=port=48101" "-m" "dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp" @args
