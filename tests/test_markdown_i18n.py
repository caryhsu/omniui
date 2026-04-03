from __future__ import annotations

import unittest

import scripts.check_markdown_i18n as check_markdown_i18n


class MarkdownI18nTests(unittest.TestCase):
    def test_repo_markdown_i18n_rules(self) -> None:
        files = check_markdown_i18n._all_markdown_files()

        self.assertEqual(check_markdown_i18n._missing_translations(files), [])
        self.assertEqual(check_markdown_i18n._bad_zh_links(files), [])


if __name__ == "__main__":
    unittest.main()
