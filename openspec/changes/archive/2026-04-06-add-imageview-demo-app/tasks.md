## 1. Java Agent — ImageView 屬性讀取

- [x] 1.1 在 `ReflectiveJavaFxTarget.java` 新增 `get_image_url` 指令處理：識別 `ImageView`，呼叫 `getImage().getUrl()`，null guard 回傳 `""`
- [x] 1.2 在 `ReflectiveJavaFxTarget.java` 新增 `is_image_loaded` 指令處理：呼叫 `!getImage().isError()`，image=null 時回傳 `false`

## 2. Python Client API

- [x] 2.1 在 `omniui/core/engine.py` 新增 `get_image_url` 指令解析，回傳 `ActionResult(value=str)`
- [x] 2.2 在 `omniui/core/engine.py` 新增 `is_image_loaded` 指令解析，回傳 `bool`
- [x] 2.3 在 `omniui/locator.py`（或 client 公開介面）新增 `get_image_url(**selector)` 與 `is_image_loaded(**selector)` 方法

## 3. Java Demo App — image-app

- [x] 3.1 建立 `demo/java/image-app/pom.xml`（port 48105，artifact `omniui-image-demo`）
- [x] 3.2 建立 `demo/java/image-app/src/main/java/module-info.java`
- [x] 3.3 實作 `ImageDemoApp.java`：
  - `imageView1`（有效 URL）、`imageView2`（broken URL）
  - `switchBtn`（交換兩個 ImageView 的 Image）
  - `urlLabel`（顯示 imageView1 目前 URL）、`statusLabel`（loaded/broken）
  - `dragImageView`（可拖曳 ImageView）、`dropZone`（接收拖曳，使用 JavaFX DnD API）
  - `dropResult` Label（drop 成功顯示 "dropped!"）
- [x] 3.4 建立 `demo/java/image-app/run-dev-with-agent.bat`
- [x] 3.5 確認 `mvn compile` 成功

## 4. Python Demo Script

- [x] 4.1 建立 `demo/python/image/__init__.py`、`_bootstrap.py`、`_runtime.py`（port 48105）
- [x] 4.2 實作 `demo/python/image/image_demo.py`：
  - 驗證 `get_image_url(imageView1)` 回傳有效 URL
  - 驗證 `is_image_loaded(imageView1)` = True
  - 驗證 `is_image_loaded(imageView2)` = False（broken）
  - 點擊 `switchBtn`，驗證 URL 互換
  - 執行 `drag(id="dragImageView").to(id="dropZone")`，驗證 `dropResult` = "dropped!"

## 5. 整合

- [x] 5.1 在 `demo/python/run_all.py` 新增 image-app import 及 Image App 區塊（port 48105）
- [x] 5.2 執行 `python -m pytest tests/ -q` 確認 tests 全數通過
