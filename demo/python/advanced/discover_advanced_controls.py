from __future__ import annotations

import json

if __package__ in (None, ""):
    import _bootstrap  # type: ignore # noqa: F401
    from _runtime import connect_or_exit  # type: ignore
else:
    from . import _bootstrap  # noqa: F401
    from ._runtime import connect_or_exit


ADVANCED_IDS = {
    "roleCombo",
    "roleStatus",
    "selectionSection",
    "selectionSectionTitle",
    "serverList",
    "listStatus",
    "hierarchySection",
    "hierarchySectionTitle",
    "assetTree",
    "treeStatus",
    "tableSection",
    "tableSectionTitle",
    "userTable",
    "tableStatus",
    "gridSection",
    "gridSectionTitle",
    "settingsGrid",
    "gridTitle",
    "regionField",
}


def filter_advanced_nodes(nodes: list[dict[str, object]]) -> list[dict[str, object]]:
    return [
        node for node in nodes
        if node.get("fxId") in ADVANCED_IDS or "Section" in (node.get("fxId") or "") or "Controls" in (node.get("text") or "")
    ]


def main() -> None:
    client = connect_or_exit()
    nodes = client.get_nodes()
    advanced_nodes = filter_advanced_nodes(nodes)
    print(json.dumps(advanced_nodes, indent=2))


if __name__ == "__main__":
    main()
