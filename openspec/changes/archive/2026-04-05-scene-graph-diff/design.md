## Context

`OmniUIClient.get_nodes()` already returns the full scene graph as a list of dicts (via the Java agent's `/discover` endpoint). We need higher-level wrappers to make snapshot/diff ergonomic in tests.

## Goals / Non-Goals

**Goals:**
- `snapshot()` captures the current node list with a timestamp
- `diff(before, after)` computes added/removed/changed nodes between two snapshots
- Nodes are identified by their `fxId` (handle as fallback)
- "changed" means same identity but different `text`, `enabled`, `visible`, or `value`

**Non-Goals:**
- Deep structural diff (child order, nesting changes)
- Real-time watching / reactive diffs
- Java-side diffing

## Decisions

**Identity key**: use `fxId` when non-empty, otherwise `handle`. This is stable across snapshots for the same app session.

**`UISnapshot` dataclass**:
```python
@dataclass
class UISnapshot:
    nodes: list[dict[str, Any]]
    timestamp: float   # time.time()
```

**`UIDiff` dataclass**:
```python
@dataclass
class UIDiff:
    added: list[dict[str, Any]]     # nodes in after but not before
    removed: list[dict[str, Any]]   # nodes in before but not after
    changed: list[dict[str, Any]]   # nodes present in both but with different attributes
```

**`changed` entry format**: `{"before": {...node...}, "after": {...node...}}`

**`diff()` algorithm** (pure Python, no Java):
1. Build `before_map = {identity(n): n for n in before.nodes}`
2. Build `after_map = {identity(n): n for n in after.nodes}`
3. `added = [n for k, n in after_map.items() if k not in before_map]`
4. `removed = [n for k, n in before_map.items() if k not in after_map]`
5. `changed = [{"before": before_map[k], "after": after_map[k]} for k in before_map if k in after_map and _attrs_differ(before_map[k], after_map[k])]`

**Compared attributes**: `text`, `enabled`, `visible`, `value`, `nodeType`

## Risks / Trade-offs

- `fxId` may be empty for unlabelled nodes — fallback to `handle` which is session-stable but not deterministic across restarts
- Large scene graphs may produce large snapshots; no pagination for now
