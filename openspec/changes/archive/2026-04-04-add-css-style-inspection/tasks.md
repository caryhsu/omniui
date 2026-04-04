## 1. Java Agent

- [x] 1.1 In `performOnFxThread()` in `ReflectiveJavaFxTarget.java`: add `case "get_style"` — call `safeInvoke(node, "getStyle")`, return result string (never null)
- [x] 1.2 In `performOnFxThread()`: add `case "get_style_class"` — call `safeInvoke(node, "getStyleClass")`, convert `ObservableList` to `new ArrayList<>(...)`, return as value

## 2. Python Client

- [x] 2.1 Add `get_style(**selector) -> ActionResult` to `engine.py` (after `get_tooltip`)
- [x] 2.2 Add `get_style_class(**selector) -> ActionResult` to `engine.py` (after `get_style`)

## 3. Demo App — add inline style to a node

- [x] 3.1 In `LoginDemoApp.java`: call `setStyle("-fx-text-fill: green;")` on `regionLabel` (already has id `"regionLabel"`) so the demo can read it back

## 4. Build

- [x] 4.1 Run `mvn clean install -f java-agent/pom.xml`
- [x] 4.2 Delete `demo/javafx-login-app/target/omniui-demo` and run `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`

## 5. Demo & Tests

- [x] 5.1 Create `demo/python/css_style_demo.py`: `get_style(id="regionLabel")` asserts `-fx-text-fill: green`, `get_style_class(id="loginButton")` asserts list contains `"button"`, `get_style(id="status")` asserts `""` (no inline style)
- [x] 5.2 Add `css_style_demo` to `demo/python/run_all.py`

## 6. Documentation

- [x] 6.1 Add `get_style`, `get_style_class` to `README.md` and `README.zh-TW.md`
- [x] 6.2 Add method docs to `docs/api/python-client.md`
- [x] 6.3 Mark `CSS style inspection` `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
