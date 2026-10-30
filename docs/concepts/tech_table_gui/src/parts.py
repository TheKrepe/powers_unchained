"""16x16 pixel-art icons of the concept's invented components (gear mechanism, spring, nozzle, circuit board, modules)."""
from PIL import Image


def hx(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def grid(rows, pal):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != '.':
                im.putpixel((x, y), pal[ch])
    return im


STEEL = {'k': hx('#2b2f36'), 'd': hx('#5d6670'), 'm': hx('#9aa4ad'), 'l': hx('#d4dbe0'), 'w': hx('#ffffff'),
         'c': hx('#c06a35'), 'C': hx('#e89b5e'), 'r': hx('#b3261e'), 'R': hx('#ff5a4a'), 'g': hx('#2e6b3a'),
         'G': hx('#4fa860'), 'y': hx('#e8c84a'), 'b': hx('#2f6fb3'), 'B': hx('#6fb6ff'), 'p': hx('#7a3fb0'),
         'P': hx('#b77cff')}

MECHANISM = grid([
    '......kk........',
    '...k.kmmk.k.....',
    '..kmkkmlmkmk....',
    '..kmlmmmmmlk....',
    '...kmmkkkmk.....',
    '.kkmmkCCCkmkk...',
    'kmlmkCcwcCkmlmk.',
    'kmmmkCwccCkmmmk.',
    '.kkmkCCcCCkmkk..',
    '...kmmkkkmmk....',
    '..kmlmmmmmlmk...',
    '..kmkkmlmkkmk...',
    '...k.kmmk.k.....',
    '......kk........',
    '................',
    '................'], STEEL)

SPRING = grid([
    '................',
    '....kkkkkkk.....',
    '...kllllllmk....',
    '....kkkkkkk.....',
    '...kmmmmmmlk....',
    '....kkkkkkk.....',
    '...klllllllk....',
    '....kkkkkkk.....',
    '...kmmmmmmlk....',
    '....kkkkkkk.....',
    '...klllllllk....',
    '....kkkkkkk.....',
    '...kdddddddk....',
    '....kkkkkkk.....',
    '................',
    '................'], STEEL)

NOZZLE = grid([
    '................',
    '.......kk.......',
    '......kllk......',
    '......kwlk......',
    '.....kmllmk.....',
    '.....kmllmk.....',
    '....kmmllmmk....',
    '....kmmlmmmk....',
    '...kccCCCCcck...',
    '...kcCCCCCCck...',
    '...kddmmmmddk...',
    '...kdmmllmmdk...',
    '...kddmmmmddk...',
    '....kkkkkkkk....',
    '................',
    '................'], STEEL)

BOARD = grid([
    '................',
    '.kkkkkkkkkkkkkk.',
    '.kggGgggggGgggk.',
    '.kgCCCCgkkkkggk.',
    '.kgggGCgkddkgGk.',
    '.kGgggCgkddkggk.',
    '.kgCCCCCCCCCCgk.',
    '.kggGggggCgggGk.',
    '.kgkkkgGgCgyggk.',
    '.kgkdkggCCgggGk.',
    '.kgkkkgGgggRggk.',
    '.kggGgggGgggggk.',
    '.kkkkkkkkkkkkkk.',
    '..m..m..m..m....',
    '................',
    '................'], STEEL)


def module(col, light):
    pal = dict(STEEL)
    pal['x'] = hx(col)
    pal['X'] = hx(light)
    return grid([
        '................',
        '..kkkkkkkkkkkk..',
        '..kmmmmmmmmmlk..',
        '..kmkkkkkkkkmk..',
        '..kmkxxxxxxkmk..',
        '..kmkxXXXXxkmk..',
        '..kmkxXwwXxkmk..',
        '..kmkxXwwXxkmk..',
        '..kmkxXXXXxkmk..',
        '..kmkxxxxxxkmk..',
        '..kmkkkkkkkkmk..',
        '..kdmmmmmmmmdk..',
        '..kkkkkkkkkkkk..',
        '...c.c.c.c.c....',
        '................',
        '................'], pal)


MODULES = {
    'spring': module('#2f6fb3', '#6fb6ff'),
    'drum': module('#b3261e', '#ff5a4a'),
    'feed': module('#2e6b3a', '#4fa860'),
    'electro': module('#e8c84a', '#fff4a8'),
}
