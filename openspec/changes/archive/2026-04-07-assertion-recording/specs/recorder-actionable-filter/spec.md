# recorder-actionable-filter（Delta Spec）

## 變更摘要

右鍵（secondary button）點擊事件從一般 click 錄製流程中排除，改由斷言錄製流程處理。

---

## 新增需求

### 需求：右鍵事件不產生 click 錄製步驟

`EventFilter` **必須** 在 `MOUSE_PRESSED` 事件中區分左鍵與右鍵，右鍵事件 **不得** 進入一般 click 錄製路徑。

### 情境：右鍵不產生 click 事件

- **當** 使用者在錄製中對節點按右鍵（`MouseButton.SECONDARY`）
- **則** 事件緩衝區中 **不** 增加 `event_type="click"` 的事件
- **且** `/events/pending` 回傳的事件清單不包含此右鍵操作

### 情境：左鍵仍正常錄製

- **當** 使用者在錄製中對節點按左鍵（`MouseButton.PRIMARY`）
- **則** 事件緩衝區正常增加 `event_type="click"` 的事件（行為不變）
