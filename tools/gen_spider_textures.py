"""Generates placeholder Spider-Man suit textures (player-skin UV layout) and item icons."""
import os
import sys
from PIL import Image

OUT = sys.argv[1]

T = (0, 0, 0, 0)
RED = (196, 18, 44, 255)
RED_D = (122, 8, 24, 255)
BLUE = (30, 62, 156, 255)
BLUE_D = (19, 40, 104, 255)
BLACK = (16, 16, 22, 255)
WHITE = (238, 240, 246, 255)
SILVER = (170, 176, 190, 255)


def new():
    return Image.new("RGBA", (64, 64), T)


def fill(img, x, y, w, h, c):
    for i in range(x, x + w):
        for j in range(y, y + h):
            img.putpixel((i, j), c)


def web(img, x, y, w, h, base=RED, line=RED_D, ou=0, ov=0):
    """Red area with a web grid."""
    for i in range(w):
        for j in range(h):
            u, v = i + ou, j + ov
            # diagonal lattice reads as webbing (a square grid looks like plaid)
            img.putpixel((x + i, y + j), line if ((u + v) % 6 == 0 or (u - v) % 6 == 0) else base)


def blue(img, x, y, w, h):
    for i in range(w):
        for j in range(h):
            img.putpixel((x + i, y + j), BLUE_D if (i + j) % 7 == 0 else BLUE)


# ---------- cube face helpers (skin layout) ----------
def cube(ox, oy, w, h, d):
    """Returns face rects of a box UV at (ox,oy) with size w x h x d."""
    return {
        "top": (ox + d, oy, w, d),
        "bottom": (ox + d + w, oy, w, d),
        "right": (ox, oy + d, d, h),
        "front": (ox + d, oy + d, w, h),
        "left": (ox + d + w, oy + d, d, h),
        "back": (ox + d + w + d, oy + d, w, h),
    }


def paint_head(img):
    f = cube(0, 0, 8, 8, 8)
    for name, (x, y, w, h) in f.items():
        web(img, x, y, w, h)
    fx, fy, _, _ = f["front"]
    eye = [
        ". B B . . B B .",
        "B W W B B W W B",
        ". B W B B W B .",
        ". . B . . B . .",
    ]
    for r, row in enumerate(eye):
        for c, ch in enumerate(row.split()):
            if ch == "B":
                img.putpixel((fx + c, fy + 2 + r), BLACK)
            elif ch == "W":
                img.putpixel((fx + c, fy + 2 + r), WHITE)


def paint_body(img):
    f = cube(16, 16, 8, 12, 4)
    x, y, w, h = f["front"]
    web(img, x, y, w, h)
    blue(img, x, y + 7, 2, 5)
    blue(img, x + 6, y + 7, 2, 5)
    # black spider emblem
    for (c, r) in [(3, 2), (4, 2), (3, 3), (4, 3), (3, 4), (4, 4), (3, 5), (4, 5),
                   (2, 1), (5, 1), (2, 6), (5, 6), (1, 3), (6, 3), (1, 4), (6, 4)]:
        img.putpixel((x + c, y + r), BLACK)
    # sides: red top, blue lower
    for side in ("right", "left"):
        x, y, w, h = f[side]
        web(img, x, y, w, 5)
        blue(img, x, y + 5, w, 7)
    # back: red with large red spider outline area, blue lower sides
    x, y, w, h = f["back"]
    web(img, x, y, w, h)
    blue(img, x, y + 7, 2, 5)
    blue(img, x + 6, y + 7, 2, 5)
    for (c, r) in [(3, 3), (4, 3), (3, 4), (4, 4), (2, 2), (5, 2), (2, 5), (5, 5)]:
        img.putpixel((x + c, y + r), RED_D)
    x, y, w, h = f["top"]
    web(img, x, y, w, h)
    x, y, w, h = f["bottom"]
    blue(img, x, y, w, h)


def paint_arm(img, ox, oy, width):
    f = cube(ox, oy, width, 12, 4)
    for name in ("front", "back", "right", "left"):
        x, y, w, h = f[name]
        web(img, x, y, w, 4)
        blue(img, x, y + 4, w, 4)
        web(img, x, y + 8, w, 4, ov=0)
    # inner side (towards body) mostly blue
    x, y, w, h = f["left"] if ox == 40 else f["right"]
    blue(img, x, y, w, 8)
    x, y, w, h = f["top"]
    web(img, x, y, w, h)
    x, y, w, h = f["bottom"]
    web(img, x, y, w, h)


def paint_legs_blue(img):
    for (ox, oy) in ((0, 16), (16, 48)):
        for (x, y, w, h) in cube(ox, oy, 4, 12, 4).values():
            blue(img, x, y, w, h)


def paint_boots(img):
    for (ox, oy) in ((0, 16), (16, 48)):
        f = cube(ox, oy, 4, 12, 4)
        for name in ("front", "back", "right", "left"):
            x, y, w, h = f[name]
            web(img, x, y + 6, w, 6)
            fill(img, x, y + 6, w, 1, RED_D)
        x, y, w, h = f["bottom"]
        web(img, x, y, w, h)


def main_texture(slim):
    img = new()
    paint_head(img)
    paint_body(img)
    aw = 3 if slim else 4
    paint_arm(img, 40, 16, aw)  # right arm
    paint_arm(img, 32, 48, aw)  # left arm
    paint_legs_blue(img)
    return img


def save(img, *path):
    p = os.path.join(OUT, *path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


suit_dir = ("assets", "powers_unchained", "textures", "suit", "spider_man")
save(main_texture(False), *suit_dir, "spider_man.png")
save(main_texture(True), *suit_dir, "spider_man_slim.png")
boots = new()
paint_boots(boots)
save(boots, *suit_dir, "spider_man_boots.png")


# ---------- item icons (16x16) ----------
def icon(rows, palette):
    img = Image.new("RGBA", (16, 16), T)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), palette[ch])
    return img


PAL = {"R": RED, "r": RED_D, "B": BLUE, "b": BLUE_D, "K": BLACK, "W": WHITE, "S": SILVER}

MASK = [
    "................",
    ".....KKKKKK.....",
    "....KRrRRrRK....",
    "...KRRrRRrRRK...",
    "..KRrrrrrrrrRK..",
    "..KRRrRRrRRrRK..",
    "..KKKKRrrKKKKK..",
    "..KWWWKRRKWWWK..",
    "..KKWWKrrKWWKK..",
    "..KRKWKRRKWKRK..",
    "..KRRKrRRrKRRK..",
    "...KRrrrrrrRK...",
    "...KRRrRRrRRK...",
    "....KRrRRrRK....",
    ".....KKKKKK.....",
    "................",
]
SUIT = [
    "................",
    "..KKKK....KKKK..",
    ".KRRRRKKKKRRRRK.",
    ".KRrRRRrrRRRrRK.",
    ".KRRrRKRRKRrRRK.",
    ".KBRRKKKKKKRRBK.",
    ".KBRrRKKKKRrRBK.",
    ".KBKRRKKKKRRKBK.",
    ".KBKRKRRRRKRKBK.",
    ".KKKBRRrrRRBKKK.",
    "...KBBRRRRBBK...",
    "...KBBRrrRBBK...",
    "...KBBRRRRBBK...",
    "...KBBBRRBBBK...",
    "...KKKKKKKKKK...",
    "................",
]
PANTS = [
    "................",
    "...KKKKKKKKKK...",
    "...KBBBBBBBBK...",
    "...KBbBBBBbBK...",
    "...KBBBKKBBBK...",
    "...KBBBKKBBBK...",
    "...KBbBKKBbBK...",
    "...KBBBKKBBBK...",
    "...KBBBKKBBBK...",
    "...KBBbKKbBBK...",
    "...KBBBKKBBBK...",
    "...KBBBKKBBBK...",
    "...KBbBKKBbBK...",
    "...KBBBKKBBBK...",
    "...KKKKKKKKKK...",
    "................",
]
BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "..KKKKK..KKKKK..",
    "..KRrRK..KRrRK..",
    "..KRRRK..KRRRK..",
    "..KrrrK..KrrrK..",
    "..KRrRK..KRrRK..",
    "..KRRRK..KRRRK..",
    ".KKRrRK..KRrRKK.",
    "KRRRRRK..KRRRRRK",
    "KrrrrrK..KrrrrrK",
    "KRRRRRK..KRRRRRK",
    "KKKKKKK..KKKKKKK",
    "................",
]
item_dir = ("assets", "powers_unchained", "textures", "item")
save(icon(MASK, PAL), *item_dir, "spider_man_mask.png")
save(icon(SUIT, PAL), *item_dir, "spider_man_chestplate.png")
save(icon(PANTS, PAL), *item_dir, "spider_man_pants.png")
save(icon(BOOTS, PAL), *item_dir, "spider_man_boots.png")
print("ok")
