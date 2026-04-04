## 1. Java Agent

- [x] 1.1 `handleClick()` 加入 `payload` 參數，更新 dispatch 呼叫（`case "click" -> handleClick(node, fxId, handle, payload)`）
- [x] 1.2 解析 payload 中的 `modifiers` 陣列，重用 `parseKeyString()` 將每個字串正規化為 `CONTROL/SHIFT/ALT/META`；未知字串回傳 `failure(reason="unknown_modifier")`
- [x] 1.3 有 modifiers 時：反射建構 `MouseEvent.MOUSE_CLICKED`（clickCount=1），帶入對應布林值，參考 `handleDoubleClick()` 結構
- [x] 1.4 無 modifiers 時：保持原有 `node.fire()` 路徑不變

## 2. Python Client

- [x] 2.1 `omniui/core/engine.py` — `click()` 加 `modifiers: list[str] | None = None` 參數，有值時寫入 payload `{"modifiers": modifiers}`
- [x] 2.2 `omniui/client.py` — 公開 API `click()` 更新簽章，docstring 說明用法與 modifier 別名規則

## 3. Demo

- [x] 3.1 建立 `demo/python/core/modifier_click_demo.py`：示範 Ctrl+click 加選、Shift+click 範圍選取（使用 ListView serverList）
- [x] 3.2 `demo/python/run_all.py` — core 群組加入 modifier_click_demo

## 4. Tests

- [x] 4.1 `tests/test_client.py` — 新增 `click(modifiers=["Ctrl"])` 單元測試，驗證 payload 帶入 `modifiers` 欄位
- [x] 4.2 `tests/test_client.py` — 新增 `click()` 無 modifiers 時 payload 不含 `modifiers` 欄位
- [x] 4.3 執行 `python -m pytest tests/` 確認全部通過

## 5. 文件與收尾

- [x] 5.1 更新 `ROADMAP.md` 將 Modifier+click 標記為 `[x]`
- [x] 5.2 commit + push
