from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MD_LINK_RE = re.compile(r"\]\(([^)]+\.md)\)")
EXCLUDED_PARTS = {".codex", ".opencode", "__pycache__", "target"}


def _is_excluded(path: Path) -> bool:
    return any(part in EXCLUDED_PARTS for part in path.parts)


def _all_markdown_files() -> list[Path]:
    return sorted(
        path
        for path in ROOT.rglob("*.md")
        if not _is_excluded(path) and path.is_file()
    )


def _english_files(files: list[Path]) -> list[Path]:
    return [path for path in files if not path.name.endswith(".zh-TW.md")]


def _zh_tw_pair(path: Path) -> Path:
    return path.with_name(f"{path.stem}.zh-TW.md")


def _missing_translations(files: list[Path]) -> list[Path]:
    missing: list[Path] = []
    for path in _english_files(files):
        if path.name == "README.md" or path.parent.name == "openspec":
            pass
        pair = _zh_tw_pair(path)
        if not pair.exists():
            missing.append(path)
    return missing


def _bad_zh_links(files: list[Path]) -> list[tuple[Path, int, str]]:
    issues: list[tuple[Path, int, str]] = []
    for path in files:
        if not path.name.endswith(".zh-TW.md"):
            continue
        lines = path.read_text(encoding="utf-8").splitlines()
        for line_no, line in enumerate(lines, start=1):
            for match in MD_LINK_RE.finditer(line):
                target = match.group(1)
                if target.endswith(".zh-TW.md"):
                    continue
                if target.startswith("http://") or target.startswith("https://") or target.startswith("#"):
                    continue
                issues.append((path, line_no, target))
    return issues


def main() -> int:
    files = _all_markdown_files()
    missing = _missing_translations(files)
    bad_links = _bad_zh_links(files)

    if not missing and not bad_links:
        print("OK: markdown i18n checks passed")
        return 0

    if missing:
        print("Missing zh-TW translations:")
        for path in missing:
            print(f"- {path.relative_to(ROOT)}")

    if bad_links:
        print("zh-TW files linking to non-zh-TW markdown:")
        for path, line_no, target in bad_links:
            print(f"- {path.relative_to(ROOT)}:{line_no} -> {target}")

    return 1


if __name__ == "__main__":
    sys.exit(main())
