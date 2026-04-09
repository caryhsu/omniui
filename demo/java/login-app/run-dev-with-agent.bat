@echo off
setlocal

set ROOT=%~dp0..\..\..
pushd "%ROOT%"
call mvn clean -pl java-agent -am package
if errorlevel 1 goto :fail
call mvn -f demo\java\login-app\pom.xml package
if errorlevel 1 goto :fail
java -javaagent:java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar=port=48108 -p demo\java\login-app\target\classes;java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\26\javafx-controls-26-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\26\javafx-graphics-26-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\26\javafx-base-26-win.jar;%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar -m dev.omniui.demo.login/dev.omniui.demo.login.LoginApp %*
popd
exit /b 0

:fail
popd
exit /b 1
