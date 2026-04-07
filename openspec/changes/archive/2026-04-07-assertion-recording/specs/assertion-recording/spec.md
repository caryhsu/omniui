# assertion-recording（斷言錄製）

## 目的

讓使用者在 OmniUI Recorder 錄製過程中，對任意元素按右鍵插入斷言步驟（`verify_text`、`verify_visible`、`verify_enabled`），斷言立即出現在腳本預覽區，產出含驗證邏輯的完整測試腳本，無需錄製後手動補寫。

---

## 需求一：Java agent 攔截右鍵點擊並回傳節點斷言上下文

系統 **必須** 在錄製模式下攔截 `MOUSE_PRESSED`（button=secondary）事件，並提供 `GET /events/assert-context?x={x}&y={y}` 端點，回傳被點擊節點的識別資訊與可用斷言清單。

### 情境：右鍵點擊有 fx:id 的節點

- **當** 錄製中使用者對一個 `fx:id="statusLabel"` 的節點按右鍵
- **則** `GET /events/assert-context?x=...&y=...` 回傳：
  ```json
  {
    "fxId": "statusLabel",
    "nodeType": "Label",
    "currentText": "Success",
    "visible": true,
    "enabled": true,
    "availableAssertions": ["verify_text", "verify_visible", "verify_enabled"]
  }
  ```

### 情境：右鍵點擊無 fx:id 的節點

- **當** 節點沒有 `fx:id` 但有可見文字
- **則** 回傳 `fxId: null`，`availableAssertions` 中不含 `verify_text`（因無穩定選擇器）

### 情境：右鍵點擊空白區域（無節點）

- **當** `x,y` 座標未命中任何 scene graph 節點
- **則** 回傳 HTTP 404

---

## 需求二：Recorder GUI 彈出斷言選單

系統 **必須** 在收到右鍵的 assert-context 後，在 Recorder GUI 彈出 `AssertionMenu`，列出可插入的斷言選項，使用者點選後插入對應的斷言步驟。

### 情境：選擇 verify_text

- **當** 使用者對 `statusLabel`（currentText="Success"）按右鍵，並在選單選擇「驗證文字」
- **則** 腳本預覽區插入：
  ```python
  client.verify_text(id="statusLabel", expected="Success")
  ```

### 情境：選擇 verify_visible

- **當** 使用者選擇「驗證可見」
- **則** 插入：
  ```python
  client.verify_visible(id="statusLabel")
  ```

### 情境：選擇 verify_enabled

- **當** 使用者選擇「驗證啟用」
- **則** 插入：
  ```python
  client.verify_enabled(id="statusLabel")
  ```

### 情境：使用者取消選單

- **當** 使用者按 Escape 或點選選單外部
- **則** 不插入任何步驟，腳本不變

### 情境：右鍵事件不被一般錄製機制記錄

- **當** 使用者在錄製中按右鍵
- **則** 此事件 **不** 產生 `client.click(...)` 行，僅觸發斷言選單流程

---

## 需求三：斷言步驟插入腳本預覽的位置正確

斷言步驟 **必須** 插入在腳本預覽區當前游標位置（若游標在某行），或追加到腳本末尾（若無游標焦點）。

### 情境：在腳本中間插入

- **當** 腳本已有 3 行，游標在第 2 行，使用者插入斷言
- **則** 斷言行出現在第 2 行之後，第 3 行之前

### 情境：腳本末尾插入

- **當** 游標在最後一行或腳本預覽無焦點
- **則** 斷言行追加到腳本末尾

---

## 需求四：斷言選單在非錄製狀態下不作用

- **當** Recorder 未處於 Recording 狀態時，使用者對應用程式按右鍵
- **則** 不彈出斷言選單，不呼叫 `/events/assert-context`

---

## 非功能需求

- 右鍵到選單出現的延遲 **應** 在 300ms 以內（本地 HTTP 呼叫）
- 若 `/events/assert-context` 呼叫失敗（逾時或連線中斷），**應** 靜默略過，不顯示錯誤彈窗
- 斷言步驟在腳本輸出中 **必須** 與 click/type 步驟視覺上一致（無特殊標記，直接是可執行的 Python 行）
