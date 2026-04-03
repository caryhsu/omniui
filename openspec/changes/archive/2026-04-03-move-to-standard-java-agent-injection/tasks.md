## 1. Java agent bootstrap and runtime ownership

- [x] 1.1 Add standard Java agent entrypoints in `java-agent` for startup-time injection (`premain`) and scaffold future `agentmain` support if needed
- [x] 1.2 Refactor agent server startup so HTTP transport lifecycle is owned by the Java agent bootstrap instead of target application code
- [x] 1.3 Remove default runtime dependence on built-in demo target fallback for normal agent-enabled execution

## 2. JavaFX runtime discovery without app cooperation

- [x] 2.1 Implement agent-side JavaFX runtime discovery for the supported demo support matrix
- [x] 2.2 Register discovered JavaFX stages or scenes as automation targets from inside the agent
- [x] 2.3 Preserve scene graph enumeration and direct JavaFX actions when targets are discovered by the injected agent

## 3. Demo app de-invasiveness and launch modes

- [x] 3.1 Remove direct OmniUI bootstrap classes and registration calls from `demo/javafx-login-app`
- [x] 3.2 Update development launch commands so agent-enabled mode uses JVM injection and plain mode runs without OmniUI control
- [x] 3.3 Update packaged runtime wrapper scripts so `with-agent` launches with `-javaagent` and `plain` launches without it

## 4. Python client and demo flow alignment

- [x] 4.1 Make Python demo scripts and connection handling assume agent availability depends on launch mode, not app-embedded startup
- [x] 4.2 Ensure connection failures against plain mode are explicit and do not silently bind to a fallback target
- [x] 4.3 Update demo documentation to explain plain versus agent-enabled launch behavior and the expected Python control flow

## 5. Verification and regression coverage

- [x] 5.1 Add or update Java and Python tests for startup-time agent injection, plain-mode isolation, and target resolution failures
- [x] 5.2 Validate the end-to-end login flow against the agent-enabled launch path without OmniUI code inside the target app
- [x] 5.3 Re-run packaged runtime and demo scripts to confirm operator workflows still work after the integration model change
