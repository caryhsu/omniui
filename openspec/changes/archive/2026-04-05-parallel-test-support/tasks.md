## 1. Dependencies

- [x] 1.1 Add `pytest-xdist` as optional dev dependency in `pyproject.toml` (`[project.optional-dependencies] dev`)

## 2. Fixture

- [x] 2.1 Update `conftest.py`: read `worker_id` from `pytest` request fixture and compute worker port (`BASE_PORT + worker_index`)
- [x] 2.2 Update `omniui_client` fixture (or add `omniui_parallel_client` variant) to call `OmniUI.launch()` with the worker-derived port and tear down on session end
- [x] 2.3 Ensure non-parallel path (`worker_id == "master"`) falls back to existing behaviour unchanged

## 3. Example

- [x] 3.1 Create `tests/conftest_parallel_example.py` (or update `conftest.py`) showing the full worker-aware fixture pattern with inline comments
- [x] 3.2 Add a short parallel-safe test file `tests/test_parallel_example.py` (2–3 tests against drag-app) demonstrating stateless usage with reset

## 4. Documentation

- [x] 4.1 Create `docs/parallel-testing.md` covering: install xdist, fixture design, port range recommendation, run command
- [x] 4.2 Add GitHub Actions CI snippet (job with `pytest --numprocesses auto`) to `docs/parallel-testing.md`
- [x] 4.3 Add link to `docs/parallel-testing.md` from `README.md` Docs section

## 5. ROADMAP

- [x] 5.1 Mark **Parallel test support** as `[x]` done in `ROADMAP.md` and `ROADMAP.zh-TW.md`
