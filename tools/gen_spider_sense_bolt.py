"""Comic-style spider-sense bolt (one jagged zigzag, white-gold core, dark outline): textures/entity/power/spider_sense_bolt.png.
Our own drawing (the JMCT model was only looked at as an idea)."""
import os
from PIL import Image, ImageDraw

OUT = os.path.join(os.path.dirname(__file__), '..', 'common/src/main/resources/assets/powers_unchained/textures/entity/power/spider_sense_bolt.png')
W, H = 16, 48

# zigzag from the bottom (near the head) to the tip
pts = [(7, 46), (11, 36), (4, 30), (12, 20), (5, 14), (10, 7), (7, 1)]


def main():
    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # hard pixel lines: a gold body 3px wide, a white-hot core, then a dark outline round it all
    d.line(pts, fill=(255, 190, 40, 255), width=3)
    d.line(pts, fill=(255, 250, 225, 255), width=1)
    px = img.load()
    out = img.copy()
    po = out.load()
    for y in range(H):
        for x in range(W):
            if px[x, y][3] == 0 and any(0 <= x + dx < W and 0 <= y + dy < H and px[x + dx, y + dy][3] > 0
                                        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                po[x, y] = (34, 14, 6, 255)
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    out.save(OUT)


if __name__ == '__main__':
    main()
