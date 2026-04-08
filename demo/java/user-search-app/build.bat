@echo off
setlocal
set ROOT=%~dp0\..\..\..
pushd "%ROOT%"
echo [build] Building agent...
call mvn -pl java-agent -am package -q
if errorlevel 1 goto :fail
echo [build] Building user-search-app...
call mvn -f demo\java\user-search-app\pom.xml package -q
if errorlevel 1 goto :fail
echo [build] Done.
popd
exit /b 0
:fail
echo [build] FAILED.
popd
exit /b 1
