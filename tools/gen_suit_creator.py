"""Suit Creator textures: a vanilla job-site style full block (like the smithing / fletching tables).
Dark wood cabinet with an iron top band, tools of the trade on the front, thread spool shelves on the sides,
a cutting mat with a fabric piece on top.

Usage: python3 gen_suit_creator.py <resources root>   (writes assets/powers_unchained/textures/block/suit_creator_*.png)
"""
import os
import random
import sys

from PIL import Image

OUT = os.path.join(sys.argv[1], "assets", "powers_unchained", "textures", "block")
os.makedirs(OUT, exist_ok=True)
rnd = random.Random(26_3)


def hexc(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)


# --- palette (sampled to sit next to vanilla dark-oak / smithing-table pixels) -------------------------
WOOD = hexc("#5c3d22")
WOOD_L = hexc("#704b2b")
WOOD_D = hexc("#3f2914")
WOOD_DD = hexc("#2c1c0c")
IRON_O = hexc("#2a2a2f")
IRON = hexc("#46464d")
IRON_L = hexc("#6a6a73")
IRON_H = hexc("#8d8d96")
COPPER = hexc("#c46d43")
COPPER_D = hexc("#8e4a2b")

PAL = {
    "O": IRON_O, "I": IRON, "i": IRON_L, "h": IRON_H,
    "c": COPPER, "C": COPPER_D,
    "D": WOOD_DD, "d": WOOD_D,
    # tools
    "s": hexc("#d5d9df"), "S": hexc("#8c929b"),       # steel
    "r": hexc("#b3312c"), "R": hexc("#7a1f1b"),       # red (handles, thread, fabric)
    "y": hexc("#e3c04a"), "Y": hexc("#a8862a"),       # measuring tape
    "k": hexc("#262626"),
    "p": hexc("#c49a62"), "P": hexc("#8f6a3c"),       # spool wood
    "b": hexc("#3c50b4"), "B": hexc("#28357a"),       # blue thread
    "g": hexc("#e8d046"), "G": hexc("#a8901f"),       # yellow thread
    "e": hexc("#5e8a1e"), "E": hexc("#3e5c12"),       # green thread
    "m": hexc("#a43fb0"), "M": hexc("#6c2875"),       # purple thread
    "w": hexc("#e9ecec"), "W": hexc("#b2b8ba"),       # white thread / chalk
    # cutting mat
    "t": hexc("#3e6b46"), "T": hexc("#2e5236"), "l": hexc("#5c9466"), "u": hexc("#d9c25a"),
}


def wood_px(x, y):
    """Horizontal plank grain: boards 4 px high with a dark seam, light/dark streaks."""
    if y % 4 == 3:
        return WOOD_D
    n = (x * 7 + y * 13 + (y // 4) * 5) % 11
    if n == 0:
        return WOOD_L
    if n in (3, 7) and (x + y) % 3 == 0:
        return WOOD_D
    return shade(WOOD, 1 + rnd.uniform(-0.04, 0.04))


def iron_px(x, y):
    return shade(IRON, 1 + rnd.uniform(-0.05, 0.05))


def paint(rows, fill):
    im = Image.new("RGBA", (16, 16))
    for y, row in enumerate(rows):
        assert len(row) == 16, (row, len(row))
        for x, ch in enumerate(row):
            if ch == ".":
                im.putpixel((x, y), fill(x, y))
            elif ch == "I":
                im.putpixel((x, y), iron_px(x, y))
            else:
                im.putpixel((x, y), PAL[ch])
    return im


# iron band shared by front/side/back (rows 0-3), wood below, darker frame edges
BAND = [
    "OiiiiiiiiiiiiiiO",
    "OIhIIIIIIIIIIhIO",
    "OIIIIIIIIIIIIIIO",
    "OOOOOOOOOOOOOOOO",
]

FRONT = BAND + [
    # scissors (left), measuring tape (middle), thread spool + needle (right), each on a hook
    "d..S....S....S.d",
    "d.rr.rr.yy..pPpd",
    "d.r.rr.ryk..rrrd",
    "d.rr.rr.yy..rRrd",
    "d...sSs.yk..rrrd",
    "d...s.s.yy..pPpd",
    "d...s.s.yk...s.d",
    "d...sSs.yy...s.d",
    "d....s..Yk...sRd",
    "d.......S.....Rd",
    "d..............d",
    "DdddddddddddddDD",
]

SIDE = BAND + [
    # two shelves of thread spools
    "dDDDDDDDDDDDDDDd",
    "dDpPpDpPpDpPpDDd",
    "dDrRrDbBbDgGgDDd",
    "dDrrrDbbbDgggDDd",
    "dDpPpDpPpDpPpDDd",
    "dddddddddddddddd",
    "dDDDDDDDDDDDDDDd",
    "dDpPpDpPpDpPpDDd",
    "dDeEeDmMmDwWwDDd",
    "dDeeeDmmmDwwwDDd",
    "dDpPpDpPpDpPpDDd",
    "DdddddddddddddDD",
]

BACK = BAND + [
    "d..............d",
    "d..............d",
    "d..............d",
    "d..............d",
    "d..............d",
    "d..............d",
    "d..............d",
    "d..............d",
    "d..............d",
    "d..............d",
    "d..............d",
    "DdddddddddddddDD",
]

# top: iron rim with copper corner rivets, green cutting mat with grid + ruler, a red fabric piece with
# a chalk pattern line and a pin
TOP = [
    "OiiiiiiiiiiiiiiO",
    "icTTTTTTTTTTTTci",
    "iTtlttltttlttltI",
    "iTurrrrrrrrrrttI",
    "iTlrwrwrwrwrrtlI",
    "iTurrrrrrrrrwttI",
    "iTlrwrrrrrrrrtlI",
    "iTurrrrrrrrrwttI",
    "iTlrwrrrrrrrrtlI",
    "iTurrrrrrrrrwttI",
    "iTlrrwrwrwrwrtlI",
    "iTuRRRRRRRRRRttI",
    "iTlllllllllllllI",
    "iTtutututututltI",
    "icTTTTTTTTTTTTcI",
    "OIIIIIIIIIIIIIIO",
]


def mat_fix(im):
    # vary the mat green a little so it isn't flat
    for y in range(16):
        for x in range(16):
            if im.getpixel((x, y)) == PAL["t"]:
                im.putpixel((x, y), shade(PAL["t"], 1 + rnd.uniform(-0.05, 0.05)))
    return im


def front_fill(x, y):
    return wood_px(x, y)


images = {
    "suit_creator_front": paint(FRONT, front_fill),
    "suit_creator_side": paint(SIDE, front_fill),
    "suit_creator_back": paint(BACK, front_fill),
    "suit_creator_top": mat_fix(paint(TOP, iron_px)),
}
for name, im in images.items():
    im.save(os.path.join(OUT, name + ".png"))
print("ok")
