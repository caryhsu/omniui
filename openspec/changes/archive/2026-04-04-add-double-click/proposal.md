## Why

JavaFX applications commonly use double-click to trigger actions such as expanding tree nodes, opening detail views from list/table rows, and invoking custom handlers. The current `click()` action calls `node.fire()` which dispatches an `ActionEvent` — it does not fire a `MouseEvent` and therefore cannot satisfy `setOnMouseClicked` handlers that check `event.getClickCount() == 2`. A dedicated `double_click()` action is needed to cover these scenarios.

## What Changes

- Add `double_click` action to the Java agent (`ReflectiveJavaFxTarget`) that fires a synthetic `MouseEvent.MOUSE_CLICKED` with `clickCount=2` on the target node
- Add `double_click(**selector)` method to the Python client (`engine.py`)
- Add a demo script `double_click_demo.py` and register it in `run_all.py`
- Update README (EN + zh-TW), `docs/api/python-client.md`, and ROADMAP

## Capabilities

### New Capabilities
- `double-click`: Dispatches a synthetic double-click `MouseEvent` (clickCount=2) on a target JavaFX node identified by selector

### Modified Capabilities
- `python-automation-client`: Add `double_click(**selector)` method requirement

## Impact

- `java-agent/src/main/java/dev/omniui/agent/runtime/ReflectiveJavaFxTarget.java` — new case + handler method
- `omniui/core/engine.py` — new `double_click()` method
- `demo/python/double_click_demo.py` — new demo
- `demo/python/run_all.py` — add double-click demo
- `README.md`, `README.zh-TW.md`, `docs/api/python-client.md`, `ROADMAP.md`, `ROADMAP.zh-TW.md`
- jlink image rebuild required after Java agent change
