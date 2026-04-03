## 1. Verify existing Java action paths

- [ ] 1.1 在 demo app 啟動後，以 Python 對 CheckBox 呼叫 `get_selected` / `set_selected`，確認現有反射路徑可正常運作（不需修改 Java）
- [ ] 1.2 在 demo app 啟動後，以 Python 對 ChoiceBox 呼叫 `select` / `get_value`，確認現有 SelectionModel 路徑可正常運作
- [ ] 1.3 若任一路徑失敗，在 `ReflectiveJavaFxTarget.java` 加入對應的修補分支

## 2. Demo app — add UI sections

- [ ] 2.1 在 `LoginDemoApp.java` 新增 `checkBoxSection`，包含三個獨立 CheckBox（`id="checkA"`, `"checkB"`, `"checkC"`）
- [ ] 2.2 在 `LoginDemoApp.java` 新增 `choiceBoxSection`，包含一個 ChoiceBox（`id="demoChoiceBox"`）並預填 3 個字串選項
- [ ] 2.3 將兩個新區塊加入 root VBox

## 3. Python demo scripts

- [ ] 3.1 建立 `demo/python/checkbox_demo.py`：讀取初始狀態、勾選、取消勾選、驗證各 CheckBox 獨立性
- [ ] 3.2 建立 `demo/python/choicebox_demo.py`：選取各選項、讀回目前值、驗證 item_not_found 錯誤
- [ ] 3.3 將兩個 demo 加入 `demo/python/run_all.py`

## 4. Spec zh-TW translations

- [ ] 4.1 建立 `openspec/changes/add-checkbox-choicebox-controls/specs/checkbox-automation/spec.zh-TW.md`
- [ ] 4.2 建立 `openspec/changes/add-checkbox-choicebox-controls/specs/choicebox-automation/spec.zh-TW.md`
- [ ] 4.3 建立 `openspec/changes/add-checkbox-choicebox-controls/proposal.zh-TW.md`
- [ ] 4.4 建立 `openspec/changes/add-checkbox-choicebox-controls/design.zh-TW.md`
- [ ] 4.5 建立 `openspec/changes/add-checkbox-choicebox-controls/tasks.zh-TW.md`

## 5. Validation

- [ ] 5.1 執行 `checkbox_demo.py`，確認輸出 `checkbox_demo succeeded ✓`
- [ ] 5.2 執行 `choicebox_demo.py`，確認輸出 `choicebox_demo succeeded ✓`
- [ ] 5.3 執行 `mvn package -f java-agent/pom.xml`，確認 Java agent 編譯無誤
- [ ] 5.4 git commit + push
