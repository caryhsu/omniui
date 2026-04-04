## Context

OmniUI's existing `select()` action calls `setSelectedItem()` / `scrollTo()` — single-item only. JavaFX `MultipleSelectionModel` exposes `selectIndices()`, `selectAll()`, and `getSelectedItems()` which enable full multi-select control via reflection. The `serverList` ListView in the demo already uses an `ObservableList` of strings and has `SelectionMode.SINGLE` today.

## Goals / Non-Goals

**Goals:**
- `select_multiple(values=[...])` — clear existing selection, then select each named value by finding its index in the list model and calling `selectIndices(int...)`
- `get_selected_items()` — return `List<String>` of currently selected values from `getSelectionModel().getSelectedItems()`
- Works for `ListView` and `TableView` (both use `MultipleSelectionModel`)

**Non-Goals:**
- Range selection by index (e.g. shift-click rows 3–7) — deferred
- Selecting by cell value in TableView (only row-level by `toString()` match)
- Deselecting individual items without clearing all

## Decisions

### D1 — Resolve values to indices, call `selectIndices(int...)`

`MultipleSelectionModel.selectIndices(int first, int... remaining)` is the cleanest API. We first call `clearSelection()`, then build the int[] of matching indices and call `selectIndices`.

**Alternatives considered:**
- Calling `select(value)` in a loop — only works if `MULTIPLE` mode is set; also each call dispatches a change event; `selectIndices` is atomic
- Using `setSelectedIndices(ObservableList)` — not public API in all JavaFX versions

### D2 — Value matching uses `toString()` on list items

We call `getItems()` → iterate with index → compare `item.toString()` to the requested value string. Consistent with existing `select()` behaviour.

### D3 — `get_selected_items` returns `List<String>` via `toString()`

`getSelectionModel().getSelectedItems()` returns `ObservableList<T>`. We map each item to `.toString()` and return as `ArrayList<String>`.

### D4 — No Java rebuild path changes

`select_multiple` and `get_selected_items` are new cases in the existing `performOnFxThread` switch. Selector resolution is unchanged (must resolve to a ListView or TableView node).

## Risks / Trade-offs

- [SelectionMode not MULTIPLE] If the control has `SelectionMode.SINGLE`, `selectIndices` with multiple values will only keep the last. **Mitigation:** Document requirement; no runtime guard (consistent with JavaFX behaviour).
- [Value not found] If a value string doesn't match any item, that index is skipped silently. **Mitigation:** Return the actual selected items so the caller can assert.

## Migration Plan

1. Add cases to Java agent
2. Rebuild jlink
3. Add Python methods
4. Enable MULTIPLE on `serverList` in demo app + demo script
5. Update docs
