"""Irradiation assets (owner's Radioactive Spider design): the Irradiator block (steel frame, lead-grey panels, thick
glass cylinder with a glowing green core, hazard stripes, a dose gauge; idle + active models), the Radioactive Spider
(the vanilla cave spider texture re-coloured dark blue with red patterns + the glow layer of those patterns), its spawn
egg, the Isotope Capsule (our empty cartridge with glowing isotope), the Radioactive Spider Eye and the fever effect icon.

Usage: python3 gen_irradiation.py <resources root> <vanilla cave_spider.png> <vanilla cave_spider_spawn_egg.png>
Front of the block model faces north (the blockstate rotates it).
"""
import json
import math
import os
import random
import sys

from PIL import Image

RES = sys.argv[1]
CAVE_SPIDER = sys.argv[2]
CAVE_EGG = sys.argv[3]
A = os.path.join(RES, "assets", "powers_unchained")
TEX_B = os.path.join(A, "textures", "block")
TEX_I = os.path.join(A, "textures", "item")
TEX_E = os.path.join(A, "textures", "entity", "radioactive_spider")
TEX_FX = os.path.join(A, "textures", "gui", "sprites", "mob_effect")
MODEL = os.path.join(A, "models", "block")
for d in (TEX_B, TEX_I, TEX_E, TEX_FX, MODEL):
    os.makedirs(d, exist_ok=True)
rnd = random.Random(2603)


def rgb(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3] if len(c) > 3 else 255,)


def noisy(c, a=0.05):
    return shade(c, 1 + rnd.uniform(-a, a))


def img(fn, w=16, h=16):
    im = Image.new("RGBA", (w, h))
    for x in range(w):
        for y in range(h):
            im.putpixel((x, y), fn(x, y))
    return im


STEEL = rgb("#8b8f96")
STEEL_H = rgb("#b5b9c0")
STEEL_S = rgb("#5f636a")
SEAM = rgb("#3b3e44")
LEAD = rgb("#5b6068")
LEAD_H = rgb("#737881")
LEAD_S = rgb("#41454c")
YELLOW = rgb("#f2c12e")
HAZ_K = rgb("#26272b")
GREEN = rgb("#58f05a")
GREEN_D = rgb("#1f8a35")
GREEN_L = rgb("#c8ffb0")

# ------------------------------------------------------------------------------------------------- block textures


def steel(x, y):
    if x == 0 or y == 0:
        return STEEL_H
    if x == 15 or y == 15:
        return STEEL_S
    if (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        return SEAM  # rivets
    return noisy(STEEL, 0.04)


def plinth(x, y):
    # rows 0-1: steel lip; then hazard stripes; last row dark
    if y <= 1:
        return STEEL_H if y == 0 else STEEL
    if y == 15:
        return SEAM
    return YELLOW if ((x + y) // 3) % 2 == 0 else HAZ_K


def lead(x, y):
    if y == 0:
        return LEAD_H
    if y == 15 or x % 8 == 7:
        return LEAD_S
    return noisy(LEAD, 0.05)


def top(x, y):
    # steel plate with a round grate: the core's glow seeps through the holes
    if x in (0, 15) or y in (0, 15):
        return STEEL_S if (x == 15 or y == 15) else STEEL_H
    d = math.hypot(x - 7.5, y - 7.5)
    if d < 5.2:
        if x % 2 == 1 and y % 2 == 1:
            glow = 1.0 - d / 6.0
            return shade(GREEN, 0.55 + 0.6 * glow)
        return SEAM
    if 5.2 <= d < 6.2:
        return STEEL_H
    return noisy(STEEL, 0.04)


def glass(x, y):
    # translucent green-tinted glass: darker towards the side edges (reads as a cylinder), a highlight streak
    if y in (0, 15):
        return rgb("#2f6b3a", 200)
    edge = min(x, 15 - x)
    if x == 3:
        return rgb("#e8fff0", 150)
    a = 70 + (5 - min(edge, 5)) * 22
    return rgb("#3fbf55", a)


def core(active):
    def f(x, y):
        streak = 0.5 + 0.5 * math.sin(x * 1.7 + (y // 3) * 0.6)
        base = GREEN_L if active else GREEN
        c = shade(base, (0.75 if active else 0.55) + 0.35 * streak)
        if 5 <= x <= 10 and active:
            c = shade(GREEN_L, 1.0 + 0.1 * streak)
        return c
    return f


def gauge(x, y):
    # a small dial on a steel bezel (native 16 px, only the 4x4 middle shows on the model)
    if not (6 <= x <= 9 and 6 <= y <= 9):
        return STEEL
    if y == 6:
        return (GREEN, YELLOW, YELLOW, rgb("#e5452f"))[x - 6]
    if (x, y) in ((7, 8), (8, 7)):
        return rgb("#1b1b1b")
    return rgb("#efeee6")


textures = {
    "irradiator_steel": img(steel),
    "irradiator_plinth": img(plinth),
    "irradiator_lead": img(lead),
    "irradiator_top": img(top),
    "irradiator_glass": img(glass),
    "irradiator_core": img(core(False)),
    "irradiator_core_active": img(core(True)),
    "irradiator_gauge": img(gauge),
}
for name, im in textures.items():
    im.save(os.path.join(TEX_B, name + ".png"))

# ------------------------------------------------------------------------------------------------- block models
ALL = ("north", "south", "east", "west", "up", "down")
SIDES = ("north", "south", "east", "west")


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


def model(active):
    E = []
    # plinth with hazard stripes
    E.append(box((0, 0, 0), (16, 3, 16), {"north": "#plinth", "south": "#plinth", "east": "#plinth", "west": "#plinth",
                                           "up": "#steel", "down": "#steel"},
                 uv={f: [0, 4, 16, 7] for f in SIDES},
                 cull={"down": "down", "north": "north", "south": "south", "east": "east", "west": "west"}))
    # corner pillars
    for x0, z0 in ((0, 0), (13, 0), (0, 13), (13, 13)):
        E.append(box((x0, 3, z0), (x0 + 3, 13, z0 + 3), "#steel", faces=SIDES, uv={f: [0, 3, 3, 13] for f in SIDES}))
    # lead-grey collar round the bottom of the cylinder
    E.append(box((2, 3, 2), (14, 5, 14), "#lead", faces=SIDES, uv={f: [2, 0, 14, 2] for f in SIDES}))
    # the glowing core (inside) and the thick glass cylinder round it
    E.append(box((6, 5, 6), (10, 13, 10), "#core", faces=SIDES, uv={f: [4, 2, 12, 14] for f in SIDES}, light=15 if active else 8))
    E.append(box((3, 5, 3), (13, 13, 13), "#glass", faces=SIDES, uv={f: [0, 1, 16, 15] for f in SIDES}))
    # dose gauge on the front frame
    E.append(box((9, 6, 2), (13, 10, 3), {"north": "#gauge", "south": "#steel", "east": "#steel", "west": "#steel",
                                           "up": "#steel", "down": "#steel"},
                 uv={"north": [6, 6, 10, 10]}))
    # top plate (creatures stand on it) with the grate
    E.append(box((0, 13, 0), (16, 16, 16), {"north": "#steel", "south": "#steel", "east": "#steel", "west": "#steel",
                                             "up": "#top", "down": "#steel"},
                 uv={f: [0, 0, 16, 3] for f in SIDES},
                 cull={"up": "up", "north": "north", "south": "south", "east": "east", "west": "west"}))
    return {
        "parent": "minecraft:block/block",
        "render_type": "minecraft:translucent",
        "textures": {
            "particle": "powers_unchained:block/irradiator_steel",
            "steel": "powers_unchained:block/irradiator_steel",
            "plinth": "powers_unchained:block/irradiator_plinth",
            "lead": "powers_unchained:block/irradiator_lead",
            "top": "powers_unchained:block/irradiator_top",
            "glass": "powers_unchained:block/irradiator_glass",
            "core": "powers_unchained:block/irradiator_core" + ("_active" if active else ""),
            "gauge": "powers_unchained:block/irradiator_gauge",
        },
        "elements": E,
    }


for active in (False, True):
    with open(os.path.join(MODEL, "irradiator" + ("_active" if active else "") + ".json"), "w") as f:
        json.dump(model(active), f, indent=2)
        f.write("\n")

# ------------------------------------------------------------------------------------------------- the spider
NAVY = [rgb("#070b1c"), rgb("#0c1430"), rgb("#122044"), rgb("#1a2c5a"), rgb("#253a72"), rgb("#33508e")]
RED = rgb("#c4182a")
RED_D = rgb("#7e0d1a")


def lum(p):
    return 0.3 * p[0] + 0.59 * p[1] + 0.11 * p[2]


src = Image.open(CAVE_SPIDER).convert("RGBA")
out = Image.new("RGBA", src.size)
glow = Image.new("RGBA", src.size, (0, 0, 0, 0))
for y in range(src.height):
    for x in range(src.width):
        p = src.getpixel((x, y))
        if p[3] == 0:
            continue
        # keep the head's face (eyes, fangs) exactly like the cave spider (owner: same eyes)
        if 38 <= x < 50 and 11 <= y < 21 and (p[0] > p[1] + 20 or lum(p) > 100):
            out.putpixel((x, y), p)
            continue
        l = lum(p)
        idx = min(len(NAVY) - 1, int(l / 70.0 * len(NAVY)))
        out.putpixel((x, y), NAVY[idx])


# red patterns: collected first, then shaded (light from above, darker rims / tips) and given a soft glow
RED_HI = rgb("#ff6a52")
RED_MID = rgb("#a8121f")
RED_DEEP = rgb("#5c0812")
PATTERN = {}


def pattern(x, y, weight=1.0):
    PATTERN[(x, y)] = max(PATTERN.get((x, y), 0.0), weight)


# the abdomen's back (top face of the 10x8x12 box at texOffs 0,12: u 12..21, v 12..23): a spider symbol
ox, oy = 12, 12
symbol = [
    "....##....",
    "#..####..#",
    ".#.####.#.",
    "..######..",
    "....##....",
    "..######..",
    ".#.####.#.",
    "#..####..#",
    "#...##...#",
    "....##....",
    "...#..#...",
    "..#....#..",
]
for j, row in enumerate(symbol):
    for i, ch in enumerate(row):
        if ch == "#":
            pattern(ox + i, oy + j)
# thin red markings along the abdomen's sides (front/back/east/west faces, v 24..31)
for u in range(0, 44):
    for v in (25, 28):
        if src.getpixel((u, v))[3] and (u + v) % 5 == 0:
            pattern(u, v, 0.55)
# red bands round the legs (legs texOffs 18,0: 16x2x2 -> rows 0..3)
for u in range(18, 54):
    for v in range(0, 4):
        if src.getpixel((u, v))[3] and (u - 18) % 6 in (1, 2):
            pattern(u, v, 0.8)
# a mark on the thorax top (6x6x6 at texOffs 0,0: top face u 6..11, v 0..5)
for (i, j) in ((8, 2), (9, 2), (8, 3), (9, 3)):
    pattern(i, j)


def mix(a, b, t):
    return tuple(int(a[k] + (b[k] - a[k]) * t) for k in range(3)) + (255,)


def red_shade(x, y, weight):
    n = sum((x + dx, y + dy) in PATTERN for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
    lit = (x, y - 1) not in PATTERN          # top edge catches the light
    dark = (x, y + 1) not in PATTERN         # bottom edge in shade
    if n <= 1:
        c = RED_MID                          # thin tips
    elif n >= 4:
        c = mix(RED, RED_HI, 0.55)           # the core of a shape
    else:
        c = RED
    if lit and n >= 2:
        c = mix(c, RED_HI, 0.45)
    elif dark:
        c = mix(c, RED_DEEP, 0.4)
    return mix(RED_DEEP, c, 0.35 + 0.65 * weight)


for (x, y), wgt in PATTERN.items():
    c = red_shade(x, y, wgt)
    out.putpixel((x, y), c)
    # glow: the same shading, brighter in the middle of shapes (eyes render type is additive)
    glow.putpixel((x, y), mix((0, 0, 0, 255), mix(c, rgb("#ff8a6a"), 0.25), 0.55 + 0.45 * wgt))
# a faint halo round the patterns on the dark body, so the pulse blooms softly instead of switching on/off
for (x, y), wgt in list(PATTERN.items()):
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        q = (x + dx, y + dy)
        if q in PATTERN or not (0 <= q[0] < src.width and 0 <= q[1] < src.height) or src.getpixel(q)[3] == 0:
            continue
        if glow.getpixel(q)[3] == 0:
            glow.putpixel(q, (int(70 * wgt), int(8 * wgt), int(10 * wgt), 255))
out.save(os.path.join(TEX_E, "radioactive_spider.png"))
glow.save(os.path.join(TEX_E, "radioactive_spider_glow.png"))

# spawn egg: the cave spider egg re-coloured (navy shell, red spots)
egg = Image.open(CAVE_EGG).convert("RGBA")
eo = Image.new("RGBA", egg.size)
for y in range(egg.height):
    for x in range(egg.width):
        p = egg.getpixel((x, y))
        if p[3] == 0:
            continue
        l = lum(p)
        if l > 95:
            eo.putpixel((x, y), RED if l > 125 else RED_D)
        else:
            eo.putpixel((x, y), NAVY[min(len(NAVY) - 1, int(l / 75.0 * len(NAVY)))])
eo.save(os.path.join(TEX_I, "radioactive_spider_spawn_egg.png"))

# ------------------------------------------------------------------------------------------------- items
cart = Image.open(os.path.join(TEX_I, "empty_web_cartridge.png")).convert("RGBA")
cap = cart.copy()
for y in range(16):
    for x in range(16):
        p = cart.getpixel((x, y))
        if p[3] == 0:
            continue
        if p[:3] == (0xd0, 0xea, 0xe9):
            cap.putpixel((x, y), GREEN_L)
        elif p[:3] == (0x8b, 0xc1, 0xcd):
            cap.putpixel((x, y), GREEN)
cap.save(os.path.join(TEX_I, "isotope_capsule.png"))


def eye(x, y):
    d = math.hypot(x - 7.5, y - 8.0)
    if d > 6.3:
        return (0, 0, 0, 0)
    if d > 5.5:
        return rgb("#3c0710")
    if math.hypot(x - 7.0, y - 7.5) < 2.2:
        return rgb("#a8ff8a") if (x, y) != (6, 6) else rgb("#f4fff0")
    # dark red eye with glowing green veins
    vein = abs(math.sin(math.atan2(y - 8, x - 7.5) * 3.0)) < 0.18
    if vein:
        return GREEN if d < 4.5 else GREEN_D
    return shade(rgb("#8e1424"), 1.0 - d / 14.0)


img(eye).save(os.path.join(TEX_I, "radioactive_spider_eye.png"))


# effect icon 18x18: a green radiation trefoil
def trefoil(x, y):
    cx, cy = 8.5, 8.5
    d = math.hypot(x - cx, y - cy)
    if d < 1.6:
        return GREEN_L
    if 2.6 < d < 7.6:
        a = math.degrees(math.atan2(y - cy, x - cx)) % 360
        if any(abs(((a - c + 180) % 360) - 180) < 30 for c in (270, 30, 150)):
            return GREEN if d < 6.6 else GREEN_D
    return (0, 0, 0, 0)


img(trefoil, 18, 18).save(os.path.join(TEX_FX, "radioactive_fever.png"))
print("ok")
