@echo off
setlocal

set ROOT=%~dp0target\omniui-advanced-demo\bin
"%ROOT%\java.exe" -m dev.omniui.demo.advanced/dev.omniui.demo.advanced.AdvancedDemoApp %*
