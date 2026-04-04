# Manual Smoke Checklist

英文版檔名：`manual-smoke.md`

這份 checklist 用來在本機人工驗證目前 OmniUI Java agent workflow。

## 範圍

這份 checklist 主要驗證：
- 三個 JavaFX demo app（`core-app`、`input-app`、`advanced-app`）
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

### 1. 以 with-agent mode 啟動 core-app

執行其中一種：

```bat
demo\java\core-app\run-dev-with-agent.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\java\core-app\run-dev-with-agent.ps1
```

```bash
./demo/java/core-app/run-dev-with-agent.sh
```

預期：
- JavaFX core demo 視窗開啟
- console 顯示 `OmniUI agent listening on http://127.0.0.1:48100`

### 2. 執行 Python demo suite

在另一個 terminal：

```bash
python demo/python/run_all.py
```

預期：
- 所有 20+ 個元件 demo 印出 `succeeded` 或 `succeeded ✓`
- exit code 0

涵蓋的 demo：
- Login（direct + fallback）、keyboard shortcuts、double-click、flexible verify
- ComboBox、ListView、TreeView、TableView selection、multi-select
- CSS style 檢查、recorder preview
- （Input）TextArea、PasswordField、Hyperlink、CheckBox、ChoiceBox
- （Input）RadioButton、Slider+Spinner、ColorPicker、DatePicker
- （Advanced）ContextMenu、MenuBar、Dialog、Alert、TabPane
- （Advanced）Accordion、TreeTableView、SplitPane、ProgressBar、NodeState、ScrollPane、Tooltip

### 3. 關閉 app

預期：
- JavaFX 視窗關閉
- process 正常退出

## Plain Mode Isolation 驗證

### 4. 以 plain mode 啟動 core-app

執行其中一種：

```bat
demo\java\core-app\run-dev-plain.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\java\core-app\run-dev-plain.ps1
```

```bash
./demo/java/core-app/run-dev-plain.sh
```

預期：
- JavaFX core demo 視窗開啟
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

### 6. 建置 packaged runtimes

> **重要：** jlink image 將 `dev.omniui.agent` 內嵌為 module。
> 建置 jlink image 前務必先安裝 agent 到本機 Maven repo，
> 否則 agent 的修改不會生效。

```bash
mvn install -f java-agent/pom.xml
mvn package javafx:jlink -f demo/java/core-app/pom.xml
mvn package javafx:jlink -f demo/java/input-app/pom.xml
mvn package javafx:jlink -f demo/java/advanced-app/pom.xml
```

或執行 helper script（會呼叫相同的 Maven goals）：

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

### 7. 以 with-agent mode 啟動 packaged core-app

執行其中一種：

```bat
demo\java\core-app\run-with-agent.bat
```

```powershell
powershell -ExecutionPolicy Bypass -File .\demo\java\core-app\run-with-agent.ps1
```

```bash
./demo/java/core-app/run-with-agent.sh
```

預期：
- JavaFX core demo 視窗開啟
- agent 在 `http://127.0.0.1:48100` 上監聽

### 8. 以 packaged runtime 執行完整 demo suite

```bash
python demo/python/run_all.py
```

預期：與步驟 2 相同，所有 demo 成功，exit code 0。

## 進階控制項驗證

### 9. 執行進階元件 discovery script

先啟動 advanced-app（`demo\java\advanced-app\run-dev-with-agent.bat`，port 48102），再執行：

```bash
python demo/python/advanced/discover_advanced_controls.py
```

預期：
- 輸出包含進階控制項的 node ID

### 10. 執行個別元件 demo（選用 — run_all.py 已涵蓋所有項目）

```bash
python demo/python/core/select_combo_role.py
python demo/python/core/select_list_item.py
python demo/python/core/select_tree_item.py
python demo/python/core/select_table_row.py
python demo/python/advanced/context_menu_demo.py
python demo/python/advanced/menu_bar_demo.py
python demo/python/input/date_picker_demo.py
python demo/python/advanced/alert_demo.py
python demo/python/advanced/treetableview_demo.py
```

預期：
- 每個 demo 印出 `succeeded` 或 `succeeded ✓`
- JavaFX 視窗上對應的 UI 會跟著更新

## 記錄結果

至少記錄：
- 使用的 launcher
- 是否出現 agent startup message
- `run_all.py` 在 with-agent mode 是否全部成功（exit code 0）
- plain mode 是否清楚失敗
- 任何 console error 或 stack trace
