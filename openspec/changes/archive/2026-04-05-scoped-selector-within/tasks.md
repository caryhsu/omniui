## 1. Java Agent — Scoped Node Lookup

- [ ] 1.1 Add `findNodeInSubtree(Object root, JsonObject selector)` helper that walks only descendants of `root` using the same matching logic as the existing `findNode`
- [ ] 1.2 In `performOnFxThread`, read `selector.get("scope")` if present; resolve the scope container via full-graph `findNode`; if not found return `failure("scope_not_found")`; otherwise call `findNodeInSubtree` with the container as root

## 2. Python Client — `within` Context Manager

- [ ] 2.1 Add `self._scope: dict | None = None` field to `OmniUIClient.__init__`
- [ ] 2.2 Add `within(**selector)` context manager: sets `self._scope = selector` on enter, clears on exit
- [ ] 2.3 In `_perform()`, if `self._scope` is set, merge it into the selector dict as `{"id":..., "scope": self._scope}` before calling `_perform_request`

## 3. Demo App — Scoped Selector Scene

- [ ] 3.1 In `AdvancedDemoApp`, add a `withinSection` with two panels (`leftPanel`, `rightPanel`), each containing a Button with `id="panelOk"` and a Label `id="panelStatus"`
- [ ] 3.2 Clicking each `panelOk` sets the corresponding `panelStatus` text

## 4. Python Demo

- [ ] 4.1 Create `demo/python/advanced/within_demo.py`: use `client.within(id="leftPanel")` to click `panelOk` and verify only `leftPanel`'s status updates
- [ ] 4.2 Add `within_demo` to `run_all.py`

## 5. Tests

- [ ] 5.1 Add `WithinTests` to `tests/test_client.py`: verify `within` injects `scope` into selector, verify scope is cleared after context, verify `scope_not_found` path

## 6. Documentation

- [ ] 6.1 Mark `Scoped selector (within)` as `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
