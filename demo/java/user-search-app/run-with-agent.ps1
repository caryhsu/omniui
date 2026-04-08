$ErrorActionPreference = "Stop"
$root = Join-Path $PSScriptRoot "target\omniui-user-search-demo\bin"
$agentJar = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar")).Path
& "$root\java.exe" "-javaagent:$agentJar=port=48109" -m dev.omniui.demo.usersearch/dev.omniui.demo.usersearch.UserSearchApp @args
