"""Empty web cartridge icon: the web cartridge with the white web fluid window drained to dark glass.

Usage: python3 gen_empty_cartridge.py <resources root>
"""
import os
import sys

from PIL import Image

TEX = os.path.join(sys.argv[1], "assets", "powers_unleashed", "textures", "item")
im = Image.open(os.path.join(TEX, "web_cartridge.png")).convert("RGBA")
for y in range(5, 11):
    for x in range(6, 10):
        r, g, b, a = im.getpixel((x, y))
        if a and r + g + b > 380:
            # glass: dark blue-grey with a highlight streak on the left
            im.putpixel((x, y), (92, 104, 118, 255) if x == 6 else (52, 60, 70, 255))
im.save(os.path.join(TEX, "empty_web_cartridge.png"))
print("ok")
