## 1. Java Agent

- [ ] 1.1 Add `get_scroll_position` case in `performOnFxThread()` switch → call `handleGetScrollPosition(node, fxId, handle)`
- [ ] 1.2 Implement `handleGetScrollPosition()`: `instanceof ScrollBar` check, call `getValue()`/`getMin()`/`getMax()`, return JSON `{value, min, max}`
- [ ] 1.3 Add `set_scroll_position` case in `performOnFxThread()` switch → call `handleSetScrollPosition(node, payload, handle)`
- [ ] 1.4 Implement `handleSetScrollPosition()`: parse `value` from payload, clamp to `[min, max]`, call `setValue()`

## 2. Demo App

- [ ] 2.1 Add a standalone `ScrollBar` with `fx:id="demoScrollBar"` to `AdvancedDemoApp` (vertical, min=0, max=100, value=30)

## 3. Python Client

- [ ] 3.1 Add `get_scroll_position(self, *, id: str)` in `engine.py` → `_perform("get_scroll_position", {"id": id})`
- [ ] 3.2 Add `set_scroll_position(self, *, id: str, value: float)` in `engine.py` → `_perform("set_scroll_position", {"id": id, "value": value})`

## 4. Demo Script

- [ ] 4.1 Create `demo/python/advanced/scrollbar_demo.py`: call `get_scroll_position`, `set_scroll_position`, verify round-trip
- [ ] 4.2 Add `scrollbar_demo` import + `ScrollBar Demo` section to `demo/python/run_all.py`

## 5. Tests

- [ ] 5.1 Add `ScrollBarTests` to `tests/test_client.py`: mock HTTP, verify action + payload for both methods
- [ ] 5.2 Run `python -m pytest tests/` — confirm all pass

## 6. Wrap-up

- [ ] 6.1 Update `ROADMAP.md` / `ROADMAP.zh-TW.md` — mark ScrollBar as `[x]`
- [ ] 6.2 `git commit` + `git push`
