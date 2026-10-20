"""Web-Shooters: worn model texture (32x32, used by WebShootersModel), item texture + 3D item model, item json.

Usage: python3 gen_web_shooters.py <resources root>
"""
import json
import os
import sys

from PIL import Image

RES = sys.argv[1]
A = os.path.join(RES, "assets", "powers_unchained")


def rgb(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


SILVER, SILVER_H, SILVER_D = rgb("#b9bec6"), rgb("#dfe3e8"), rgb("#7c828c")
RED, RED_D = rgb("#c4202c"), rgb("#7e1018")
GUN, GUN_D = rgb("#3b3f46"), rgb("#25282d")
WEB, WEB_D = rgb("#eef1f4"), rgb("#c3c9d0")
BLACK = rgb("#141416")


def put(im, x, y, c):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), c)


# ---- worn model texture (box UV layout, see WebShootersModel) -------------------------------------------------
tex = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
# cuff: box 5x3x5 at (0,0) -> top/bottom rows 0-4, sides rows 5-7 (20 px wide)
for x in range(20):
    for y in range(5):
        put(tex, x, y, SILVER_D if y in (0, 4) or x in (5, 10, 15) else SILVER)
    put(tex, x, 5, SILVER_H)
    put(tex, x, 6, RED if x % 5 not in (0,) else RED_D)
    put(tex, x, 7, SILVER_D)
# cartridge housing: box 1x2x3 at (0,10) -> rows 10-14, 8 px wide; light window = web fluid
for x in range(8):
    for y in range(10, 15):
        put(tex, x, y, GUN)
for x, y in ((3, 13), (7, 13), (3, 14), (7, 14)):
    put(tex, x, y, WEB)
put(tex, 0, 13, WEB_D)
put(tex, 0, 14, WEB_D)
# nozzle: box 1x1x1 at (0,16)
for x in range(4):
    for y in range(16, 18):
        put(tex, x, y, BLACK if (x + y) % 2 else GUN_D)
p = os.path.join(A, "textures", "models", "web_shooters.png")
os.makedirs(os.path.dirname(p), exist_ok=True)
tex.save(p)

# ---- item texture (strips sampled by the 3D item model) ------------------------------------------------------
it = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for x in range(16):
    for y, c in ((0, SILVER_H), (1, SILVER), (2, RED), (3, SILVER_D)):
        put(it, x, y, c if not (y == 2 and x % 4 == 0) else RED_D)
    for y in range(4, 8):
        put(it, x, y, WEB if (y in (5, 6) and 1 <= x <= 14) else GUN)
    for y in range(8, 10):
        put(it, x, y, BLACK)
    for y in range(10, 16):
        put(it, x, y, SILVER if (x + y) % 3 else SILVER_D)
it.save(os.path.join(A, "textures", "item", "web_shooters.png"))

# ---- 3D item model: one cuff with cartridge and nozzle ------------------------------------------------------------
METAL_SIDE = [0, 0, 16, 4]
METAL_TOP = [0, 10, 16, 16]
SIDES = ("north", "south", "east", "west")


def box(f, t, side_uv, top_uv, tex="#tex"):
    faces = {s: {"texture": tex, "uv": side_uv} for s in SIDES}
    faces["up"] = {"texture": tex, "uv": top_uv}
    faces["down"] = {"texture": tex, "uv": top_uv}
    return {"from": f, "to": t, "faces": faces}


elements = [
    box([4, 6, 4], [12, 10, 5], METAL_SIDE, METAL_TOP),     # north wall
    box([4, 6, 11], [12, 10, 12], METAL_SIDE, METAL_TOP),   # south wall
    box([4, 6, 5], [5, 10, 11], METAL_SIDE, METAL_TOP),     # west wall
    box([11, 6, 5], [12, 10, 11], METAL_SIDE, METAL_TOP),   # east wall
    box([12, 6.5, 6], [13.5, 9.5, 10], [0, 4, 16, 8], [0, 4, 16, 8]),   # cartridge housing
    box([7, 7, 2.5], [9, 9, 4], [0, 8, 16, 10], [0, 8, 16, 10]),         # nozzle
]
model = {
    "parent": "minecraft:block/block",
    "textures": {"tex": "powers_unchained:item/web_shooters", "particle": "powers_unchained:item/web_shooters"},
    "elements": elements,
    "display": {
        "gui": {"rotation": [30, 225, 0], "translation": [0, 1, 0], "scale": [1.4, 1.4, 1.4]},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.6, 0.6, 0.6]},
        "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
        "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.6, 0.6, 0.6]},
        "thirdperson_lefthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.6, 0.6, 0.6]},
        "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.6, 0.6, 0.6]},
        "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 2, 0], "scale": [0.6, 0.6, 0.6]},
    },
}
for path, data in (
        (os.path.join(A, "models", "item", "web_shooters.json"), model),
        (os.path.join(A, "items", "web_shooters.json"),
         {"model": {"type": "minecraft:model", "model": "powers_unchained:item/web_shooters"}}),
):
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
print("ok")
