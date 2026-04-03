## 主要決策

### 1. 不需要新增動作類型
`TextArea` 和 `PasswordField` 均繼承自 `TextInputControl`，而該類別宣告了
`getText()` 和 `setText(String)`。現有的 `get_text` 和 `type` 動作已透過反射呼叫這些方法。
一旦在示範應用程式中加入這兩種控件，無需在 `performOnFxThread` 新增任何 case。

### 2. PasswordField — 讀取明文
`PasswordField.getText()` 回傳未遮罩的字串（遮罩僅為顯示效果）。
自動化直接讀取原始值，無需特殊處理。

### 3. Hyperlink — 沿用現有的 `click` / `fire`
`Hyperlink` 繼承自 `ButtonBase`，呼叫 `fire()` 會觸發其 `onAction` 處理器。
現有的 `handleClick` 路徑可直接使用。若要驗證已訪問狀態，可透過現有的
`get_value` → `safeInvoke` 機制呼叫 `isVisited()`。

### 4. LoginDemoApp 示範區塊
在 TabPane 區塊下方新增三個 VBox 區塊：
- **TextArea 區塊**：id `demoTextArea`，預填文字，讀回驗證
- **PasswordField 區塊**：id `demoPasswordField`，寫入 → 讀取明文
- **Hyperlink 區塊**：id `demoHyperlink`，點擊 → 確認 `isVisited()`

### 5. Python 客戶端 — 不需要新公開方法
三種控件均使用現有 API：
- `client.type(selector, text)` — 設定文字
- `client.get_text(selector)` — 讀取文字
- `client.click(selector)` — 觸發 Hyperlink
- `client.get_value(selector)` — 透過 `get_value` 讀取 `isVisited()` 布林值

### 6. 示範腳本（每個控件一個）
- `text_area_demo.py` — 寫入多行文字、讀回、驗證
- `password_field_demo.py` — 寫入密碼、讀回明文、驗證
- `hyperlink_demo.py` — 確認初始 visited=False，點擊，確認 visited=True
