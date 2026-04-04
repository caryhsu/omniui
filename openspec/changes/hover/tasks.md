## 1. Java Agent

- [ ] 1.1 新增 `"hover"` case 到 dispatch switch，呼叫 `doHover(node, fxId, handle)`
- [ ] 1.2 實作 `doHover()`：計算節點中心座標（同 `handleDoubleClick()`），反射建構並發送 `MOUSE_ENTERED`，再發送 `MOUSE_MOVED`；失敗回傳 `reason="hover_failed"`

## 2. Python Client

- [ ] 2.1 `omniui/core/engine.py` — 新增 `hover(**selector)` 方法，呼叫 `self._perform("hover", selector)`
- [ ] 2.2 `omniui/client.py` — 在 docstring 中標注 hover 可搭配 `wait_for_visible` 驗證 tooltip

## 3. Demo

- [ ] 3.1 建立 `demo/python/advanced/hover_demo.py`：對 advanced-app 的 tooltip 節點呼叫 `hover()`，再呼叫 `get_tooltip()` 驗證 tooltip 文字
- [ ] 3.2 `demo/python/run_all.py` — advanced 群組加入 hover_demo

## 4. Tests

- [ ] 4.1 `tests/test_client.py` — 新增 `hover(id=...)` 單元測試，驗證 action="hover" 與 selector 正確送出
- [ ] 4.2 執行 `python -m pytest tests/` 確認全部通過

## 5. 收尾

- [ ] 5.1 更新 `ROADMAP.md` 將 Hover 標記為 `[x]`
- [ ] 5.2 commit + push
