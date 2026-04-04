## Context

`resolve(JsonObject selector, DiscoverySnapshot snapshot)` in `ReflectiveJavaFxTarget` currently handles three selector shapes: `id`, `text+type`, and fallback (returns empty). Each uses `.findFirst()` — there is no way to pick the Nth match.

Adding `index=N` requires only modifying the filter pipeline: collect all matches satisfying existing criteria, then skip N entries.

## Goals / Non-Goals

**Goals:**
- `index=N` (0-based) selects the Nth node among all nodes that match the other selector fields
- Works with every existing selector combination: `type=+index=`, `text=+type=+index=`, `id=+index=` (id should be unique but index still applies)
- Default `index=0` (first match) — existing behaviour unchanged

**Non-Goals:**
- Negative index (-1 for last)
- Named index ("last", "first")
- Out-of-bounds returns `selector_not_found` (same as today)

## Decisions

### D1: 0-based index

Consistent with Java/Python array conventions and Playwright's `.nth(N)`.

### D2: index is extracted before the existing filter branches

Rather than duplicating logic in each `if` branch, refactor `resolve()` to: (1) extract and remove `index` from the selector copy, (2) run all existing filter branches but collect ALL matches (not just first), (3) return `matches.get(index)` if in range.

Actually, simpler: keep existing branches, but replace `.findFirst()` with `.skip(index).findFirst()` in each branch. Only three branches to touch.

### D3: No Python changes

`index=2` arrives in `**selector` and passes through `_perform()` into the JSON payload unchanged. Zero Python code changes needed.

## Risks / Trade-offs

- **id + index**: If two nodes somehow share the same id, `index=1` picks the second one. This is a power-user escape hatch, not an encouraged pattern.
- **Snapshot ordering**: `index=` relies on snapshot walk order (depth-first, scene tree). This is deterministic for a given layout but may shift if the UI structure changes.
