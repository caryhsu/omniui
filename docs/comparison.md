# OmniUI vs Other Tools — Feature Comparison

> Last reviewed: 2026-04

This document tracks feature gaps between OmniUI and mainstream UI automation tools.
Use it to prioritise future Roadmap items.

---

## Comparison Table

| Feature | Playwright | TestFX | WinAppDriver | OmniUI |
|---|---|---|---|---|
| **App launch** | ✅ | ✅ | ✅ | ❌ agent must already be running |
| **pytest / JUnit fixture integration** | ✅ | ✅ JUnit | ✅ | ❌ no built-in fixture |
| **Auto-screenshot on failure** | ✅ | ✅ | ✅ | ❌ manual call only |
| **Visual regression (screenshot diff)** | ✅ | ❌ | ❌ | ❌ |
| **Custom wait condition** | ✅ `expect(locator)` | ✅ | ✅ | ⚠️ predefined `wait_for_*` only |
| **Absolute coordinate click** | ✅ | ✅ | ✅ | ❌ |
| **Headless mode** | ✅ | ✅ Monocle | ❌ | ❌ not documented/supported |
| **CI/CD examples** | ✅ | ✅ | ⚠️ | ❌ |
| **Drag & Drop** | ✅ | ✅ | ✅ | ❌ on roadmap |
| **Hover** | ✅ | ✅ | ✅ | ❌ on roadmap |
| **FileChooser** | ✅ | ✅ | ✅ | ❌ on roadmap |
| **Test report (HTML / XML)** | ✅ HTML | ✅ JUnit XML | ✅ | ❌ |

---

## Gap Priority

### 🔴 Critical — affects day-to-day usability

| Gap | Notes |
|---|---|
| **App launch API** | `launch_app(jar=..., port=...)` — start a JavaFX app from Python directly; currently requires manual startup |
| **pytest fixture** | Auto connect/disconnect via `@pytest.fixture`; keeps test code clean |
| **Auto-screenshot on failure** | Save screenshot when a test fails; essential for CI debugging |

### 🟡 DX — affects developer comfort

| Gap | Notes |
|---|---|
| **Custom wait condition** | `wait_until(lambda: client.get_text(id="x") == "done")` |
| **Absolute coordinate click** | `click_at(x=100, y=200)` — fallback for Canvas-rendered UIs |
| **Headless mode** | JavaFX Monocle support; avoids needing a display in CI |

### 🟢 Nice-to-have

| Gap | Notes |
|---|---|
| **Visual regression** | Screenshot baseline comparison |
| **CI/CD examples** | GitHub Actions workflow template |
| **HTML test report** | Pytest-html or Allure integration |

---

## OmniUI Advantages (vs the above tools)

- **Scene graph access** — element identification is exact, not guessed from CSS/XPath
- **Language-agnostic client** — Python client; TestFX requires Java test code
- **Multi-modal fallback chain** — scene graph → OCR → vision template match
- **Self-healing locator** — `id`-based `Locator` caches `text` and `type+index` from a pre-action snapshot; if `fx:id` is later renamed, OmniUI auto-retries with cached fallbacks before failing
- **No driver install** — agent is embedded in the app JAR; no WebDriver/WinAppDriver setup
