## 1. Java Agent

- [ ] 1.1 Add `case "select_multiple"` in `performOnFxThread()`: read `values` array from payload, call `getSelectionModel()` → `clearSelection()`, iterate items with index, build matching int list, call `selectIndices(first, rest...)`
- [ ] 1.2 Add `case "get_selected_items"`: call `getSelectionModel().getSelectedItems()`, map each item to `.toString()`, return as `ArrayList<String>`
- [ ] 1.3 Build java-agent: `mvn clean install -f java-agent/pom.xml`
- [ ] 1.4 Rebuild jlink: delete `demo/javafx-login-app/target/omniui-login-demo`, run `mvn package javafx:jlink -f demo/javafx-login-app/pom.xml`

## 2. Demo App

- [ ] 2.1 In `LoginDemoApp.java`: set `serverList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE)`

## 3. Python Client

- [ ] 3.1 Add `select_multiple(values: list[str], **selector) -> ActionResult` to `engine.py`
- [ ] 3.2 Add `get_selected_items(**selector) -> ActionResult` to `engine.py`

## 4. Demo & Tests

- [ ] 4.1 Create `demo/python/multi_select_demo.py`: select ["Alpha", "Beta"], verify `get_selected_items` returns both; then select single item and verify only one selected
- [ ] 4.2 Add `multi_select_demo` to `demo/python/run_all.py`

## 5. Documentation

- [ ] 5.1 Add `select_multiple` and `get_selected_items` to `docs/api/python-client.md`
- [ ] 5.2 Update actions list in `README.md` and `README.zh-TW.md`
- [ ] 5.3 Mark `Multi-select` `[x]` in `ROADMAP.md` and `ROADMAP.zh-TW.md`
