@echo off
setlocal

set ROOT=%~dp0..\..\..
pushd "%ROOT%"
call mvn -pl java-agent -am package
if errorlevel 1 goto :fail
call mvn -f demo\java\input-app\pom.xml package
if errorlevel 1 goto :fail
java -javaagent:java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar=port=48101 -p demo\java\input-app\target\classes;java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\26\javafx-controls-26-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\26\javafx-graphics-26-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\26\javafx-base-26-win.jar;%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar -m dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp %*
popd
exit /b 0

:fail
popd
exit /b 1
