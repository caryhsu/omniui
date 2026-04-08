@echo off
set "ROOT=%~dp0target\omniui-user-search-demo\bin"
set "AGENT=%~dp0..\..\..\java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar"
"%ROOT%\java.exe" "-javaagent:%AGENT%=port=48109" -m dev.omniui.demo.usersearch/dev.omniui.demo.usersearch.UserSearchApp %*
