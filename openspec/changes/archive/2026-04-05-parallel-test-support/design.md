## Context

OmniUI's pytest fixture (`omniui_client` in `conftest.py`) currently creates a single `OmniUI` connection per test session. Tests share one app instance and run sequentially. As the suite grows (currently 153+ tests, more coming), this limits CI throughput.

pytest-xdist supports worker-based parallelism: each `--numprocesses N` worker is a separate Python process with its own fixture scope. The challenge is that each worker needs its own JavaFX app instance on a distinct port.

## Goals / Non-Goals

**Goals:**
- Each xdist worker connects to its own isolated JavaFX app instance
- Port assignment is deterministic and collision-free across workers
- Existing sequential tests continue to work unchanged
- Document the pattern clearly for test authors
- Provide a working example

**Non-Goals:**
- Shared-state tests (tests that depend on state from a previous test) are not supported in parallel mode — this is intentional
- Automatic test discovery / parallelisation without any fixture change
- Support for non-pytest test runners

## Decisions

### D1: Port assignment via `worker_id`

**Decision:** Derive each worker's port from a base port + worker index.
- xdist exposes `worker_id` as `"gw0"`, `"gw1"`, ... (or `"master"` for non-parallel runs)
- Port = `BASE_PORT + int(worker_id[2:])` when parallel; use `BASE_PORT` when `master`

**Alternatives considered:**
- `find_free_port()` per worker — race condition risk when workers start simultaneously
- Env-var injection — more complex, requires xdist hooks

### D2: Fixture scope = `session` per worker

**Decision:** Keep fixture scope `session` (one app per worker process lifetime). xdist runs each worker as a separate process, so `session`-scoped fixtures are naturally isolated.

**Alternatives considered:**
- `function`-scoped launch — too slow; spawning a JVM per test is prohibitive

### D3: App launch in fixture, not in conftest directly

**Decision:** The parallel-safe fixture wraps `OmniUI.launch()` using the worker-derived port, and tears down via context manager. This keeps the pattern identical to the existing single-process fixture.

### D4: Documentation-first delivery

**Decision:** Deliver as `docs/parallel-testing.md` + example conftest snippet + optional `conftest_parallel.py` example file. No changes to the core library are needed because `OmniUI.launch()` already accepts a `port=` argument.

## Risks / Trade-offs

- **Port collision on shared CI hosts** → Mitigation: use a port range far from defaults (e.g. 49000–49099); document that teams should not hardcode ports
- **JVM startup time** → Workers stagger naturally due to process startup; no extra mitigation needed
- **Tests that mutate shared app state** → These will fail non-deterministically in parallel mode; document that tests must be stateless or use `resetBtn`-style teardown

## Open Questions

- Should we ship an `omniui_parallel_client` fixture in `conftest.py` as a built-in example, or only document the pattern? → Lean toward adding a documented example `conftest` snippet in docs only; avoids coupling core fixtures to xdist.
