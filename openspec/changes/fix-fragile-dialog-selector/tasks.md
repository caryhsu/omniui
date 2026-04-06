## 1. Java agent — actionable node filter

- [x] 1.1 在 `ReflectiveJavaFxTarget.java` 新增 `isActionableNode(Object node)` — 以 `getSimpleName()` 比對 actionable 集合（Button、TextField、ComboBox、CheckBox 等）
- [x] 1.2 新增 `nearestActionableAncestor(Object node)` — 向上 traverse 最多 15 層，回傳第一個 actionable 節點，找不到回傳 `null`
- [x] 1.3 在 `onMouseClicked` 的 `isInsideDialogButton` check 之後加入過濾邏輯：若 `node` 非 actionable，呼叫 `nearestActionableAncestor`；有祖先則以祖先替代 `node`；無祖先則 `return`

## 2. Build & 手動驗證

- [x] 2.1 Build agent JAR：`mvn clean package -pl java-agent -am -q`
- [x] 2.2 啟動 todo-app with agent（`run-dev-with-agent.bat`），用 Recorder 錄製「Add task → 填欄位 → OK」
- [x] 2.3 確認錄製腳本中無任何 `# WARN: fragile selector` 行
- [x] 2.4 確認 OK / Cancel 仍被錄製為 `dismiss_dialog(button="確定")`（由 attachDialogButtonListeners 直接 attach 處理）

## 3. Unit tests

- [x] 3.1 在 `tests/test_recorder.py` 新增 `SelectorInferenceTests`：文件化 `_fragile` fallback 行為，確認 Pane 事件產生 WARN、Button with id/text 不產生 WARN
- [x] 3.2 新增 `isActionableNode` 相關 Java 邏輯的 Python-side 單元測試（若有對應的 Python 模型層測試）

## 4. Commit & PR

- [x] 4.1 `git checkout -b fix/fragile-dialog-selector`
- [x] 4.2 `pytest tests/ -q` — 158 passed
- [ ] 4.3 Commit：`fix: suppress fragile layout-node selectors in recorder`
- [ ] 4.4 Push + open PR → merge to main
- [ ] 4.5 更新 ROADMAP — 標記 `fragile selector in dialogs` 為 ✅
