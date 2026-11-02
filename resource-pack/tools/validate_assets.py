#!/usr/bin/env python3
"""Validate model namespaces, texture references and mob chest split seams."""

from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PACK = ROOT / "resource-pack"
ASSETS = PACK / "assets"
NAMESPACE = "ultraop_mobchests"
JAVA_SOURCES = [
    ROOT / "plugin/src/main/java/in/ultraop/mobchests/animation/CowChestAnimator.java",
    ROOT / "plugin/src/main/java/in/ultraop/mobchests/animation/CreeperChestAnimator.java",
]
PACK_ZIP = ROOT / "build" / "distributions" / "UltraOP-MobChests-ResourcePack.zip"


def load_json(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def resource_path(identifier: str, kind: str, suffix: str) -> Path:
    namespace, separator, path = identifier.partition(":")
    if not separator:
        namespace, path = "minecraft", identifier
    return ASSETS / namespace / kind / f"{path}{suffix}"


def validate_split(lower_path: Path, upper_path: Path, name: str) -> None:
    lower = load_json(lower_path)
    upper = load_json(upper_path)
    lower_max_y = max(element["to"][1] for element in lower["elements"])
    upper_min_y = min(element["from"][1] for element in upper["elements"])
    if lower_max_y != 8 or upper_min_y != 8:
        raise SystemExit(
            f"{name} split seam must be y=8; lower max={lower_max_y}, upper min={upper_min_y}"
        )


def main() -> None:
    meta = load_json(PACK / "pack.mcmeta")
    pack_format = meta["pack"].get("max_format")
    if pack_format != [97, 1]:
        raise SystemExit(f"Unexpected resource-pack format: {pack_format!r}")

    for source_path in JAVA_SOURCES:
        source = source_path.read_text(encoding="utf-8")
        match = re.search(r'RESOURCE_NAMESPACE\s*=\s*"([^"]+)"', source)
        if not match or match.group(1) != NAMESPACE:
            raise SystemExit(f"{source_path.name} namespace does not match the resource pack.")

    for definition in sorted((ASSETS / NAMESPACE / "items").glob("*.json")):
        definition_data = load_json(definition)
        model_id = definition_data.get("model", {}).get("model")
        if not isinstance(model_id, str):
            raise SystemExit(f"Invalid item model definition: {definition}")
        model_file = resource_path(model_id, "models", ".json")
        if not model_file.is_file():
            raise SystemExit(f"Missing item model file: {model_file}")

    for model_file in sorted((ASSETS / NAMESPACE / "models").rglob("*.json")):
        model = load_json(model_file)
        for texture_id in model.get("textures", {}).values():
            if not isinstance(texture_id, str) or texture_id.startswith("#"):
                continue
            texture_file = resource_path(texture_id, "textures", ".png")
            if not texture_file.is_file():
                raise SystemExit(f"Missing model texture: {texture_file}")

    validate_split(
        ASSETS / NAMESPACE / "models/item/cow_base.json",
        ASSETS / NAMESPACE / "models/item/cow_lid.json",
        "Cow",
    )
    validate_split(
        ASSETS / NAMESPACE / "models/item/creeper_base.json",
        ASSETS / NAMESPACE / "models/item/creeper_head.json",
        "Creeper",
    )

    if not PACK_ZIP.is_file():
        raise SystemExit("Resource-pack ZIP was not generated.")
    required = {
        "pack.mcmeta",
        f"assets/{NAMESPACE}/items/cow_lid.json",
        f"assets/{NAMESPACE}/items/cow_base.json",
        f"assets/{NAMESPACE}/items/creeper_head.json",
        f"assets/{NAMESPACE}/items/creeper_base.json",
        f"assets/{NAMESPACE}/textures/entity/cow_hide.png",
        f"assets/{NAMESPACE}/textures/entity/cow_inside.png",
        f"assets/{NAMESPACE}/textures/entity/cow_muzzle.png",
        f"assets/{NAMESPACE}/textures/entity/creeper_hide.png",
        f"assets/{NAMESPACE}/textures/entity/creeper_face.png",
        f"assets/{NAMESPACE}/items/golden_potato.json",
        f"assets/{NAMESPACE}/items/golden_porkchop.json",
        f"assets/{NAMESPACE}/items/golden_bread.json",
        f"assets/{NAMESPACE}/items/liquid_gold_bucket.json",
        f"assets/{NAMESPACE}/textures/item/golden_potato.png",
        f"assets/{NAMESPACE}/textures/item/golden_porkchop.png",
        f"assets/{NAMESPACE}/textures/item/golden_bread.png",
        f"assets/{NAMESPACE}/textures/item/liquid_gold_bucket.png",
    }
    with zipfile.ZipFile(PACK_ZIP) as archive:
        names = set(archive.namelist())
        missing = sorted(required - names)
        if missing:
            raise SystemExit("Missing files in resource-pack ZIP: " + ", ".join(missing))
        bad_file = archive.testzip()
        if bad_file:
            raise SystemExit(f"Corrupt resource-pack ZIP member: {bad_file}")

    print("Resource-pack validation passed: namespaces, JSON, textures and Cow/Creeper y=8 seams.")


if __name__ == "__main__":
    main()
