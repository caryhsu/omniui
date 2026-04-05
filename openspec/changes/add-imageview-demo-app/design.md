## Context

OmniUI 已支援大多數 JavaFX 控件（Button、TextField、ProgressBar 等），但尚未支援 `ImageView`（`javafx.scene.image.ImageView`）。Java agent 的 `ReflectiveJavaFxTarget` 目前透過反射讀取節點屬性，需要新增對 `ImageView` 的識別與屬性提取邏輯。

現有 port 分配：48100–48104，新 image-app 使用 48105。

## Goals / Non-Goals

**Goals:**
- Java agent 能從 `ImageView` 讀取 `imageUrl`（`Image.getUrl()`）與 `isLoaded`（`!Image.isError()`）
- Python client 提供 `get_image_url(**selector)` → `ActionResult(value=str)` 與 `is_image_loaded(**selector)` → `bool`
- 獨立 image-app（port 48105）示範：多張圖片切換、broken image 狀態
- Python demo script 驗證以上 API

**Non-Goals:**
- 截圖或像素比對
- 上傳/下載圖片檔案
- 動態 GIF 動畫狀態
- 非 `ImageView` 的圖片（如 Button icon）

## Decisions

### D1：Java agent 屬性讀取方式

使用 `ImageView.getImage()` → `Image.getUrl()` 取得 URL，`Image.isError()` 取反得 `isLoaded`。  
**理由**：`getUrl()` 回傳載入時傳入的 URL 字串，即使圖片載入失敗仍可取得；`isError()` 是 JavaFX 官方判斷載入失敗的方式。  
**替代方案**：用 `Image.progressProperty()` 判斷（繁瑣，且 progress=1.0 不代表無 error）。

### D2：Python API 設計

`get_image_url(**selector)` 回傳 `ActionResult(value=url_string)`，與 `get_text` 一致。  
`is_image_loaded(**selector)` 直接回傳 `bool`，與 `is_visible`、`is_enabled` 一致。  
**理由**：一致的 API 風格降低使用者學習成本。

### D3：Demo app 圖片來源

使用公開網路圖片 URL（`https://picsum.photos/`）模擬正常載入，用無效 URL（`https://invalid.example/broken.png`）模擬載入失敗。  
**理由**：無需打包圖片資源到 jar，簡化 build；broken 用途明確，易於自動化測試驗證。  
**風險**：需要網路連線；若 picsum.photos 無法連線測試會失敗。  
**替代方案**：打包 classpath 圖片資源 → 選擇不採用，以避免 Maven module 複雜度。

### D4：`imageUrl` 為 null 時的處理

`ImageView.getImage()` 可能為 null（尚未設圖）。此時 `get_image_url` 回傳 `ActionResult(ok=True, value=null)` 而非 error，讓 Python 端自行判斷。  
`is_image_loaded` 在 image=null 時回傳 `false`。

## Risks / Trade-offs

- **網路依賴** → Mitigation：demo script 加入 timeout 處理；若離線可改用本機 URL
- **`Image.getUrl()` 在部分版本回傳 null**（e.g., 由 `InputStream` 建立的 Image）→ Mitigation：Java agent 加 null guard，改回傳空字串
- **JavaFX 線程**：`Image` 屬性讀取必須在 FX Application Thread，`ReflectiveJavaFxTarget` 已有 `Platform.runLater` 機制，沿用即可
