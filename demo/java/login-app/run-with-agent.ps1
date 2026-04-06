$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-login-demo\bin"
$agentJar = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar")).Path
& "$root\java.exe" "-javaagent:$agentJar=port=48108" -m dev.omniui.demo.login/dev.omniui.demo.login.LoginApp @args
