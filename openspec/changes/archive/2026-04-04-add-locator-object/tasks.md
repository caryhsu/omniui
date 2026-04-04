## 1. Python — Locator class

- [x] 1.1 Create `omniui/locator.py` with `Locator` class
  - Constructor `__init__(client, **selector)` raises `ValueError` if no selector given
  - Delegation methods: `click`, `double_click`, `right_click`, `press_key`, `type`,
    `get_text`, `verify_text`, `get_tooltip`, `get_style`, `get_style_class`,
    `is_visible`, `is_enabled`, `is_visited`,
    `get_value`, `get_progress`, `get_selected`, `get_selected_items`,
    `select`, `select_multiple`, `set_selected`, `set_slider`, `set_spinner`, `step_spinner`,
    `get_tabs`, `select_tab`, `scroll_to`, `scroll_by`,
    `expand_pane`, `collapse_pane`, `get_expanded`
  - Wait methods: `wait_for_visible`, `wait_for_enabled`, `wait_for_node`,
    `wait_for_text`, `wait_for_value` — extract `id` from `_sel`; raise `ValueError` if missing
  - `__repr__` returns `Locator(id='...')`
- [x] 1.2 Add `OmniUIClient.locator(**selector) -> Locator` in `omniui/core/engine.py` (lazy import)
- [x] 1.3 Export `Locator` from `omniui/__init__.py`

## 2. Tests

- [x] 2.1 `LocatorTests.test_locator_repr` — `repr(loc)` contains selector value
- [x] 2.2 `LocatorTests.test_locator_requires_selector` — `client.locator()` raises `ValueError`
- [x] 2.3 `LocatorTests.test_locator_click_delegates_to_client` — `loc.click()` returns `ok=True`
- [x] 2.4 `LocatorTests.test_locator_verify_text_delegates_to_client` — `loc.verify_text("Hello")` returns correct result
- [x] 2.5 `LocatorTests.test_locator_wait_for_visible_requires_id` — `loc.wait_for_visible()` raises `ValueError` when locator has no `id=`
- [x] 2.6 `LocatorTests.test_locator_exported_from_package` — `from omniui import Locator` works

## 3. Documentation

- [x] 3.1 Update `docs/api/python-client.md` — add Locator section with factory method and full method list
- [x] 3.2 Update `docs/api/python-client.zh-TW.md` — same in Traditional Chinese
- [x] 3.3 Update `openspec/specs/python-automation-client/spec.md` — add Locator requirement with 3 scenarios
- [x] 3.4 Update `openspec/specs/python-automation-client/spec.zh-TW.md` — same in Traditional Chinese
- [x] 3.5 Update `ROADMAP.md` — mark Locator `[x]`
- [x] 3.6 Update `ROADMAP.zh-TW.md` — same
