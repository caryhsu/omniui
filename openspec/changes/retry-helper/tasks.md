## 1. Python Client

- [ ] 1.1 `omniui/core/engine.py` — 新增模組級 `retry(fn, *, times=3, delay=0.5, exceptions=(AssertionError,))` 函式：循環呼叫 `fn()`，捕捉 `exceptions` 中的例外或 `ActionResult.ok=False`，每次失敗後 `sleep(delay)`，最後一次失敗時 re-raise 或 raise `AssertionError`
- [ ] 1.2 `omniui/core/engine.py` — 在 `OmniUIClient` 上新增 `retry` 實例方法，直接委派到模組級 `retry()`
- [ ] 1.3 `omniui/__init__.py` — re-export `retry` 讓 `from omniui import retry` 可用

## 2. Demo

- [ ] 2.1 建立 `demo/python/core/retry_demo.py`：模擬一個需要重試的情境（用 counter 讓前幾次故意失敗，第 N 次成功），示範 `client.retry()` 和 `omniui.retry()`
- [ ] 2.2 `demo/python/run_all.py` — core 群組加入 retry_demo

## 3. Tests

- [ ] 3.1 `tests/test_client.py` — 新增 `RetryHelperTests`：
  - 第一次成功不重試
  - 第 N 次成功後回傳結果
  - 全部失敗 re-raise 最後例外
  - `ActionResult(ok=False)` 觸發重試
  - 自訂 `exceptions` tuple
  - `delay` 有被呼叫（mock `time.sleep`）
- [ ] 3.2 執行 `python -m pytest tests/` 確認全部通過

## 4. 收尾

- [ ] 4.1 更新 `ROADMAP.md` 將 Retry helper 標記為 `[x]`
- [ ] 4.2 commit + push
