$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
Set-Location $root
mvn -pl java-agent -am package
mvn -f demo/java/core-app/pom.xml package
$javafxControls = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar"
$javafxGraphics = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar"
$agentJar = Join-Path $root "java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar"
$modulePath = @(
    "demo/java/core-app/target/classes",
    "java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar",
    $javafxControls,
    $javafxGraphics
) -join ";"
java "-javaagent:$agentJar=port=48100" -p $modulePath -m dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp @args
