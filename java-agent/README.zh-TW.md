# OmniUI Java Agent

英文版檔名：`README.md`

Phase 1 的 JavaFX runtime hook 目前放在本機 Java agent module 中。

規劃職責：
- attach 到受支援的本機 JVM
- 檢查 JavaFX scene graph snapshot
- 執行 node-bound direct action
- 對外提供本機 HTTP JSON protocol，定義見 [docs/protocol/agent-protocol.zh-TW.md](../docs/protocol/agent-protocol.zh-TW.md)

目前狀態：
- 已有 Maven module 與 loopback-only HTTP server
- 已支援 session、discover、action、screenshot endpoint
- 有一個 demo JavaFX-like target 用於驗證 protocol
- 已有 reflection-based JavaFX bridge，可供註冊 live `Scene` 或 `Stage` 的 app 使用

本機執行：

```bash
mvn -f java-agent/pom.xml package
java -jar java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar
```

執行測試：

```bash
mvn -f java-agent/pom.xml test
```

目前 Java 測試範圍：
- 已建立 JUnit 5 + Maven Surefire 測試基礎架構
- `mvn test` 現在可以直接對 `java-agent` module 執行
- 這一批維持不依賴 JavaFX runtime
- runtime helper 測試目前已擴到 `ReflectiveJavaFxSupport`
- registry 測試目前已覆蓋 `JavaFxRuntimeDiscovery` 的 discovery path（透過 test seam）

前置條件：
- Maven 必須使用 JDK 22 以上，因為此 module 目前以 `--release 22` 編譯

註冊受支援的 JavaFX app：

```java
JavaFxRuntimeBridge.registerStage("LoginDemo", primaryStage);
```

這條註冊路徑是目前的 Phase 1 attach model。它能讓 agent 存取 live JavaFX scene graph 與 direct node-level action，同時避免在 agent module 內硬綁特定 JavaFX SDK。

若要看 end-to-end 參考 target，請使用 `demo/java/` 底下的 demo app（例如 [demo/java/core-app](../demo/java/core-app/)），它會自動嵌入 agent server 並註冊 live JavaFX stage。
