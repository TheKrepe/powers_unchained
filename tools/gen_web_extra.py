"""Extra web textures: web zip rope, cocoon tile, first-person web overlays (head webs / cocoon).

Usage: python3 gen_web_extra.py <resources root>
"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

ROOT = os.path.join(sys.argv[1], "assets", "powers_unleashed", "textures")
ENT = os.path.join(ROOT, "entity", "web")
MISC = os.path.join(ROOT, "misc")
os.makedirs(MISC, exist_ok=True)
rnd = random.Random(26)

# cocoon: drawn over the wrapped entity's own model with its UVs, so this is a seamless 64x64 web wrap (any mob
# texture maps onto a part of it): milky film, wrapping threads, a few thick strands
tile = Image.new("RGBA", (64, 64), (236, 239, 244, 105))
d = ImageDraw.Draw(tile)


def wrapped_line(p0, p1, fill, width=1):
    for dx in (-64, 0, 64):
        for dy in (-64, 0, 64):
            d.line([(p0[0] + dx, p0[1] + dy), (p1[0] + dx, p1[1] + dy)], fill=fill, width=width)


for _ in range(26):
    x0, y0 = rnd.randrange(64), rnd.randrange(64)
    ang = rnd.uniform(0, math.pi)
    ln = rnd.uniform(25, 60)
    wrapped_line((x0, y0), (x0 + math.cos(ang) * ln, y0 + math.sin(ang) * ln), (250, 252, 255, 235))
for _ in range(6):
    x0, y0 = rnd.randrange(64), rnd.randrange(64)
    ang = rnd.uniform(0, math.pi)
    wrapped_line((x0, y0), (x0 + math.cos(ang) * 50, y0 + math.sin(ang) * 50), (255, 255, 255, 250), 2)
# owner's hand-made texture (3rd archive): web_cocoon.png - do not overwrite

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

# web zip rope (Fisk layout: 4 columns of 4 px = the 4 sides of the rope, 32 px = 16 blocks of rope... repeats
# along the rope): a 2 px strand per side with stray fibres
rope = Image.new("RGBA", (16, 32))
shades = [(255, 255, 255, 255), (234, 236, 240, 255), (210, 213, 218, 255)]
for col in range(4):
    for y in range(32):
        for x in (1, 2):
            rope.putpixel((col * 4 + x, y), rnd.choice(shades[:2]))
        if rnd.random() < 0.35:
            rope.putpixel((col * 4 + rnd.choice((0, 3)), y), rnd.choice(shades))
rope.save(os.path.join(ENT, "web_rope.png"))
