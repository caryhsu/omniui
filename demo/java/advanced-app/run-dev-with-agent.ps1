$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
Set-Location $root
mvn -pl java-agent -am package
mvn -f demo/java/advanced-app/pom.xml package
$javafxControls = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-controls\24.0.1\javafx-controls-24.0.1-win.jar"
$javafxGraphics = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-graphics\24.0.1\javafx-graphics-24.0.1-win.jar"
$javafxBase = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-base\24.0.1\javafx-base-24.0.1-win.jar"
$gsonJar = Join-Path $env:USERPROFILE ".m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar"
$agentJar = Join-Path $root "java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar"
$modulePath = @(
    "demo/java/advanced-app/target/classes",
    "java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar",
    $javafxControls,
    $javafxGraphics,
    $javafxBase,
    $gsonJar
) -join ";"
java "-javaagent:$agentJar=port=48102" -p $modulePath -m dev.omniui.demo.advanced/dev.omniui.demo.advanced.AdvancedDemoApp @args
