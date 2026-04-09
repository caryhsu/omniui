@echo off
setlocal

set ROOT=%~dp0..\..\..\..
pushd "%ROOT%"
call mvn -f demo\java\advanced-app\pom.xml package
if errorlevel 1 goto :fail
java -p demo\java\advanced-app\target\classes;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\26\javafx-controls-26-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\26\javafx-graphics-26-win.jar -m dev.omniui.demo.advanced/dev.omniui.demo.advanced.AdvancedDemoApp %*
popd
exit /b 0

:fail
popd
exit /b 1
