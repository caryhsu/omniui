## 1. Java Agent

- [ ] 1.1 Add `get_toolbar_items` case in `performOnFxThread()` switch → call `doGetToolbarItems(node, fxId, handle)`
- [ ] 1.2 Implement `doGetToolbarItems()`: call `getItems()` on the ToolBar node, iterate items, build list of `{fxId, text, type, disabled}` maps

## 2. Python Client

- [ ] 2.1 Add `get_toolbar_items(self, *, id: str)` → `_perform("get_toolbar_items", {"id": id})`

## 3. Demo App

- [ ] 3.1 Confirm advanced-app has a ToolBar with `fx:id`; add one if missing
- [ ] 3.2 Create `demo/python/advanced/toolbar_demo.py`
- [ ] 3.3 Add `toolbar_demo` import + call to `demo/python/run_all.py` (advanced group)

## 4. Tests

- [ ] 4.1 Add `ToolBarTests` to `tests/test_client.py`: mock HTTP, verify action + payload
- [ ] 4.2 Run `python -m pytest tests/` — confirm all pass

## 5. Wrap-up

- [ ] 5.1 Update `ROADMAP.md` / `ROADMAP.zh-TW.md` — mark ToolBar as `[x]`
- [ ] 5.2 `git commit` + `git push`
