## 1. Java Agent

- [ ] 1.1 `ReflectiveJavaFxTarget.java` 第一層 switch — 加入 `click_at` case，呼叫 `doClickAt(payload)`
- [ ] 1.2 實作 `doClickAt(JsonObject payload)`：從 payload 取 x/y，在 FX thread 取 scene root，使用與 `handleDoubleClick` 相同的 MouseEvent 反射建構方式（clickCount=1），以 scene root 為 EventTarget 呼叫 `Event.fireEvent`
- [ ] 1.3 `mvn clean install -pl java-agent -am -q` 確認無錯誤

## 2. Python Client

- [ ] 2.1 `omniui/core/engine.py` — 加入 `click_at(*, x: float, y: float)` 方法：呼叫 `self._perform("click_at", {}, {"x": x, "y": y})`

## 3. Demo

- [ ] 3.1 建立 `demo/python/core/click_at_demo.py`：先用 `discover` 取得某個按鈕的座標，再用 `click_at` 點擊並驗證效果
- [ ] 3.2 `demo/python/run_all.py` — core 群組加入 click_at_demo

## 4. Tests

- [ ] 4.1 `tests/test_client.py` — 新增 `ClickAtTests`：mock HTTP，驗證 click_at 發出正確 action 與 payload (x, y)
- [ ] 4.2 執行 `python -m pytest tests/` 確認全部通過

## 5. 收尾

- [ ] 5.1 更新 `ROADMAP.md` 將 Absolute coordinate click 標記為 `[x]`
- [ ] 5.2 commit + push
