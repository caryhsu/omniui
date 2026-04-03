# Tasks: add-text-input-controls

## Group 1 — Demo App (LoginDemoApp.java)

- [x] 1.1 Add `import javafx.scene.control.TextArea`
- [x] 1.2 Add `import javafx.scene.control.PasswordField`
- [x] 1.3 Add `import javafx.scene.control.Hyperlink`
- [x] 1.4 Add TextArea demo section (id `demoTextArea`, pre-filled with multi-line text)
- [x] 1.5 Add PasswordField demo section (id `demoPasswordField`)
- [x] 1.6 Add Hyperlink demo section (id `demoHyperlink`, label "Click me")

## Group 2 — Java Agent (ReflectiveJavaFxTarget.java)

- [x] 2.1 Verify `get_text` path calls `getText()` — works for `TextInputControl` subclasses including `TextArea` and `PasswordField` (no code change expected; confirm by test)
- [x] 2.2 Verify `type` path calls `setText()` — same as above (no code change expected)
- [x] 2.3 Verify `click` path calls `fire()` — works for `ButtonBase` subclasses including `Hyperlink` (no code change expected)
- [x] 2.4 Add `get_value` support for `Hyperlink` visited state: ensure `safeInvoke(node, "isVisited")` is reachable via the `get_value` action

## Group 3 — Python Client (engine.py)

- [x] 3.1 Confirm `type(selector, text)` works for `TextArea` and `PasswordField` (no new method needed)
- [x] 3.2 Confirm `get_text(selector)` works for `TextArea` and `PasswordField`
- [x] 3.3 Confirm `click(selector)` works for `Hyperlink`
- [x] 3.4 Confirm `get_value(selector)` returns `isVisited()` for `Hyperlink`

## Group 4 — Demo Scripts

- [x] 4.1 Create `demo/python/text_area_demo.py`
- [x] 4.2 Create `demo/python/password_field_demo.py`
- [x] 4.3 Create `demo/python/hyperlink_demo.py`
- [x] 4.4 Register all three in `demo/python/run_all.py`

## Group 5 — Build & Test

- [x] 5.1 `mvn compile` — no errors
- [x] 5.2 Run `text_area_demo.py` — pass
- [x] 5.3 Run `password_field_demo.py` — pass
- [x] 5.4 Run `hyperlink_demo.py` — pass
