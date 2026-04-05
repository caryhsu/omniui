# Headless 模式與 CI/CD

OmniUI 支援在無螢幕環境（如 GitHub Actions、Docker）下透過 **Xvfb**（Linux 虛擬 Framebuffer）執行 JavaFX 測試。

## 運作原理

JavaFX 需要顯示器才能渲染 UI。在沒有實體螢幕的 CI 環境中，**Xvfb** 提供虛擬顯示器，JavaFX 可以透明地連接使用，**不需要修改任何程式碼**。

```bash
Xvfb :99 -screen 0 1280x800x24 &
export DISPLAY=:99
python demo/python/run_all.py
```

## GitHub Actions Workflows

專案提供兩個 workflow：

### `ci-unit.yml` — 單元測試

執行 Python 單元測試（不需要 Java app）。每次 push 和 PR 時觸發。

```bash
python -m pytest tests/ -v
```

### `ci-integration.yml` — 整合測試

建置所有 Java app，並透過 Xvfb 對運行中的 JavaFX app 執行完整 demo 測試。
可透過 `workflow_dispatch` 手動觸發。

## 本機 Headless 測試（Linux / WSL）

```bash
# 安裝 Xvfb
sudo apt-get install xvfb libgtk-3-0 libgl1

# 啟動虛擬顯示器
Xvfb :99 -screen 0 1280x800x24 &
export DISPLAY=:99

# 執行測試
cd demo/python
python run_all.py
```

## OS 平台自動偵測

`run_all.py` 會自動選擇對應的 JavaFX 平台 JAR：

| 作業系統 | JAR classifier |
|---|---|
| Windows | `-win.jar` |
| macOS | `-mac.jar` |
| Linux | `-linux.jar` |

此功能由 `run_all.py` 中的 `_javafx_classifier()` 處理，無需手動設定。

## Maven pom.xml 說明

Demo app 的 `pom.xml` 目前宣告 `<classifier>win</classifier>` 用於 JavaFX 依賴。這僅影響編譯時的解析；執行時，JAR 由 `run_all.py` 透過 OS 偵測的 classifier 從本機 `.m2` 快取載入，不受此影響。

若要讓 Maven 本身也支援跨平台，可加入 profiles（選用）：

```xml
<profiles>
  <profile>
    <id>linux</id>
    <activation><os><family>unix</family></os></activation>
    <properties><javafx.platform>linux</javafx.platform></properties>
  </profile>
  <profile>
    <id>windows</id>
    <activation><os><family>windows</family></os></activation>
    <properties><javafx.platform>win</javafx.platform></properties>
  </profile>
  <profile>
    <id>mac</id>
    <activation><os><family>mac</family></os></activation>
    <properties><javafx.platform>mac</javafx.platform></properties>
  </profile>
</profiles>
```
