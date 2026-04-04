@echo off
setlocal

set ROOT=%~dp0target\omniui-input-demo\bin
"%ROOT%\java.exe" -m dev.omniui.demo.input/dev.omniui.demo.input.InputDemoApp %*
