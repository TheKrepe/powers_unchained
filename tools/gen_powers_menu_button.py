"""Powers menu inventory button (20x18, like the recipe book button): the vanilla button frame + our own emblem
(a hexagonal amber badge with a cyan lightning bolt — generic, not tied to one hero).

python3 tools/gen_powers_menu_button.py <vanilla recipe_book sprites dir>
-> textures/gui/sprites/powers_menu/button.png, button_highlighted.png
"""
import sys
from PIL import Image

SRC = sys.argv[1]
OUT = 'common/src/main/resources/assets/powers_unchained/textures/gui/sprites/powers_menu/'

# 12x12 emblem: an amber round badge (outline, lit top-left, shaded bottom-right) with a cyan zig-zag lightning bolt
BOLT = [(7, 1), (8, 1), (6, 2), (7, 2), (5, 3), (6, 3), (4, 4), (5, 4), (3, 5), (4, 5), (5, 5), (6, 5), (7, 5), (8, 5),
        (6, 6), (7, 6), (5, 7), (6, 7), (4, 8), (5, 8), (3, 9), (4, 9), (3, 10)]
PAL = {
    'normal': {'O': (52, 30, 14), 'a': (214, 128, 52), 'b': (255, 180, 92), 'd': (150, 80, 32), 'c': (89, 228, 255),
               'w': (210, 248, 255)},
    'highlighted': {'O': (52, 30, 14), 'a': (240, 156, 72), 'b': (255, 210, 126), 'd': (176, 98, 40), 'c': (150, 242, 255),
                    'w': (255, 255, 255)},
}


def emblem(px, ox, oy, pal):
    for y in range(12):
        for x in range(12):
            dx, dy = x - 5.5, y - 5.5
            r = (dx * dx + dy * dy) ** 0.5
            if r > 6.0:
                continue
            if r > 5.0:
                c = pal['O']
            elif dx + dy < -3:
                c = pal['b']
            elif dx + dy > 3:
                c = pal['d']
            else:
                c = pal['a']
            px[ox + x, oy + y] = c + (255,)
    for i, (x, y) in enumerate(BOLT):
        px[ox + x, oy + y] = (pal['w'] if i in (0, 2, 4, 6) else pal['c']) + (255,)


for name, key in (('button', 'normal'), ('button_highlighted', 'highlighted')):
    base = Image.open(f'{SRC}/{name}.png').convert('RGBA')
    out = base.copy()
    px = out.load()
    # clear the book: keep the frame (2 px) and fill the inside with the button's face colour
    face = base.getpixel((2, 2))
    for y in range(2, 16):
        for x in range(2, 18):
            px[x, y] = face
    emblem(px, 4, 3, PAL[key])
    out.save(OUT + name + '.png')
    print('wrote', name)
