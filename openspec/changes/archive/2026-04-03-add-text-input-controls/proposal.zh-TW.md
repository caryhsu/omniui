## 原因

OmniUI 目前透過 `TextField` 支援單行文字輸入的自動化，但尚未支援
`TextArea`（多行文字）、`PasswordField`（遮罩輸入）和 `Hyperlink`（可點擊連結）。
這些控件在實際 JavaFX 應用中非常常見，是達到有意義端對端自動化覆蓋所必需的。

## 變更內容

- 新增 `TextArea` 的 `get_text` / `set_text`（多行文字節點）
- 新增 `PasswordField` 的 `get_text` / `set_text`（遮罩；透過屬性讀取明文）
- 新增 `Hyperlink` 的 `click`（觸發連結動作）
- 擴充 `LoginDemoApp`，加入 TextArea、PasswordField、Hyperlink 示範區塊
- 新增 Python 示範腳本：`text_area_demo.py`、`password_field_demo.py`、`hyperlink_demo.py`

## 能力

### 新增能力

- `text-area-automation`：讀取與寫入 `TextArea` 節點的多行文字
- `password-field-automation`：讀取與寫入 `PasswordField` 節點的文字
- `hyperlink-automation`：點擊 `Hyperlink` 節點並驗證已訪問狀態

### 修改能力

- `javafx-automation-core`：擴充 `get_text` / `set_text` 動作支援的節點類型，涵蓋 `TextArea` 和 `PasswordField`；新增 `Hyperlink` 點擊支援
- `advanced-javafx-demo-scenarios`：新增 TextArea、PasswordField、Hyperlink 示範區塊與腳本

## 影響範圍

- `ReflectiveJavaFxTarget.java` — 現有 `handleType` 已使用 `setText`；`TextArea` 與 `PasswordField` 均繼承自 `TextInputControl`，無需新增 handler；確認 `getText` 可透過現有 `get_text` 路徑正常運作
- `LoginDemoApp.java` — 三個控件的新 UI 區塊
- `omniui/core/engine.py` — 無需新增方法；`type()`、`get_text()` 和 `click()` 已存在
- `demo/python/` — 三個新示範腳本
