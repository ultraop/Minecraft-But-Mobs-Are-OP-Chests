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
            # A recognizable bucket handle loop and a tapered metal pail.
            handle = ((y == 1 and 6 <= x <= 9)
                      or (y in (2, 3) and x in (5, 10)))
            rim = y == 4 and 3 <= x <= 12
            if 5 <= y <= 13:
                inset = (y - 5) // 5
                left, right = 3 + inset, 12 - inset
                inside = left <= x <= right
            else:
                left, right, inside = 0, 0, False
            bottom = y == 14 and 5 <= x <= 10
            if not (handle or rim or inside or bottom):
                row.append((0, 0, 0, 0))
                continue

            edge = inside and (x == left or x == right)
            if handle or rim or bottom or edge:
                color = PALETTE["edge"]
            elif y == 5 or (x + y) % 7 == 0:
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
    if kind == "potato":
        return inside_ellipse(px + (py - 8) * 0.10, py, 8, 8, 5.5, 4.0)
    if kind == "baked_potato":
        return inside_ellipse(px + (py - 8) * 0.10, py, 8, 8, 5.2, 3.8)
    if kind == "steak":
        rows = {4: (6, 9), 5: (4, 12), 6: (3, 13), 7: (2, 13),
                8: (2, 14), 9: (3, 13), 10: (4, 12), 11: (6, 10)}
        return y in rows and rows[y][0] <= x <= rows[y][1]
    if kind == "porkchop":
        meat = inside_ellipse(px, py, 7, 8, 4.5, 4.3)
        bone = (11 <= x <= 13 and 5 <= y <= 10) or (12 <= x <= 14 and y in (5, 10))
        return meat or bone
    if kind == "mutton":
        body = inside_ellipse(px, py, 8, 8, 5.4, 4.1)
        notch = 2 <= x <= 4 and y in (5, 6)
        return body and not notch
    if kind in ("drumstick", "rabbit"):
        cx = 6.2 if kind == "drumstick" else 6.0
        meat = inside_ellipse(px, py, cx, 7.0, 4.2 if kind == "drumstick" else 3.6, 4.0 if kind == "drumstick" else 3.3)
        bone = 10 <= x <= 12 and 8 <= y <= 10
        bone_tip = (12 <= x <= 14 and y in (7, 10)) or (13 <= x <= 14 and 8 <= y <= 9)
        return meat or bone or bone_tip
    if kind == "bread":
        rows = {4: (6, 9), 5: (4, 11), 6: (3, 12), 7: (3, 12),
                8: (3, 12), 9: (3, 12), 10: (4, 11), 11: (5, 10)}
        return y in rows and rows[y][0] <= x <= rows[y][1]
    if kind == "carrot":
        leaves = (y <= 4 and ((2 <= x <= 5 and y <= 2)
                              or (5 <= x <= 8 and y <= 3)
                              or (8 <= x <= 11 and y <= 2)))
        if 4 <= y <= 14:
            center = 6.0 + (y - 4) * 0.35
            half_width = max(0.7, 3.2 - (y - 4) * 0.23)
            return leaves or abs(px - center) <= half_width
        return leaves
    if kind in ("fish", "salmon"):
        body = inside_ellipse(px, py, 8.5, 8, 4.8 if kind == "salmon" else 4.3, 3.2 if kind == "salmon" else 2.8)
        tail = 2 <= x <= 4 and abs(py - 8) <= (x - 1) * 0.8
        fin = (y in (4, 11) and 7 <= x <= 10) if kind == "salmon" else (y in (5, 10) and 7 <= x <= 9)
        return body or tail or fin
    if kind == "pie":
        if not 3 <= y <= 13:
            return False
        left = 3 + int((y - 3) * 0.35)
        right = 14 - int((y - 3) * 0.22)
        return left <= x <= right
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
