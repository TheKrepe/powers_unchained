"""Tech Table: empty device slot icon (16x16, gui sprite tech_table/device_slot).

A pixel cog (any device goes here, not only web-shooters) in translucent white, so it reads both on the vanilla grey
slot (Customization tab) and on the dark blueprint socket (Upgrade tab).
"""
import sys
from PIL import Image

OUT = 'common/src/main/resources/assets/powers_unleashed/textures/gui/sprites/tech_table/device_slot.png'

# O = outline, f = fill, h = hole rim
ART = [
    "................",
    "......OOOO......",
    "..OO..OffO..OO..",
    ".OffOOOffOOOffO.",
    ".OffffffffffffO.",
    "..OffffOOOffffO.",
    "..OfffOhhhOfffO.",
    "OOOffOh..hOffOOO",
    "OffffOh..hOffffO",
    "OOOffOh..hOffOOO",
    "..OfffOhhhOfffO.",
    "..OffffOOOffffO.",
    ".OffffffffffffO.",
    ".OffOOOffOOOffO.",
    "..OO..OffO..OO..",
    "......OOOO......",
]
COL = {'O': (255, 255, 255, 170), 'f': (255, 255, 255, 95), 'h': (255, 255, 255, 140)}

img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
for y, row in enumerate(ART):
    for x, ch in enumerate(row):
        if ch in COL:
            img.putpixel((x, y), COL[ch])
img.save(sys.argv[1] if len(sys.argv) > 1 else OUT)
print('wrote', OUT)
