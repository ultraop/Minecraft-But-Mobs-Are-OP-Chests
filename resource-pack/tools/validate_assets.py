#!/usr/bin/env python3
"""Validate the Cow visual pack, texture references and hinge seam."""

from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PACK = ROOT / "resource-pack"
ASSETS = PACK / "assets"
NAMESPACE = "ultraop_mobchests"
MODEL_SOURCE = ROOT / "plugin/src/main/java/in/ultraop/mobchests/animation/CowChestAnimator.java"
PACK_ZIP = ROOT / "build/distributions/UltraOP-MobChests-ResourcePack.zip"


def load_json(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def resource_path(identifier: str, kind: str, suffix: str) -> Path:
    namespace, separator, path = identifier.partition(":")
    if not separator:
        namespace, path = "minecraft", identifier
    return ASSETS / namespace / kind / f"{path}{suffix}"


def main() -> None:
    meta = load_json(PACK / "pack.mcmeta")
    pack_format = meta["pack"].get("max_format")
    if pack_format != [97, 1]:
        raise SystemExit(f"Unexpected resource-pack format: {pack_format!r}")

    # A Java namespace mismatch is enough to produce Minecraft's missing-model
    # checkerboard, even if the resource pack itself is installed correctly.
    source = MODEL_SOURCE.read_text(encoding="utf-8")
    match = re.search(r'RESOURCE_NAMESPACE\s*=\s*"([^"]+)"', source)
    if not match or match.group(1) != NAMESPACE:
        raise SystemExit("CowChestAnimator namespace does not match the resource pack.")

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

    # These two model halves meet at the exact same Y plane. The lid's
    # ItemDisplay rotation pivots at this model-space centre seam.
    base = load_json(ASSETS / NAMESPACE / "models/item/cow_base.json")
    lid = load_json(ASSETS / NAMESPACE / "models/item/cow_lid.json")
    base_max_y = max(element["to"][1] for element in base["elements"])
    lid_min_y = min(element["from"][1] for element in lid["elements"])
    if base_max_y != 8 or lid_min_y != 8:
        raise SystemExit(
            f"Cow split seam must be y=8; base max={base_max_y}, lid min={lid_min_y}"
        )

    if not PACK_ZIP.is_file():
        raise SystemExit("Pack ZIP was not generated.")
    with zipfile.ZipFile(PACK_ZIP) as archive:
        names = set(archive.namelist())
        required = {
            f"assets/{NAMESPACE}/items/cow_lid.json",
            f"assets/{NAMESPACE}/items/cow_base.json",
            f"assets/{NAMESPACE}/textures/entity/cow_hide.png",
            f"assets/{NAMESPACE}/textures/entity/cow_inside.png",
            f"assets/{NAMESPACE}/textures/entity/cow_muzzle.png",
            "pack.mcmeta",
        }
        missing = sorted(required - names)
        if missing:
            raise SystemExit("Missing files in resource-pack ZIP: " + ", ".join(missing))

    print("Resource-pack validation passed: namespace, models, textures and y=8 split seam.")


if __name__ == "__main__":
    main()
