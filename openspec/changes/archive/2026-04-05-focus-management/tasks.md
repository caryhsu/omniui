## 1. Java Agent

- [x] 1.1 新增 `"focus"` case 到 dispatch switch：呼叫 `node.requestFocus()`，回傳 `{"action":"focus","status":"ok"}`
- [x] 1.2 新增 `"get_focused"` case 到 dispatch switch（不需要 selector）：取 `scene.getFocusOwner()`，提取 `fxId` 和 `nodeType`，回傳 `{"fxId":"...","nodeType":"..."}`；無 focus 時兩者為 `null`

## 2. Python Client

- [x] 2.1 `omniui/core/engine.py` — 新增 `focus(**selector)` 方法，呼叫 `self._perform("focus", selector)`
- [x] 2.2 `omniui/core/engine.py` — 新增 `tab_focus(times=1, reverse=False)` 方法，呼叫 `press_key("Shift+Tab" if reverse else "Tab")` 共 `times` 次
- [x] 2.3 `omniui/core/engine.py` — 新增 `get_focused()` 方法，呼叫 `self._perform("get_focused", {})` 回傳 dict
- [x] 2.4 `omniui/core/engine.py` — 新增 `verify_focused(id=...)` 方法，呼叫 `get_focused()` 並比對 `fxId`，不符合時 raise `AssertionError`

## 3. Demo

- [x] 3.1 建立 `demo/python/core/focus_demo.py`：使用 core-app login form（`username`、`password`）示範 `focus()`、`tab_focus()`、`get_focused()`、`verify_focused()`
- [x] 3.2 `demo/python/run_all.py` — core 群組加入 focus_demo

## 4. Tests

- [x] 4.1 `tests/test_client.py` — 新增 `FocusManagementTests`：測試 `focus(id=...)` 送出正確 action、`tab_focus()` 呼叫 `press_key("Tab")`、`get_focused()` 解析回傳、`verify_focused()` pass/fail 兩種情境
- [x] 4.2 執行 `python -m pytest tests/` 確認全部通過

## 5. 收尾

- [x] 5.1 更新 `ROADMAP.md` 將 Focus management 標記為 `[x]`
- [x] 5.2 `mvn clean install -pl java-agent -am` 重編 Java agent
- [x] 5.3 執行 `python run_all.py` 確認 focus_demo 通過
- [x] 5.4 commit + push
