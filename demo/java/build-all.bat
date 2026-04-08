@echo off
setlocal
set ROOT=%~dp0..\..
pushd "%ROOT%"

echo [build-all] Building agent...
call mvn -pl java-agent -am package -q
if errorlevel 1 goto :fail

for %%A in (
    core-app
    input-app
    advanced-app
    drag-app
    progress-app
    image-app
    color-app
    todo-app
    login-app
    user-search-app
    dynamic-fxml-app
    explorer-app
    settings-app
) do (
    echo [build-all] Building %%A...
    call mvn -f demo\java\%%A\pom.xml package -q
    if errorlevel 1 goto :fail
)

echo [build-all] All apps built successfully.
popd
exit /b 0
:fail
echo [build-all] FAILED.
popd
exit /b 1
