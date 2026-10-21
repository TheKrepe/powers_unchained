"""Moves the web-shooter pixels from the arms' base layer to the outer (sleeve) layer of the skin-layout textures,
keeping existing outer-layer pixels on top. The outer layer is drawn further out than a skin's own sleeves, so the
web-shooters stay visible over jackets / gloves of any skin (in first and third person).

Usage: python3 web_shooters_to_outer_layer.py <texture.png> [...]
"""
import sys

from PIL import Image

# (base region x0, y0, x1, y1) -> offset to the matching outer-layer region
REGIONS = [((40, 16, 56, 32), (0, 16)),   # right arm -> right sleeve
           ((32, 48, 48, 64), (16, 0))]   # left arm -> left sleeve

for path in sys.argv[1:]:
    im = Image.open(path).convert("RGBA")
    for (x0, y0, x1, y1), (dx, dy) in REGIONS:
        for y in range(y0, y1):
            for x in range(x0, x1):
                p = im.getpixel((x, y))
                if p[3] == 0:
                    continue
                if im.getpixel((x + dx, y + dy))[3] == 0:
                    im.putpixel((x + dx, y + dy), p)
                im.putpixel((x, y), (0, 0, 0, 0))
    im.save(path)
    print("ok", path)
