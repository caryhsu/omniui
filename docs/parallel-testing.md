# Parallel Testing with OmniUI + pytest-xdist

> 中文版：[parallel-testing.zh-TW.md](parallel-testing.zh-TW.md)

OmniUI tests can run in parallel using [pytest-xdist](https://pytest-xdist.readthedocs.io/).
Each worker process gets its own isolated JavaFX app instance on a unique port — no shared state, no port collisions.

---

## Prerequisites

```bash
pip install pytest-xdist
```

Or add to your project's dev dependencies:

```toml
# pyproject.toml
[project.optional-dependencies]
dev = ["pytest>=9", "pytest-html>=4", "pytest-xdist>=3"]
```

---

## How It Works

```
pytest --numprocesses 4
        │
        ├── worker gw0 → port 49000 → JavaFX app A
        ├── worker gw1 → port 49001 → JavaFX app B
        ├── worker gw2 → port 49002 → JavaFX app C
        └── worker gw3 → port 49003 → JavaFX app D
```

Each worker is a separate Python process.  A **session-scoped fixture** launches one app per worker and tears it down when the worker finishes.

xdist sets the `PYTEST_XDIST_WORKER` environment variable (`gw0`, `gw1`, …).  Non-parallel runs set it to `master`.

---

## Port Assignment Pattern

```python
def _worker_port(base: int) -> int:
    """Return base + worker index.  Falls back to base for non-parallel runs."""
    worker_id = os.environ.get("PYTEST_XDIST_WORKER", "master")
    if worker_id == "master":
        return base
    try:
        return base + int(worker_id[2:])   # "gw3" → 3
    except (ValueError, IndexError):
        return base
```

**Recommended port range:** `49000–49099` — well above the standard demo app ports (48100–48103) and unlikely to conflict with other services.

---

## Fixture Setup

Add to your `conftest.py` (see `tests/conftest_parallel_example.py` for a fully annotated version):

```python
import os, pytest
from omniui import OmniUI

_BASE_PORT = 49000

def _worker_port(base):
    wid = os.environ.get("PYTEST_XDIST_WORKER", "master")
    return base if wid == "master" else base + int(wid[2:])

@pytest.fixture(scope="session")
def client():
    port = _worker_port(_BASE_PORT)
    cmd = _build_launch_cmd(port)   # your existing launch helper
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as c:
        yield c
```

---

## Writing Parallel-Safe Tests

Each test **must be stateless** — it should not depend on the execution order or the state left by a previous test.

### ✅ Good pattern — reset at the start

```python
def test_drag_apple(client):
    client.click(id="resetBtn")          # always start from known state
    client.drag(id="item_apple").to(id="rightPanel")
    assert "dropped Apple" in client.get_text(id="dragStatus").value
```

### ❌ Bad pattern — relies on previous test's state

```python
def test_verify_apple_dropped(client):
    # Assumes test_drag_apple ran first — BREAKS in parallel!
    assert client.get_text(id="item_apple_dropped").ok
```

---

## Running Tests

```bash
# Auto-detect CPU count
pytest tests/ --numprocesses auto

# Explicit worker count
pytest tests/ --numprocesses 4

# Combine with HTML report
pytest tests/ --numprocesses auto --html=report.html --self-contained-html

# Sequential (no xdist)
pytest tests/
```

---

## CI / GitHub Actions

Add a parallel job alongside your existing CI:

```yaml
jobs:
  parallel-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"

      - name: Build agent + drag-app
        run: |
          mvn -q package -pl java-agent -am
          mvn -q package -f demo/java/drag-app/pom.xml

      - name: Set up Python
        uses: actions/setup-python@v5
        with:
          python-version: "3.12"

      - name: Install dependencies
        run: pip install -e ".[dev]"

      - name: Install Xvfb
        run: sudo apt-get install -y xvfb

      - name: Run parallel tests
        run: |
          Xvfb :99 -screen 0 1280x1024x24 &
          DISPLAY=:99 pytest tests/test_parallel_example.py \
            --numprocesses auto \
            --html=report.html --self-contained-html
        env:
          DISPLAY: ":99"

      - name: Upload report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: parallel-test-report
          path: report.html
```

---

## Tips

| Tip | Details |
|-----|---------|
| **Port range** | Use 49000–49099 to avoid collisions with demo apps (48100–48103) and common services |
| **JVM startup time** | Workers stagger naturally; `timeout=30.0` in `OmniUI.launch()` is usually sufficient |
| **Shared state** | Tests that call `resetBtn` (or equivalent) at the start are safe to parallelise |
| **Debugging** | Run with `-v --numprocesses 1` to reproduce a failure sequentially |
| **Scope** | Use `scope="session"` for the fixture — spawning a JVM per test is too slow |

---

## See Also

- [`tests/conftest_parallel_example.py`](../tests/conftest_parallel_example.py) — fully annotated fixture
- [`tests/test_parallel_example.py`](../tests/test_parallel_example.py) — parallel-safe test examples
- [`docs/html-report.md`](html-report.md) — combining with HTML reports
- [`docs/headless.md`](headless.md) — running headless on Linux/CI
