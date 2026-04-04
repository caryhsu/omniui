$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-core-demo\bin"
$agentJar = Join-Path $PSScriptRoot "..\..\..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar"
& (Join-Path $root "java.exe") "-javaagent:$agentJar=port=48100" "-m" "dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp" @args
