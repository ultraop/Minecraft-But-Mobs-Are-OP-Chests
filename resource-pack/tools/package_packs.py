#!/usr/bin/env python3
"""Build versioned companion datapack and resource-pack ZIP files."""

from __future__ import annotations

import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DIST = ROOT / "build" / "distributions"


def zip_tree(source: Path, output: Path, include_paths: set[str]) -> None:
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for relative in include_paths:
            candidate = source / relative
            if candidate.is_file():
                archive.write(candidate, relative)
            elif candidate.is_dir():
                for path in sorted(candidate.rglob("*")):
                    if path.is_file():
                        archive.write(path, path.relative_to(source).as_posix())


def main() -> None:
    resource_root = ROOT / "resource-pack"
    datapack_root = ROOT / "datapack"
    generator = resource_root / "tools" / "generate_assets.py"

    import runpy
    runpy.run_path(str(generator), run_name="__main__")

    zip_tree(
        resource_root,
        DIST / "UltraOP-MobChests-ResourcePack.zip",
        {"pack.mcmeta", "assets"},
    )
    zip_tree(
        datapack_root,
        DIST / "UltraOP-MobChests-Datapack.zip",
        {"pack.mcmeta", "data"},
    )
    print(f"Pack ZIPs created in {DIST}")


if __name__ == "__main__":
    main()
