# Manual Smoke Checklist

英文版檔名：`manual-smoke.md`

這份 checklist 用來在本機人工驗證目前 OmniUI Java agent workflow。

## 範圍

這份 checklist 主要驗證：
- JavaFX login demo app
- 標準 Java agent 啟動
- Python demo flows
- plain-mode isolation

目前主要針對 Windows 驗證環境使用。

## 前置條件

- Java 21+
- Maven 3.9+
- Python 3.11+
- 若最近有改 code，請先確保 Java agent 與 demo app 已重新建置

## 開發期模式驗證

### 1. 以 with-agent mode 啟動 app

執行其中一種：

```bat
demo\javafx-login-app\run-dev-with-agent.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-dev-with-agent.ps1
```

```bash
./demo/javafx-login-app/run-dev-with-agent.sh
```

預期：
- JavaFX login 視窗開啟
- console 顯示 `OmniUI agent listening on http://127.0.0.1:48100`

### 2. 執行 Python demo suite

在另一個 terminal：

```bash
python scripts/run_demo.py
```

預期：
- node discovery 會列出 JavaFX nodes
- advanced discovery 會列出 `ComboBox`、`ListView`、`TreeView`、`TableView` 與 grid-oriented demo nodes
- 進階互動 demo 會成功完成 combo-box、list、tree 與 table row selection
- direct login 成功
- fallback login 成功
- recorder preview 會印出 script lines
- benchmark 會印出 JSON output

### 3. 關閉 app

預期：
- JavaFX 視窗關閉
- process 正常退出

## Plain Mode Isolation 驗證

### 4. 以 plain mode 啟動 app

執行其中一種：

```bat
demo\javafx-login-app\run-dev-plain.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-dev-plain.ps1
```

```bash
./demo/javafx-login-app/run-dev-plain.sh
```

預期：
- JavaFX login 視窗開啟
- console 不應顯示 OmniUI agent startup message

### 5. 對 plain mode 嘗試執行 Python demo

在另一個 terminal：

```bash
python scripts/run_demo.py
```

預期：
- script 明確結束
- 訊息會提示你要先用 `with-agent` mode 啟動
- 不會默默連到 fallback target

## Packaged Runtime 驗證

### 6. 建置 packaged runtime

執行其中一種：

```bat
scripts\build_demo_runtime.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build_demo_runtime.ps1
```

```bash
./scripts/build_demo_runtime.sh
```

預期：
- build 成功
- script 會列出下一步可用的 `run-with-agent.*` 與 `run-plain.*` launcher

### 7. 以 with-agent mode 啟動 packaged app

執行其中一種：

```bat
demo\javafx-login-app\run-with-agent.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\javafx-login-app\run-with-agent.ps1
```

```bash
./demo/javafx-login-app/run-with-agent.sh
```

預期：
- JavaFX login 視窗開啟
- agent 在 `http://127.0.0.1:48100` 上監聽

### 8. 執行聚焦的 Python flow

```bash
python scripts/demo_login_flow.py
```

預期：
- login flow 成功
- 會印出 trace history
- 會印出 recorder output

## 進階控制項驗證

### 9. 執行進階元件 discovery script

```bash
python demo/python/discover_advanced_controls.py
```

預期：
- 輸出包含 `roleCombo`
- 輸出包含 `serverList`
- 輸出包含 `assetTree`
- 輸出包含 `userTable`
- 輸出包含 `settingsGrid`

### 10. 執行進階互動 demo

```bash
python demo/python/select_combo_role.py
python demo/python/select_list_item.py
python demo/python/select_tree_item.py
python demo/python/select_table_row.py
```

預期：
- combo-box demo 印出 `ComboBox selection succeeded`
- list-view demo 印出 `ListView selection succeeded`
- tree-view demo 印出 `TreeView selection succeeded`
- table-view demo 印出 `TableView selection succeeded`
- JavaFX 視窗上對應的 status label 會跟著更新

### 11. 記錄明確尚未支援的案例

請額外記錄以下情況是否仍需後續處理：
- list/tree/table 中 off-screen 或尚未 materialized 的 item
- 超過 direct selection-model control 範圍的 combo-box popup 互動
- 超過可見項目 selection 範圍的 tree expand/collapse 行為
- 當多列資料有相同值時，table row 的 disambiguation

## 記錄結果

至少記錄：
- 使用的 launcher
- 是否出現 agent startup message
- `run_demo.py` 在 with-agent mode 是否成功
- advanced discovery 是否回傳預期的 control ids
- 進階互動 demo 是否成功
- plain mode 是否清楚失敗
- 任何 console error 或 stack trace
