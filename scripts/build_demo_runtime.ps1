$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "[1/2] Installing local Java agent..."
mvn -pl java-agent -am clean install

Write-Host "[2/2] Building JavaFX demo runtime image..."
mvn -f demo/javafx-login-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink

$withAgent = Join-Path $root "demo\javafx-login-app\run-with-agent.bat"
$plain = Join-Path $root "demo\javafx-login-app\run-plain.bat"
Write-Host ""
Write-Host "Runtime image ready."
Write-Host "Next steps:"
Write-Host "  With agent: $withAgent"
Write-Host "  Plain app : $plain"
