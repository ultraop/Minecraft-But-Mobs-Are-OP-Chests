#!/usr/bin/env python3
"""Generate and validate the Minecraft 26.3 MobChests resource pack."""
from __future__ import annotations

import json
import struct
import zlib
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / "assets" / "mobchests"
OUTPUT = ROOT / "build" / "MobChests-Liquid-Gold-ResourcePack.zip"

FOODS = {
    "golden_potato": "potato",
    "golden_baked_potato": "baked_potato",
    "golden_steak": "steak",
    "golden_porkchop": "porkchop",
    "golden_chicken": "drumstick",
    "golden_mutton": "mutton",
    "golden_rabbit": "rabbit",
    "golden_bread": "bread",
    "golden_carrot": "carrot",
    "golden_cod": "fish",
    "golden_salmon": "salmon",
    "golden_pumpkin_pie": "pie",
}

PALETTE = {
    "edge": (112, 68, 10, 255),
    "deep": (168, 108, 15, 255),
    "gold": (224, 164, 32, 255),
    "light": (255, 211, 72, 255),
    "shine": (255, 242, 168, 255),
}


def write_png(path: Path, pixels: list[list[tuple[int, int, int, int]]]) -> None:
    height = len(pixels)
    width = len(pixels[0])
    raw = b"".join(b"\x00" + bytes(channel for pixel in row for channel in pixel) for row in pixels)

    def chunk(kind: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xffffffff)

    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", struct.pack(">2I5B", width, height, 8, 6, 0, 0, 0))
           + chunk(b"IDAT", zlib.compress(raw, 9))
           + chunk(b"IEND", b""))
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def bucket_texture() -> list[list[tuple[int, int, int, int]]]:
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            handle = (y in (1, 2) and 5 <= x <= 10) or (y == 3 and 4 <= x <= 11)
            if 4 <= y <= 13:
                inset = (y - 4) // 5
                inside = 3 + inset <= x <= 12 - inset
            else:
                inside = False
            rim = y == 3 and 3 <= x <= 12
            bottom = y == 14 and 5 <= x <= 10
            if not (handle or inside or rim or bottom):
                row.append((0, 0, 0, 0))
                continue
            edge = (not inside or x in (3 + max(0, (y - 4) // 5), 12 - max(0, (y - 4) // 5)))
            if handle or rim or bottom or edge:
                color = PALETTE["edge"]
            elif y == 4 or (x + y) % 7 == 0:
                color = PALETTE["shine"]
            elif (x + y) % 3 == 0:
                color = PALETTE["deep"]
            elif y % 3 == 0:
                color = PALETTE["light"]
            else:
                color = PALETTE["gold"]
            row.append(color)
        rows.append(row)
    return rows


def inside_ellipse(x: float, y: float, cx: float, cy: float, rx: float, ry: float) -> bool:
    return ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 <= 1.0


def food_mask(kind: str, x: int, y: int) -> bool:
    px, py = x + 0.5, y + 0.5
    if kind in ("potato", "baked_potato"):
        return inside_ellipse(px + (py - 8) * 0.10, py, 8, 8, 5.6, 4.2)
    if kind in ("steak", "porkchop", "mutton", "rabbit"):
        body = inside_ellipse(px, py, 7.5, 8, 5.8, 4.3)
        notch = kind == "rabbit" and 3 <= x <= 5 and y <= 4
        return body and not notch
    if kind == "drumstick":
        meat = inside_ellipse(px, py, 6.5, 7, 4.7, 4.1)
        bone = (10 <= x <= 13 and 8 <= y <= 11) or (12 <= x <= 14 and 7 <= y <= 8)
        return meat or bone
    if kind == "bread":
        return inside_ellipse(px, py, 8, 8, 6.0, 3.8) and not (x < 2 and y < 5)
    if kind == "carrot":
        body = 4 <= y <= 14 and abs(px - (8 + (y - 8) * 0.22)) <= (3.5 - (y - 4) * 0.19)
        leaves = y <= 5 and ((x in (4, 5, 6) and y <= 3) or (8 <= x <= 10 and y <= 4) or (11 <= x <= 12 and y <= 2))
        return body or leaves
    if kind in ("fish", "salmon"):
        body = inside_ellipse(px, py, 7.5, 8, 5.1, 3.0)
        tail = (x <= 3 and abs(py - 8) <= (x + 1) * 0.75)
        fin = y in (4, 11) and 7 <= x <= 10
        return body or tail or fin
    if kind == "pie":
        return 2 <= x <= 14 and 3 <= y <= 13 and x >= 14 - (y - 3) * 0.8 and x >= 2 + (y - 3) * 0.15
    return inside_ellipse(px, py, 8, 8, 5.5, 4.0)


def food_texture(kind: str) -> list[list[tuple[int, int, int, int]]]:
    mask = [[food_mask(kind, x, y) for x in range(16)] for y in range(16)]
    rows = []
    for y in range(16):
        row = []
        for x in range(16):
            if not mask[y][x]:
                row.append((0, 0, 0, 0))
                continue
            edge = any(
                nx < 0 or nx >= 16 or ny < 0 or ny >= 16 or not mask[ny][nx]
                for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))
            )
            if edge:
                color = PALETTE["edge"]
            elif y <= 5 and (x + y) % 4 == 0:
                color = PALETTE["shine"]
            elif (x * 3 + y * 5) % 7 == 0:
                color = PALETTE["deep"]
            elif (x + y) % 3 == 0:
                color = PALETTE["light"]
            else:
                color = PALETTE["gold"]
            row.append(color)
        rows.append(row)
    return rows


def fluid_texture() -> list[list[tuple[int, int, int, int]]]:
    colors = [PALETTE["deep"], PALETTE["gold"], PALETTE["light"], PALETTE["shine"]]
    rows = []
    for frame in range(4):
        for y in range(16):
            row = []
            for x in range(16):
                band = (x * 3 + y * 2 + frame * 4 + ((y // 3) % 2) * 2) % 13
                if band in (0, 1):
                    color = colors[0]
                elif band in (2, 3, 4, 5):
                    color = colors[1]
                elif band in (6, 7, 8, 9, 10):
                    color = colors[2]
                else:
                    color = colors[3]
                row.append(color)  # Fully opaque: no vanilla water tint can show through.
            rows.append(row)
    return rows


def write_item_model(item_id: str) -> None:
    item_dir = ASSETS / "items"
    model_dir = ASSETS / "models" / "item"
    item_dir.mkdir(parents=True, exist_ok=True)
    model_dir.mkdir(parents=True, exist_ok=True)
    (item_dir / f"{item_id}.json").write_text(json.dumps({
        "model": {"type": "minecraft:model", "model": f"mobchests:item/{item_id}"}
    }, indent=2) + "\n", encoding="utf-8")
    (model_dir / f"{item_id}.json").write_text(json.dumps({
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"mobchests:item/{item_id}"}
    }, indent=2) + "\n", encoding="utf-8")


def generate_assets() -> None:
    texture_dir = ASSETS / "textures" / "item"
    write_png(texture_dir / "liquid_gold_bucket.png", bucket_texture())
    write_png(texture_dir / "liquid_gold_flow.png", fluid_texture())
    for item_id, kind in FOODS.items():
        write_png(texture_dir / f"{item_id}.png", food_texture(kind))
        write_item_model(item_id)

    (texture_dir / "liquid_gold_flow.png.mcmeta").write_text(json.dumps({
        "animation": {"frametime": 4, "interpolate": True}
    }, indent=2) + "\n", encoding="utf-8")

    # The full-block ItemDisplay model must be opaque so underlying WATER
    # never leaks through as blue pixels or seams.
    for model_path in (ASSETS / "models" / "item").glob("liquid_gold_flow_*.json"):
        model = json.loads(model_path.read_text(encoding="utf-8"))
        model["render_type"] = "minecraft:solid"
        model_path.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")


def validate_png(path: Path) -> None:
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise SystemExit(f"Invalid PNG texture: {path}")
    width, height = struct.unpack(">II", data[16:24])
    if width <= 0 or height <= 0:
        raise SystemExit(f"Invalid PNG dimensions: {path}")


def main() -> None:
    generate_assets()
    meta_path = ROOT / "pack.mcmeta"
    if not meta_path.is_file():
        raise SystemExit("pack.mcmeta is missing")
    meta = json.loads(meta_path.read_text(encoding="utf-8"))
    pack = meta.get("pack", {})
    if pack.get("min_format") != [97, 1] or pack.get("max_format") != [97, 1]:
        raise SystemExit("Resource pack must target Minecraft 26.3 format 97.1")

    model_files = sorted(ASSETS.rglob("*.json"))
    expected = 18 + (len(FOODS) * 2)
    if len(model_files) != expected:
        raise SystemExit(f"Expected {expected} item definition/model JSON files, found {len(model_files)}")
    for path in model_files:
        json.loads(path.read_text(encoding="utf-8"))

    texture_dir = ASSETS / "textures" / "item"
    for name in ["liquid_gold_bucket.png", "liquid_gold_flow.png"] + [f"{item_id}.png" for item_id in FOODS]:
        validate_png(texture_dir / name)

    animation = json.loads((texture_dir / "liquid_gold_flow.png.mcmeta").read_text(encoding="utf-8"))
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
        if "pack.mcmeta" not in names or "assets/mobchests/items/liquid_gold_bucket.json" not in names:
            raise SystemExit("Resource pack is missing its root metadata or Liquid Gold item definition")
        if "assets/mobchests/items/liquid_gold_flow_7.json" not in names:
            raise SystemExit("Liquid Gold flow level models are incomplete")
        for item_id in FOODS:
            if f"assets/mobchests/items/{item_id}.json" not in names:
                raise SystemExit(f"Missing Golden Food model: {item_id}")

    print(f"Resource pack OK: {OUTPUT} ({OUTPUT.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
