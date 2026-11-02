#!/usr/bin/env python3
"""Generate tiny dependency-free PNG textures for the Cow chest proof of concept."""

from __future__ import annotations

import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = ROOT / "assets" / "ultraop_mobchests" / "textures" / "entity"
ITEM_TEXTURE_DIR = ROOT / "assets" / "ultraop_mobchests" / "textures" / "item"
SIZE = 64


def png_chunk(kind: bytes, payload: bytes) -> bytes:
    return struct.pack(">I", len(payload)) + kind + payload + struct.pack(">I", zlib.crc32(kind + payload) & 0xFFFFFFFF)


def write_png(path: Path, pixels: list[list[tuple[int, int, int, int]]]) -> None:
    raw = b"".join(b"\x00" + bytes(channel for pixel in row for channel in pixel) for row in pixels)
    data = b"\x89PNG\r\n\x1a\n"
    data += png_chunk(b"IHDR", struct.pack(">2I5B", SIZE, SIZE, 8, 6, 0, 0, 0))
    data += png_chunk(b"IDAT", zlib.compress(raw, 9))
    data += png_chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def canvas(color: tuple[int, int, int, int]) -> list[list[tuple[int, int, int, int]]]:
    return [[color for _ in range(SIZE)] for _ in range(SIZE)]


def rect(pixels, x1, y1, x2, y2, color):
    for y in range(max(0, y1), min(SIZE, y2)):
        for x in range(max(0, x1), min(SIZE, x2)):
            pixels[y][x] = color


def make_hide() -> None:
    cream = (235, 225, 203, 255)
    cocoa = (83, 56, 39, 255)
    tan = (164, 122, 82, 255)
    dark = (45, 35, 30, 255)
    pixels = canvas(cream)

    # Large, deliberately blocky coat patches that remain readable at 16px scale.
    rect(pixels, 4, 5, 19, 20, cocoa)
    rect(pixels, 8, 20, 24, 31, cocoa)
    rect(pixels, 37, 7, 55, 22, cocoa)
    rect(pixels, 42, 22, 59, 33, cocoa)
    rect(pixels, 23, 39, 39, 57, tan)
    rect(pixels, 5, 43, 18, 58, cocoa)
    rect(pixels, 19, 12, 23, 16, tan)
    rect(pixels, 50, 11, 54, 15, tan)
    # Pixel highlights and dark hoof sample areas.
    rect(pixels, 5, 59, 16, 63, dark)
    rect(pixels, 45, 59, 59, 63, dark)
    write_png(TEXTURE_DIR / "cow_hide.png", pixels)


def make_inside() -> None:
    wood = (83, 42, 28, 255)
    dark = (37, 20, 17, 255)
    gold = (219, 161, 45, 255)
    pixels = canvas(wood)
    for i in range(0, SIZE, 8):
        rect(pixels, i, 0, i + 2, SIZE, dark)
        rect(pixels, 0, i, SIZE, i + 1, (105, 54, 32, 255))
    rect(pixels, 2, 2, 62, 5, gold)
    rect(pixels, 2, 59, 62, 62, gold)
    rect(pixels, 2, 5, 5, 59, gold)
    rect(pixels, 59, 5, 62, 59, gold)
    rect(pixels, 13, 24, 51, 42, dark)
    write_png(TEXTURE_DIR / "cow_inside.png", pixels)


def make_muzzle() -> None:
    pink = (211, 137, 135, 255)
    light = (238, 178, 168, 255)
    dark = (84, 47, 47, 255)
    pixels = canvas(pink)
    rect(pixels, 3, 3, 61, 10, light)
    rect(pixels, 10, 24, 21, 38, dark)
    rect(pixels, 43, 24, 54, 38, dark)
    rect(pixels, 5, 50, 59, 58, (177, 100, 104, 255))
    write_png(TEXTURE_DIR / "cow_muzzle.png", pixels)



def make_creeper_hide() -> None:
    green = (63, 137, 53, 255)
    light = (91, 164, 68, 255)
    dark = (35, 83, 38, 255)
    mid = (48, 111, 45, 255)
    pixels = canvas(green)
    rect(pixels, 5, 7, 18, 18, light)
    rect(pixels, 23, 4, 36, 15, mid)
    rect(pixels, 42, 9, 59, 23, dark)
    rect(pixels, 7, 28, 21, 43, dark)
    rect(pixels, 27, 24, 39, 39, light)
    rect(pixels, 46, 32, 60, 48, mid)
    rect(pixels, 13, 48, 27, 61, mid)
    rect(pixels, 34, 48, 51, 60, dark)
    write_png(TEXTURE_DIR / "creeper_hide.png", pixels)


def make_creeper_face() -> None:
    green = (63, 137, 53, 255)
    light = (82, 154, 61, 255)
    dark = (20, 32, 22, 255)
    pixels = canvas(green)
    rect(pixels, 4, 4, 60, 60, green)
    rect(pixels, 9, 12, 23, 27, dark)
    rect(pixels, 41, 12, 55, 27, dark)
    rect(pixels, 23, 28, 41, 36, dark)
    rect(pixels, 17, 36, 47, 54, dark)
    rect(pixels, 25, 37, 39, 46, light)
    rect(pixels, 11, 7, 20, 10, light)
    rect(pixels, 45, 7, 54, 10, light)
    write_png(TEXTURE_DIR / "creeper_face.png", pixels)


def make_golden_potato() -> None:
    clear = (0, 0, 0, 0)
    gold = (239, 180, 36, 255)
    bright = (255, 225, 102, 255)
    shadow = (164, 105, 23, 255)
    pixels = canvas(clear)
    rect(pixels, 16, 18, 48, 50, gold)
    rect(pixels, 20, 12, 43, 55, gold)
    rect(pixels, 23, 8, 39, 14, bright)
    rect(pixels, 10, 25, 17, 43, shadow)
    rect(pixels, 43, 14, 52, 22, bright)
    rect(pixels, 26, 19, 33, 25, bright)
    rect(pixels, 20, 43, 26, 49, shadow)
    rect(pixels, 37, 35, 43, 41, shadow)
    write_png(ITEM_TEXTURE_DIR / "golden_potato.png", pixels)


def make_golden_porkchop() -> None:
    clear = (0, 0, 0, 0)
    gold = (230, 153, 38, 255)
    bright = (255, 219, 111, 255)
    meat = (190, 78, 69, 255)
    dark = (113, 49, 42, 255)
    pixels = canvas(clear)
    rect(pixels, 14, 17, 48, 47, gold)
    rect(pixels, 21, 11, 43, 53, gold)
    rect(pixels, 25, 18, 39, 45, meat)
    rect(pixels, 18, 24, 46, 40, meat)
    rect(pixels, 29, 8, 36, 14, bright)
    rect(pixels, 13, 18, 20, 24, bright)
    rect(pixels, 42, 39, 49, 46, dark)
    rect(pixels, 24, 46, 35, 51, dark)
    write_png(ITEM_TEXTURE_DIR / "golden_porkchop.png", pixels)


def make_golden_bread() -> None:
    clear = (0, 0, 0, 0)
    gold = (201, 128, 34, 255)
    bright = (255, 220, 104, 255)
    mid = (239, 177, 58, 255)
    shadow = (135, 76, 24, 255)
    pixels = canvas(clear)
    rect(pixels, 12, 22, 52, 48, gold)
    rect(pixels, 17, 16, 47, 25, bright)
    rect(pixels, 21, 10, 41, 19, mid)
    rect(pixels, 15, 27, 49, 39, mid)
    rect(pixels, 18, 42, 46, 50, shadow)
    rect(pixels, 25, 19, 30, 32, bright)
    rect(pixels, 37, 19, 42, 32, bright)
    write_png(ITEM_TEXTURE_DIR / "golden_bread.png", pixels)


def make_liquid_gold_bucket() -> None:
    clear = (0, 0, 0, 0)
    gold = (217, 151, 32, 255)
    bright = (255, 220, 89, 255)
    dark = (111, 75, 19, 255)
    pixels = canvas(clear)
    rect(pixels, 12, 15, 52, 52, gold)
    rect(pixels, 17, 20, 47, 46, bright)
    rect(pixels, 21, 26, 43, 43, (255, 238, 147, 255))
    rect(pixels, 8, 12, 56, 17, dark)
    rect(pixels, 10, 8, 14, 17, dark)
    rect(pixels, 50, 8, 54, 17, dark)
    rect(pixels, 14, 48, 50, 55, dark)
    write_png(ITEM_TEXTURE_DIR / "liquid_gold_bucket.png", pixels)


def main() -> None:
    make_hide()
    make_inside()
    make_muzzle()
    make_creeper_hide()
    make_creeper_face()
    make_golden_potato()
    make_golden_porkchop()
    make_golden_bread()
    make_liquid_gold_bucket()
    print(f"Generated mob and custom item textures in {TEXTURE_DIR.parent}")


if __name__ == "__main__":
    main()
