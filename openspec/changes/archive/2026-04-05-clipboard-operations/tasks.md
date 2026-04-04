## 1. Java Agent

- [ ] 1.1 `ReflectiveJavaFxTarget.java` 第一層 switch — 加入 `get_clipboard` case：在 FX thread 呼叫 `Clipboard.getSystemClipboard().getString()`，回傳 `{"ok": true, "value": "<text>"}` 或空字串
- [ ] 1.2 `ReflectiveJavaFxTarget.java` 第一層 switch — 加入 `set_clipboard` case：從 payload 取 `text`，在 FX thread 呼叫 `ClipboardContent` + `Clipboard.getSystemClipboard().setContent(...)`，回傳 `{"ok": true}`
- [ ] 1.3 `mvn clean install -pl java-agent -am -q` 重新編譯確認無錯誤

## 2. Python Client

- [ ] 2.1 `omniui/core/engine.py` — 加入 `get_clipboard()` 方法：呼叫 `self._perform("get_clipboard", {}, None)`，回傳 `ActionResult`
- [ ] 2.2 `omniui/core/engine.py` — 加入 `set_clipboard(text: str)` 方法：呼叫 `self._perform("set_clipboard", {}, {"text": text})`
- [ ] 2.3 `omniui/core/engine.py` — 加入 `copy(**selector)` 方法：依序呼叫 `self.press_key("Control+A", **selector)` 和 `self.press_key("Control+C", **selector)`
- [ ] 2.4 `omniui/core/engine.py` — 加入 `paste(**selector)` 方法：呼叫 `self.press_key("Control+V", **selector)`

## 3. Demo

- [ ] 3.1 建立 `demo/python/core/clipboard_demo.py`：示範 `set_clipboard` → `paste` → `get_text` 驗證；以及 `copy` → `get_clipboard` 驗證
- [ ] 3.2 `demo/python/run_all.py` — core 群組加入 clipboard_demo

## 4. Tests

- [ ] 4.1 `tests/test_client.py` — 新增 `ClipboardTests`：mock HTTP，驗證 get_clipboard / set_clipboard / copy / paste 發出正確 action；驗證 copy 發出兩次 press_key（Control+A + Control+C）
- [ ] 4.2 執行 `python -m pytest tests/` 確認全部通過

## 5. 收尾

- [ ] 5.1 更新 `ROADMAP.md` 將 Clipboard operations 標記為 `[x]`
- [ ] 5.2 commit + push
