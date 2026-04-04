$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-advanced-demo\bin"
$agentJar = Join-Path $PSScriptRoot "..\..\..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar"
& (Join-Path $root "java.exe") "-javaagent:$agentJar=port=48102" "-m" "dev.omniui.demo.advanced/dev.omniui.demo.advanced.AdvancedDemoApp" @args
