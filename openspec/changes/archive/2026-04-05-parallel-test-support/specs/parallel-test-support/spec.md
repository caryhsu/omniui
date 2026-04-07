## ADDED Requirements

### Requirement: Worker-scoped port assignment
Each pytest-xdist worker SHALL be assigned a unique port derived from its `worker_id` so that concurrent workers do not share a JavaFX app instance.

#### Scenario: Parallel workers get distinct ports
- **WHEN** pytest is run with `--numprocesses 4`
- **THEN** workers `gw0`–`gw3` connect on ports `BASE + 0` through `BASE + 3` respectively

#### Scenario: Non-parallel run uses base port unchanged
- **WHEN** pytest is run without `--numprocesses` (worker_id = "master")
- **THEN** the fixture connects on `BASE_PORT` with no offset applied

### Requirement: Session-scoped fixture launches isolated app per worker
The `omniui_client` fixture SHALL launch a separate JavaFX app instance per xdist worker process, using `OmniUI.launch()` with the worker-assigned port, and SHALL shut it down after the worker's session ends.

#### Scenario: App launched and connected in fixture setup
- **WHEN** the first test in a worker runs
- **THEN** a JavaFX app is started on the worker's port and `OmniUI.connect(port=...)` returns a live client

#### Scenario: App shut down after worker session
- **WHEN** all tests in a worker complete
- **THEN** the JavaFX app launched by that worker is terminated

### Requirement: Parallel-safe tests are stateless
Tests running in parallel SHALL NOT depend on execution order or shared mutable app state; each test SHALL reset any state it changes (e.g. via a reset button or re-launching the app).

#### Scenario: Test resets state it modifies
- **WHEN** a test drags items or modifies UI state
- **THEN** the test calls a reset action before finishing so the next test in the same worker starts from a known state

### Requirement: Documentation covers parallel setup
The project SHALL provide `docs/parallel-testing.md` documenting how to configure pytest-xdist with OmniUI, including fixture pattern, port range recommendation, and CI example.

#### Scenario: Developer reads the guide and can run tests in parallel
- **WHEN** a developer follows `docs/parallel-testing.md`
- **THEN** they can run `pytest --numprocesses auto` and all tests pass without port conflicts

#### Scenario: CI example is provided
- **WHEN** `docs/parallel-testing.md` is opened
- **THEN** it contains a GitHub Actions job snippet showing `pytest --numprocesses auto`
