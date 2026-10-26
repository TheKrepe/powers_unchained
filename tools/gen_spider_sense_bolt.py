"""Spider-Sense bolt, Minecraft pixel-art style (one texel = one model pixel when drawn): a small stepped zigzag in
yellow with a white-hot edge — textures/entity/power/spider_sense_bolt.png. Our own drawing (the JMCT model was only
looked at as an idea)."""
import os
from PIL import Image

OUT = os.path.join(os.path.dirname(__file__), '..', 'common/src/main/resources/assets/powers_unchained/textures/entity/power/spider_sense_bolt.png')

# top = tip (far from the head), bottom = root (next to the head)
GRID = """
...wy
..wy.
.wy..
wyyyy
..yy.
.yy..
.y...
y....
"""
COLOURS = {'y': (255, 214, 48, 255), 'w': (255, 252, 214, 255)}


def main():
    rows = GRID.strip('\n').split('\n')
    img = Image.new('RGBA', (len(rows[0]), len(rows)), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in COLOURS:
                img.putpixel((x, y), COLOURS[ch])
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    img.save(OUT)


if __name__ == '__main__':
    main()
