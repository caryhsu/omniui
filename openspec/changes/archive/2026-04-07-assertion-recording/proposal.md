## 為何需要此變更

OmniUI Recorder 目前只能錄製「操作步驟」（click、type、set_date 等），無法在錄製過程中插入斷言（assertion）。使用者必須在錄製完畢後手動編輯腳本才能加入 `verify_text` / `verify_visible` / `verify_enabled` 等驗證行，造成額外工作且容易遺漏。

讓使用者在錄製時可以對任意元素按右鍵，從選單選擇要插入的斷言類型，斷言會立即出現在腳本預覽區，產出更完整、免後製的測試腳本。

## 什麼會改變

- **Java agent**：在錄製模式下啟用滑鼠右鍵事件攔截（`MOUSE_PRESSED` + button=secondary）；新增 `/events/assert-context` 端點，回傳被點擊節點的可用斷言清單與當前值。
- **Python client**（`record-session`）：`RecordedEvent` 新增 `assertion` 類型；`generate_script` 對 assertion 事件產生對應的 `client.verify_*` 呼叫。
- **Recorder GUI**（`omniui/recorder/gui.py`）：右鍵事件觸發彈出斷言選單（`AssertionMenu`），使用者選擇後插入 assertion event 到腳本預覽。
- **腳本輸出**：assertion 步驟與 click/type 步驟混排，順序與錄製時相同。

## 能力清單

### 新能力

- `assertion-recording`：錄製模式下插入斷言步驟的完整流程，涵蓋 Java 端事件攔截、Python 端 codegen、Recorder GUI 右鍵選單。

### 修改既有能力

- `record-session`：`RecordedEvent` 新增 `assertion` event type 及對應欄位（`assertion_type`、`expected`）。
- `script-generation`：`generate_script` 對 `assertion` 類型事件產生 `client.verify_text` / `client.verify_visible` / `client.verify_enabled` 呼叫。
- `recorder-actionable-filter`：右鍵（secondary click）排除於一般 click 錄製之外，改由斷言流程處理。

## 影響範圍

- `java-agent/src/main/java/dev/omniui/agent/runtime/ReflectiveJavaFxTarget.java`：右鍵事件攔截、節點當前值取得
- `java-agent/src/main/java/dev/omniui/agent/OmniUiAgentServer.java`：`/events/assert-context` 端點
- `omniui/recorder/script_gen.py`：assertion event codegen
- `omniui/recorder/events.py`：`RecordedEvent` 新增欄位
- `omniui/recorder/gui.py`：`AssertionMenu` 彈出選單、右鍵 polling/事件處理
- `tests/test_recorder.py`：新增斷言 codegen 單元測試
