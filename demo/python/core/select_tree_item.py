from __future__ import annotations

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


def main() -> None:
    client = connect_or_exit()
    selection = client.select("Edge Cluster", id="assetTree")
    if not selection.ok:
        raise SystemExit(f"TreeView select request failed: {selection.trace.details}")
    result = client.verify_text(id="treeStatus", expected="Selected tree item: Edge Cluster")
    if not result.ok:
        raise SystemExit(f"TreeView select failed: {result.value}")
    print("TreeView selection succeeded")


if __name__ == "__main__":
    main()
