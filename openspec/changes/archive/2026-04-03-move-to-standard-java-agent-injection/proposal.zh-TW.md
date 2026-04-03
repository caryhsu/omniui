## Why

目前的 JavaFX demo app 雖然證明了 OmniUI Phase 1 架構可行，但它是透過把 OmniUI 類別直接嵌進 target app 來完成。這讓 sample 太具侵入性，也弱化了產品敘事，因為真實場景需要的是透過標準 Java agent 邊界去控制未修改的 JavaFX app。

## What Changes

- 將 JavaFX runtime discovery 與 agent startup 從 target app 移回 `java-agent` 模組。
- 新增標準 Java agent 入口，讓 OmniUI 可透過 `-javaagent` 載入，並為未來 dynamic attach 預留空間。
- 移除 demo JavaFX app 內直接使用 `OmniUiAgentServer`、`JavaFxRuntimeBridge` 等 OmniUI bootstrap code。
- 新增明確的 launch modes 與文件，區分有 agent 與 plain app 的啟動方式。
- 定義 Python client 在 agent 存在與不存在時的連線與行為差異。

## Capabilities

### New Capabilities

- `standard-java-agent-injection`: 以標準 JVM agent 啟動與 JavaFX runtime discovery 完成自動化，不要求 target app 內含 OmniUI code。

### Modified Capabilities

- `javafx-automation-core`: JavaFX automation 必須在 target app 透過標準 Java agent 啟動時仍可運作，而不是依賴 app-embedded bootstrap。
- `python-automation-client`: client 連線行為必須能明確區分 agent-enabled 與 plain app 模式。

## Impact

- 影響程式碼：`java-agent/`、`demo/javafx-login-app/`、demo launch scripts、Python demo scripts，以及相關文件。
- 影響 runtime model：Java agent 會成為 HTTP server 與 JavaFX runtime attachment 的擁有者。
- 影響操作流程：demo 與未來 target app 必須能在不修改 app source 的前提下支援 agent-enabled launch。
