# HTML 測試報告

OmniUI 整合 [pytest-html](https://pytest-html.readthedocs.io/)，可產生獨立的 HTML 測試報告。
測試失敗時，JavaFX 應用程式的**截圖**會自動嵌入報告，讓你直接看到失敗當下的 UI 狀態。

## 快速開始

```bash
pip install -e ".[dev]"          # pytest-html 已包含在 dev extras

pytest tests/ --html=report.html --self-contained-html
```

用瀏覽器開啟 `report.html`——不需要任何伺服器。

## 功能

| 功能 | 說明 |
|------|------|
| **獨立單檔** | 單一 `.html` 檔案，不依賴外部資源 |
| **失敗截圖** | 每個失敗測試都會內嵌 App 截圖 |
| **Trace 紀錄** | `format_trace()` 輸出會印在 log 中 |
| **截圖檔案** | PNG 同時儲存到 `screenshots/<test_name>.png` |
| **自訂標題** | 報告標題為「OmniUI Test Report」 |

## 設定

可在 `pyproject.toml` 設定預設輸出路徑，不需每次手動輸入：

```toml
[tool.pytest.ini_options]
addopts = "--html=report.html --self-contained-html"
```

## CI / GitHub Actions

CI 中 HTML 報告會自動產生並上傳為 workflow artifact：

- **Unit tests** → artifact `unit-test-report` → `report.html`
- **Integration tests** → artifact `integration-test-report` → `report.html`

執行完成後，在 GitHub 的 **Actions** 頁面下載 artifact 即可查看。

## 截圖運作方式

`omniui` pytest fixture 在測試失敗時自動截圖：

```
conftest.py
  └─ pytest_runtest_makereport hook
       └─ 失敗時 → client.screenshot()
            ├─ 儲存 screenshots/<test_name>.png
            └─ 透過 extras API 將 base64 PNG 嵌入 pytest-html 報告
```

截圖邏輯**只對使用 `omniui` fixture 的測試有效**，純 unit tests 不受影響。

## 撰寫測試

```python
def test_login_success(omniui):
    omniui.type(id="username", text="admin")
    omniui.type(id="password", text="secret")
    omniui.click(id="loginButton")
    omniui.verify_text(id="status", expected="Welcome, admin!")
```

失敗時報告會顯示：

1. 測試名稱與失敗原因
2. Captured stdout（含 trace log）
3. JavaFX 視窗的 inline 截圖
