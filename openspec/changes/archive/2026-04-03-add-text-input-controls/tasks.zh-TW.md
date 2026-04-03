# 任務：add-text-input-controls

## 第 1 組 — 示範應用程式（LoginDemoApp.java）

- [x] 1.1 新增 `import javafx.scene.control.TextArea`
- [x] 1.2 新增 `import javafx.scene.control.PasswordField`
- [x] 1.3 新增 `import javafx.scene.control.Hyperlink`
- [x] 1.4 新增 TextArea 示範區塊（id `demoTextArea`，預填多行文字）
- [x] 1.5 新增 PasswordField 示範區塊（id `demoPasswordField`）
- [x] 1.6 新增 Hyperlink 示範區塊（id `demoHyperlink`，標籤「Click me」）

## 第 2 組 — Java Agent（ReflectiveJavaFxTarget.java）

- [x] 2.1 確認 `get_text` 路徑呼叫 `getText()` — 適用於 `TextInputControl` 子類別（含 `TextArea`、`PasswordField`，預期無需修改程式碼；以測試確認）
- [x] 2.2 確認 `type` 路徑呼叫 `setText()` — 同上（預期無需修改程式碼）
- [x] 2.3 確認 `click` 路徑呼叫 `fire()` — 適用於 `ButtonBase` 子類別（含 `Hyperlink`，預期無需修改程式碼）
- [x] 2.4 新增 `Hyperlink` 已訪問狀態的 `get_value` 支援：確認可透過 `get_value` 動作呼叫 `safeInvoke(node, "isVisited")`

## 第 3 組 — Python 客戶端（engine.py）

- [x] 3.1 確認 `type(selector, text)` 適用於 `TextArea` 和 `PasswordField`（無需新增方法）
- [x] 3.2 確認 `get_text(selector)` 適用於 `TextArea` 和 `PasswordField`
- [x] 3.3 確認 `click(selector)` 適用於 `Hyperlink`
- [x] 3.4 確認 `get_value(selector)` 對 `Hyperlink` 回傳 `isVisited()`

## 第 4 組 — 示範腳本

- [x] 4.1 建立 `demo/python/text_area_demo.py`
- [x] 4.2 建立 `demo/python/password_field_demo.py`
- [x] 4.3 建立 `demo/python/hyperlink_demo.py`
- [x] 4.4 在 `demo/python/run_all.py` 中註冊以上三個腳本

## 第 5 組 — 建置與測試

- [x] 5.1 `mvn compile` — 無錯誤
- [x] 5.2 執行 `text_area_demo.py` — 通過
- [x] 5.3 執行 `password_field_demo.py` — 通過
- [x] 5.4 執行 `hyperlink_demo.py` — 通過
