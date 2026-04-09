@echo off
setlocal

set ROOT=%~dp0..\..\..\..
pushd "%ROOT%"
call mvn -f demo\java\input-app\pom.xml package
if errorlevel 1 goto :fail
java -p demo\java\input-app\target\classes;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\24.0.1\javafx-controls-24.0.1-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\24.0.1\javafx-graphics-24.0.1-win.jar -m dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp %*
popd
exit /b 0

:fail
popd
exit /b 1
