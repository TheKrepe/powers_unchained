"""Web-shooter related assets: item icons (web-shooters, web cartridge, web projectiles), web selection menu icons,
the sticky-web entity overlay and the webbed screen overlay. (The worn web-shooter textures in textures/gadget are
hand-made, not generated.)

Usage: python3 gen_web_assets.py <resources root>
"""
import json
import math
import os
import sys

from PIL import Image, ImageDraw

RES = sys.argv[1]
A = os.path.join(RES, "assets", "powers_unchained")


def rgb(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def save(img, *path):
    p = os.path.join(A, *path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


def pixels(rows, pal):
    img = Image.new("RGBA", (len(rows[0]), len(rows)), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), pal[ch])
    return img


PAL = {
    "K": rgb("#1b1d22"), "S": rgb("#c3c8cf"), "s": rgb("#868c96"), "H": rgb("#eef1f4"),
    "R": rgb("#c4202c"), "r": rgb("#7e1018"), "W": rgb("#f4f6f8"), "w": rgb("#b9c2cb"),
    "G": rgb("#3b3f46"), "g": rgb("#5a606a"), "C": rgb("#9fe8f0"), "c": rgb("#5fc2cf"),
}

WEB_SHOOTERS = [
    "................",
    "................",
    "..KKKKKK........",
    ".KHSSSSsK.......",
    ".KRRRRRrK.......",
    ".KSSGGSsK.......",
    "..KKKKKK........",
    "........KKKKKK..",
    ".......KHSSSSsK.",
    ".......KRRRRRrK.",
    ".......KSSGGSsK.",
    "........KKKKKK..",
    "................",
    "................",
    "................",
    "................",
]
WEB_CARTRIDGE = [
    "................",
    "......KKKK......",
    ".....KsSSsK.....",
    ".....KKKKKK.....",
    "....KGgGGgGK....",
    "....KGWWWwGK....",
    "....KGWHWwGK....",
    "....KGWWWwGK....",
    "....KGWWHwGK....",
    "....KGWWWwGK....",
    "....KGwwwwGK....",
    "....KGgGGgGK....",
    ".....KKKKKK.....",
    ".....KsRRsK.....",
    "......KKKK......",
    "................",
]
for name, rows in (("web_shooters", WEB_SHOOTERS), ("web_cartridge", WEB_CARTRIDGE)):
    save(pixels(rows, PAL), "textures", "item", name + ".png")
    for path, data in (
            (("items", name + ".json"), {"model": {"type": "minecraft:model", "model": f"powers_unchained:item/{name}"}}),
            (("models", "item", name + ".json"), {"parent": "minecraft:item/generated",
                                                  "textures": {"layer0": f"powers_unchained:item/{name}"}}),
    ):
        p = os.path.join(A, *path)
        os.makedirs(os.path.dirname(p), exist_ok=True)
        with open(p, "w") as f:
            json.dump(data, f, indent=2)
            f.write("\n")


# ---- menu icons 32x32: a web disc, impact = spiky burst, sticky = web blob with drips -------------------------
def web_disc(draw, cx, cy, radius, spokes, rings, color, width=1):
    pts = []
    for i in range(spokes):
        a = 2 * math.pi * i / spokes
        pts.append((cx + math.cos(a) * radius, cy + math.sin(a) * radius))
        draw.line([(cx, cy), pts[-1]], fill=color, width=width)
    for r in range(1, rings + 1):
        f = r / (rings + 0.3)
        ring = [(cx + (x - cx) * f, cy + (y - cy) * f) for x, y in pts]
        draw.line(ring + [ring[0]], fill=color, width=width)


def icon(kind):
    big = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    if kind == "impact":
        for i in range(8):  # burst spikes
            a = 2 * math.pi * i / 8 + math.pi / 8
            d.polygon([(64 + math.cos(a - 0.18) * 30, 64 + math.sin(a - 0.18) * 30),
                       (64 + math.cos(a) * 60, 64 + math.sin(a) * 60),
                       (64 + math.cos(a + 0.18) * 30, 64 + math.sin(a + 0.18) * 30)], fill=rgb("#b9c2cb"))
        d.ellipse([28, 28, 100, 100], fill=rgb("#e9edf1"))
        web_disc(d, 64, 64, 36, 8, 3, rgb("#8a939e"), 4)
        d.ellipse([54, 54, 74, 74], fill=rgb("#c4202c"))
    else:
        d.ellipse([22, 18, 106, 96], fill=rgb("#eef3f4"))
        for x, h in ((36, 30), (56, 22), (80, 34), (96, 18)):  # drips
            d.rounded_rectangle([x - 6, 80, x + 6, 80 + h], radius=6, fill=rgb("#eef3f4"))
        web_disc(d, 64, 57, 38, 8, 3, rgb("#7fd3dc"), 4)
        d.ellipse([56, 49, 72, 65], fill=rgb("#5fc2cf"))
    small = big.resize((32, 32), Image.NEAREST)
    # dark outline for readability on any background
    out = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    for x in range(32):
        for y in range(32):
            if small.getpixel((x, y))[3] == 0 and any(
                    0 <= x + dx < 32 and 0 <= y + dy < 32 and small.getpixel((x + dx, y + dy))[3] > 0
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                out.putpixel((x, y), rgb("#14181d", 220))
    out.alpha_composite(small)
    return out


save(icon("impact"), "textures", "gui", "web", "impact_web.png")
save(icon("sticky"), "textures", "gui", "web", "sticky_web.png")

# ---- web shots (drawn as a four-leaved star, texture centre = web centre) ------------------------------------
def web_texture(size, spokes, rings, seed):
    import random
    rnd = random.Random(seed)
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    c = (size - 1) / 2
    shades = [rgb("#ffffff"), rgb("#ebeef0"), rgb("#d3d8dc"), rgb("#b8bec4")]
    ends = []
    for i in range(spokes):
        a = 2 * math.pi * i / spokes + rnd.uniform(-0.12, 0.12)
        r = c + rnd.uniform(-0.6, 0.4)
        ends.append((a, r))
        d.line([(c, c), (c + math.cos(a) * r, c + math.sin(a) * r)], fill=shades[0])
    for k in range(1, rings + 1):
        f = k / (rings + 0.5)
        pts = [(c + math.cos(a) * r * f * rnd.uniform(0.92, 1.05), c + math.sin(a) * r * f * rnd.uniform(0.92, 1.05)) for a, r in ends]
        d.line(pts + [pts[0]], fill=shades[min(k, len(shades) - 1)])
    return img


save(web_texture(16, 8, 3, 7), "textures", "entity", "web", "web.png")
save(web_texture(24, 10, 4, 11), "textures", "entity", "web", "web_large.png")
print("ok")
