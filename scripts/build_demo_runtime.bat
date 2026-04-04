@echo off
setlocal

set ROOT=%~dp0..
pushd "%ROOT%"

echo [1/4] Installing local Java agent...
call mvn -pl java-agent -am clean install
if errorlevel 1 goto :fail

echo [2/4] Building core-app runtime image...
call mvn -f demo/java/core-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink
if errorlevel 1 goto :fail

echo [3/4] Building input-app runtime image...
call mvn -f demo/java/input-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink
if errorlevel 1 goto :fail

echo [4/4] Building advanced-app runtime image...
call mvn -f demo/java/advanced-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink
if errorlevel 1 goto :fail

echo.
echo Runtime images ready.
echo Next steps:
echo   core-app     with agent: %ROOT%\demo\java\core-app\run-with-agent.bat
echo   core-app     plain app : %ROOT%\demo\java\core-app\run-plain.bat
echo   input-app    with agent: %ROOT%\demo\java\input-app\run-with-agent.bat
echo   input-app    plain app : %ROOT%\demo\java\input-app\run-plain.bat
echo   advanced-app with agent: %ROOT%\demo\java\advanced-app\run-with-agent.bat
echo   advanced-app plain app : %ROOT%\demo\java\advanced-app\run-plain.bat
popd
exit /b 0

:fail
popd
exit /b 1
