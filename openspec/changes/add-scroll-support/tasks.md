## 1. Java Agent

- [ ] 1.1 In `ReflectiveJavaFxTarget.performOnFxThread()`: add `case "scroll_to"` — walk `node.getParent()` chain to find `ScrollPane`, compute `vvalue` from node bounds, call `scrollPane.setVvalue(clamped)`
- [ ] 1.2 Add `case "scroll_by"` — resolve selector to `ScrollPane` (or find first in scene), read `delta_x`/`delta_y` from params, apply clamped offset to `hvalue`/`vvalue`
- [ ] 1.3 Build java-agent: `mvn clean install -f java-agent/pom.xml`
- [ ] 1.4 Rebuild jlink: delete `demo/javafx-login-app/target/omniui-demo`, run `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`

## 2. Demo App Fixture

- [ ] 2.1 In `LoginDemoApp.java`: add a `ScrollPane` with `fx:id="demoScrollPane"` wrapping a `VBox` containing 30 `Label` rows (`row0`..`row29`) with incrementing text
- [ ] 2.2 Ensure the `ScrollPane` is tall enough that rows beyond ~10 are off-screen in the default window size

## 3. Python Client

- [ ] 3.1 In `engine.py`: add `scroll_to(**selector) -> ActionResult` — dispatches `scroll_to` action with selector
- [ ] 3.2 Add `scroll_by(delta_x: float, delta_y: float, **selector) -> ActionResult` — dispatches `scroll_by` action with `delta_x`, `delta_y`, and selector merged into payload

## 4. Demo & Tests

- [ ] 4.1 Create `demo/python/scroll_demo.py`: test `scroll_to(id="row29")`, then `scroll_by(0.0, -1.0, id="demoScrollPane")` to scroll back to top, verify both return `ok=True`
- [ ] 4.2 Add `scroll_demo` to `demo/python/run_all.py`

## 5. Documentation

- [ ] 5.1 Add `scroll_to` and `scroll_by` to `docs/api/python-client.md`
- [ ] 5.2 Update `README.md` and `README.zh-TW.md` actions list
- [ ] 5.3 Mark `Scroll` `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
