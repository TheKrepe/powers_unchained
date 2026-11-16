"""Web-shooter looks (Tech Table -> Customization): recolours of the owner's hand-made web-shooter textures.

Writes NEW files only (the owner's originals stay as they are):
  textures/item/web_shooters_<skin>.png              item icon
  textures/gadget/web_shooters_<skin>(_slim).png     on the wrists
Skins: advanced (Marvel's Spider-Man game: white / black / red), brand_new_day (Spider-Man: Brand New Day: silver / blue / red).
"""
from PIL import Image

ROOT = 'common/src/main/resources/assets/powers_unleashed/textures/'
# the owner's grey palette -> per skin colour
SKINS = {
    'advanced': {
        (225, 225, 225): (246, 246, 246), (178, 178, 178): (46, 46, 54), (149, 149, 149): (196, 26, 36),
        (119, 119, 119): (122, 16, 24), (90, 90, 90): (62, 62, 70), (68, 68, 68): (32, 32, 38), (56, 56, 56): (22, 22, 28),
    },
    'brand_new_day': {
        (225, 225, 225): (218, 224, 234), (178, 178, 178): (44, 76, 168), (149, 149, 149): (204, 32, 38),
        (119, 119, 119): (26, 42, 104), (90, 90, 90): (72, 80, 100), (68, 68, 68): (42, 48, 64), (56, 56, 56): (30, 34, 48),
    },
}


def recolor(src, dst, table):
    im = Image.open(ROOT + src).convert('RGBA')
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a and (r, g, b) in table:
                px[x, y] = table[(r, g, b)] + (a,)
    im.save(ROOT + dst)
    print('wrote', dst)


for skin, table in SKINS.items():
    recolor('item/web_shooters.png', f'item/web_shooters_{skin}.png', table)
    recolor('gadget/web_shooters.png', f'gadget/web_shooters_{skin}.png', table)
    recolor('gadget/web_shooters_slim.png', f'gadget/web_shooters_{skin}_slim.png', table)
