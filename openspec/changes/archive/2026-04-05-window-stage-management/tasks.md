## 1. Java Agent

- [ ] 1.1 Add `findStageByTitle(title)` helper: call `Stage.getWindows()` via reflection, filter to Stage instances, find by `getTitle()`
- [ ] 1.2 Add window actions to `perform()` **before** node lookup (detect by action name):
  - `get_windows` → list all Stage titles
  - `focus_window` / `maximize_window` / `minimize_window` / `restore_window`
  - `is_maximized` / `is_minimized`
  - `set_window_size` / `set_window_position`
  - `get_window_size` / `get_window_position`
- [ ] 1.3 Implement `handleGetWindows()`: iterate `Stage.getWindows()`, collect titles
- [ ] 1.4 Implement `handleWindowAction(action, title, payload)`: find stage by title, dispatch to appropriate Stage method

## 2. Demo App

- [ ] 2.1 Add "Open Second Window" button (`id="openSecondWindowBtn"`) to `AdvancedDemoApp`; clicking opens a new Stage with title `"OmniUI Second Window"`

## 3. Python Client

- [ ] 3.1 Add `get_windows(self)` — no selector, `_perform_window("get_windows", {})`
- [ ] 3.2 Add `focus_window(self, *, title)`, `maximize_window`, `minimize_window`, `restore_window`
- [ ] 3.3 Add `is_maximized(self, *, title)`, `is_minimized(self, *, title)`
- [ ] 3.4 Add `set_window_size(self, *, title, width, height)`, `set_window_position(self, *, title, x, y)`
- [ ] 3.5 Add `get_window_size(self, *, title)`, `get_window_position(self, *, title)`
- [ ] 3.6 Add `_perform_window` helper that sends action with empty selector + payload

## 4. Demo Script

- [ ] 4.1 Create `demo/python/advanced/window_demo.py`
- [ ] 4.2 Add import + `Window Demo` section to `demo/python/run_all.py`

## 5. Tests

- [ ] 5.1 Add `WindowTests` to `tests/test_client.py`
- [ ] 5.2 Run `python -m pytest tests/` — confirm all pass

## 6. Wrap-up

- [ ] 6.1 Update `ROADMAP.md` / `ROADMAP.zh-TW.md` — mark Window / Stage management as `[x]`
- [ ] 6.2 `git commit` + `git push`
