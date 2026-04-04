@echo off
setlocal

set ROOT=%~dp0target\omniui-core-demo\bin
set AGENT_JAR=%~dp0..\..\..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar
"%ROOT%\java.exe" -javaagent:"%AGENT_JAR%=port=48100" -m dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp %*
