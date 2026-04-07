# Spec: Recorder Actionable Filter

## Purpose

Defines how the Java agent event recorder filters out fragile layout-node selectors by identifying actionable nodes and traversing the parent chain when a non-actionable node is clicked.

---

## Requirements

### Requirement: Recorder skips non-actionable layout nodes
The Java agent event recorder SHALL skip recording click events on pure layout nodes
(`Pane`, `HBox`, `VBox`, `BorderPane`, `StackPane`, `AnchorPane`, `ButtonBar`, `DialogPane`,
and any node whose simple class name ends in `Pane`).
When such a node is clicked, the recorder SHALL traverse the parent chain (up to 15 levels)
to find the nearest actionable ancestor.
If an actionable ancestor is found, the click SHALL be recorded using that ancestor's selector.
If no actionable ancestor is found within the limit, the click SHALL NOT be recorded at all.

#### Scenario: Layout node click inside dialog — actionable ancestor found
- **WHEN** user clicks a `Pane` that is a child of a `Button` inside a `DialogPane`
- **THEN** the recorder records `click(text="OK")` (or the button's selector), NOT `click(type="Pane", index=0)`

#### Scenario: Layout node click — no actionable ancestor
- **WHEN** user clicks a spacing `Pane` with no actionable ancestor within 15 levels
- **THEN** the recorder emits no event for this click

#### Scenario: Actionable node click — recorded normally
- **WHEN** user clicks a `Button` with `fx:id="loginButton"`
- **THEN** the recorder records `click(id="loginButton")` as before

#### Scenario: No fragile selector produced for dialog interactions
- **WHEN** user records a full dialog workflow (open dialog → fill fields → click OK)
- **THEN** the generated script contains NO lines with `# WARN: fragile selector`

---

### Requirement: Actionable node set
The Java agent SHALL define the following node types as actionable (by `getSimpleName()`):
`Button`, `ButtonBase`, `ToggleButton`, `CheckBox`, `RadioButton`,
`TextField`, `TextArea`, `PasswordField`,
`ComboBox`, `ChoiceBox`, `Slider`, `Spinner`,
`DatePicker`, `ColorPicker`,
`ListView`, `TreeView`, `TableView`, `TableCell`,
`Hyperlink`, `Label`, `MenuButton`, `MenuItem`.
All other node types (especially those ending in `Pane` or `Bar`) SHALL be treated as non-actionable.

#### Scenario: Button is actionable
- **WHEN** `isActionableNode` is called with a `Button` instance
- **THEN** the method returns `true`

#### Scenario: Pane is non-actionable
- **WHEN** `isActionableNode` is called with a `Pane` instance
- **THEN** the method returns `false`

#### Scenario: ButtonBar is non-actionable
- **WHEN** `isActionableNode` is called with a `ButtonBar` instance
- **THEN** the method returns `false`

---

### Requirement: 右鍵事件不產生 click 錄製步驟

`EventFilter` **必須** 在 `MOUSE_PRESSED` 事件中區分左鍵與右鍵，右鍵事件 **不得** 進入一般 click 錄製路徑。

#### Scenario: 右鍵不產生 click 事件

- **WHEN** 使用者在錄製中對節點按右鍵（`MouseButton.SECONDARY`）
- **THEN** 事件緩衝區中 **不** 增加 `event_type="click"` 的事件
- **且** `/events/pending` 回傳的事件清單不包含此右鍵操作

#### Scenario: 左鍵仍正常錄製

- **WHEN** 使用者在錄製中對節點按左鍵（`MouseButton.PRIMARY`）
- **THEN** 事件緩衝區正常增加 `event_type="click"` 的事件（行為不變）

