# OmniUI Demo

本目錄收集 OmniUI Phase 1 的可執行 demo。

## 可用 Demo

### JavaFX target app

- [javafx-login-app](javafx-login-app/README.zh-TW.md)

這是下方 Python demo 所使用的參考 JavaFX 應用程式。

目前 demo app 內包含：
- 原本的 login flow
- 額外可見的進階元件，例如 `ComboBox`、`ListView`、`TreeView`、`TableView` 與 grid layout section

請先啟動它：

```bash
demo/javafx-login-app/run-dev-with-agent.bat
```

也可以先用 `jlink` 打包，再依 [javafx-login-app/README.zh-TW.md](javafx-login-app/README.zh-TW.md) 裡的 `run-with-agent.*` 啟動方式執行。

`scripts/` 目錄也提供輔助腳本：
- `build_demo_runtime.ps1`
- `build_demo_runtime.bat`
- `build_demo_runtime.sh`

這些 build helper 在完成後會直接列出接下來可用的 `run-with-agent.*` 與 `run-plain.*` launcher。

補充：
- `.sh` 輔助腳本目前主要是提供 Windows 上的 Git Bash 使用。demo app 與打包後 launcher 流程仍以 Windows 為主要文件與驗證環境。

## Python demo

所有 Python demo 都假設 JavaFX login app 已經用 `with-agent` mode 啟動，並在 `http://127.0.0.1:48100` 上提供控制端點。

### 一次執行全部 demo

```bash
python demo/python/run_all.py
```

會依序執行 discovery、direct login、fallback login、recorder preview 與 benchmark。
現在也會在互動 demo 開始前先輸出可見的進階控制項節點。

也可以使用：

```bash
python -m demo.python.run_all
python scripts/run_demo.py
```

### 列出節點

```bash
python demo/python/discover_nodes.py
```

列出 agent 回傳的目前 JavaFX node snapshot。

### 列出進階元件

```bash
python demo/python/discover_advanced_controls.py
```

輸出目前 JavaFX demo 畫面中可見的進階元件節點。

### 進階元件互動

```bash
python demo/python/select_combo_role.py
python demo/python/select_list_item.py
python demo/python/select_tree_item.py
python demo/python/select_table_row.py
```

直接操作 `ComboBox`、`ListView`、`TreeView` 與 `TableView` 的 selection，並驗證各自的 status label。

### Direct login flow

```bash
python demo/python/login_direct.py
```

以 JavaFX direct selector，例如 `id="loginButton"`，執行登入流程。

### 含 OCR fallback 的 login flow

```bash
python demo/python/login_with_fallback.py
```

將最後一次 click 改寫成 `text="Login"`，方便觀察 fallback trace。

### Recorder 預覽

```bash
python demo/python/recorder_preview.py
```

執行一段簡短流程，並輸出由 action history 產生的 recorder-lite script。

### Benchmark

```bash
python demo/python/run_benchmark.py
```

執行 Phase 1 benchmark，輸出 JavaFX node query 與 OCR parsing 的耗時結果。
