# record-session（Delta Spec）

## 變更摘要

`RecordedEvent` 新增 `assertion` 事件類型，支援在錄製腳本中混入斷言步驟。

---

## 新增需求

### 需求：RecordedEvent 支援 assertion 事件類型

`RecordedEvent` **必須** 支援 `event_type="assertion"`，並包含下列額外欄位：

| 欄位 | 類型 | 說明 |
|------|------|------|
| `assertion_type` | `str` | `"verify_text"` / `"verify_visible"` / `"verify_enabled"` |
| `expected` | `str \| None` | verify_text 用；其他類型為 `None` |

### 情境：建立 assertion 類型的 RecordedEvent

- **當** 建立 `RecordedEvent(event_type="assertion", fx_id="statusLabel", assertion_type="verify_text", expected="Success")`
- **則** 物件合法，`event.assertion_type == "verify_text"`，`event.expected == "Success"`

### 情境：非 assertion 事件不需要 assertion_type

- **當** 建立 `RecordedEvent(event_type="click", fx_id="loginBtn")`
- **則** `assertion_type` 預設為 `None`，不影響現有行為
