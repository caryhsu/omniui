## 1. Java Agent

- [x] 1.1 Add `case "close_app" -> doCloseApp();` to the top-level `perform()` switch in `ReflectiveJavaFxTarget.java` (before the `default` case)
- [x] 1.2 Implement `doCloseApp()` — wrap `Platform.exit()` in `ReflectiveJavaFxSupport.onFxThread()`, invoke via reflection using `ReflectiveJavaFxSupport.loadClass("javafx.application.Platform")` and call `exit()` statically; return `ActionResult.success(...)` immediately

## 2. Build

- [x] 2.1 Run `mvn clean install -f java-agent/pom.xml` to compile and install the updated agent
- [x] 2.2 Delete the old jlink image (`demo/javafx-login-app/target/omniui-demo`) and run `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml` to rebuild

## 3. Python Engine

- [x] 3.1 Add `close_app()` to `omniui/core/engine.py` — sends `"close_app"` action with no selector or payload

## 4. Documentation

- [x] 4.1 Update `README.md` — add `close_app()` to the Actions section
- [x] 4.2 Update `README.zh-TW.md` — same update in Traditional Chinese
- [x] 4.3 Update `docs/api/python-client.md` — add Close App section
- [x] 4.4 Update `ROADMAP.md` — mark close_app `[x]`
- [x] 4.5 Update `ROADMAP.zh-TW.md` — same update
