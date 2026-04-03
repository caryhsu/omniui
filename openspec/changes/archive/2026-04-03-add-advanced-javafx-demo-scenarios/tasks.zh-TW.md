## 1. 進階 JavaFX demo app 場景

- [x] 1.1 新增進階 demo 場景，涵蓋 `ComboBox`、`ListView`、`TreeView`、`TableView` 與 grid-oriented layout
- [x] 1.2 為進階場景填入 deterministic、可讀的 sample data，方便 selector-based automation
- [x] 1.3 將進階控制項整理成容易辨識與測試的 screens、tabs 或 sections

## 2. JavaFX discovery 與 interaction coverage

- [x] 2.1 驗證並調整 JavaFX node discovery，讓 visible advanced-control state 能出現在 `get_nodes()` 輸出中
- [x] 2.2 針對進階場景所需的 selection-oriented 或 hierarchy-oriented interaction 補強或調整 JavaFX action support
- [x] 2.3 驗證 popup-backed 或 multi-window control content（例如 `ComboBox`）是否能被目前 discovery path 看見

## 3. Python demos 與 client surface

- [x] 3.1 新增可執行的 Python demos，覆蓋進階 JavaFX 場景
- [x] 3.2 判斷支援中的進階場景是否需要新的高階 client action，例如 `select`、`expand`、`collapse`
- [x] 3.3 若需要新的進階 action，則更新 Python client 與範例 script

## 4. 文件與驗證

- [x] 4.1 擴充 manual smoke guidance，納入進階 JavaFX 場景
- [x] 4.2 新增 regression tests 或 smoke tests，覆蓋進階 demo launcher、scripts 與 interaction trace
- [x] 4.3 在 `with-agent` mode 下對支援中的進階場景做 end-to-end 驗證，並記錄明確不支援的案例
