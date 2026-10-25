"""Suit Creator GUI sprites (generic, shared by every suit).

Sprites go to textures/gui/sprites/suit_creator/ (GUI atlas, id powers_unchained:suit_creator/<name>).
Usage: python3 tools/gen_suit_creator_gui.py <resources root>
"""
import os
import sys
from PIL import Image

ROOT = sys.argv[1]
A = os.path.join(ROOT, "assets", "powers_unchained", "textures")
SPR = os.path.join(A, "gui", "sprites", "suit_creator")
os.makedirs(SPR, exist_ok=True)


def hx(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def from_art(art, pal):
    rows = [r for r in art.strip("\n").split("\n")]
    w = max(len(r) for r in rows)
    im = Image.new("RGBA", (w, len(rows)))
    for y, r in enumerate(rows):
        for x, c in enumerate(r):
            if c in pal:
                im.putpixel((x, y), pal[c])
    return im


def save(im, name, folder=SPR):
    im.save(os.path.join(folder, name + ".png"))


# ---- part toggles (10x10): generic mask / shirt / pants / boots silhouettes ----
PARTS = {
    "head": """
..######..
.########.
.##OO#OO#.
.#OOO#OOO.
.########.
.########.
..######..
...####...
..........
..........""",
    "chest": """
.###..###.
##########
##########
.########.
..######..
..######..
..######..
..######..
..######..
..........""",
    "legs": """
.########.
.########.
.###..###.
.###..###.
.###..###.
.###..###.
.###..###.
.###..###.
.###..###.
..........""",
    "feet": """
..........
.###..###.
.###..###.
.###..###.
.###..###.
.###..###.
####.####.
####.####.
####.####.
..........""",
}
for name, art in PARTS.items():
    on = from_art(art, {"#": hx("#ffc24a"), "O": hx("#7a4a10")})
    off = from_art(art, {"#": hx("#5b6470"), "O": hx("#2a2f36")})
    # outline / shading for depth
    for im, dark in ((on, hx("#a8650f")), (off, hx("#3a4048"))):
        w, h = im.size
        px = im.load()
        for y in range(h - 1, 0, -1):
            for x in range(w):
                if px[x, y][3] and not px[x, y - 1][3] and False:
                    pass
        for x in range(w):
            for y in range(h - 1, -1, -1):
                if px[x, y][3] and px[x, y] != dark:
                    if y + 1 < h and not px[x, y + 1][3]:
                        px[x, y] = dark
    save(on.crop((0, 0, 10, 10)), f"part_{name}_on")
    save(off.crop((0, 0, 10, 10)), f"part_{name}_off")

# ---- poses (10x10) ----
POSES = {
    "stand": """
....##....
....##....
...####...
..#.##.#..
..#.##.#..
....##....
...#..#...
...#..#...
...#..#...
..........""",
    "run": """
.....##...
.....##...
...####.#.
..#.##.#..
....##....
....###...
...#...#..
..#.....#.
.#........
..........""",
}
for name, art in POSES.items():
    save(from_art(art, {"#": hx("#e8f6ff")}), f"pose_{name}")

# ---- small icons ----
save(from_art("""
.#####.
#.....#
#..#..#
#.....#
.#...#.
..#.#..
...#...""", {"#": hx("#ffc24a")}), "tag_icon")
save(from_art("""
##...##
#######
.#####.
.#####.
.#####.
.#####.
.......""", {"#": hx("#59e4ff")}), "suit_icon")
save(from_art("""
...#...
...#...
#######
.#####.
..###..
.##.##.
.#...#.""", {"#": hx("#ffd040")}), "star_on")
save(from_art("""
...#...
..#.#..
##...##
.#...#.
.#...#.
#..#..#
##.#.##""", {"#": hx("#6d7682")}), "star_off")
save(from_art("""
..####..
.#....#.
.#....#.
########
#oooooo#
#ooOOoo#
#ooOOoo#
#oooooo#
########""", {"#": hx("#9aa3ad"), "o": hx("#5c646e"), "O": hx("#2a2f36")}), "lock")
save(from_art("""
.........#
........#.
.......#..
......#...
.....#....
....#.....
...#......
.##.......
#.#.......
##........""", {"#": hx("#e8f6ff")}), "needle")
save(from_art("""
..##..
.#oo#.
#oOOo#
#oOOo#
.#oo#.
..##..""", {"#": hx("#3f8a1c"), "o": hx("#80ff20"), "O": hx("#d8ffa8")}), "xp_orb")

# ---- ghost silhouette (locked suit), 24x48, drawn scaled ----
g = Image.new("RGBA", (24, 48))
px = g.load()
def rect(x0, y0, x1, y1):
    for y in range(y0, y1):
        for x in range(x0, x1):
            px[x, y] = hx("#59e4ff", 150 if y % 2 else 95)
rect(8, 0, 16, 8)      # head
rect(8, 8, 16, 20)     # body
rect(4, 8, 8, 20)      # arms
rect(16, 8, 20, 20)
rect(8, 20, 12, 32)    # legs
rect(12, 20, 16, 32)
# edges brighter
for y in range(48):
    for x in range(24):
        if px[x, y][3]:
            edge = any(not (0 <= x + dx < 24 and 0 <= y + dy < 48) or not px[x + dx, y + dy][3]
                       for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if edge:
                px[x, y] = hx("#b8f4ff", 230)
g = g.crop((0, 0, 24, 32))
save(g, "ghost")

# ---- hero group icons (9x9), drawn after the group name in the catalog: suit_creator/group/<group> ----
GROUP = os.path.join(SPR, "group")
os.makedirs(GROUP, exist_ok=True)
save(from_art("""
.r.....r.
r..r.r..r
r.rRRRr.r
.rrRRRrr.
...rRr...
.rrRRRrr.
r.rRRRr.r
r..rrr..r
.r..r..r.""", {"r": hx("#d4252c"), "R": hx("#ff5a52")}), "spider_man", GROUP)

print("suit creator gui sprites written")
