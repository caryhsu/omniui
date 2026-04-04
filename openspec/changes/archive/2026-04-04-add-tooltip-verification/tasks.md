## 1. Java Agent

- [x] 1.1 In `performOnFxThread()` in `ReflectiveJavaFxTarget.java`: add `case "get_tooltip"` — resolve node, call `getTooltip()` via reflection, call `getText()` on result; return `""` if tooltip or text is null

## 2. Python Client

- [x] 2.1 Add `get_tooltip(**selector) -> ActionResult` to `engine.py` (after `get_text`)

## 3. Demo App — add tooltip to a node

- [x] 3.1 In `LoginDemoApp.java`: add a `Tooltip` to an existing node (e.g. `loginButton`) so the demo can read it back

## 4. Build

- [x] 4.1 Run `mvn clean install -f java-agent/pom.xml`
- [x] 4.2 Delete `demo/javafx-login-app/target/omniui-demo` and run `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`

## 5. Demo & Tests

- [x] 5.1 Create `demo/python/tooltip_demo.py`: call `get_tooltip(id="loginButton")`, assert text matches expected, also verify node-with-no-tooltip returns `""`
- [x] 5.2 Add `tooltip_demo` to `demo/python/run_all.py`

## 6. Documentation

- [x] 6.1 Add `get_tooltip` to `README.md` and `README.zh-TW.md`
- [x] 6.2 Add `get_tooltip` method doc to `docs/api/python-client.md`
- [x] 6.3 Mark `Tooltip verification` `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
