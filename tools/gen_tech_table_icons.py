"""Tech Table GUI sprites: web-shooter module chips (16x16): a steel chip with a coloured symbol.

Usage: python3 tools/gen_tech_table_icons.py <resources root>   (e.g. common/src/main/resources)
"""
import os
import sys
from PIL import Image

OUT = os.path.join(sys.argv[1], 'assets', 'powers_unleashed', 'textures', 'gui', 'sprites', 'tech_table', 'module')
os.makedirs(OUT, exist_ok=True)


def hx(h):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


K, D, M, L, C = hx('#22262c'), hx('#4d5560'), hx('#8d97a1'), hx('#c9d1d8'), hx('#c06a35')


def chip(base, light, symbol):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = im.load()
    for y in range(1, 15):
        for x in range(1, 15):
            edge = x in (1, 14) or y in (1, 14)
            p[x, y] = K if edge else (L if (x == 2 or y == 2) else (D if (x == 13 or y == 13) else M))
    # pins
    for x in (3, 6, 9, 12):
        p[x, 15] = C
        p[x, 0] = C
    for y in range(4, 12):
        for x in range(4, 12):
            p[x, y] = base
    for x in range(4, 12):
        p[x, 4] = light
    for (x, y) in symbol:
        p[x, y] = (255, 255, 255, 255) if (x + y) % 3 else light
    return im


SPRING = [(5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7),
          (5, 9), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9), (10, 6), (5, 8), (10, 10), (5, 10), (6, 10)]
FEED = [(5, 5), (6, 6), (7, 7), (6, 8), (5, 9), (8, 5), (9, 6), (10, 7), (9, 8), (8, 9)]
DRUM = [(5, 5), (6, 5), (5, 6), (6, 6), (5, 7), (6, 7), (5, 8), (6, 8), (5, 9), (6, 9),
        (9, 6), (10, 6), (9, 7), (10, 7), (9, 8), (10, 8), (9, 9), (10, 9), (9, 10), (10, 10)]
BOLT = [(9, 5), (8, 6), (7, 7), (8, 7), (9, 7), (8, 8), (7, 9), (6, 10)]

chip(hx('#2f6fb3'), hx('#6fb6ff'), SPRING).save(os.path.join(OUT, 'tight_spring.png'))
chip(hx('#2e6b3a'), hx('#7cff6b'), FEED).save(os.path.join(OUT, 'fast_feed.png'))
chip(hx('#9c2a22'), hx('#ff6a5a'), DRUM).save(os.path.join(OUT, 'second_drum.png'))
chip(hx('#8a6a12'), hx('#ffe66a'), BOLT).save(os.path.join(OUT, 'electro_web.png'))
print('module sprites ->', OUT)
