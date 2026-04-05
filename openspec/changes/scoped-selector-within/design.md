## Context

OmniUI currently resolves nodes by searching the entire scene graph. When a selector matches multiple nodes (common in tabbed panels or repeated list rows), it picks the first match. Tests break or produce wrong results when duplicate IDs/labels exist in different containers.

## Goals / Non-Goals

**Goals:**
- Allow test authors to scope all actions inside a container using `with client.within(id="panel"):` syntax
- Pass the scope constraint to the Java agent as part of the selector JSON (`"scope": {...}`)
- Java agent restricts node traversal to the subtree rooted at the scoped container
- No API changes for code that doesn't use `within`

**Non-Goals:**
- Nested `within` blocks (only one level of scoping for now)
- Server-side session scoping (scope is per-request, not sticky)
- Scoped screenshot or discover calls

## Decisions

**Python — context manager approach**

`within()` returns a `_ScopedClient` context manager. On `__enter__`, it returns a thin proxy of `OmniUIClient` that injects `scope` into every `_perform()` call. On `__exit__`, scope is cleared.

```python
with client.within(id="leftPanel") as scoped:
    scoped.click(id="ok")
```

Alternatively, `within` can mutate `self._scope` temporarily:

```python
with client.within(id="leftPanel"):
    client.click(id="ok")   # self._scope is active
```

We choose the **mutating approach** (no proxy object needed, simpler API): `within()` sets `self._scope`, yields, then clears it. `_perform()` includes `scope` in the selector JSON if `self._scope` is not None.

**Java agent — scope field in selector**

Selector JSON currently: `{"id": "ok"}`. With scope: `{"id": "ok", "scope": {"id": "leftPanel"}}`.

`performOnFxThread` first resolves the scope container using the existing `findNode` logic on the full scene graph. Then resolves the target node within the container's subtree only.

**`findNode` extension**

Add a `findNodeInSubtree(root, selector)` variant that walks only descendants of `root`. Reuse existing matching logic.

## Risks / Trade-offs

- If the scope container is not found, the action fails with `reason: scope_not_found`
- Mutating `self._scope` is not thread-safe; document that `within` is for single-threaded test flows
- Scope applies to `_perform()` only; `get_windows`, `screenshot`, `discover` are unaffected
