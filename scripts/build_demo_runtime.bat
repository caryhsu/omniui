@echo off
setlocal

set ROOT=%~dp0..
pushd "%ROOT%"

echo [1/2] Installing local Java agent...
call mvn -pl java-agent -am clean install
if errorlevel 1 goto :fail

echo [2/2] Building JavaFX demo runtime image...
call mvn -f demo/javafx-login-app/pom.xml clean org.openjfx:javafx-maven-plugin:jlink
if errorlevel 1 goto :fail

echo.
echo Runtime image ready.
echo Next steps:
echo   With agent: %ROOT%\demo\javafx-login-app\run-with-agent.bat
echo   Plain app : %ROOT%\demo\javafx-login-app\run-plain.bat
popd
exit /b 0

:fail
popd
exit /b 1
