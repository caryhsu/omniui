## MODIFIED Requirements

### Requirement: Python client advanced-control compatibility
系統 SHALL 允許 Python automation scripts 在不暴露 backend-specific transport detail 的前提下，操作受支援的進階 JavaFX demo 場景。

#### Scenario: Run advanced scenario from Python
- **WHEN** 使用者對支援中的進階 JavaFX demo 場景執行 Python script
- **THEN** script 可以透過正常的 OmniUI client API 表達所需互動，並檢視 resulting state

### Requirement: High-level action API
系統 SHALL 提供足以表達支援中的進階 JavaFX demo interaction 的高階 Python methods；若 `click` 與 `type` 不足，則可擴充更高階 action。

#### Scenario: Use selection-oriented action for advanced control
- **WHEN** 某個支援中的進階 JavaFX 場景需要較適合用 selection 或 expansion 表達的互動，而不是 raw click
- **THEN** Python client 會提供可表達該互動的高階 action，而不要求使用者直接操作 transport payload
