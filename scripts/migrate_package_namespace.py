#!/usr/bin/env python3
"""Mechanically migrate the legacy AndroidIDE package to FlutterIDE's package ID."""
from pathlib import Path
import subprocess
import sys

OLD_DOTTED = "com.itsaky.androidide"
NEW_DOTTED = "org.techwithkaushik.flutteride"
OLD_PATH = "com/itsaky/androidide"
NEW_PATH = "org/techwithkaushik/flutteride"


def tracked_paths():
    raw = subprocess.check_output(["git", "ls-files", "-z"])
    return [p.decode("utf-8") for p in raw.split(b"\0") if p]


def main():
    paths = tracked_paths()
    edited = moved = 0
    for name in paths:
        path = Path(name)
        if not path.is_file():
            continue
        try:
            data = path.read_bytes()
            if b"\0" in data:
                continue
            text = data.decode("utf-8")
        except (UnicodeDecodeError, OSError):
            continue
        changed = text.replace(OLD_DOTTED, NEW_DOTTED).replace(OLD_PATH, NEW_PATH)
        if changed != text:
            path.write_text(changed, encoding="utf-8", newline="")
            edited += 1

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
        path = Path(name)
        if not path.is_file():
            continue
        try:
            data = path.read_bytes()
            if b"\0" in data:
                continue
            text = data.decode("utf-8")
        except (UnicodeDecodeError, OSError):
            continue
        if OLD_DOTTED in text or OLD_PATH in text or OLD_PATH in name:
            remaining.append(name)
    print(f"Package migration: edited {edited} text files; moved {moved} tracked paths.")
    if remaining:
        print("Legacy package references remain:")
        print("\n".join(remaining[:100]))
        return 3
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
