@echo off
setlocal

set ROOT=%~dp0target\omniui-input-demo\bin
set AGENT_JAR=%~dp0..\..\..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar
"%ROOT%\java.exe" -javaagent:"%AGENT_JAR%=port=48101" -m dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp %*
