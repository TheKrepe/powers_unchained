"""Tiny Minecraft-GUI mockup kit: vanilla bitmap font (with Cyrillic), nine-slice sprites, panels, slots, icons."""
import json
import os
from PIL import Image

MC = '/tmp/claude-0/-home-claude/d1a7857d-e1c0-50d7-96f4-5fbbc1adfa14/scratchpad/mcref8/assets/minecraft'
MC_OLD = '/tmp/claude-0/-home-claude/d1a7857d-e1c0-50d7-96f4-5fbbc1adfa14/scratchpad/mcref4/assets/minecraft'
MOD = '/home/claude/powers_unchained/common/src/main/resources/assets/powers_unleashed'


def hx(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


# ---------------- font ----------------
class Font:
    def __init__(self):
        self.glyphs = {}
        d = json.load(open(f'{MC}/font/include/default.json'))
        for p in d['providers']:
            if p.get('type') != 'bitmap':
                continue
            f = p['file'].split(':')[1]
            path = f'{MC}/textures/{f}'
            if not os.path.exists(path):
                continue
            im = Image.open(path).convert('RGBA')
            rows = p['chars']
            cols = len(rows[0])
            cw = im.width // cols
            ch = im.height // len(rows)
            height = p.get('height', 8)
            ascent = p['ascent']
            for r, row in enumerate(rows):
                for c, char in enumerate(row):
                    if char in self.glyphs or char == '\x00':
                        continue
                    g = im.crop((c * cw, r * ch, c * cw + cw, r * ch + ch))
                    if cw != 8:
                        g = g.resize((int(cw * height / ch) or 1, height), Image.NEAREST)
                    # width = last opaque column
                    w = 0
                    for x in range(g.width):
                        if any(g.getpixel((x, y))[3] > 0 for y in range(g.height)):
                            w = x + 1
                    self.glyphs[char] = (g, w, ascent)
        self.glyphs[' '] = (Image.new('RGBA', (4, 8)), 3, 7)
        # й / ё (vanilla draws them from the accented sheet, not fetched): base letter + marks
        def mark(base, pts):
            g, w, a = self.glyphs[base]
            g = g.copy()
            for (px, py) in pts:
                g.putpixel((px, py), (255, 255, 255, 255))
            return (g, w, a)
        self.glyphs['й'] = mark('и', [(1, 0), (3, 0), (2, 1)])
        self.glyphs['ё'] = mark('е', [(1, 0), (3, 0)])
        g, w, a = self.glyphs['И']
        g2 = Image.new('RGBA', (8, 8)); g2.paste(g.crop((0, 1, 8, 8)), (0, 1))
        self.glyphs['Й'] = (g2, w, a)
        for (px, py) in [(1, 0), (3, 0)]:
            g2.putpixel((px, py), (255, 255, 255, 255))
        g, w, a = self.glyphs['Е']
        self.glyphs['Ё'] = (g, w, a)

    def width(self, text):
        return sum(self.glyphs.get(c, self.glyphs['?'])[1] + 1 for c in text) - (1 if text else 0)

    def draw(self, img, text, x, y, color, shadow=True, bold=False):
        if shadow:
            sc = (color[0] // 4, color[1] // 4, color[2] // 4, 255)
            self._draw(img, text, x + 1, y + 1, sc, bold)
        self._draw(img, text, x, y, color, bold)
        return self.width(text) + (len(text) if bold else 0)

    def _draw(self, img, text, x, y, color, bold):
        cx = x
        for ch in text:
            g, w, ascent = self.glyphs.get(ch, self.glyphs['?'])
            mask = g.split()[3]
            col = Image.new('RGBA', g.size, color)
            oy = y + 7 - ascent
            for dx in ((0, 1) if bold else (0,)):
                img.paste(col, (cx + dx, oy), mask)
            cx += w + 1 + (1 if bold else 0)

    def center(self, img, text, cx, y, color, shadow=True):
        self.draw(img, text, cx - self.width(text) // 2, y, color, shadow)


def small(img, font, text, x, y, color, shadow=False):
    """Label text (vanilla has a single font size)."""
    font.draw(img, text, x, y, color, shadow)


# ---------------- sprites ----------------
def sprite(path):
    for base in (MC, MC_OLD):
        p = f'{base}/textures/{path}.png'
        if os.path.exists(p):
            im = Image.open(p).convert('RGBA')
            return im.crop((0, 0, im.width, im.width)) if im.height > im.width * 2 else im
    raise FileNotFoundError(path)


def mod_tex(path):
    return Image.open(f'{MOD}/textures/{path}.png').convert('RGBA')


def nine(img, src, x, y, w, h, border):
    sw, sh = src.size
    b = border
    parts = {
        'tl': (0, 0, b, b), 'tr': (sw - b, 0, sw, b), 'bl': (0, sh - b, b, sh), 'br': (sw - b, sh - b, sw, sh),
        't': (b, 0, sw - b, b), 'b': (b, sh - b, sw - b, sh), 'l': (0, b, b, sh - b), 'r': (sw - b, b, sw, sh - b),
        'c': (b, b, sw - b, sh - b)}
    def put(key, dx, dy, dw, dh):
        if dw <= 0 or dh <= 0:
            return
        p = src.crop(parts[key]).resize((dw, dh), Image.NEAREST)
        img.alpha_composite(p, (dx, dy))
    put('tl', x, y, b, b); put('tr', x + w - b, y, b, b); put('bl', x, y + h - b, b, b); put('br', x + w - b, y + h - b, b, b)
    put('t', x + b, y, w - 2 * b, b); put('b', x + b, y + h - b, w - 2 * b, b)
    put('l', x, y + b, b, h - 2 * b); put('r', x + w - b, y + b, b, h - 2 * b)
    put('c', x + b, y + b, w - 2 * b, h - 2 * b)


def fill(img, x, y, w, h, c):
    if w > 0 and h > 0:
        img.alpha_composite(Image.new('RGBA', (w, h), c), (x, y))


def panel(img, x, y, w, h):
    """Vanilla container background (light grey, white/dark bevel, black rounded outline)."""
    fill(img, x + 2, y + 2, w - 4, h - 4, hx('#c6c6c6'))
    K, W, D = hx('#000000'), hx('#ffffff'), hx('#555555')
    fill(img, x + 2, y, w - 4, 1, K); fill(img, x + 2, y + h - 1, w - 4, 1, K)
    fill(img, x, y + 2, 1, h - 4, K); fill(img, x + w - 1, y + 2, 1, h - 4, K)
    fill(img, x + 1, y + 1, 1, 1, K); fill(img, x + w - 2, y + 1, 1, 1, K)
    fill(img, x + 1, y + h - 2, 1, 1, K); fill(img, x + w - 2, y + h - 2, 1, 1, K)
    fill(img, x + 2, y + 1, w - 5, 2, W); fill(img, x + 1, y + 2, 2, h - 5, W)
    fill(img, x + 3, y + h - 3, w - 5, 2, D); fill(img, x + w - 3, y + 3, 2, h - 5, D)
    fill(img, x + 3, y + 3, 1, 1, W); fill(img, x + w - 4, y + h - 4, 1, 1, D)


def inset(img, x, y, w, h, c=None):
    """Vanilla slot-style sunken area."""
    fill(img, x, y, w, h, c or hx('#8b8b8b'))
    fill(img, x, y, w - 1, 1, hx('#373737')); fill(img, x, y, 1, h - 1, hx('#373737'))
    fill(img, x + 1, y + h - 1, w - 1, 1, hx('#ffffff')); fill(img, x + w - 1, y + 1, 1, h - 1, hx('#ffffff'))


def monitor(img, x, y, w, h, scan=True):
    """Tech screen: sunken dark glass with faint scanlines."""
    inset(img, x, y, w, h, hx('#12161b'))
    if scan:
        for yy in range(y + 2, y + h - 1, 2):
            fill(img, x + 1, yy, w - 2, 1, (60, 90, 110, 22))
    fill(img, x + 1, y + 1, w - 2, 1, (89, 228, 255, 40))


def slot(img, x, y):
    img.alpha_composite(sprite('gui/sprites/container/slot'), (x, y))


def icon(img, im, x, y, size=16, dark=False):
    im = im.resize((size, size), Image.NEAREST) if im.size != (size, size) else im
    if dark:
        a = im.split()[3]
        im = Image.new('RGBA', im.size, (20, 22, 26, 255))
        im.putalpha(a.point(lambda v: 255 if v > 0 else 0))
    img.alpha_composite(im, (x, y))


def button(img, font, x, y, w, h, text, state='normal', color=hx('#ffffff')):
    name = {'normal': 'button', 'hover': 'button_highlighted', 'disabled': 'button_disabled'}[state]
    nine(img, sprite(f'gui/sprites/widget/{name}'), x, y, w, h, 3)
    if text:
        font.center(img, text, x + w // 2, y + (h - 8) // 2 + 1, color if state != 'disabled' else hx('#a0a0a0'))


def outline(img, x, y, w, h, c, t=1):
    fill(img, x, y, w, t, c); fill(img, x, y + h - t, w, t, c); fill(img, x, y, t, h, c); fill(img, x + w - t, y, t, h, c)


def brackets(img, x, y, w, h, c, l=4):
    for (bx, by, dx, dy) in ((x, y, 1, 1), (x + w - 1, y, -1, 1), (x, y + h - 1, 1, -1), (x + w - 1, y + h - 1, -1, -1)):
        for i in range(l):
            fill(img, bx + i * dx, by, 1, 1, c)
            fill(img, bx, by + i * dy, 1, 1, c)


def chip(img, font, x, y, text, bg, fg=hx('#ffffff'), selected=False):
    w = font.width(text) + 6
    fill(img, x, y, w, 11, bg)
    fill(img, x, y + 10, w, 1, (0, 0, 0, 90))
    if selected:
        outline(img, x - 1, y - 1, w + 2, 13, hx('#ffffff'))
    font.draw(img, text, x + 3, y + 2, fg, shadow=False)
    return w
