"""Tech Table block: vanilla-style workstation textures (16x16) + block/item models, blockstates.

A dark-oak workbench (like the crafting / smithing tables) with a riveted steel rim and work plate: a circuit board,
a wrench and rivets on top, a drawer with a copper handle and two indicator lamps on the front, tools hanging on the
sides (screwdriver, pliers, a coil of copper wire).

Usage: python3 tools/gen_tech_table_block.py <resources root>   (e.g. common/src/main/resources)
"""
import json
import os
import random
import sys

from PIL import Image

ROOT = sys.argv[1]
ASSETS = os.path.join(ROOT, "assets", "powers_unchained")
TEX = os.path.join(ASSETS, "textures", "block")
rnd = random.Random(1337)


def hx(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def sh(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)


# palettes (sampled from vanilla dark oak planks / iron / copper / redstone)
WOOD = [hx("#4f3218"), hx("#492f17"), hx("#53381a"), hx("#3e2912")]
WOOD_SEAM = hx("#291a0c")
WOOD_DARK = hx("#301e0e")
POST = [hx("#3a2411"), hx("#301e0e"), hx("#3e2912")]
STEEL = [hx("#80858d"), hx("#787d85"), hx("#878c94"), hx("#7c8189")]
STEEL_L = hx("#b9bdc3")
STEEL_D = hx("#5a5e65")
STEEL_DD = hx("#3d4046")
SHINE = hx("#d8dadd")
CU, CU_L, CU_D = hx("#c15a36"), hx("#e77c56"), hx("#8a3d24")
RS, RS_L, RS_D = hx("#d81c0a"), hx("#ff5a3c"), hx("#7a0c04")
AMBER, AMBER_L = hx("#f0a020"), hx("#ffd56a")
PCB, PCB_L, PCB_D = hx("#2f6b3a"), hx("#3f8a4b"), hx("#1f4a27")
CHIP = hx("#262626")
GRIP, GRIP_L = hx("#b8261a"), hx("#de4a33")


def canvas():
    return [[None] * 16 for _ in range(16)]


def save(px, name):
    im = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            im.putpixel((x, y), px[y][x])
    im.save(os.path.join(TEX, name + ".png"))


def planks(px, y0=0, y1=16, x0=0, x1=16):
    """Horizontal dark oak planks, 4 px high, with seams and a few darker grain streaks."""
    for y in range(y0, y1):
        row = (y - y0) // 4
        base = WOOD[row % len(WOOD)]
        for x in range(x0, x1):
            if (y - y0) % 4 == 3:
                c = WOOD_SEAM
            else:
                c = sh(base, 1 + rnd.uniform(-0.06, 0.06))
                if rnd.random() < 0.08:
                    c = sh(base, 0.82)
            px[y][x] = c
        # plank ends (vertical joints), offset per row like vanilla
        joint = x0 + (3 + row * 7) % (x1 - x0)
        if (y - y0) % 4 != 3 and x0 < joint < x1:
            px[y][joint] = WOOD_SEAM


def posts(px, y0, y1):
    for y in range(y0, y1):
        for x in (0, 15):
            px[y][x] = sh(POST[(y + x) % 3], 1 + rnd.uniform(-0.05, 0.05))
        px[y][1] = sh(px[y][1], 0.85)
        px[y][14] = sh(px[y][14], 0.85)


def steel(px, x0, y0, x1, y1, bevel=True):
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[y][x] = sh(STEEL[rnd.randrange(len(STEEL))], 1 + rnd.uniform(-0.03, 0.03))
    if bevel:
        for x in range(x0, x1):
            px[y0][x] = STEEL_L
            px[y1 - 1][x] = STEEL_D
        for y in range(y0, y1):
            px[y][x0] = sh(px[y][x0], 1.12) if y != y1 - 1 else px[y][x0]
            px[y][x1 - 1] = sh(px[y][x1 - 1], 0.85) if y != y0 else px[y][x1 - 1]


def rivet(px, x, y):
    px[y][x] = STEEL_L
    if y + 1 < 16 and x + 1 < 16:
        px[y + 1][x + 1] = STEEL_DD
    if x + 1 < 16:
        px[y][x + 1] = STEEL_D


def rim(px):
    """Steel rim along the top of a side face (rows 0-2) with rivets."""
    steel(px, 0, 0, 16, 3)
    for x in (2, 7, 13):
        rivet(px, x, 1)


# ---- top ----
top = canvas()
# dark oak frame
for y in range(16):
    for x in range(16):
        top[y][x] = sh(WOOD[(x + y) % 4], 1 + rnd.uniform(-0.05, 0.05))
for i in range(16):
    for c in ((0, i), (15, i), (i, 0), (i, 15)):
        top[c[1]][c[0]] = sh(POST[i % 3], 1 + rnd.uniform(-0.05, 0.05))
steel(top, 1, 1, 15, 15)
for (x, y) in ((2, 2), (12, 2), (2, 12), (12, 12)):
    rivet(top, x, y)
# circuit board (top left): green board, copper traces, a chip with a redstone dot
for y in range(4, 10):
    for x in range(4, 10):
        top[y][x] = sh(PCB, 1 + rnd.uniform(-0.05, 0.05))
for x in range(4, 10):
    top[4][x] = PCB_L
    top[9][x] = PCB_D
for y in range(4, 10):
    top[y][4] = PCB_L
    top[y][9] = PCB_D
for (x, y) in ((5, 5), (6, 5), (7, 5), (5, 6), (5, 7), (8, 7), (8, 8), (7, 8), (6, 8)):
    top[y][x] = CU_L if (x + y) % 2 else CU
top[6][6] = CHIP
top[6][7] = CHIP
top[7][6] = CHIP
top[7][7] = CHIP
top[5][8] = RS_L  # a redstone dot lights the board
# wrench lying diagonally (bottom right)
for i, (x, y) in enumerate(((6, 13), (7, 12), (8, 11), (9, 10), (10, 9))):
    top[y][x] = SHINE if i % 2 else STEEL_L
    top[y + 1][x] = STEEL_DD
for (x, y) in ((11, 8), (12, 8), (11, 7), (12, 9), (13, 8)):
    top[y][x] = SHINE
top[8][12] = STEEL_D  # jaw opening
top[10][13] = STEEL_DD
save(top, "tech_table_top")

# ---- front ----
front = canvas()
planks(front, 3, 16)
posts(front, 3, 16)
rim(front)
# drawer
for y in range(5, 11):
    for x in range(3, 13):
        front[y][x] = sh(WOOD[0], 1.08 + rnd.uniform(-0.04, 0.04))
for x in range(3, 13):
    front[5][x] = WOOD_DARK
    front[10][x] = WOOD_SEAM
for y in range(5, 11):
    front[y][3] = WOOD_DARK
    front[y][12] = WOOD_SEAM
for x in range(6, 10):
    front[7][x] = CU_L
    front[8][x] = CU_D if x in (6, 9) else CU
front[8][7] = CU_D
front[8][8] = CU_D
# indicator lamps (bottom right): lit amber, redstone
for (x, y, c, cl) in ((10, 12, AMBER, AMBER_L), (12, 12, RS, RS_L)):
    front[y][x] = cl
    front[y][x + 1] = c
    front[y + 1][x] = c
    front[y + 1][x + 1] = sh(c, 0.7)
# little gauge (bottom left)
for (x, y) in ((3, 12), (4, 12), (5, 12), (3, 13), (5, 13), (3, 14), (4, 14), (5, 14)):
    front[y][x] = STEEL_D
front[13][4] = STEEL_L
save(front, "tech_table_front")

# ---- side ----
side = canvas()
planks(side, 3, 16)
posts(side, 3, 16)
rim(side)
# tool rack bar
for x in range(2, 14):
    side[4][x] = STEEL_D
    side[5][x] = STEEL_DD if x % 3 == 0 else sh(WOOD_SEAM, 1.0)
# screwdriver: red grip, steel shaft
for y in range(6, 9):
    side[y][4] = GRIP_L if y == 6 else GRIP
for y in range(9, 13):
    side[y][4] = SHINE if y % 2 else STEEL_L
side[13][4] = STEEL_D
# pliers: steel jaws up, joint, red handles spreading down
for (x, y) in ((8, 6), (8, 7)):
    side[y][x] = SHINE
side[8][8] = STEEL_D
for (x, y) in ((7, 9), (9, 9), (7, 10), (9, 10), (6, 11), (10, 11), (6, 12), (10, 12)):
    side[y][x] = GRIP_L if y == 9 else GRIP
# coil of copper wire
for (x, y) in ((11, 8), (12, 8), (13, 9), (13, 10), (13, 11), (12, 12), (11, 12), (10, 11), (10, 10), (10, 9)):
    side[y][x] = CU_L if (x + y) % 2 else CU
for (x, y) in ((11, 9), (12, 9), (12, 10), (12, 11), (11, 11), (11, 10)):
    side[y][x] = CU_D
save(side, "tech_table_side")

# ---- bottom ----
bottom = canvas()
planks(bottom, 0, 16)
save(bottom, "tech_table_bottom")

# ---- models / states ----
model = {
    "parent": "minecraft:block/orientable_with_bottom",
    "textures": {
        "top": "powers_unchained:block/tech_table_top",
        "front": "powers_unchained:block/tech_table_front",
        "side": "powers_unchained:block/tech_table_side",
        "bottom": "powers_unchained:block/tech_table_bottom",
    },
}
with open(os.path.join(ASSETS, "models", "block", "tech_table.json"), "w") as f:
    json.dump(model, f, indent=2)
    f.write("\n")
states = {"variants": {}}
for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
    v = {"model": "powers_unchained:block/tech_table"}
    if y:
        v["y"] = y
    states["variants"]["facing=" + facing] = v
with open(os.path.join(ASSETS, "blockstates", "tech_table.json"), "w") as f:
    json.dump(states, f, indent=2)
    f.write("\n")
with open(os.path.join(ASSETS, "items", "tech_table.json"), "w") as f:
    json.dump({"model": {"type": "minecraft:model", "model": "powers_unchained:block/tech_table"}}, f, indent=2)
    f.write("\n")
print("tech table assets written")
