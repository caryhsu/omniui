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

Run tests:

```bash
mvn -f java-agent/pom.xml test
```

Current Java test scope:
- JUnit 5 + Maven Surefire test foundation is in place
- `mvn test` now runs against the `java-agent` module
- this first batch stays JavaFX-runtime-free
- runtime helper coverage now includes `ReflectiveJavaFxSupport`
- reflective target coverage now includes selector resolution, snapshot shaping, and range/clamping action helpers in `ReflectiveJavaFxTarget`
- registry coverage now includes the discovery path via a test seam in `JavaFxRuntimeDiscovery`

Prerequisite:
- Maven must run with JDK 22 or newer because this module is compiled with `--release 22`

Register a supported JavaFX app:

```java
JavaFxRuntimeBridge.registerStage("LoginDemo", primaryStage);
```

This registration path is the current Phase 1 attach model. It gives the agent access to the live JavaFX scene graph and direct node-level actions without introducing a hard compile-time dependency on a specific JavaFX SDK in the agent module.

For an end-to-end reference target, use the demo apps under `demo/java/` (e.g. `demo/java/core-app`), which embed the agent server and register a live JavaFX stage automatically.
