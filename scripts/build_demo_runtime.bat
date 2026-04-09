@echo off
setlocal

set ROOT=%~dp0..
pushd "%ROOT%"

set APPS=core-app input-app advanced-app login-app drag-app progress-app image-app color-app todo-app settings-app dynamic-fxml-app explorer-app user-search-app
set STEP=0
set TOTAL=14

set /a STEP+=1
echo [%STEP%/%TOTAL%] Installing local Java agent...
call mvn -pl java-agent -am clean install
if errorlevel 1 goto :fail

for %%A in (%APPS%) do (
    set /a STEP+=1
    call echo [%%STEP%%/%TOTAL%] Building %%A runtime image...
    call mvn -f demo/java/%%A/pom.xml clean org.openjfx:javafx-maven-plugin:jlink
    if errorlevel 1 goto :fail
)

echo.
echo Runtime images ready.
echo Next steps:
for %%A in (%APPS%) do (
    echo   %%A  with agent: %ROOT%\demo\java\%%A\run-with-agent.bat
    echo   %%A  plain app : %ROOT%\demo\java\%%A\run-plain.bat
)
popd
exit /b 0

:fail
popd
exit /b 1
