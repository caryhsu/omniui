@echo off
setlocal
set ROOT=%~dp0\..\..\..
pushd "%ROOT%"
java -javaagent:java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar=port=48109 ^
  -p demo\java\user-search-app\target\classes;java-agent\target\omniui-java-agent-0.1.0-SNAPSHOT.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\26\javafx-controls-26-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\26\javafx-graphics-26-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\26\javafx-base-26-win.jar;%USERPROFILE%\.m2\repository\com\google\code\gson\gson\2.11.0\gson-2.11.0.jar ^
  -m dev.omniui.demo.usersearch/dev.omniui.demo.usersearch.UserSearchApp %*
popd
exit /b 0
