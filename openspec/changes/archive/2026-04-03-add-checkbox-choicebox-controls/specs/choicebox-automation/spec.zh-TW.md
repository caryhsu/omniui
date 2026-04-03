## ADDED Requirements

### Requirement: ChoiceBox item selection
系統 SHALL 支援透過比對字串值，在 `ChoiceBox` node 中選取項目。

#### Scenario: Select a ChoiceBox item by value
- **WHEN** automation client 呼叫 `select(id="<choiceBoxId>", value="<item>")`
- **THEN** ChoiceBox 的 selection model 選取符合的項目，使其成為目前值

#### Scenario: Report error when item not found
- **WHEN** automation client 呼叫 `select(id="<choiceBoxId>", value="<nonExistentItem>")`
- **THEN** 系統回傳失敗結果，reason 為 `item_not_found`

### Requirement: ChoiceBox current value read
系統 SHALL 支援讀取 `ChoiceBox` node 目前已選取的值。

#### Scenario: Read the current ChoiceBox value
- **WHEN** automation client 在已選取項目後呼叫 `get_value(id="<choiceBoxId>")`
- **THEN** 系統回傳目前已選取項目的字串表示

#### Scenario: Read ChoiceBox value when nothing is selected
- **WHEN** automation client 在未選取任何項目時呼叫 `get_value(id="<choiceBoxId>")`
- **THEN** 系統回傳 `null` 或空字串
