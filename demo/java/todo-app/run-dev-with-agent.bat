@echo off
setlocal

set ROOT=%~dp0..\..\..
pushd "%ROOT%"
call mvn -pl java-agent -am clean package
if errorlevel 1 goto :fail
call mvn -f demo\java\todo-app\pom.xml package
if errorlevel 1 goto :fail
java -javaagent:java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar=port=48107 -p demo\java\todo-app\target\classes;java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\24.0.1\javafx-controls-24.0.1-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\24.0.1\javafx-graphics-24.0.1-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\24.0.1\javafx-base-24.0.1-win.jar;%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar -m dev.omniui.demo.todo/dev.omniui.demo.todo.TodoDemoApp %*
popd
exit /b 0

:fail
popd
exit /b 1
