## Why

OmniUI 目前無法對 JavaFX `ImageView` 節點進行自動化操作，無法讀取目前顯示的圖片 URL 或判斷圖片是否載入成功。隨著 UI 自動化需求擴展，驗證圖片顯示狀態（正確圖片、載入成功）是常見測試場景。

## What Changes

- 新增 Java agent 對 `ImageView` 的屬性讀取支援（`imageUrl`、`isLoaded`）
- 新增 Python client API：`get_image_url(**selector)` 和 `is_image_loaded(**selector)`
- 新增獨立 demo app `image-app`（port 48105）：包含圖片切換、載入失敗模擬、ImageView drag & drop 區域
- image-app 包含 drag source（可拖曳的 ImageView）與 drop target（接收區），拖曳後顯示結果
- 驗證現有 `client.drag().to()` API 可對 ImageView 節點正常運作
- 新增 Python demo script `demo/python/image/image_demo.py`
- 整合進 `demo/python/run_all.py`

## Capabilities

### New Capabilities

- `imageview-automation`: Python client 可讀取 ImageView 的圖片 URL 及載入狀態；Java agent 回報 `imageUrl`（`Image.getUrl()`）與 `isLoaded`（`Image.isError()` 取反）；`get_image_url()` 回傳 `ActionResult(value=url_string)`；`is_image_loaded()` 回傳 `bool`
- `imageview-demo-scenarios`: 獨立 image-app demo app（port 48105）含按鈕切換多張圖片、故意載入失敗的 broken image、可拖曳的 ImageView 節點（drag source）與接收區（drop target）、Python 端驗證 `get_image_url`、`is_image_loaded` 及 `drag().to()` 對 ImageView 運作正常

### Modified Capabilities

- `javafx-automation-core`: 新增 `ImageView` 節點類型的屬性讀取能力（非 breaking，純新增）

## Impact

- `java-agent/src/main/java/dev/omniui/agent/runtime/ReflectiveJavaFxTarget.java` — 新增 ImageView 屬性讀取
- `omniui/core/engine.py` — 新增 `get_image_url`、`is_image_loaded` 指令解析
- `omniui/client.py`（或 locator） — 新增公開 API 方法
- `demo/java/image-app/` — 新建 Maven module
- `demo/python/image/` — 新建 Python demo 目錄
- `demo/python/run_all.py` — 新增 image-app 區塊
- Port 分配：48105
