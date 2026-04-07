## Context

OmniUI Recorder 目前的錄製流程：Java agent 的 `EventFilter` 攔截 `MOUSE_PRESSED`（左鍵）和 `KEY_TYPED` 事件，透過 `/events/pending` polling 傳回 Python，Python 端 `script_gen.py` 將事件序列轉為 Python 腳本。

目前右鍵事件完全未被處理。Recorder GUI 只有一個腳本文字區，使用者必須手動輸入斷言行。

**主要涉及模組：**
- `ReflectiveJavaFxTarget.java`：EventFilter 邏輯，目前在 `startRecording()` 中加入 `MOUSE_PRESSED` filter
- `OmniUiAgentServer.java`：HTTP server，需新增 `/events/assert-context` 端點
- `omniui/recorder/events.py`：`RecordedEvent` dataclass
- `omniui/recorder/script_gen.py`：`generate_script` 函式
- `omniui/recorder/gui.py`：Recorder Tkinter GUI，管理 polling loop 和腳本文字區

## Goals / Non-Goals

**Goals：**
- 錄製中右鍵點擊任意節點，彈出斷言選單（verify_text / verify_visible / verify_enabled）
- 選擇後在腳本預覽對應位置插入可執行的 Python 斷言行
- 右鍵事件不產生 `client.click(...)` 錄製步驟
- 最小化 Java 端變更（不依賴新的 JavaFX API，只用已有的 PickResult）

**Non-Goals：**
- 斷言的執行驗證（不在 GUI 即時跑 verify_text）
- 複雜的斷言條件（只支援 equals，不支援 contains/regex）
- 非 JavaFX 節點的斷言（Canvas、WebView 等）

## Decisions

### 決策一：assert-context 用 HTTP polling 而非 side-channel event

**選擇**：右鍵發生時，Python polling loop 收到一個特殊的 `right_click` 事件（含 `x,y`），Python 端再呼叫 `GET /events/assert-context?x=...&y=...` 取得節點資訊。

**理由**：
- 不需要改變現有 polling 架構（`/events/pending` 已有穩定機制）
- 右鍵事件可與左鍵事件走同一個 pending 佇列，保持時序
- 替代方案：在右鍵時 Java 直接 push 到 pending 佇列並附帶節點資訊 → 但 pending 佇列的 event 結構要改，影響面較大

**結構**：Java 端在右鍵時把 `{event_type: "right_click", x, y}` 加入 pending 佇列；Python polling loop 收到後呼叫 assert-context 端點，彈出選單。

---

### 決策二：節點查找用 PickResult（Scene.pick）

**選擇**：`/events/assert-context?x=...&y=...` 在 Platform.runLater 中呼叫 `scene.pick(x, y)` 取得目標節點。

**理由**：
- JavaFX 內建 `PickResult`，不需要遍歷 scene graph
- 座標來自錄製事件的 screen X/Y，需轉換為 scene-local 座標（`node.sceneToLocal` 或直接用 scene 座標）
- 替代方案：用 event 的 source node reference → 但 pending 佇列已序列化，無法傳 Java 物件

---

### 決策三：AssertionMenu 用 Tkinter Menu（不用獨立視窗）

**選擇**：使用 `tk.Menu` 的 `post(x, y)` 在滑鼠位置彈出 context menu，選擇後消失。

**理由**：
- 比 `Toplevel` 視窗輕量，UX 更符合一般右鍵選單直覺
- 不阻擋主視窗操作
- Tkinter `Menu.post()` 可在任意螢幕座標彈出

---

### 決策四：斷言插入位置在游標所在行之後

**選擇**：取 `text_widget.index(tk.INSERT)` 確定行號，在當行末尾換行後插入。若游標不在文字區，追加到末尾。

**理由**：符合使用者直覺——剛做完某個操作後右鍵插入斷言，斷言應在該行之後。

## Risks / Trade-offs

- **[Risk] Scene.pick 座標轉換**：recording event 的 `x,y` 是 screen 座標，pick 需要 scene-local 座標。若視窗有縮放或多螢幕 DPI，可能有偏差。→ **Mitigation**：使用 `scene.getWindow().getX/Y()` 轉換，並在 assert-context 加容忍半徑（5px）
- **[Risk] 右鍵在 PasswordField 上**：`currentText` 不應回傳密碼。→ **Mitigation**：assert-context 端點對 PasswordField 節點不回傳 `currentText`，`verify_text` 不出現在 `availableAssertions`
- **[Risk] polling 延遲**：right_click 事件在下一個 polling 週期（500ms）才被處理，選單延遲約 0-500ms。→ **Mitigation**：可接受，不需改 polling 頻率

## Open Questions

- 無。所有設計決策已確定。
