## Why

`core-app` bundles a login form (username, password, loginButton, status) together with unrelated controls (ListView, TableView, ComboBox, TreeView, Grid). This makes `core-app` harder to read and means the login-specific Python demos (`login_direct.py`, `login_with_fallback.py`) live in a `core/` folder that doesn't describe their purpose. Extracting login into a dedicated app gives it a clear home and keeps `core-app` focused on control coverage.

## What Changes

- New JavaFX app `demo/java/login-app/` with a standalone login form (username, password, loginButton, status label)
- Remove the login section from `CoreDemoApp.java` (keep all other sections: selection, hierarchy, table, grid)
- New Python demo directory `demo/python/login/` with `login_demo.py` (consolidates `login_direct.py` + `login_with_fallback.py`)
- Move `login_direct.py` and `login_with_fallback.py` from `demo/python/core/` to `demo/python/login/`
- Add `login-app` fixture and smoke tests to `tests/integration/test_smoke_apps.py`
- Register `login-app` in root `pom.xml` modules list

## Capabilities

### New Capabilities

- `login-app-demo`: Standalone JavaFX login form app used to demonstrate and test `click`, `type`, `verify_text` authentication flow automation.

### Modified Capabilities

- `core-app-demo`: Login section removed from `CoreDemoApp.java`; remaining sections (selection, hierarchy, table, grid) unchanged. Existing `core` smoke tests (login success/failure) will be replaced by new `login` smoke tests.

## Impact

- `demo/java/core-app/src/main/java/dev/omniui/demo/core/CoreDemoApp.java` — remove login section (~30 lines)
- `demo/java/login-app/` — new Maven module (pom.xml + module-info.java + LoginApp.java)
- `pom.xml` — add `demo/java/login-app` module
- `demo/python/login/` — new directory with `_bootstrap.py`, `_runtime.py`, `login_demo.py`
- `demo/python/core/login_direct.py`, `login_with_fallback.py` — moved to `demo/python/login/`
- `tests/integration/test_smoke_apps.py` — replace core login tests with login-app fixture; update core smoke tests to reflect removed login section
- Port assignment: login-app demo = 48108, smoke test = 49108
