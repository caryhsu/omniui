## Context

`CoreDemoApp.java` is a single 200-line class that combines a login form with four unrelated control sections (selection/ListView, hierarchy/TreeView, table/TableView, grid/GridPane). The login Python demos (`login_direct.py`, `login_with_fallback.py`) live in `demo/python/core/` despite testing a separate concern. All other demo apps are self-contained modules with their own Maven module, Python demo dir, port, and smoke test fixture.

## Goals / Non-Goals

**Goals:**
- New standalone `login-app` Maven module matching the structure of every other demo app
- `CoreDemoApp.java` retains all non-login sections with zero behavior change
- Existing core smoke tests updated (remove login tests, keep non-login ones)
- New login smoke tests cover the same scenarios previously in `test_core_login_*`
- Python demo files follow the same `_bootstrap.py` / `_runtime.py` / `*_demo.py` pattern

**Non-Goals:**
- No changes to the OmniUI Python client or Java agent
- No changes to the login logic itself (admin/1234 credentials stay the same)
- No changes to other demo apps

## Decisions

### 1. Java app structure — copy drag-app skeleton

Each demo app is a Java module with:
```
demo/java/login-app/
  pom.xml                        (parent = omniui-parent, artifactId = login-app)
  src/main/java/
    module-info.java             (module dev.omniui.demo.login)
    dev/omniui/demo/login/
      LoginApp.java
```
`LoginApp.java` contains exactly the login UI currently inside `CoreDemoApp.java` — no new logic, just extraction.

Node IDs preserved: `username`, `password`, `loginButton`, `status` (same as today so existing Python demos need no edits).

### 2. Port assignment

| App | Demo port | Smoke test port |
|-----|-----------|-----------------|
| login-app | 48108 | 49108 |

Port 48108 is the next free slot after todo-app (48107).

### 3. Python demo consolidation

`login_direct.py` and `login_with_fallback.py` move from `demo/python/core/` to `demo/python/login/`. A combined `login_demo.py` is added that runs both scenarios in sequence (direct login + fallback). The original files are kept at the new path unchanged (they use `connect_or_exit()` which reads the port from env, so no code edits needed).

### 4. root pom.xml registration

`demo/java/login-app` added to `<modules>` so `mvn install` builds it. Only core-app, input-app, advanced-app are currently listed — the other apps (drag, progress, image, color, todo) are built separately. We add login-app alongside the others.

Wait — actually checking: drag-app, progress-app etc. are NOT in root pom.xml. They have standalone pom.xml but build independently. Decision: **do NOT add to root pom.xml** to stay consistent. The CI already runs `mvn -q install` which only builds the 3 listed modules + java-agent. Integration tests for the new apps rely on pre-built `target/classes`. We add login-app to root pom.xml to keep it consistent with core/input/advanced.

Actually, re-evaluating: the smoke tests use `target/classes` which must exist. Since the CI runs `mvn -q install` from root and only core/input/advanced are modules, drag/progress/etc. tests would fail in CI unless pre-built. For now, mirror the existing pattern: **add login-app to root pom.xml** alongside core/input/advanced (these are the "tier-1" apps that CI fully builds and tests).

### 5. Smoke test update

`test_core_login_success` and `test_core_login_failure` currently test the login section of core-app. After the split:
- Remove those two tests from `core_client`
- Add `login_client` fixture (port 49108)
- Add `test_login_success` and `test_login_failure` using `login_client`
- Add a replacement core smoke test that exercises a non-login node (e.g. verify a ListView item exists)

## Risks / Trade-offs

- **[Risk] CoreDemoApp.java regression** — removing the login section could accidentally break other sections if they share a variable. Mitigation: run `pytest tests/integration/ -k core` before and after.
- **[Risk] Python demo path change** — any script that imports `from core.login_direct import ...` would break. Mitigation: grep confirms no cross-imports; demos are standalone scripts.

## Migration Plan

1. Create `demo/java/login-app/` module
2. Edit `CoreDemoApp.java` — remove login section
3. Add `login-app` to root `pom.xml`
4. Build: `mvn package -f demo/java/login-app/pom.xml`
5. Move Python demos to `demo/python/login/`
6. Update `tests/integration/test_smoke_apps.py`
7. Run `pytest tests/integration/` — all 17+ tests must pass
8. Commit + PR
