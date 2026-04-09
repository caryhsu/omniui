$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
Set-Location $root
mvn -f demo/java/core-app/pom.xml package
$javafxControls = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-controls\26\javafx-controls-26-win.jar"
$javafxGraphics = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-graphics\26\javafx-graphics-26-win.jar"
$modulePath = @(
    "demo/java/core-app/target/classes",
    $javafxControls,
    $javafxGraphics
) -join ";"
java -p $modulePath -m dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp @args
