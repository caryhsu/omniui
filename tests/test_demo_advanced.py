from __future__ import annotations

import unittest
from unittest.mock import patch

from demo.python.advanced.discover_advanced_controls import filter_advanced_nodes
from omniui.core.models import ActionResult, ActionTrace, Selector

import demo.python.core.select_combo_role as select_combo_role
import demo.python.core.select_list_item as select_list_item
import demo.python.core.select_table_row as select_table_row
import demo.python.core.select_tree_item as select_tree_item


class _FakeAdvancedClient:
    def __init__(self) -> None:
        self.last_select: tuple[str, dict[str, object]] | None = None

    def select(self, value: str, **selector):
        self.last_select = (value, selector)
        return ActionResult(
            ok=True,
            trace=ActionTrace(
                selector=Selector(id=str(selector.get("id")) if selector.get("id") else None),
                attempted_tiers=["javafx"],
                resolved_tier="javafx",
            ),
            value=value,
        )

    def verify_text(self, **selector):
        expected = selector["expected"]
        return ActionResult(
            ok=True,
            trace=ActionTrace(
                selector=Selector(id=str(selector.get("id")) if selector.get("id") else None),
                attempted_tiers=["javafx"],
                resolved_tier="javafx",
            ),
            value={"actual": expected, "expected": expected, "matches": True},
        )


class AdvancedDemoTests(unittest.TestCase):
    def test_filter_advanced_nodes_keeps_expected_advanced_controls(self) -> None:
        nodes = [
            {"fxId": "username", "nodeType": "TextField", "text": ""},
            {"fxId": "roleCombo", "nodeType": "ComboBox", "text": ""},
            {"fxId": "serverList", "nodeType": "ListView", "text": ""},
            {"fxId": "assetTree", "nodeType": "TreeView", "text": ""},
            {"fxId": "userTable", "nodeType": "TableView", "text": ""},
            {"fxId": "settingsGrid", "nodeType": "GridPane", "text": ""},
        ]

        result = filter_advanced_nodes(nodes)
        result_ids = {node["fxId"] for node in result}

        self.assertEqual(
            result_ids,
            {"roleCombo", "serverList", "assetTree", "userTable", "settingsGrid"},
        )

    def test_filter_advanced_nodes_keeps_section_headings(self) -> None:
        nodes = [
            {"fxId": "selectionSection", "nodeType": "VBox", "text": None},
            {"fxId": None, "nodeType": "Label", "text": "Hierarchy Controls"},
            {"fxId": None, "nodeType": "Label", "text": "Login Flow"},
        ]

        result = filter_advanced_nodes(nodes)

        self.assertEqual(len(result), 2)
        self.assertEqual(result[0]["fxId"], "selectionSection")
        self.assertEqual(result[1]["text"], "Hierarchy Controls")

    @patch("demo.python.core.select_combo_role.connect_or_exit")
    @patch("demo.python.core.select_list_item.connect_or_exit")
    @patch("demo.python.core.select_tree_item.connect_or_exit")
    @patch("demo.python.core.select_table_row.connect_or_exit")
    def test_advanced_interaction_scripts_run(self, mock_table, mock_tree, mock_list, mock_combo) -> None:
        combo_client = _FakeAdvancedClient()
        list_client = _FakeAdvancedClient()
        tree_client = _FakeAdvancedClient()
        table_client = _FakeAdvancedClient()
        mock_combo.return_value = combo_client
        mock_list.return_value = list_client
        mock_tree.return_value = tree_client
        mock_table.return_value = table_client

        select_combo_role.main()
        select_list_item.main()
        select_tree_item.main()
        select_table_row.main()

        self.assertEqual(combo_client.last_select, ("Operator", {"id": "roleCombo"}))
        self.assertEqual(list_client.last_select, ("beta-node", {"id": "serverList"}))
        self.assertEqual(tree_client.last_select, ("Edge Cluster", {"id": "assetTree"}))
        self.assertEqual(table_client.last_select, ("Noah", {"id": "userTable", "column": "name"}))


if __name__ == "__main__":
    unittest.main()
