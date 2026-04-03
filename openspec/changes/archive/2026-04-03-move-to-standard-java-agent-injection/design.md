## Context

Phase 1 proved that OmniUI can discover JavaFX nodes, execute direct actions, and expose a local HTTP control plane to Python. The current reference app achieves that by embedding OmniUI classes directly inside the target JavaFX application. That was acceptable for a controlled demo, but it is the wrong long-term integration shape because the target app becomes responsible for starting the agent server and registering its own JavaFX runtime.

The next step is to move that ownership into the `java-agent` module and treat the target app as an unmodified JavaFX application. This is a cross-cutting change because it affects the Java agent lifecycle, JavaFX runtime discovery, demo launch flow, Python connection expectations, and operator documentation.

Current constraints:
- Phase 1 remains local-machine and JavaFX-only.
- The Python client still talks to a loopback HTTP JSON endpoint.
- The demo app should remain usable in two modes:
  - plain mode, which behaves like a normal JavaFX app
  - agent-enabled mode, which exposes the OmniUI control plane
- Dynamic attach may be desirable later, but the first implementation needs a stable startup-time integration story.

## Goals / Non-Goals

**Goals:**
- Remove direct OmniUI bootstrap code from the JavaFX demo application source.
- Support standard JVM agent startup via `-javaagent`.
- Make the Java agent responsible for starting the HTTP server and discovering JavaFX runtime state.
- Preserve the existing Python API shape and keep the control endpoint behavior compatible with the Phase 1 client.
- Keep plain launch and agent-enabled launch as explicit operator-visible modes.
- Leave room for a future `agentmain` attach path without forcing it into the first implementation.

**Non-Goals:**
- Full dynamic attach support in this change.
- Support for non-JavaFX desktop toolkits.
- Solving every possible JavaFX stage discovery edge case in one pass.
- Production-hardening for remote transport, authentication, or multi-app orchestration.
- Eliminating all wrappers and launch scripts; operator ergonomics still matter for demos and local validation.

## Decisions

### 1. Make `-javaagent` the primary integration mode

The Java agent will expose a standard `premain(String, Instrumentation)` entrypoint and become the primary way to enable OmniUI automation in a target JVM.

Rationale:
- `-javaagent` is the least ambiguous non-invasive integration point for a JVM application.
- It removes the need for target app source changes.
- It gives the agent a deterministic startup lifecycle before the JavaFX app is fully running.

Alternatives considered:
- Continue embedding agent startup in the app: rejected because it keeps OmniUI-specific code inside the target.
- Implement dynamic attach first: rejected because it adds more lifecycle complexity before the base agent contract is stabilized.
- Run the HTTP server in a separate external process only: rejected for now because the agent still needs in-process access to JavaFX runtime state.

### 2. Split agent startup into bootstrap, runtime discovery, and transport layers

The `java-agent` module will be reorganized conceptually into:
- agent bootstrap: `premain` and future `agentmain`
- transport: HTTP server lifecycle and session management
- runtime discovery: JavaFX runtime detection and registration
- target adapters: JavaFX automation target instances

Rationale:
- The current code mixes server startup and target registration assumptions.
- Separating these concerns makes it easier to support both startup-time injection and future attach flows.
- It allows plain app mode and agent-enabled mode to differ only by how the JVM is launched, not by app code shape.

Alternatives considered:
- Leave the current server and registry design intact and just call it from `premain`: rejected because it would preserve too much embedded-app bias, especially the static demo fallback registry.

### 3. Replace app-driven `JavaFxRuntimeBridge.registerStage(...)` with agent-driven JavaFX discovery

The agent will discover JavaFX runtime state itself instead of requiring the app to register a `Stage` or `Scene`. The first implementation should discover runtime state by instrumenting JavaFX application lifecycle boundaries and capturing visible stages or scenes from inside the agent.

Expected approach:
- instrument or hook JavaFX startup entrypoints that are stable enough for the demo support matrix
- once JavaFX is live, enumerate windows/stages from the agent side
- construct `ReflectiveJavaFxTarget` instances from discovered scenes
- register those targets internally in the agent registry

Rationale:
- This removes the biggest remaining app-level OmniUI dependency.
- It aligns with the product claim that OmniUI can automate an unmodified JavaFX app.
- It preserves the current reflective access strategy, which already avoids hard-binding to a specific JavaFX SDK type surface.

Alternatives considered:
- Use a service API that the app calls manually after startup: rejected because it is still invasive.
- Require FXML/controller annotations for discovery: rejected because that is app-specific and not generally applicable.

### 4. Keep the existing HTTP JSON protocol, but make agent availability explicit

The Python client contract should stay HTTP JSON over loopback. What changes is how the app becomes agent-enabled:
- plain launch: no agent, no port
- agent-enabled launch: JVM started with `-javaagent`, agent starts the HTTP server, Python can connect

Rationale:
- This preserves the working Phase 1 client and contract tests.
- It keeps the integration change focused on runtime ownership rather than changing the protocol at the same time.
- It makes failure cases easier to explain: if no agent is injected, the client cannot connect.

Alternatives considered:
- Change protocol and injection model at once: rejected because it adds too many moving parts to one change.

### 5. Remove demo-target fallback registration as the default runtime behavior

`TargetRegistry` currently contains a built-in `DemoJavaFxTarget` fallback and default app-name behavior. The new design should stop treating that as the standard runtime path.

Expected direction:
- demo fallback remains test-only or explicitly opt-in
- real runtime registration comes from discovered JavaFX targets
- unresolved target lookup should fail clearly instead of silently routing to a built-in demo target

Rationale:
- Silent fallback hides integration mistakes.
- It makes agent-enabled vs plain-mode behavior ambiguous.
- It weakens confidence that a real app is being automated.

Alternatives considered:
- Keep the demo fallback in production code for convenience: rejected because it confuses diagnostics and masks missing attachment.

### 6. Keep launch ergonomics with wrappers, but drive them through JVM arguments instead of app code branches

Demo scripts and packaged launchers should remain simple, but they should express mode through JVM launch configuration:
- with-agent wrappers add `-javaagent:...`
- plain wrappers launch the app normally

Rationale:
- This gives operators the two modes they want without polluting application code.
- It keeps the README and demo flow understandable.
- It mirrors how a real customer app would be launched in development, CI, or a controlled environment.

Alternatives considered:
- Remove wrapper scripts and require users to type full Java commands: rejected because it makes the demo harder to use and document.

## Risks / Trade-offs

- [JavaFX runtime discovery without app cooperation may be brittle across launch patterns] -> Mitigation: define a narrow support matrix first, starting with the reference demo app and startup-time `-javaagent`.
- [Instrumentation hooks may depend on JavaFX internals that are less stable than app-side registration] -> Mitigation: keep discovery logic isolated behind an internal runtime discovery layer and preserve reflective access where possible.
- [Removing default demo fallback may break existing demo assumptions and tests] -> Mitigation: convert tests and demo scripts to explicitly launch agent-enabled mode and update contract fixtures accordingly.
- [Packaged runtime launchers become more complex because they need to inject `-javaagent`] -> Mitigation: keep dedicated wrapper scripts and document them as the supported operator entrypoints.
- [Future dynamic attach may need different lifecycle handling than `premain`] -> Mitigation: design bootstrap and transport as reusable layers so `agentmain` can be added later without rewriting the target adapter logic.

## Migration Plan

1. Add standard Java agent bootstrap entrypoints in `java-agent`.
2. Refactor server startup so the agent owns HTTP transport lifecycle.
3. Implement agent-side JavaFX runtime discovery for the demo support matrix.
4. Remove embedded OmniUI startup and registration code from `demo/javafx-login-app`.
5. Replace current mode wrappers so `with-agent` uses Java agent injection and `plain` launches without injection.
6. Update Python demos and documentation to assume agent availability depends on launch mode, not app source configuration.
7. Convert or isolate any tests that still depend on `DemoJavaFxTarget` as the default runtime path.

Rollback strategy:
- If agent-side discovery proves unstable, keep the current embedded demo flow on a temporary branch or compatibility path while the Java agent discovery layer is completed.
- Do not merge silent fallback behavior back into the default runtime; failure should stay explicit.

## Open Questions

- Which JavaFX lifecycle hook is the best first discovery anchor for `premain`: `Application.start`, stage/window visibility changes, or toolkit/window enumeration after startup?
- Should the agent expose target selection by app name only, or also by JVM PID / window title once multiple JavaFX windows exist?
- How should the packaged runtime locate the agent jar for `-javaagent` in a way that remains stable after `jlink` packaging?
- Should `agentmain` support be scaffolded in this change even if it is not fully wired yet?
