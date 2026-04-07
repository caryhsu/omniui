## 1. Java agent — 右鍵事件攔截

- [x] 1.1 在 `ReflectiveJavaFxTarget.startRecording()` 的 EventFilter 中，偵測 `MOUSE_PRESSED` 且 `button == MouseButton.SECONDARY`，加入 `{event_type: "right_click", x, y}` 到事件佇列（不產生 click 事件）
- [x] 1.2 左鍵 click 過濾條件加上 `button == MouseButton.PRIMARY` 判斷，確保右鍵不進入 click 錄製路徑

## 2. Java agent — assert-context 端點

- [x] 2.1 在 `OmniUiAgentServer.java` 新增 `GET /events/assert-context?x=...&y=...` 端點
- [x] 2.2 端點在 `Platform.runLater` 中呼叫 `scene.pick(x, y)` 取得目標節點（`CountDownLatch` 等待結果）
- [x] 2.3 回傳 JSON：`fxId`、`nodeType`、`currentText`（PasswordField 回傳 null）、`visible`、`enabled`、`availableAssertions`（陣列）
- [x] 2.4 命中空白區域（無節點）時回傳 HTTP 404

## 3. Python — RecordedEvent 新增欄位

- [x] 3.1 在 `omniui/core/models.py` 的 `RecordedEvent` 新增欄位：`assertion_type: str = ""`（`"verify_text"` / `"verify_visible"` / `"verify_enabled"`）、`expected: str = ""`

## 4. Python — script_gen.py 新增 assertion codegen

- [x] 4.1 在 `omniui/recorder/script_gen.py` 的 `generate_script` 加入 `event_type == "assertion"` 分支，依 `assertion_type` 產生對應的 `client.verify_text(...)` / `client.verify_visible(...)` / `client.verify_enabled(...)` 行
- [x] 4.2 新增單元測試：`tests/test_recorder.py` 加入 `test_generate_script_assertion_*` 三個測試（verify_text / verify_visible / verify_enabled）

## 5. Python — Recorder GUI 右鍵選單

- [x] 5.1 在 `omniui/recorder/gui.py` 的 polling loop 加入 `right_click` 事件處理：收到後呼叫 `GET /events/assert-context?x=...&y=...`，失敗時靜默略過
- [x] 5.2 實作 `_show_assertion_menu(context, screen_x, screen_y)`：用 `tk.Menu.post()` 在滑鼠位置彈出選單，顯示 `availableAssertions` 中的選項（中文標籤：「驗證文字」/ 「驗證可見」/ 「驗證啟用」）
- [x] 5.3 選單選擇後，建立 `RecordedEvent(event_type="assertion", ...)` 並呼叫 `_append_script_lines` 插入到腳本預覽游標位置（或末尾）

## 6. Build & 驗收

- [x] 6.1 重建 Java agent JAR：`mvn package -pl java-agent -am -q`
- [x] 6.2 執行 `python -m pytest tests/test_recorder.py -v` — 全部通過
- [x] 6.3 手動驗證：啟動 login-app + Recorder，錄製中對 username 右鍵，確認選單彈出、腳本插入正確
- [x] 6.4 Commit：`feat: assertion recording — right-click to insert verify_* in Recorder`
