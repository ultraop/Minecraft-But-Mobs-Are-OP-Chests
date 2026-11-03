#!/usr/bin/env python3
"""Validate and package the Minecraft 26.3 Liquid Gold resource pack."""
from __future__ import annotations

import json
import struct
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / "build" / "MobChests-Liquid-Gold-ResourcePack.zip"


def validate_png(path: Path) -> None:
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise SystemExit(f"Invalid PNG texture: {path}")
    width, height = struct.unpack(">II", data[16:24])
    if width <= 0 or height <= 0:
        raise SystemExit(f"Invalid PNG dimensions: {path}")


def main() -> None:
    meta_path = ROOT / "pack.mcmeta"
    if not meta_path.is_file():
        raise SystemExit("pack.mcmeta is missing")
    meta = json.loads(meta_path.read_text(encoding="utf-8"))
    pack = meta.get("pack", {})
    if pack.get("min_format") != [97, 1] or pack.get("max_format") != [97, 1]:
        raise SystemExit("Resource pack must target Minecraft 26.3 format 97.1")

    model_files = sorted((ROOT / "assets" / "mobchests").rglob("*.json"))
    if len(model_files) != 18:
        raise SystemExit(f"Expected 18 item definition/model JSON files, found {len(model_files)}")
    for path in model_files:
        json.loads(path.read_text(encoding="utf-8"))

    bucket_texture = ROOT / "assets/mobchests/textures/item/liquid_gold_bucket.png"
    flow_texture = ROOT / "assets/mobchests/textures/item/liquid_gold_flow.png"
    validate_png(bucket_texture)
    validate_png(flow_texture)

    animation_path = flow_texture.with_suffix(".png.mcmeta")
    animation = json.loads(animation_path.read_text(encoding="utf-8"))
    if animation.get("animation", {}).get("frametime", 0) <= 0:
        raise SystemExit("Liquid Gold fluid texture animation metadata is invalid")

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(OUTPUT, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path in sorted(ROOT.rglob("*")):
            if not path.is_file() or "build" in path.relative_to(ROOT).parts:
                continue
            archive.write(path, path.relative_to(ROOT).as_posix())
        if archive.testzip() is not None:
            raise SystemExit("Resource pack ZIP integrity check failed")

    with zipfile.ZipFile(OUTPUT) as archive:
        names = set(archive.namelist())
        if "pack.mcmeta" not in names:
            raise SystemExit("pack.mcmeta is not at the ZIP root")
        if "assets/mobchests/items/liquid_gold_bucket.json" not in names:
            raise SystemExit("Liquid Gold bucket item model is missing")
        if "assets/mobchests/items/liquid_gold_flow_7.json" not in names:
            raise SystemExit("Liquid Gold flow level models are incomplete")

    print(f"Resource pack OK: {OUTPUT} ({OUTPUT.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
