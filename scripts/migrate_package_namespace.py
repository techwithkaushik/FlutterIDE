#!/usr/bin/env python3
"""Mechanically migrate source packages without rewriting published Maven coordinates."""
from pathlib import Path
import re
import subprocess
import sys

OLD_DOTTED = "com.itsaky.androidide"
NEW_DOTTED = "org.techwithkaushik.flutteride"
OLD_PATH = "com/itsaky/androidide"
NEW_PATH = "org/techwithkaushik/flutteride"

# These are published/composite dependency coordinates, not Java/Kotlin package names.
# Renaming them makes Gradle resolve artifacts that do not exist.
DEPENDENCY_COORDINATE = re.compile(
    r"org\.techwithkaushik\.flutteride\.(?:build|treesitter|gradle):"
)
LEGACY_DEPENDENCY_COORDINATE = re.compile(
    r"com\.itsaky\.androidide\.(?:build|treesitter|gradle):"
)


def tracked_paths():
    raw = subprocess.check_output(["git", "ls-files", "-z"])
    return [p.decode("utf-8") for p in raw.split(b"\0") if p]


def read_text(path):
    try:
        data = path.read_bytes()
        if b"\0" in data:
            return None
        return data.decode("utf-8")
    except (UnicodeDecodeError, OSError):
        return None


def main():
    paths = tracked_paths()
    edited = moved = 0

    for name in paths:
        path = Path(name)
        if not path.is_file():
            continue
        text = read_text(path)
        if text is None:
            continue

        changed = text.replace(OLD_DOTTED, NEW_DOTTED).replace(OLD_PATH, NEW_PATH)
        # Keep dependency coordinates stable even when the surrounding project package
        # is migrated. Gradle artifacts are published under the original group IDs.
        changed = DEPENDENCY_COORDINATE.sub(
            lambda match: match.group(0).replace(NEW_DOTTED, OLD_DOTTED), changed
        )
        # Tree-sitter runtime classes are supplied by a published artifact whose
        # JVM packages still use the upstream namespace. Keep those direct imports
        # stable while migrating our own *.treesitter.api wrappers.
        changed = EXTERNAL_TREESITTER_IMPORT.sub(
            lambda match: match.group(1) + "com.itsaky.androidide.treesitter.", changed
        )
        if changed != text:
            path.write_text(changed, encoding="utf-8", newline="")
            edited += 1

    # Move package directories after text edits. Refuse collisions rather than overwrite.
    for name in sorted(paths, key=lambda p: (p.count("/"), p)):
        if OLD_PATH not in name:
            continue
        source = Path(name)
        target = Path(name.replace(OLD_PATH, NEW_PATH))
        if not source.exists():
            continue
        if target.exists():
            print(f"Refusing to overwrite existing destination: {target}", file=sys.stderr)
            return 2
        target.parent.mkdir(parents=True, exist_ok=True)
        subprocess.check_call(["git", "mv", "--", str(source), str(target)])
        moved += 1

    remaining = []
    for name in tracked_paths():
        # The migration script and documentation must mention the legacy name.
        # Verify executable source/configuration rather than explanatory text.
        if name == "scripts/migrate_package_namespace.py":
            continue
        if name.startswith(("docs/", ".github/")) or Path(name).name.lower() in {
            "readme.md", "license", "license.md", "notice", "notice.md"
        }:
            continue
        path = Path(name)
        if not path.is_file():
            continue
        text = read_text(path)
        if text is None:
            continue
        # Existing dependency group IDs in the version catalog are intentional.
        checked = LEGACY_DEPENDENCY_COORDINATE.sub("external-dependency:", text)
        if OLD_DOTTED in checked or OLD_PATH in checked or OLD_PATH in name:
            remaining.append(name)
    print(f"Package migration: edited {edited} text files; moved {moved} tracked paths.")
    if remaining:
        print("Legacy package references remain:")
        print("\n".join(remaining[:100]))
        return 3
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
