@echo off
setlocal

set ROOT=%~dp0target\omniui-login-demo\bin
"%ROOT%\java.exe" -m dev.omniui.demo/dev.omniui.demo.LoginDemoApp %*
