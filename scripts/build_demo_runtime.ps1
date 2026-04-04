$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

Write-Host "[1/4] Installing local Java agent..."
mvn -pl java-agent -am clean install

Write-Host "[2/4] Building core-app runtime image..."
mvn -f demo/java/core-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink

Write-Host "[3/4] Building input-app runtime image..."
mvn -f demo/java/input-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink

Write-Host "[4/4] Building advanced-app runtime image..."
mvn -f demo/java/advanced-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink

Write-Host ""
Write-Host "Runtime images ready."
Write-Host "Next steps:"
Write-Host "  core-app     with agent: $root\demo\java\core-app\run-with-agent.bat"
Write-Host "  core-app     plain app : $root\demo\java\core-app\run-plain.bat"
Write-Host "  input-app    with agent: $root\demo\java\input-app\run-with-agent.bat"
Write-Host "  input-app    plain app : $root\demo\java\input-app\run-plain.bat"
Write-Host "  advanced-app with agent: $root\demo\java\advanced-app\run-with-agent.bat"
Write-Host "  advanced-app plain app : $root\demo\java\advanced-app\run-plain.bat"
