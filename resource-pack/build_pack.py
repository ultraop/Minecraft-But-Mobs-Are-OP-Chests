#!/usr/bin/env python3
"""Build the Minecraft 26.3 MobChests pack from original vanilla item PNGs."""
from __future__ import annotations

import io
import json
import math
import time
import urllib.request
import zipfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / "assets" / "mobchests"
OUTPUT = ROOT / "build" / "MobChests-Liquid-Gold-ResourcePack.zip"
CACHE = ROOT / ".cache" / "minecraft-textures-26.3"

TEXTURE_PACKAGE = "https://cdn.jsdelivr.net/npm/minecraft-textures@26.3.0"
MANIFEST_URL = f"{TEXTURE_PACKAGE}/dist/textures/manifest/26.3.json"
ASSET_BASE_URL = f"{TEXTURE_PACKAGE}/dist/textures/assets/"

# Original Minecraft 26.3 inventory sprites; only a pale-gold tint is applied.
FOODS = {
    "golden_potato": "potato",
    "golden_baked_potato": "baked_potato",
    "golden_steak": "cooked_beef",
    "golden_porkchop": "cooked_porkchop",
    "golden_chicken": "cooked_chicken",
    "golden_mutton": "cooked_mutton",
    "golden_rabbit": "cooked_rabbit",
    "golden_bread": "bread",
    "golden_carrot": "carrot",
    "golden_cod": "cooked_cod",
    "golden_salmon": "cooked_salmon",
    "golden_pumpkin_pie": "pumpkin_pie",
}
BUCKET_SOURCE = "lava_bucket"
GOLD_TINT = (247, 202, 105)
BUCKET_LIQUID_TINT = (242, 194, 88)


def fetch_bytes(url: str, label: str) -> bytes:
    request = urllib.request.Request(
        url,
        headers={"User-Agent": "UltraOP-MobChests/26.3 resource-pack-builder"},
    )
    last_error: Exception | None = None
    for attempt in range(3):
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                data = response.read()
                if not data:
                    raise RuntimeError(f"Empty download for {label}")
                return data
        except Exception as error:
            last_error = error
            if attempt < 2:
                time.sleep(1.0 + attempt)
    raise RuntimeError(f"Could not download {label} from {url}: {last_error}")


def load_vanilla_manifest() -> dict[str, str]:
    CACHE.mkdir(parents=True, exist_ok=True)
    manifest_path = CACHE / "manifest-26.3.json"
    if not manifest_path.is_file():
        manifest_path.write_bytes(fetch_bytes(MANIFEST_URL, "Minecraft 26.3 item manifest"))
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    items = manifest.get("items", [])
    if manifest.get("version") != "26.3" or not items:
        raise RuntimeError("The upstream texture manifest is not a valid 26.3 manifest.")
    return {
        str(item["id"]).removeprefix("minecraft:"): str(item["texture"])
        for item in items
        if item.get("id") and item.get("texture")
    }


def source_texture_bytes(texture_path: str) -> bytes:
    safe_path = texture_path.replace("\\", "/").lstrip("/")
    if ".." in Path(safe_path).parts:
        raise RuntimeError(f"Unsafe texture path from manifest: {texture_path}")
    cache_path = CACHE / safe_path
    if cache_path.is_file():
        return cache_path.read_bytes()
    data = fetch_bytes(ASSET_BASE_URL + safe_path, safe_path)
    cache_path.parent.mkdir(parents=True, exist_ok=True)
    cache_path.write_bytes(data)
    return data


def tint_food_sprite(source_bytes: bytes, output: Path) -> None:
    """Blend 18% pale gold into the original PNG without changing its alpha."""
    image = Image.open(io.BytesIO(source_bytes)).convert("RGBA")
    original_alpha = image.getchannel("A").copy()
    tint = Image.new("RGBA", image.size, (*GOLD_TINT, 255))
    result = Image.blend(image, tint, 0.18)
    result.putalpha(original_alpha)
    output.parent.mkdir(parents=True, exist_ok=True)
    result.save(output, format="PNG", optimize=True)


def tint_lava_bucket_sprite(source_bytes: bytes, output: Path) -> None:
    """Keep the exact vanilla lava-bucket silhouette; tint only its lava payload."""
    image = Image.open(io.BytesIO(source_bytes)).convert("RGBA")
    original_alpha = image.getchannel("A").copy()
    updated = []
    for red, green, blue, alpha in image.getdata():
        if alpha > 0 and red > 95 and red > green * 1.12 and red > blue * 1.25:
            strength = 0.52
            red = round(red * (1.0 - strength) + BUCKET_LIQUID_TINT[0] * strength)
            green = round(green * (1.0 - strength) + BUCKET_LIQUID_TINT[1] * strength)
            blue = round(blue * (1.0 - strength) + BUCKET_LIQUID_TINT[2] * strength)
        updated.append((red, green, blue, alpha))
    image.putdata(updated)
    image.putalpha(original_alpha)
    output.parent.mkdir(parents=True, exist_ok=True)
    image.save(output, format="PNG", optimize=True)


def fluid_texture() -> Image.Image:
    """Four animated 16x16 fluid frames with pale gold ripples and soft transparency."""
    palette = [
        (174, 126, 45),
        (202, 157, 70),
        (230, 190, 105),
        (248, 221, 157),
        (255, 235, 183),
    ]
    pixels = []
    for frame in range(4):
        for y in range(16):
            for x in range(16):
                wave = math.sin((x * 0.70) + (y * 0.43) + frame * 1.15)
                ripple = math.sin((x * 0.27) - (y * 0.82) + frame * 0.75)
                value = (wave + ripple + 2.0) / 4.0
                index = min(4, max(0, int(value * 5)))
                alpha = 222 if index < 4 else 232
                pixels.append((*palette[index], alpha))
    image = Image.new("RGBA", (16, 64))
    image.putdata(pixels)
    return image


def write_item_model(item_id: str) -> None:
    item_dir = ASSETS / "items"
    model_dir = ASSETS / "models" / "item"
    item_dir.mkdir(parents=True, exist_ok=True)
    model_dir.mkdir(parents=True, exist_ok=True)
    (item_dir / f"{item_id}.json").write_text(
        json.dumps({
            "model": {
                "type": "minecraft:model",
                "model": f"mobchests:item/{item_id}",
            }
        }, indent=2) + "\n",
        encoding="utf-8",
    )
    (model_dir / f"{item_id}.json").write_text(
        json.dumps({
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"mobchests:item/{item_id}"},
        }, indent=2) + "\n",
        encoding="utf-8",
    )


def generate_assets() -> dict[str, str]:
    manifest = load_vanilla_manifest()
    texture_dir = ASSETS / "textures" / "item"
    required = {"liquid_gold_bucket": BUCKET_SOURCE, **FOODS}

    for custom_id, vanilla_id in required.items():
        if vanilla_id not in manifest:
            raise RuntimeError(
                f"Vanilla item '{vanilla_id}' is missing from Minecraft 26.3 manifest."
            )
        original = source_texture_bytes(manifest[vanilla_id])
        output = texture_dir / f"{custom_id}.png"
        if custom_id == "liquid_gold_bucket":
            tint_lava_bucket_sprite(original, output)
        else:
            tint_food_sprite(original, output)
        write_item_model(custom_id)

    texture_dir.mkdir(parents=True, exist_ok=True)
    fluid_texture().save(texture_dir / "liquid_gold_flow.png", format="PNG", optimize=True)
    (texture_dir / "liquid_gold_flow.png.mcmeta").write_text(
        json.dumps({"animation": {"frametime": 5, "interpolate": True}}, indent=2) + "\n",
        encoding="utf-8",
    )

    # Match each vanilla Levelled water height. Keep the existing full-cube
    # fluid model parent; the added falling model is cloned from level 0.
    model_dir = ASSETS / "models" / "item"
    item_dir = ASSETS / "items"
    for level in range(9):
        item_id = f"liquid_gold_flow_{level}"
        item_path = item_dir / f"{item_id}.json"
        if level == 8:
            item_path.parent.mkdir(parents=True, exist_ok=True)
            item_path.write_text(
                json.dumps({
                    "model": {
                        "type": "minecraft:model",
                        "model": f"mobchests:item/{item_id}",
                    }
                }, indent=2) + "\n",
                encoding="utf-8",
            )
            model = json.loads((model_dir / "liquid_gold_flow_0.json").read_text(encoding="utf-8"))
        else:
            model_path = model_dir / f"{item_id}.json"
            model = json.loads(model_path.read_text(encoding="utf-8"))
        height = 16 if level == 8 else max(2, round((8 - level) / 9 * 16))
        for element in model.get("elements", []):
            element["from"] = [0, 0, 0]
            element["to"] = [16, height, 16]
        model["render_type"] = "minecraft:translucent"
        (model_dir / f"{item_id}.json").write_text(
            json.dumps(model, indent=2) + "\n",
            encoding="utf-8",
        )

    return {custom_id: manifest[vanilla_id] for custom_id, vanilla_id in required.items()}


def validate_png(path: Path) -> tuple[int, int]:
    with Image.open(path) as image:
        image.verify()
    with Image.open(path) as image:
        return image.size


def main() -> None:
    source_map = generate_assets()

    meta_path = ROOT / "pack.mcmeta"
    if not meta_path.is_file():
        raise SystemExit("pack.mcmeta is missing")
    meta = json.loads(meta_path.read_text(encoding="utf-8"))
    pack = meta.get("pack", {})
    if pack.get("min_format") != [97, 1] or pack.get("max_format") != [97, 1]:
        raise SystemExit("Resource pack must target Minecraft 26.3 format 97.1")

    model_files = sorted(ASSETS.rglob("*.json"))
    expected = 20 + (len(FOODS) * 2)
    if len(model_files) != expected:
        raise SystemExit(f"Expected {expected} item definition/model JSON files, found {len(model_files)}")
    for path in model_files:
        json.loads(path.read_text(encoding="utf-8"))

    texture_dir = ASSETS / "textures" / "item"
    for name in ["liquid_gold_bucket.png", "liquid_gold_flow.png"] + [
        f"{item_id}.png" for item_id in FOODS
    ]:
        size = validate_png(texture_dir / name)
        if size[0] <= 0 or size[1] <= 0:
            raise SystemExit(f"Invalid texture dimensions: {name}")

    # Food icons must keep their exact original vanilla PNG dimensions.
    for custom_id, vanilla_path in source_map.items():
        if custom_id == "liquid_gold_bucket":
            continue
        expected_size = Image.open(io.BytesIO(source_texture_bytes(vanilla_path))).size
        actual_size = Image.open(texture_dir / f"{custom_id}.png").size
        if expected_size != actual_size:
            raise SystemExit(
                f"Vanilla sprite size changed for {custom_id}: {expected_size} -> {actual_size}"
            )

    animation = json.loads(
        (texture_dir / "liquid_gold_flow.png.mcmeta").read_text(encoding="utf-8")
    )
    if animation.get("animation", {}).get("frametime", 0) <= 0:
        raise SystemExit("Liquid Gold fluid texture animation metadata is invalid")

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(
        OUTPUT, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9
    ) as archive:
        for path in sorted(ROOT.rglob("*")):
            relative = path.relative_to(ROOT)
            if (
                not path.is_file()
                or "build" in relative.parts
                or ".cache" in relative.parts
                or path.name in ("build_pack.py", "requirements.txt")
            ):
                continue
            archive.write(path, relative.as_posix())
        if archive.testzip() is not None:
            raise SystemExit("Resource pack ZIP integrity check failed")

    with zipfile.ZipFile(OUTPUT) as archive:
        names = set(archive.namelist())
        if "pack.mcmeta" not in names:
            raise SystemExit("pack.mcmeta is missing at the ZIP root")
        if "assets/mobchests/items/liquid_gold_bucket.json" not in names:
            raise SystemExit("Liquid Gold bucket item definition is missing")
        if "assets/mobchests/items/liquid_gold_flow_8.json" not in names:
            raise SystemExit("Liquid Gold flow level models are incomplete")
        for item_id in FOODS:
            if f"assets/mobchests/items/{item_id}.json" not in names:
                raise SystemExit(f"Missing Golden Food item definition: {item_id}")
            if f"assets/mobchests/textures/item/{item_id}.png" not in names:
                raise SystemExit(f"Missing vanilla-derived Golden Food PNG: {item_id}")

    print(f"Resource pack OK: {OUTPUT} ({OUTPUT.stat().st_size} bytes)")
    print("Vanilla sprite sources:")
    for custom_id, vanilla_path in source_map.items():
        print(f"  {custom_id} <- {vanilla_path}")


if __name__ == "__main__":
    main()
