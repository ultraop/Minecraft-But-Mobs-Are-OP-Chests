#!/usr/bin/env python3
"""Generate tiny dependency-free PNG textures for the Cow chest proof of concept."""

from __future__ import annotations

import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEXTURE_DIR = ROOT / "assets" / "ultraop_mobchests" / "textures" / "entity"
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


def main() -> None:
    make_hide()
    make_inside()
    make_muzzle()
    print(f"Generated Cow model textures in {TEXTURE_DIR}")


if __name__ == "__main__":
    main()
