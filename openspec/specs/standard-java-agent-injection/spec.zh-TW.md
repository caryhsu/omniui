## Purpose

定義 OmniUI 如何透過標準 JVM Java agent injection 啟用 JavaFX automation，而不需要 target app source change。

## Requirements

### Requirement: Standard JVM agent startup
系統 SHALL 支援透過標準 JVM `-javaagent` 啟動路徑進行 startup-time Java agent injection，以啟用 JavaFX automation。

#### Scenario: Launch target app with Java agent enabled
- **WHEN** 一個受支援的 JavaFX app 以 `-javaagent` 啟動 OmniUI Java agent
- **THEN** Java agent 會在 target JVM 內啟動，且不要求 target app source 直接引用 OmniUI class

### Requirement: Agent-owned control server
系統 SHALL 由 Java agent 在自身內部啟動並擁有 OmniUI HTTP control server，而不是由 target application bootstrap code 啟動。

#### Scenario: Agent starts control plane after injection
- **WHEN** target JVM 以 OmniUI Java agent enabled 模式啟動
- **THEN** Java agent 會打開 loopback control endpoint 並回報 ready，而不需要 target app 呼叫 OmniUI startup API

### Requirement: Agent-driven JavaFX runtime discovery
系統 SHALL 從 injected Java agent 端發現 JavaFX runtime state 並註冊 automation targets，而不要求 application code 呼叫 `registerStage`、`registerScene` 或其他 OmniUI bridge API。

#### Scenario: Discover live JavaFX scene without app-side bridge call
- **WHEN** injected Java agent 正在受支援的 JavaFX app 內運作
- **THEN** agent 可發現 live JavaFX scene 或 stage，並使其可供 node discovery 與 action execution 使用

### Requirement: Plain launch isolation
系統 SHALL 允許同一個 JavaFX app 在沒有 OmniUI control 的情況下正常啟動。

#### Scenario: Launch app in plain mode
- **WHEN** 一個受支援的 JavaFX app 在未注入 OmniUI Java agent 的情況下啟動
- **THEN** app 會正常運作，且不暴露 OmniUI HTTP control endpoint
