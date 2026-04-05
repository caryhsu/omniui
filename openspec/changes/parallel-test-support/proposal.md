## Why

OmniUI tests currently run sequentially against a single app instance, which becomes a bottleneck as the test suite grows. Teams need to run test suites faster in CI and locally by distributing tests across multiple app instances in parallel using pytest-xdist.

## What Changes

- Document how to run OmniUI tests in parallel using `pytest-xdist` with worker-scoped fixtures
- Add a `pytest` fixture helper that assigns each worker a unique port so each test worker connects to its own isolated JavaFX app instance
- Add a documented example test file showing the parallel-safe pattern
- Add a `docs/parallel-testing.md` guide covering setup, fixture design, and CI configuration

## Capabilities

### New Capabilities

- `parallel-test-support`: pytest-xdist integration guide and fixtures enabling multiple OmniUI clients to run concurrently, each against an isolated JavaFX app instance on a unique port

### Modified Capabilities

<!-- No existing spec-level requirements change -->

## Impact

- `omniui/pytest_plugin.py` (or new file): new `omniui_client` fixture variant that is `worker_id`-aware
- `docs/parallel-testing.md`: new documentation page
- `demo/python/` or `tests/`: example showing parallel-safe test structure
- `pyproject.toml`: `pytest-xdist` added as optional dev dependency
