"""Extra web textures: web zip line, cocoon tile, first-person web overlays (head webs / cocoon).

Usage: python3 gen_web_extra.py <resources root>
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

ROOT = os.path.join(sys.argv[1], "assets", "powers_unchained", "textures")
ENT = os.path.join(ROOT, "entity", "web")
MISC = os.path.join(ROOT, "misc")
os.makedirs(MISC, exist_ok=True)
rnd = random.Random(26)

# web zip line: plain white strand with a little shading across
line = Image.new("RGBA", (16, 16))
for x in range(16):
    for y in range(16):
        c = 235 - abs(x - 7.5) * 4
        line.putpixel((x, y), (int(c), int(c), int(c + 5), 235))
line.save(os.path.join(ENT, "web_line.png"))

# cocoon tile (16 px = half a block): milky film with criss-cross threads, tiles seamlessly
tile = Image.new("RGBA", (16, 16), (238, 240, 244, 120))
d = ImageDraw.Draw(tile)
for _ in range(7):
    x0, y0 = rnd.randrange(16), 0
    x1 = (x0 + rnd.randint(-10, 10))
    for dx in (-16, 0, 16):
        d.line([(x0 + dx, y0), (x1 + dx, 15)], fill=(250, 252, 255, 235))
for _ in range(7):
    y0 = rnd.randrange(16)
    y1 = y0 + rnd.randint(-10, 10)
    for dy in (-16, 0, 16):
        d.line([(0, y0 + dy), (15, y1 + dy)], fill=(250, 252, 255, 235))
tile.save(os.path.join(ENT, "web_cocoon.png"))


def web(draw, cx, cy, radius, spokes, rings, alpha, start=0.0, sweep=2 * math.pi):
    angles = [start + sweep * i / (spokes - (0 if sweep >= 2 * math.pi else 1)) for i in range(spokes)]
    for a in angles:
        draw.line([(cx, cy), (cx + math.cos(a) * radius, cy + math.sin(a) * radius)], fill=(245, 247, 250, alpha), width=2)
    for r in range(1, rings + 1):
        rr = radius * r / rings * (0.9 + rnd.random() * 0.1)
        pts = [(cx + math.cos(a) * rr * (0.92 + rnd.random() * 0.08), cy + math.sin(a) * rr * (0.92 + rnd.random() * 0.08))
               for a in angles]
        for p, q in zip(pts, pts[1:] + ([pts[0]] if sweep >= 2 * math.pi else [])):
            draw.line([p, q], fill=(240, 243, 248, alpha - 40), width=1)


# head webs: webs growing in from the corners and edges, clear middle
over = Image.new("RGBA", (256, 256))
d = ImageDraw.Draw(over)
web(d, 0, 0, 150, 7, 8, 230, 0, math.pi / 2)
web(d, 256, 0, 120, 6, 7, 230, math.pi / 2, math.pi / 2)
web(d, 0, 256, 120, 6, 7, 230, -math.pi / 2, math.pi / 2)
web(d, 256, 256, 150, 7, 8, 230, math.pi, math.pi / 2)
web(d, 128, 0, 60, 6, 4, 200, 0, math.pi)
over.save(os.path.join(MISC, "web_overlay.png"))

# cocoon: the whole view behind a milky web, thickest at the edges
coc = Image.new("RGBA", (256, 256))
px = coc.load()
for x in range(256):
    for y in range(256):
        e = max(abs(x - 127.5), abs(y - 127.5)) / 128.0
        px[x, y] = (236, 238, 242, int(90 + 120 * e ** 2))
d = ImageDraw.Draw(coc)
web(d, 128, 128, 190, 12, 10, 235)
for _ in range(40):
    a = (rnd.randrange(256), rnd.randrange(256))
    b = (a[0] + rnd.randint(-90, 90), a[1] + rnd.randint(-90, 90))
    d.line([a, b], fill=(250, 251, 253, 200), width=1)
coc.save(os.path.join(MISC, "web_cocoon_overlay.png"))
print("ok")
