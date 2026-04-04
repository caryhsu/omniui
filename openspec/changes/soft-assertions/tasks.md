## 1. Python Client

- [ ] 1.1 `omniui/core/engine.py` — 新增 `SoftAssertContext` class：`__enter__` 回傳 self，`__exit__` 若有累積的 `AssertionError` 則合併為一個並 raise；新增 `_assert(fn)` 方法供使用者手動包裹呼叫（optional helper）
- [ ] 1.2 `omniui/core/engine.py` — 新增模組級 `soft_assert()` 函式，回傳 `SoftAssertContext` 實例
- [ ] 1.3 `omniui/core/engine.py` — 在 `OmniUIClient` 上新增 `soft_assert()` 實例方法，委派到模組級 `soft_assert()`
- [ ] 1.4 `omniui/__init__.py` — re-export `soft_assert` 和 `SoftAssertContext`

## 2. Demo

- [ ] 2.1 建立 `demo/python/core/soft_assert_demo.py`：示範 `client.soft_assert()` 收集多個 verify_text 失敗、一次性回報；同時示範無失敗時正常通過
- [ ] 2.2 `demo/python/run_all.py` — core 群組加入 soft_assert_demo

## 3. Tests

- [ ] 3.1 `tests/test_client.py` — 新增 `SoftAssertTests`：
  - 無失敗不 raise
  - 單一失敗在 exit 時 raise
  - 多個失敗全部收集並合併為一個 AssertionError
  - 非 AssertionError 立即 propagate
  - 合併訊息格式符合 `"N assertion(s) failed:"` 規格
  - `client.soft_assert()` 行為與 module-level 相同
- [ ] 3.2 執行 `python -m pytest tests/` 確認全部通過

## 4. 收尾

- [ ] 4.1 更新 `ROADMAP.md` 將 Soft assertions 標記為 `[x]`
- [ ] 4.2 commit + push
