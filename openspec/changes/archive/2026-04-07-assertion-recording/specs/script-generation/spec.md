# script-generation（Delta Spec）

## 變更摘要

`generate_script` 對 `assertion` 類型的 `RecordedEvent` 產生對應的 `client.verify_*` 呼叫。

---

## 新增需求

### 需求：assertion 事件產生 verify_* 呼叫

`generate_script` **必須** 將 `event_type="assertion"` 的事件轉換為對應的 Python 行。

### 情境：verify_text 斷言

- **當** `RecordedEvent(event_type="assertion", fx_id="statusLabel", assertion_type="verify_text", expected="Success")`
- **則** 輸出包含：
  ```python
  client.verify_text(id="statusLabel", expected="Success")
  ```

### 情境：verify_visible 斷言

- **當** `RecordedEvent(event_type="assertion", fx_id="submitBtn", assertion_type="verify_visible")`
- **則** 輸出包含：
  ```python
  client.verify_visible(id="submitBtn")
  ```

### 情境：verify_enabled 斷言

- **當** `RecordedEvent(event_type="assertion", fx_id="submitBtn", assertion_type="verify_enabled")`
- **則** 輸出包含：
  ```python
  client.verify_enabled(id="submitBtn")
  ```

### 情境：斷言行不帶 WARN 注解

- **當** assertion 事件有穩定的 `fx_id`
- **則** 產生的行 **不含** `# WARN: fragile selector`

### 情境：assertion 步驟與 click 步驟混排順序正確

- **當** 錄製事件序列為 `[click("loginBtn"), assertion("statusLabel", "verify_text", "Success")]`
- **則** 腳本輸出順序相同：
  ```python
  client.click(id="loginBtn")
  client.verify_text(id="statusLabel", expected="Success")
  ```
