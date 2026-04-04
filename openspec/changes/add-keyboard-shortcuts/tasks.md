## 1. Java Agent

- [x] 1.1 Add `case "press_key"` to `performOnFxThread()` switch in `ReflectiveJavaFxTarget.java`, calling a new `handlePressKey(node, fxId, handle, payload)` method; handle the no-selector case by resolving to scene focus owner or root
- [x] 1.2 Implement key string parser: split on `+`, uppercase each part, map `CTRL`→`CONTROL` / `WIN`→`META`, last token = key name, rest = modifiers; call `KeyCode.valueOf(name)` and catch `IllegalArgumentException` for unknown keys
- [x] 1.3 Implement `handlePressKey()`: build `KeyEvent.KEY_PRESSED` and `KEY_RELEASED` via reflection using parsed `KeyCode` + modifier flags; fire both events via `Event.fireEvent()` on the resolved node

## 2. Build

- [x] 2.1 Kill any running demo Java process, then run `mvn clean install -f java-agent/pom.xml`
- [x] 2.2 Delete `demo/javafx-login-app/target/omniui-demo` and run `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`

## 3. Python Client

- [x] 3.1 Add `press_key(key, **selector)` to `OmniUIClient` in `omniui/core/engine.py`; build payload `{"key": key, **selector}` and call `self._perform("press_key", payload)`

## 4. Demo & Tests

- [x] 4.1 Create `demo/python/keyboard_shortcuts_demo.py`: press `Tab` to move focus between fields, press `Escape` to dismiss a dialog, press `Enter` to confirm — verify via `get_text` or `verify_text`
- [x] 4.2 Add `keyboard_shortcuts_demo` to `demo/python/run_all.py`

## 5. Documentation

- [x] 5.1 Add `press_key()` to the Actions section of `README.md` and `README.zh-TW.md`
- [x] 5.2 Add `press_key()` method reference to `docs/api/python-client.md`
- [x] 5.3 Mark Keyboard shortcuts `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
