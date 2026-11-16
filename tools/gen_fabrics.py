"""Fabric item icons (palette taken from the matching vanilla wool texture) + item models,
recipes and the fabrics item tag."""
import json
import os
import sys
from PIL import Image

REPO, WOOL_DIR = sys.argv[1], sys.argv[2]
RES = os.path.join(REPO, "common", "src", "main", "resources")
NS = "powers_unleashed"
COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
          "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]

TEMPLATE = [
    "................",
    "................",
    "..oooooooooooo..",
    ".ohhhhhhhhhhhbo.",
    ".ohbbbbbbbbbbso.",
    ".ohbwbwbwbwbwso.",
    ".ohbbbbbbbbbbso.",
    ".obbbbbbbbbbsso.",
    ".oooooooooooooo.",
    ".ohhhhhhhhhhhbo.",
    ".ohbbbbbbbbbbso.",
    ".ohbwbwbwbwbwso.",
    ".obbbbbbbbbbsso.",
    ".osssssssssbsdo.",
    "..ooooooooosbso.",
    "...........ooo..",
]


def lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def scale(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c) + (255,)


def palette(color):
    im = Image.open(os.path.join(WOOL_DIR, f"{color}_wool.png")).convert("RGB")
    px = sorted(im.get_flattened_data(), key=lum)
    n = len(px)
    avg = lambda a, b: tuple(sum(p[i] for p in px[a:b]) // max(1, b - a) for i in range(3))
    base = avg(n // 3, 2 * n // 3)
    dark = avg(0, n // 6)
    hi = avg(5 * n // 6, n)
    if color == "black":
        hi = scale(base, 2.2)[:3]
    if color == "white":
        dark = scale(base, 0.72)[:3]
    return {
        "o": scale(dark, 0.55),
        "d": scale(dark, 0.8),
        "s": scale(dark, 1.0) if color != "white" else scale(base, 0.82),
        "b": base + (255,),
        "h": hi + (255,),
        "w": scale(hi, 1.12),
    }


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


for color in COLORS:
    pal = palette(color)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(TEMPLATE):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), pal[ch])
    name = f"{color}_fabric"
    p = os.path.join(RES, "assets", NS, "textures", "item", name + ".png")
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)
    write_json(os.path.join(RES, "assets", NS, "items", name + ".json"),
               {"model": {"type": "minecraft:model", "model": f"{NS}:item/{name}"}})
    write_json(os.path.join(RES, "assets", NS, "models", "item", name + ".json"),
               {"parent": "minecraft:item/generated", "textures": {"layer0": f"{NS}:item/{name}"}})
    # 2 wool + 1 stick -> 4 fabric
    write_json(os.path.join(RES, "data", NS, "recipe", name + ".json"), {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "group": "powers_unleashed_fabric",
        "ingredients": [f"minecraft:{color}_wool", f"minecraft:{color}_wool", "minecraft:stick", "minecraft:string"],
        "result": {"count": 4, "id": f"{NS}:{name}"},
    })
    # dyeing: dye + any other fabric -> 1 fabric of that color
    write_json(os.path.join(RES, "data", NS, "recipe", f"dye_{name}.json"), {
        "type": "minecraft:crafting_shapeless",
        "category": "misc",
        "group": "powers_unleashed_dyed_fabric",
        "ingredients": [f"minecraft:{color}_dye", [f"{NS}:{c}_fabric" for c in COLORS if c != color]],
        "result": {"id": f"{NS}:{name}"},
    })

write_json(os.path.join(RES, "data", NS, "tags", "item", "fabrics.json"),
           {"values": [f"{NS}:{c}_fabric" for c in COLORS]})
print("ok")
