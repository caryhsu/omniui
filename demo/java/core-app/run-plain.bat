@echo off
setlocal

set ROOT=%~dp0target\omniui-core-demo\bin
"%ROOT%\java.exe" -m dev.omniui.demo.core/dev.omniui.demo.core.CoreDemoApp %*
