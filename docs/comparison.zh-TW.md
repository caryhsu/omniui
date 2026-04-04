# OmniUI 與其他工具的功能對照

> 最後更新：2026-04

本文件追蹤 OmniUI 與主流 UI 自動化工具之間的功能落差。
供未來規劃 Roadmap 優先順序時參考。

---

## 對照表

| 功能 | Playwright | TestFX | WinAppDriver | OmniUI |
|---|---|---|---|---|
| **App 啟動** | ✅ | ✅ | ✅ | ❌ 需手動先啟動 agent |
| **pytest / JUnit fixture 整合** | ✅ | ✅ JUnit | ✅ | ❌ 無內建 fixture |
| **失敗自動截圖** | ✅ | ✅ | ✅ | ❌ 需手動呼叫 |
| **視覺回歸（截圖比對）** | ✅ | ❌ | ❌ | ❌ |
| **自訂 wait 條件** | ✅ `expect(locator)` | ✅ | ✅ | ⚠️ 只有預設 `wait_for_*` |
| **絕對座標點擊** | ✅ | ✅ | ✅ | ❌ |
| **Headless 模式** | ✅ | ✅ Monocle | ❌ | ❌ 未支援 |
| **CI/CD 範例** | ✅ | ✅ | ⚠️ | ❌ |
| **Drag & Drop** | ✅ | ✅ | ✅ | ❌ roadmap 中 |
| **Hover（懸停）** | ✅ | ✅ | ✅ | ❌ roadmap 中 |
| **FileChooser** | ✅ | ✅ | ✅ | ❌ roadmap 中 |
| **測試報告（HTML / XML）** | ✅ HTML | ✅ JUnit XML | ✅ | ❌ |

---

## 落差優先級

### 🔴 重要缺口 — 影響實際可用性

| 落差 | 說明 |
|---|---|
| **App 啟動 API** | `launch_app(jar=..., port=...)` — 從 Python 直接啟動 JavaFX app；目前每次需手動先跑 |
| **pytest fixture** | `@pytest.fixture` 自動 connect/disconnect，讓測試程式碼更乾淨 |
| **失敗自動截圖** | 測試失敗時自動儲存截圖，CI debug 必備 |

### 🟡 開發體驗缺口 — 影響開發舒適度

| 落差 | 說明 |
|---|---|
| **自訂 wait 條件** | `wait_until(lambda: client.get_text(id="x") == "done")` |
| **絕對座標點擊** | `click_at(x=100, y=200)`，Canvas 自繪 UI 的 fallback |
| **Headless 模式** | 支援 JavaFX Monocle，CI 不需要顯示器 |

### 🟢 加分項目

| 落差 | 說明 |
|---|---|
| **視覺回歸測試** | 截圖 baseline 比對 |
| **CI/CD 範例** | GitHub Actions workflow 範本 |
| **HTML 測試報告** | pytest-html 或 Allure 整合 |

---

## OmniUI 的優勢（相較於上述工具）

- **Scene graph 直接存取** — 元素識別精確，不像 CSS/XPath 是猜測
- **語言無關的 client** — Python client；TestFX 需要寫 Java 測試程式碼
- **多模態 fallback 鏈** — scene graph → OCR → vision template match
- **無需安裝 driver** — agent 嵌入 app JAR；不需要 WebDriver / WinAppDriver 安裝設定
