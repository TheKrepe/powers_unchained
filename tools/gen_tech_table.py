"""Tech-styled tailoring table concepts: textures (16x16) + JSON block models (front = north)."""
import json
import os
import random
import sys
from PIL import Image

ROOT = sys.argv[1]
TEX = os.path.join(ROOT, "assets", "powers_unleashed", "textures", "block")
MOD = os.path.join(ROOT, "assets", "powers_unleashed", "models", "block")
os.makedirs(TEX, exist_ok=True)
os.makedirs(MOD, exist_ok=True)
rnd = random.Random(42)


def hx(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def sh(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3],)


def nz(c, a=0.05):
    return sh(c, 1 + rnd.uniform(-a, a))


IRON, IRON_L, IRON_D, IRON_DD = hx("#c6c9ce"), hx("#e4e6ea"), hx("#8c9097"), hx("#5d6168")
DARK, DARK_L, DARK_D = hx("#3a3d44"), hx("#4b4f57"), hx("#24262b")
CU, CU_L, CU_D = hx("#c56d4b"), hx("#e5906a"), hx("#8f4630")
RED, CYAN, CYAN_D = hx("#ff3b2a"), hx("#6ff3ff"), hx("#2aa9c4")


def img(fn):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x in range(16):
        for y in range(16):
            c = fn(x, y)
            if c:
                im.putpixel((x, y), c)
    return im


def frame(x, y, inner):
    """Iron frame with bevel; inner(x,y) draws the inside."""
    if x == 0 or y == 0:
        return IRON_L
    if x == 15 or y == 15:
        return IRON_DD
    if x == 1 or y == 1 or x == 14 or y == 14:
        return nz(IRON, 0.03)
    return inner(x, y)


def station_front(x, y):
    def inner(x, y):
        # drawer (rows 8-13) with copper handle, upper panel with status lights
        if 8 <= y <= 13 and 2 <= x <= 13:
            if y in (8, 13) or x in (2, 13):
                return DARK_D
            if y == 10 and 6 <= x <= 9:
                return CU_L
            if y == 11 and 6 <= x <= 9:
                return CU_D
            return nz(DARK_L, 0.04)
        if y == 7:
            return IRON_D
        if y in (3, 4) and x in (3, 4):
            return RED if y == 3 else sh(RED, 0.6)
        if y in (3, 4) and x in (6, 7):
            return hx("#5fd35a") if y == 3 else hx("#35852f")
        if 3 <= y <= 4 and 9 <= x <= 12:
            return CU if y == 3 else CU_D
        return nz(DARK, 0.04)
    return frame(x, y, inner)


def station_side(x, y):
    def inner(x, y):
        if 3 <= x <= 4:  # copper pipe
            return CU_L if x == 3 else CU
        if y in (2, 13) and 3 <= x <= 4:
            return IRON_D
        if 7 <= x <= 12 and 3 <= y <= 12:  # vent slats
            return DARK_D if y % 2 == 1 else nz(IRON_D, 0.04)
        return nz(DARK, 0.04)
    return frame(x, y, inner)


def station_top(x, y):
    # work bed: dark slate with a measuring grid, copper rails on the left/right edge
    if x in (0, 15):
        return CU_L if y % 4 else CU_D
    if x in (1, 14):
        return CU_D
    if y in (0, 15):
        return IRON_D
    if x % 4 == 2 or y % 4 == 2:
        return DARK_L
    return nz(DARK, 0.04)


def screen(x, y):
    # dark glass with a glowing cyan suit outline (used with light_emission)
    fig = [
        "................",
        "................",
        "......####......",
        "......#..#......",
        "......####......",
        "...##########...",
        "..#.#......#.#..",
        "..#.#......#.#..",
        "..#.#......#.#..",
        "....##.##.##....",
        "....#..##..#....",
        "....#..##..#....",
        "....#..##..#....",
        "....##.##.##....",
        "................",
        "................",
    ]
    if fig[y][x] == "#":
        return CYAN
    if y % 3 == 0:
        return hx("#173a45")
    return hx("#10262e")


def machine(x, y):
    # sewing head: rows 0-7 iron casing, rows 8-11 dark with copper band, 12-15 copper
    if y < 8:
        return IRON_L if y == 0 else (IRON_DD if y == 7 else nz(IRON, 0.03))
    if y < 12:
        return CU_L if y == 9 else (CU_D if y == 10 else nz(DARK, 0.05))
    return CU_L if y == 12 else (CU_D if y == 15 else nz(CU, 0.05))


def led(x, y):
    return RED if (x + y) % 3 else sh(RED, 0.8)


for name, fn in [("tailoring_station_front", station_front), ("tailoring_station_side", station_side),
                 ("tailoring_station_top", station_top), ("tailoring_station_screen", screen),
                 ("tailoring_station_machine", machine), ("tailoring_station_led", led)]:
    img(fn).save(os.path.join(TEX, name + ".png"))

FACES = ("north", "south", "east", "west", "up", "down")


def box(f, t, tex, faces=FACES, uv=None, rot=None, cull=None, glow=None):
    el = {"from": list(f), "to": list(t), "faces": {}}
    for face in faces:
        tx = tex[face] if isinstance(tex, dict) else tex
        if tx is None:
            continue
        fd = {"texture": tx}
        if uv and face in uv:
            fd["uv"] = uv[face]
        if cull and face in cull:
            fd["cullface"] = cull[face]
        el["faces"][face] = fd
    if rot:
        el["rotation"] = rot
    if glow:
        el["light_emission"] = glow
    return el


def save(name, textures, elements, particle, render_type=None):
    textures = dict(textures, particle=particle)
    m = {"parent": "minecraft:block/block", "textures": textures, "elements": elements}
    if render_type:
        m["render_type"] = render_type
    with open(os.path.join(MOD, name + ".json"), "w") as f:
        json.dump(m, f, indent=2)


def spool(x, y, z, idx, h=3):
    u = idx * 4
    return box((x, y, z), (x + 2, y + h, z + 2),
               {f: "#spools" for f in ("north", "south", "east", "west")} | {"up": "#spool_top", "down": "#spool_top"},
               uv={f: [u + 1, 0, u + 3, 16] for f in ("north", "south", "east", "west")} | {"up": [6, 6, 10, 10], "down": [6, 6, 10, 10]})


# ------------------------------------------------------------------------------------------
# T1. Tailoring station: iron & copper cabinet, a gantry with an automatic sewing head over
#     the work bed (fabric on it), a glowing pattern screen at the back, status LEDs.
# ------------------------------------------------------------------------------------------
def t1():
    T = {
        "front": "powers_unleashed:block/tailoring_station_front",
        "side": "powers_unleashed:block/tailoring_station_side",
        "top": "powers_unleashed:block/tailoring_station_top",
        "bottom": "minecraft:block/polished_deepslate",
        "copper": "minecraft:block/cut_copper",
        "iron": "minecraft:block/iron_block",
        "dark": "minecraft:block/polished_deepslate",
        "machine": "powers_unleashed:block/tailoring_station_machine",
        "screen": "powers_unleashed:block/tailoring_station_screen",
        "led": "powers_unleashed:block/tailoring_station_led",
        "cloth": "minecraft:block/red_wool",
        "spools": "powers_unleashed:block/thread_spools",
        "spool_top": "minecraft:block/oak_log_top",
    }
    E = []
    # cabinet + work bed
    E.append(box((0, 0, 0), (16, 9, 16), {"north": "#front", "south": "#side", "east": "#side", "west": "#side", "up": "#top", "down": "#bottom"},
                 uv={"north": [0, 7, 16, 16], "south": [0, 7, 16, 16], "east": [0, 7, 16, 16], "west": [0, 7, 16, 16]},
                 cull={"down": "down", "north": "north", "south": "south", "east": "east", "west": "west"}))
    # fabric lying on the bed
    E.append(box((3, 9, 2), (12, 9.5, 10), "#cloth"))
    # gantry: copper posts + iron rail
    E.append(box((0.5, 9, 6), (2.5, 15, 8), "#copper"))
    E.append(box((13.5, 9, 6), (15.5, 15, 8), "#copper"))
    E.append(box((0, 15, 5.5), (16, 16.5, 8.5), "#iron"))
    # sewing head on the rail
    E.append(box((6, 12, 5), (10, 15, 9), "#machine", uv={f: [0, 4, 4, 11] for f in ("north", "south", "east", "west")} | {"up": [0, 0, 4, 4], "down": [0, 12, 4, 16]}))
    E.append(box((7.5, 10, 6.5), (8.5, 12, 7.5), "#iron"))                    # needle bar
    E.append(box((7.75, 9.5, 6.75), (8.25, 10, 7.25), "#iron"))               # needle
    E.append(box((7, 13, 4.75), (9, 14, 5), "#led", glow=12))                  # status light
    # cable from the head to the back
    E.append(box((8.25, 15, 8.5), (9.25, 16, 13), "#dark"))
    # pattern screen at the back (glowing)
    E.append(box((2, 9, 13), (14, 16, 15), "#dark"))
    E.append(box((3, 10, 12.9), (13, 15, 13), {"north": "#screen"}, faces=("north",), uv={"north": [3, 1, 13, 15]}, glow=15))
    # thread spool on the left post
    E.append(spool(1, 16.5, 6, 0, h=2))
    save("concept_t1_tailoring_station", T, E, "minecraft:block/iron_block")


# ------------------------------------------------------------------------------------------
# T2. Industrial sewing table: concept A modernised — iron legs and treadle, copper trimmed
#     top, the sewing machine gets a copper casing with a glowing control screen.
# ------------------------------------------------------------------------------------------
def t2():
    T = {
        "top": "minecraft:block/polished_deepslate",
        "trim": "minecraft:block/cut_copper",
        "leg": "minecraft:block/iron_block",
        "iron": "minecraft:block/iron_block",
        "dark": "minecraft:block/polished_deepslate",
        "machine": "powers_unleashed:block/tailoring_station_machine",
        "screen": "powers_unleashed:block/tailoring_station_screen",
        "led": "powers_unleashed:block/tailoring_station_led",
        "cloth": "minecraft:block/light_blue_wool",
        "spools": "powers_unleashed:block/thread_spools",
        "spool_top": "minecraft:block/oak_log_top",
    }
    E = []
    E.append(box((0, 8, 0), (16, 10, 16), {"north": "#trim", "south": "#trim", "east": "#trim", "west": "#trim", "up": "#top", "down": "#dark"}))
    for x, z in ((1, 1), (13, 1), (1, 13), (13, 13)):
        E.append(box((x, 0, z), (x + 2, 8, z + 2), "#leg", cull={"down": "down"}))
    E.append(box((3, 3, 1.5), (13, 4, 2.5), "#dark"))
    E.append(box((3, 3, 13.5), (13, 4, 14.5), "#dark"))
    E.append(box((4, 1, 4), (12, 2, 12), "#iron"))
    m = "#machine"
    E.append(box((3, 10, 5), (13, 11, 11), m, uv={f: [0, 12, 10, 13] for f in FACES}))
    E.append(box((10, 11, 6), (13, 14, 10), m, uv={f: [0, 1, 4, 4] for f in FACES}))
    E.append(box((4, 14, 6), (13, 16, 10), m, uv={f: [0, 8, 9, 10] for f in ("north", "south")} | {f: [0, 1, 4, 3] for f in ("east", "west", "up", "down")}))
    E.append(box((3, 12, 6), (5, 16, 10), m, uv={f: [0, 1, 4, 5] for f in FACES}))
    E.append(box((3.5, 11, 7.5), (4.5, 12, 8.5), "#iron"))
    E.append(box((6, 14.5, 5.9), (10, 15.5, 6), {"north": "#screen"}, faces=("north",), uv={"north": [4, 6, 12, 8]}, glow=15))
    E.append(box((11, 12, 5.9), (12, 13, 6), {"north": "#led"}, faces=("north",), glow=12))
    E.append(spool(8, 16, 7, 1, h=2))
    E.append(box((0.5, 10, 0.5), (6, 10.5, 6), "#cloth"))
    E.append(box((0.5, 5, 0), (6, 10.5, 0.5), "#cloth"))
    save("concept_t2_industrial_sewing_table", T, E, "minecraft:block/iron_block")


# ------------------------------------------------------------------------------------------
# T3. Fabricator pod: iron cabinet with a tinted-glass capsule on top; inside a small suit
#     being "printed" on a stand by a glowing red laser emitter.
# ------------------------------------------------------------------------------------------
def t3():
    T = {
        "front": "powers_unleashed:block/tailoring_station_front",
        "side": "powers_unleashed:block/tailoring_station_side",
        "iron": "minecraft:block/iron_block",
        "copper": "minecraft:block/cut_copper",
        "dark": "minecraft:block/polished_deepslate",
        "glass": "minecraft:block/glass",
        "suit": "minecraft:block/red_wool",
        "suit2": "minecraft:block/blue_wool" if False else "minecraft:block/light_blue_wool",
        "led": "powers_unleashed:block/tailoring_station_led",
    }
    E = []
    E.append(box((0, 0, 0), (16, 8, 16), {"north": "#front", "south": "#side", "east": "#side", "west": "#side", "up": "#dark", "down": "#dark"},
                 uv={f: [0, 8, 16, 16] for f in ("north", "south", "east", "west")}, cull={"down": "down", "north": "north", "south": "south", "east": "east", "west": "west"}))
    E.append(box((1, 8, 1), (15, 9, 15), "#copper"))
    # mini suit on a stand
    E.append(box((7.5, 9, 7.5), (8.5, 10, 8.5), "#iron"))
    E.append(box((6, 10, 7), (10, 13, 9), "#suit"))
    E.append(box((5, 12, 7), (6, 13, 9), "#suit"))
    E.append(box((10, 12, 7), (11, 13, 9), "#suit"))
    E.append(box((7, 13, 7), (9, 15, 9), "#suit"))
    # laser emitter + beam
    E.append(box((7, 15.5, 7), (9, 16, 9), "#dark"))
    E.append(box((7.75, 13, 7.75), (8.25, 15.5, 8.25), "#led", glow=15))
    # glass capsule (four walls + roof) and copper corner posts
    E.append(box((1.5, 9, 1.5), (14.5, 16, 1.5), {"north": "#glass", "south": "#glass"}, faces=("north", "south")))
    E.append(box((1.5, 9, 14.5), (14.5, 16, 14.5), {"north": "#glass", "south": "#glass"}, faces=("north", "south")))
    E.append(box((1.5, 9, 1.5), (1.5, 16, 14.5), {"east": "#glass", "west": "#glass"}, faces=("east", "west")))
    E.append(box((14.5, 9, 1.5), (14.5, 16, 14.5), {"east": "#glass", "west": "#glass"}, faces=("east", "west")))
    for x, z in ((1, 1), (14, 1), (1, 14), (14, 14)):
        E.append(box((x, 9, z), (x + 1, 16, z + 1), "#copper"))
    E.append(box((1.5, 16, 1.5), (14.5, 16, 14.5), {"up": "#glass", "down": "#glass"}, faces=("up", "down")))
    for a, b in (((1, 16, 1), (15, 16.5, 2)), ((1, 16, 14), (15, 16.5, 15)), ((1, 16, 2), (2, 16.5, 14)), ((14, 16, 2), (15, 16.5, 14))):
        E.append(box(a, b, "#copper"))
    save("concept_t3_fabricator_pod", T, E, "minecraft:block/iron_block", render_type="minecraft:cutout")


t1()
t2()
t3()
print("ok")
