$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
Set-Location $root
mvn -f demo/java/input-app/pom.xml package
$javafxControls = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar"
$javafxGraphics = Join-Path $env:USERPROFILE ".m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar"
$modulePath = @(
    "demo/java/input-app/target/classes",
    $javafxControls,
    $javafxGraphics
) -join ";"
java -p $modulePath -m dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp @args
