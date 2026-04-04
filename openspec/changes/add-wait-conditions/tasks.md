## 1. Python Engine — Wait Methods

- [x] 1.1 Add `wait_for_text(id, expected, timeout=5.0, interval=0.2)` to `omniui/core/engine.py` — poll `get_text(id=id)` until result matches `expected`, raise `TimeoutError` on expiry
- [x] 1.2 Add `wait_for_visible(id, timeout=5.0, interval=0.2)` to `omniui/core/engine.py` — poll `is_visible(id=id)`, raise `TimeoutError` on expiry
- [x] 1.3 Add `wait_for_enabled(id, timeout=5.0, interval=0.2)` to `omniui/core/engine.py` — poll `is_enabled(id=id)`, raise `TimeoutError` on expiry
- [x] 1.4 Add `wait_for_node(id, timeout=5.0, interval=0.2)` to `omniui/core/engine.py` — poll `get_nodes()` for a node with matching `fxId`, raise `TimeoutError` on expiry
- [x] 1.5 Add `wait_for_value(id, expected, timeout=5.0, interval=0.2)` to `omniui/core/engine.py` — delegate to `wait_for_text`

## 2. Demo Script

- [x] 2.1 Create `demo/python/wait_conditions_demo.py` — demo that uses `set_visible`/`set_disabled` to simulate async state changes and verifies `wait_for_visible`, `wait_for_enabled`, `wait_for_text` work correctly
- [x] 2.2 Add `wait_conditions_demo` to `demo/python/run_all.py`

## 3. Documentation

- [x] 3.1 Update `README.md` — add wait condition methods to the Actions section
- [x] 3.2 Update `README.zh-TW.md` — same update in Traditional Chinese
- [x] 3.3 Update `docs/api/python-client.md` — add Wait Conditions section with all 5 methods, signatures, and examples
- [x] 3.4 Update `ROADMAP.md` — mark wait conditions `[x]`
- [x] 3.5 Update `ROADMAP.zh-TW.md` — same update
