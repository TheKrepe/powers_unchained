"""Suit Creator: a tech machine inspired by Fisk's Superheroes suit blocks — steel frame with corner pillars,
recessed body, glowing amber vent on the front, glowing data screen on top inside a raised rim, light strips on
the sides, dark plinth with hazard stripes.

Usage: python3 gen_suit_creator.py <resources root>
Writes assets/powers_unleashed/textures/block/suit_creator_*.png and models/block/suit_creator.json.
Front of the model faces north (blockstate rotates it).
"""
import json
import os
import random
import sys

from PIL import Image

RES = sys.argv[1]
TEX = os.path.join(RES, "assets", "powers_unleashed", "textures", "block")
MODEL = os.path.join(RES, "assets", "powers_unleashed", "models", "block")
os.makedirs(TEX, exist_ok=True)
os.makedirs(MODEL, exist_ok=True)
rnd = random.Random(263)


def rgb(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)


def noisy(c, a=0.05):
    return shade(c, 1 + rnd.uniform(-a, a))


STEEL = rgb("#8b8f96")
STEEL_H = rgb("#b5b9c0")
STEEL_S = rgb("#5f636a")
SEAM = rgb("#3b3e44")
DARK = rgb("#3e4147")
DARK_D = rgb("#2a2c31")
YELLOW = rgb("#f2c12e")
AMBER = rgb("#e89a1e")
AMBER_D = rgb("#8a5512")
HAZ_K = rgb("#26272b")


def img(fn):
    im = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), fn(x, y))
    return im


def bevel_panel(x, y, x0, y0, x1, y1, base=STEEL):
    """Raised panel: highlight top/left edge, shadow bottom/right edge."""
    if y == y0 or x == x0:
        return STEEL_H
    if y == y1 or x == x1:
        return STEEL_S
    return noisy(base, 0.04)


def frame(x, y):
    # steel plates: two panels split horizontally, dark seams between
    if x in (0, 15) or y in (0, 15):
        return SEAM
    if y == 7 or y == 8:
        return SEAM if y == 7 else STEEL_S
    if y < 7:
        c = bevel_panel(x, y, 1, 1, 14, 6)
    else:
        c = bevel_panel(x, y, 1, 9, 14, 14)
    if (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        return STEEL_S  # rivets
    return c


def pillar(x, y):
    # vertical bevelled bar with a dark groove and rivets
    if x in (0, 15):
        return SEAM
    if x == 7 or x == 8:
        return DARK if (y % 4) else STEEL_S
    if x in (1, 9):
        return STEEL_H
    if x in (6, 14):
        return STEEL_S
    if y in (1, 14) and x in (3, 12):
        return STEEL_H
    return noisy(STEEL, 0.04)


def panel(x, y):
    # side/back/front body panel: steel with a recessed dark window and seams
    if y in (0, 15) or x in (0, 15):
        return SEAM
    if 3 <= x <= 12 and 4 <= y <= 11:
        if x in (3, 12) or y in (4, 11):
            return STEEL_S if (x == 12 or y == 11) else DARK_D
        return noisy(DARK, 0.06)
    return bevel_panel(x, y, 1, 1, 14, 14)


def vent(x, y):
    # glowing amber bars in a dark slot
    if x in (0, 15):
        return DARK_D
    if y % 3 == 0:
        return DARK_D
    if y % 3 == 1:
        return YELLOW if 2 <= x <= 13 else AMBER
    return AMBER if 2 <= x <= 13 else AMBER_D


def plinth(x, y):
    # dark base: hazard stripe band in the middle rows, used through uv rows 6-9 on the 2 px high sides
    if 6 <= y <= 9:
        return YELLOW if ((x + y) // 2) % 2 == 0 else HAZ_K
    return noisy(DARK, 0.06) if (x + y) % 5 else DARK_D


def screen(x, y):
    # Fisk-style data grid: concentric frames of colored blocks around a dark core
    pal = {
        "k": rgb("#15181e"), "y": YELLOW, "o": rgb("#e8742a"), "c": rgb("#7cc7e8"), "w": rgb("#e9eef2"),
        "g": rgb("#79a35a"), "b": rgb("#4a3a26"), "d": rgb("#262b33"), "l": rgb("#f1e3a0"),
    }
    rows = [
        "kkkkkkkkkkkkkkkk",
        "kylkwwkcckwwklyk",
        "klyykcwlwlwckyyk",
        "kkykddddddddkykk",
        "kwkdyyoooyydkcwk",
        "kwcdyooooooydwck",
        "kkldoobbbbbodlkk",
        "kcwdoobkkbbodwck",
        "kcwdoobkkbbodwlk",
        "kkldoobbbbbodlkk",
        "kwcdyooooooydcwk",
        "kwkdyyoooyydkgwk",
        "kkykddddddddkykk",
        "klyykcwlwlwckyyk",
        "kylkwwkgckwwklyk",
        "kkkkkkkkkkkkkkkk",
    ]
    return pal[rows[y][x]]


def light(x, y):
    # segmented status lights
    if y % 3 == 0:
        return DARK_D
    return YELLOW if x % 4 in (1, 2) else AMBER


textures = {
    "suit_creator_frame": img(frame),
    "suit_creator_pillar": img(pillar),
    "suit_creator_panel": img(panel),
    "suit_creator_vent": img(vent),
    "suit_creator_plinth": img(plinth),
    "suit_creator_screen": img(screen),
    "suit_creator_light": img(light),
    "suit_creator_vent_back": img(lambda x, y: DARK_D if y % 3 == 0 or x in (0, 15) else (STEEL_S if y % 3 == 1 else DARK)),
}
for name, im in textures.items():
    im.save(os.path.join(TEX, name + ".png"))

# ------------------------------------------------------------------------------------------------- model
ALL = ("north", "south", "east", "west", "up", "down")


def box(f, t, tex, faces=ALL, uv=None, cull=None, light=0):
    el = {"from": list(f), "to": list(t), "faces": {}}
    for face in faces:
        tx = tex[face] if isinstance(tex, dict) else tex
        fd = {"texture": tx}
        if uv and face in uv:
            fd["uv"] = uv[face]
        if cull and face in cull:
            fd["cullface"] = cull[face]
        el["faces"][face] = fd
    if light:
        el["light_emission"] = light
    return el


E = []
# plinth with hazard stripes on its sides
side_uv = {f: [0, 6, 16, 8] for f in ("north", "south", "east", "west")}
E.append(box((0, 0, 0), (16, 2, 16), "#plinth", uv=side_uv,
             cull={"down": "down", "north": "north", "south": "south", "east": "east", "west": "west"}))
# corner pillars
for x0, z0 in ((0, 0), (13, 0), (0, 13), (13, 13)):
    E.append(box((x0, 2, z0), (x0 + 3, 13, z0 + 3), "#pillar", faces=("north", "south", "east", "west"),
                 uv={f: [5, 0, 11, 11] for f in ("north", "south", "east", "west")}))
# recessed body: front/back 2 px behind the pillars, sides 1 px in
E.append(box((1, 2, 2), (15, 13, 14), {"north": "#panel", "south": "#panel", "east": "#panel", "west": "#panel",
                                        "up": "#frame", "down": "#frame"}, faces=("north", "south", "east", "west"),
             uv={f: [1, 3, 15, 14] for f in ("north", "south", "east", "west")}))
# glowing amber vent in the front window
E.append(box((4, 4.5, 1.9), (12, 11.5, 2), "#vent", faces=("north",), uv={"north": [2, 3, 14, 13]}, light=15))
# light strips on the sides and the back
for z0 in (5.5, 9.5):
    E.append(box((0.9, 4.5, z0), (1, 11.5, z0 + 1), "#light", faces=("west",), uv={"west": [1, 1, 2, 15]}, light=15))
    E.append(box((15, 4.5, z0), (15.1, 11.5, z0 + 1), "#light", faces=("east",), uv={"east": [1, 1, 2, 15]}, light=15))
# back: unlit exhaust vent
E.append(box((4, 4.5, 14), (12, 11.5, 14.1), "#vent_back", faces=("south",), uv={"south": [2, 3, 14, 13]}))
# top deck
E.append(box((0, 13, 0), (16, 15, 16), "#frame", faces=("north", "south", "east", "west", "down"),
             uv={f: [0, 9, 16, 11] for f in ("north", "south", "east", "west")},
             cull={"north": "north", "south": "south", "east": "east", "west": "west"}))
# glowing screen inside the rim
E.append(box((2, 13, 2), (14, 15.25, 14), "#screen", faces=("up",), uv={"up": [2, 2, 14, 14]}, light=15))
# raised rim around the screen
rim = {"north": "#frame", "south": "#frame", "east": "#frame", "west": "#frame", "up": "#frame"}
rim_uv = {f: [0, 0, 16, 1] for f in ("north", "south", "east", "west")}
E.append(box((0, 15, 0), (16, 16, 2), rim, faces=("north", "south", "east", "west", "up"), uv=rim_uv | {"up": [0, 0, 16, 2]},
             cull={"north": "north", "east": "east", "west": "west", "up": "up"}))
E.append(box((0, 15, 14), (16, 16, 16), rim, faces=("north", "south", "east", "west", "up"), uv=rim_uv | {"up": [0, 14, 16, 16]},
             cull={"south": "south", "east": "east", "west": "west", "up": "up"}))
E.append(box((0, 15, 2), (2, 16, 14), rim, faces=("east", "west", "up"), uv=rim_uv | {"up": [0, 2, 2, 14]},
             cull={"west": "west", "up": "up"}))
E.append(box((14, 15, 2), (16, 16, 14), rim, faces=("east", "west", "up"), uv=rim_uv | {"up": [14, 2, 16, 14]},
             cull={"east": "east", "up": "up"}))

model = {
    "parent": "minecraft:block/block",
    "textures": {
        "particle": "powers_unleashed:block/suit_creator_frame",
        "frame": "powers_unleashed:block/suit_creator_frame",
        "pillar": "powers_unleashed:block/suit_creator_pillar",
        "panel": "powers_unleashed:block/suit_creator_panel",
        "vent": "powers_unleashed:block/suit_creator_vent",
        "plinth": "powers_unleashed:block/suit_creator_plinth",
        "screen": "powers_unleashed:block/suit_creator_screen",
        "light": "powers_unleashed:block/suit_creator_light",
        "vent_back": "powers_unleashed:block/suit_creator_vent_back",
    },
    "elements": E,
}
with open(os.path.join(MODEL, "suit_creator.json"), "w") as f:
    json.dump(model, f, indent=2)
    f.write("\n")
print("ok")
