## 1. Java Agent

- [x] 1.1 Add `case "double_click"` to `performOnFxThread()` switch in `ReflectiveJavaFxTarget.java`, calling a new `handleDoubleClick(node, fxId, handle)` method
- [x] 1.2 Implement `handleDoubleClick()`: get node center from `getBoundsInLocal()`, convert to screen coords via `localToScreen()`, construct `MouseEvent.MOUSE_CLICKED` with `clickCount=2` via reflection, fire via `Event.fireEvent()` static method via reflection

## 2. Build

- [x] 2.1 Kill any running demo Java process, then run `mvn clean install -f java-agent/pom.xml`
- [x] 2.2 Delete `demo/javafx-login-app/target/omniui-demo` and run `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`

## 3. Python Client

- [x] 3.1 Add `double_click(**selector)` method to `OmniUIClient` in `omniui/core/engine.py`, delegating to `self._perform("double_click", selector)`

## 4. Demo & Tests

- [x] 4.1 Create `demo/python/double_click_demo.py` that launches the app, double-clicks a TreeView or ListView item, and verifies the interaction
- [x] 4.2 Add `double_click_demo` to `demo/python/run_all.py`

## 5. Documentation

- [x] 5.1 Add `double_click()` to the Actions section of `README.md` and `README.zh-TW.md`
- [x] 5.2 Add `double_click()` method reference to `docs/api/python-client.md`
- [x] 5.3 Mark Double-click `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
