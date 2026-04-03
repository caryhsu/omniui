@echo off
setlocal

set ROOT=%~dp0..\..
pushd "%ROOT%"
call mvn -pl java-agent -am package
if errorlevel 1 goto :fail
call mvn -f demo\javafx-login-app\pom.xml package
if errorlevel 1 goto :fail
java -javaagent:java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar -p demo\javafx-login-app\target\classes;java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar -m dev.omniui.demo/dev.omniui.demo.LoginDemoApp %*
popd
exit /b 0

:fail
popd
exit /b 1
