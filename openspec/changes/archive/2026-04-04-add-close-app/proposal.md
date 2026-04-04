## Why

Test scripts currently have no way to shut down the JavaFX application under test. The only option is to kill the process externally (e.g., `taskkill`), which is fragile and bypasses normal application lifecycle. A `close_app()` method allows tests to verify graceful shutdown behavior and clean up reliably after a test suite.

## What Changes

- Add `close_app()` to the Python client (`engine.py`) — sends a `"close_app"` action to the agent
- Add `case "close_app"` to the Java agent's `perform()` switch
- Implement `doCloseApp()` in the Java agent — wraps `Platform.exit()` via `ReflectiveJavaFxSupport.onFxThread()`
- Rebuild jlink after Java agent changes

## Capabilities

### New Capabilities
- `close-app`: Trigger graceful JavaFX application shutdown from a Python automation script via the agent

### Modified Capabilities
- `python-automation-client`: New `close_app()` method added to the public API surface

## Impact

- `java-agent/src/main/java/dev/omniui/agent/runtime/ReflectiveJavaFxTarget.java` — new case + method
- `omniui/core/engine.py` — new `close_app()` method
- `README.md` / `README.zh-TW.md` — document new method
- `docs/api/python-client.md` — new Close App section
- `ROADMAP.md` / `ROADMAP.zh-TW.md` — mark close_app `[x]`
- Requires `mvn clean install` + jlink rebuild
