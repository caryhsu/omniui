# OmniUI Java Agent

Phase 1 hosts the JavaFX runtime hook inside a local Java agent module.

Planned responsibilities:
- attach to a supported local JVM
- inspect JavaFX scene graph snapshots
- execute direct node-bound actions
- expose the local HTTP JSON protocol documented under `docs/protocol/agent-protocol.md`

Current state:
- Maven module with a loopback-only HTTP server
- session, discover, action, and screenshot endpoints
- demo JavaFX-like target used to validate the protocol before real runtime attachment lands
- reflection-based JavaFX bridge for supported apps that register a live `Scene` or `Stage`

Run locally:

```bash
mvn -f java-agent/pom.xml package
java -jar java-agent/target/omniui-java-agent-0.1.0-SNAPSHOT.jar
```

Register a supported JavaFX app:

```java
JavaFxRuntimeBridge.registerStage("LoginDemo", primaryStage);
```

This registration path is the current Phase 1 attach model. It gives the agent access to the live JavaFX scene graph and direct node-level actions without introducing a hard compile-time dependency on a specific JavaFX SDK in the agent module.

For an end-to-end reference target, use `demo/javafx-login-app`, which embeds the agent server and registers a live JavaFX stage automatically.
