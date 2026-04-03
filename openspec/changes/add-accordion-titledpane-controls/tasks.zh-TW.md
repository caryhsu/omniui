## 1. Java agent — 新增 action

- [x] 1.1 在 `ReflectiveJavaFxTarget.java` 的 switch 中新增 `expand_pane` case：呼叫 `safeInvoke(node, "setExpanded", true)`
- [x] 1.2 新增 `collapse_pane` case：呼叫 `safeInvoke(node, "setExpanded", false)`
- [x] 1.3 新增 `get_expanded` case：呼叫 `safeInvoke(node, "isExpanded")`，回傳布林值
- [x] 1.4 確認 `safeInvoke` 支援帶 boolean 參數的呼叫，或改用 `ReflectiveJavaFxSupport.invoke`

## 2. Python engine — 新增方法

- [x] 2.1 在 `omniui/core/engine.py` 新增 `expand_pane(**selector)` 方法
- [x] 2.2 新增 `collapse_pane(**selector)` 方法
- [x] 2.3 新增 `get_expanded(**selector)` 方法，回傳布林值

## 3. Demo app — 新增 UI 區塊

- [x] 3.1 在 `LoginDemoApp.java` 新增 Accordion/TitledPane import（若尚未匯入）
- [x] 3.2 新增 `accordionSection`，包含 `demoAccordion`（含 `pane1`、`pane2`、`pane3` 三個 TitledPane）
- [x] 3.3 將 `accordionSection` 加入 root VBox

## 4. Python demo script

- [x] 4.1 建立 `demo/python/accordion_demo.py`：讀取初始展開狀態、展開各面板、驗證互斥行為、收合
- [x] 4.2 將 `accordion_demo` 加入 `demo/python/run_all.py`

## 5. 規格繁體中文翻譯

- [x] 5.1 建立 `specs/accordion-titledpane-automation/spec.zh-TW.md`
- [x] 5.2 建立 `proposal.zh-TW.md`
- [x] 5.3 建立 `design.zh-TW.md`
- [x] 5.4 建立 `tasks.zh-TW.md`

## 6. 驗證

- [ ] 6.1 執行 `mvn package -f java-agent/pom.xml`，確認編譯無誤
- [ ] 6.2 執行 `accordion_demo.py`，確認輸出 `accordion_demo succeeded ✓`
- [ ] 6.3 git commit + push
