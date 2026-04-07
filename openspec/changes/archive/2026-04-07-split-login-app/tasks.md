## 1. Create login-app Java module

- [x] 1.1 Create directory `demo/java/login-app/src/main/java/dev/omniui/demo/login/`
- [x] 1.2 Create `demo/java/login-app/pom.xml` (parent = omniui-parent, artifactId = login-app)
- [x] 1.3 Create `demo/java/login-app/src/main/java/module-info.java` (module dev.omniui.demo.login)
- [x] 1.4 Create `demo/java/login-app/src/main/java/dev/omniui/demo/login/LoginApp.java` — extract login section from CoreDemoApp (username, password, loginButton, status)
- [x] 1.5 Add `demo/java/login-app` to root `pom.xml` modules list
- [x] 1.6 Build: `mvn package -f demo/java/login-app/pom.xml` — must succeed with no errors

## 2. Update CoreDemoApp

- [x] 2.1 Remove login section from `CoreDemoApp.java` (loginSectionTitle, username, password, loginButton, status label, ~30 lines)
- [x] 2.2 Rebuild core-app: `mvn package -f demo/java/core-app/pom.xml` — must succeed
- [x] 2.3 Verify core-app still launches and non-login sections work

## 3. Python demo — login directory

- [x] 3.1 Create `demo/python/login/` with `__init__.py`, `_bootstrap.py`, `_runtime.py` (copy from another demo dir, update port to 48108)
- [x] 3.2 Move `demo/python/core/login_direct.py` → `demo/python/login/login_direct.py`
- [x] 3.3 Move `demo/python/core/login_with_fallback.py` → `demo/python/login/login_with_fallback.py`
- [x] 3.4 Create `demo/python/login/login_demo.py` — runs both direct and fallback scenarios in sequence
- [x] 3.5 Verify `python demo/python/login/login_demo.py` passes end-to-end

## 4. Integration smoke tests

- [x] 4.1 Add `login_client` session fixture to `tests/integration/test_smoke_apps.py` (port 49108)
- [x] 4.2 Add `test_login_success` and `test_login_failure` tests using `login_client`
- [x] 4.3 Replace `test_core_login_success` / `test_core_login_failure` with a non-login core smoke test (e.g. verify a ListView or ComboBox node exists)
- [x] 4.4 Run `pytest tests/integration/ -v` — all tests pass (fixed `test_core_server_list_exists`: `get_items` → `get_nodes`)

## 5. Cleanup and commit

- [x] 5.1 Update `README.md` and `README.zh-TW.md` — add login-app to demo apps table/list
- [x] 5.2 Update `docs/parallel-testing.md` / `docs/parallel-testing.zh-TW.md` if login-app port appears in examples (no changes needed)
- [x] 5.3 Update `ROADMAP.md` / `ROADMAP.zh-TW.md` — mark `split-login-app` as done (move to ✅ Done)
- [x] 5.4 Commit all changes with message `feat: extract login-app from core-app`
- [x] 5.5 Push to main
