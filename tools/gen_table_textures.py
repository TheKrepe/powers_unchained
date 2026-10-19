"""Custom 16x16 textures for the suit tailoring table concepts (vanilla-like palettes)."""
import os
import random
import sys
from PIL import Image

OUT = os.path.join(sys.argv[1], "assets", "powers_unchained", "textures", "block")
os.makedirs(OUT, exist_ok=True)
rnd = random.Random(1337)


def hexc(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (c[3],)


def noisy(c, amount=0.06):
    return shade(c, 1 + rnd.uniform(-amount, amount))


def new(fill=None):
    return Image.new("RGBA", (16, 16), fill or (0, 0, 0, 0))


def save(img, name):
    img.save(os.path.join(OUT, name + ".png"))


# --- cutting mat: green with a light grid and yellow ruler ticks along the edges ---------------
def cutting_mat():
    g, gl, gd, y = hexc("#3d7348"), hexc("#5e9d68"), hexc("#2c5636"), hexc("#d9c25a")
    im = new()
    for x in range(16):
        for z in range(16):
            c = noisy(g, 0.05)
            if x % 4 == 2 or z % 4 == 2:
                c = gl
            if x in (0, 15) or z in (0, 15):
                c = gd
            im.putpixel((x, z), c)
    for x in range(1, 15, 2):
        im.putpixel((x, 1), y)
    for z in range(1, 15, 2):
        im.putpixel((1, z), y)
    return im


# --- sewing machine: atlas of black enamel / gold decor / chrome --------------------------------
def sewing_machine():
    """Atlas: rows 0-3 plain enamel, rows 4-7 enamel with a gold pinstripe, rows 8-11 gold
    ornament on enamel, rows 12-15 chrome."""
    k, kh, kd = hexc("#202027"), hexc("#343440"), hexc("#141418")
    gold, goldd = hexc("#d7a93c"), hexc("#9c7424")
    cr, crd, crh = hexc("#b9bec7"), hexc("#7d838e"), hexc("#e6e9ee")
    im = new()
    for x in range(16):
        for y in range(12):
            im.putpixel((x, y), kh if y in (0, 4, 8) else noisy(k, 0.06))
    for x in range(16):
        im.putpixel((x, 5), gold)
        im.putpixel((x, 7), kd)
    for x in range(1, 15):
        if x % 4 in (1, 2):
            im.putpixel((x, 9), gold)
        if x % 4 in (0, 3):
            im.putpixel((x, 10), goldd)
    for x in range(16):
        for y in range(12, 16):
            im.putpixel((x, y), crh if y == 12 else (crd if y == 15 else noisy(cr, 0.05)))
    return im


# --- thread spools: four 4px-wide spool strips (wood ends + colored thread) ---------------------
def thread_spools():
    wood, woodd = hexc("#b48a55"), hexc("#7f5f36")
    colors = ["#b02e26", "#3c44aa", "#f9d43a", "#5e7c16"]
    im = new()
    for i, col in enumerate(colors):
        c = hexc(col)
        for x in range(i * 4, i * 4 + 4):
            for y in range(16):
                if y < 2 or y > 13:
                    p = wood if (x + y) % 3 else woodd
                else:
                    p = shade(c, 1.15) if (x + y) % 4 == 0 else (shade(c, 0.8) if (x - y) % 5 == 0 else c)
                im.putpixel((x, y), p)
    return im


# --- dress form: linen with seams ---------------------------------------------------------------
def dress_form():
    lin, lind, seam = hexc("#dccaa4"), hexc("#c4b088"), hexc("#a58e66")
    im = new()
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), noisy(lin if (x * 7 + y * 3) % 5 else lind, 0.04))
    for y in range(16):
        im.putpixel((7, y), seam)
    for x in range(16):
        im.putpixel((x, 9), seam)
    return im


def measuring_tape():
    yl, blk = hexc("#e8c84a"), hexc("#2a2a2a")
    im = new()
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), noisy(yl, 0.05))
        if x % 2 == 0:
            im.putpixel((x, 0), blk)
            im.putpixel((x, 15), blk)
        if x % 4 == 0:
            im.putpixel((x, 1), blk)
    return im


# --- suit pattern sheet: paper with grid and a red suit outline --------------------------------
def suit_pattern():
    paper, grid, red = hexc("#efe6cc"), hexc("#d9ccab"), hexc("#b3332b")
    im = new()
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), grid if (x % 4 == 0 or y % 4 == 0) else noisy(paper, 0.03))
    outline = [
        "................",
        "......####......",
        "......#..#......",
        "......####......",
        "...##########...",
        "..#....##....#..",
        "..#.#..##..#.#..",
        "..#.#..##..#.#..",
        "..###..##..###..",
        "....########....",
        "....#..##..#....",
        "....#..##..#....",
        "....#..##..#....",
        "....##.##.##....",
        "....###..###....",
        "................",
    ]
    for y, row in enumerate(outline):
        for x, ch in enumerate(row):
            if ch == "#":
                im.putpixel((x, y), red)
    return im


# --- tailor workbench front: dark oak frame, two drawers with brass knobs -----------------------
def workbench_front():
    p, pd, pl = hexc("#4f3218"), hexc("#2f1d0d"), hexc("#6a4624")
    brass, brassd = hexc("#e1b84a"), hexc("#a27c26")
    im = new()
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), noisy(p, 0.07))
    for x in range(16):
        im.putpixel((x, 0), pl)
        im.putpixel((x, 15), pd)
    for y in range(16):
        im.putpixel((0, y), pd)
        im.putpixel((15, y), pd)
    for top in (2, 9):
        for x in range(2, 14):
            im.putpixel((x, top), pd)
            im.putpixel((x, top + 5), pd)
        for y in range(top, top + 6):
            im.putpixel((2, y), pd)
            im.putpixel((13, y), pd)
        for x in range(3, 13):
            for y in range(top + 1, top + 5):
                im.putpixel((x, y), noisy(pl, 0.05))
        im.putpixel((7, top + 2), brass)
        im.putpixel((8, top + 2), brass)
        im.putpixel((7, top + 3), brassd)
        im.putpixel((8, top + 3), brassd)
    return im


# --- tailor workbench side: shelf cubbies with spools of thread -------------------------------
def workbench_side():
    p, pd = hexc("#4f3218"), hexc("#241508")
    colors = ["#b02e26", "#f9d43a", "#3c44aa", "#5e7c16", "#c74ebd", "#f9801d"]
    im = new()
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), noisy(p, 0.07))
    i = 0
    for top in (1, 9):
        for left in (1, 6, 11):
            for x in range(left, left + 4):
                for y in range(top, top + 6):
                    im.putpixel((x, y), pd)
            c = hexc(colors[i])
            i += 1
            for x in range(left + 1, left + 3):
                im.putpixel((x, top + 1), hexc("#b48a55"))
                for y in range(top + 2, top + 5):
                    im.putpixel((x, y), c if (x + y) % 3 else shade(c, 0.8))
                im.putpixel((x, top + 5), hexc("#b48a55"))
    return im


for name, fn in [
    ("tailoring_cutting_mat", cutting_mat),
    ("sewing_machine", sewing_machine),
    ("thread_spools", thread_spools),
    ("dress_form", dress_form),
    ("measuring_tape", measuring_tape),
    ("suit_pattern", suit_pattern),
    ("tailoring_workbench_front", workbench_front),
    ("tailoring_workbench_side", workbench_side),
]:
    save(fn(), name)
print("ok")
