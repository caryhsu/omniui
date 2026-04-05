# 平行測試：OmniUI + pytest-xdist

> English: [parallel-testing.md](parallel-testing.md)

OmniUI 測試可透過 [pytest-xdist](https://pytest-xdist.readthedocs.io/) 並行執行。
每個 worker 程序各自擁有獨立的 JavaFX app 實例與獨立 port，完全不共享狀態、不發生 port 衝突。

---

## 前置條件

```bash
pip install pytest-xdist
```

或加入 dev 依賴：

```toml
[project.optional-dependencies]
dev = ["pytest>=9", "pytest-html>=4", "pytest-xdist>=3"]
```

---

## 運作原理

```
pytest --numprocesses 4
        │
        ├── worker gw0 → port 49000 → JavaFX app A
        ├── worker gw1 → port 49001 → JavaFX app B
        ├── worker gw2 → port 49002 → JavaFX app C
        └── worker gw3 → port 49003 → JavaFX app D
```

xdist 會設定環境變數 `PYTEST_XDIST_WORKER`（`gw0`、`gw1` …）。非並行執行時設為 `master`。

---

## Port 分配方式

```python
def _worker_port(base: int) -> int:
    worker_id = os.environ.get("PYTEST_XDIST_WORKER", "master")
    if worker_id == "master":
        return base
    return base + int(worker_id[2:])   # "gw3" → base + 3
```

**建議 port 範圍：** `49000–49099`（高於 demo app 使用的 48100–48103）。

---

## Fixture 設定

```python
@pytest.fixture(scope="session")
def client():
    port = _worker_port(49000)
    cmd = _build_launch_cmd(port)
    with OmniUI.launch(cmd=cmd, port=port, timeout=30.0) as c:
        yield c
```

完整範例請參考 `tests/conftest_parallel_example.py`。

---

## 撰寫平行安全的測試

每個測試**必須無狀態**，不能依賴其他測試執行後的 UI 狀態。

### ✅ 正確做法 — 每次測試開始先 reset

```python
def test_drag_apple(client):
    client.click(id="resetBtn")   # 回到已知初始狀態
    client.drag(id="item_apple").to(id="rightPanel")
    assert "dropped Apple" in client.get_text(id="dragStatus").value
```

### ❌ 錯誤做法 — 依賴前一個測試的狀態

```python
def test_verify_dropped(client):
    # 假設 test_drag_apple 已經執行 — 並行時順序不保證！
    assert client.get_text(id="item_apple_dropped").ok
```

---

## 執行指令

```bash
# 自動偵測 CPU 核心數
pytest tests/ --numprocesses auto

# 指定 worker 數量
pytest tests/ --numprocesses 4

# 搭配 HTML 報告
pytest tests/ --numprocesses auto --html=report.html --self-contained-html
```

---

## CI / GitHub Actions 範例

```yaml
jobs:
  parallel-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: "21" }
      - run: |
          mvn -q package -pl java-agent -am
          mvn -q package -f demo/java/drag-app/pom.xml
      - uses: actions/setup-python@v5
        with: { python-version: "3.12" }
      - run: pip install -e ".[dev]"
      - run: sudo apt-get install -y xvfb
      - run: |
          Xvfb :99 -screen 0 1280x1024x24 &
          DISPLAY=:99 pytest tests/test_parallel_example.py \
            --numprocesses auto --html=report.html --self-contained-html
        env: { DISPLAY: ":99" }
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: parallel-test-report, path: report.html }
```

---

## 參考資料

- [`tests/conftest_parallel_example.py`](../tests/conftest_parallel_example.py) — 完整注解的 fixture 範例
- [`tests/test_parallel_example.py`](../tests/test_parallel_example.py) — 平行安全測試範例
- [`docs/html-report.md`](html-report.md) — 搭配 HTML 報告
- [`docs/headless.md`](headless.md) — Linux/CI 無頭模式
