"""Tech Table GUI concept v2 — a holographic research tree in the spirit of Fisk's Superheroes' Suit Database
(full-screen dark teal hologram, hexagonal nodes joined by lines, a sliding sidebar on the right). Our own drawing;
only the general idea and palette mood follow Fisk.

python3 tech_screens_v2.py <out dir>  ->  tech_table_v2_1_tree.png, _2_assembly.png, _3_research.png (1440x810)

Shapes (background, lines, hexagons, panels) are drawn smooth at 3x; text / item icons are pixel art at 1x scaled x3.
"""
import math
import os
import sys
from PIL import Image, ImageDraw, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', '..', 'suit_creator_gui', 'src'))
import mc_ui as U  # noqa: E402
import parts  # noqa: E402

F = U.Font()
hx = U.hx
OUT = sys.argv[1] if len(sys.argv) > 1 else '.'
SW, SH, K = 480, 270, 3

# palette (hologram teal)
BG1, BG2 = hx('#3f6670'), hx('#172124')
TEXT = hx('#adf4ff')
TEXT_D = hx('#5f97a3')
DONE = hx('#31e84a')
UNL_BORDER, UNL_FILL, UNL_FILL_H = hx('#adf4ff'), hx('#3f6670'), hx('#6ba3ad')
RDY_BORDER_A, RDY_BORDER_B, RDY_FILL = hx('#a1d69a'), hx('#477741'), hx('#333f31')
LKD_BORDER, LKD_FILL = hx('#364447'), hx('#1c2528')
HL_BORDER, HL_FILL = hx('#f6e087'), hx('#635c3e')
LINE_UNL, LINE_LKD, LINE_RDY = hx('#73b0bc'), hx('#364447'), hx('#a1d69a')
WHITE = hx('#ffffff')
COPPER = hx('#ff9a52')


def blend(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


class Canvas:
    """Hi-res shape layer + 1x pixel layer."""

    def __init__(self):
        self.hi = Image.new('RGB', (SW * K, SH * K), (0, 0, 0))
        self.px = Image.new('RGBA', (SW, SH), (0, 0, 0, 0))
        self.d = ImageDraw.Draw(self.hi, 'RGBA')

    def comp(self, im, pos=(0, 0)):
        self.hi.paste(im.convert('RGB'), pos, im)

    def flush_px(self):
        """Commit the pixel layer onto the hi-res image (so later shapes can go over it)."""
        self.comp(self.px.resize((SW * K, SH * K), Image.NEAREST))
        self.px = Image.new('RGBA', (SW, SH), (0, 0, 0, 0))

    def save(self, name):
        self.flush_px()
        os.makedirs(OUT, exist_ok=True)
        self.hi.convert('RGB').save(os.path.join(OUT, name + '.png'))


def vgrad(c, x0, y0, x1, y1, top, bottom):
    for y in range(int(y0 * K), int(y1 * K)):
        t = (y - y0 * K) / max(1, (y1 - y0) * K - 1)
        c.d.line([(x0 * K, y), (x1 * K - 1, y)], fill=blend(top, bottom, t)[:3] + (top[3] if len(top) > 3 else 255,))


def hgrad(c, x0, y0, x1, y1, left, right, a0=255, a1=0):
    for x in range(int(x0 * K), int(x1 * K)):
        t = (x - x0 * K) / max(1, (x1 - x0) * K - 1)
        col = blend(left, right, t)[:3] + (int(a0 + (a1 - a0) * t),)
        c.d.line([(x, y0 * K), (x, y1 * K - 1)], fill=col)


def background(c):
    vgrad(c, 0, 0, SW, SH, BG1, BG2)
    # faint hex grid
    r = 9
    for row in range(-1, 40):
        for col in range(-1, 40):
            cx = col * r * math.sqrt(3) + (row % 2) * r * math.sqrt(3) / 2
            cy = row * r * 1.5
            pts = [((cx + r * math.cos(math.radians(60 * i + 30))) * K, (cy + r * math.sin(math.radians(60 * i + 30))) * K)
                   for i in range(6)]
            c.d.polygon(pts, outline=(173, 244, 255, 14))
    # scanlines
    for y in range(0, SH * K, 6):
        c.d.line([(0, y), (SW * K, y)], fill=(0, 0, 0, 22))
    # vignette
    vig = Image.new('L', (SW * K, SH * K), 0)
    vd = ImageDraw.Draw(vig)
    for i in range(40):
        a = int(150 * (1 - i / 40) ** 2)
        vd.rectangle([i * 6, i * 6, SW * K - i * 6, SH * K - i * 6], outline=a, width=6)
    black = Image.new('RGBA', (SW * K, SH * K), (0, 0, 0, 255))
    black.putalpha(vig)
    c.comp(black)


def hexagon(c, cx, cy, scale, col, center_dark=0.33, alpha=255):
    """Pointy-top hexagon (Fisk-size: 12 x 13.86) shaded from the rim colour to a darker centre."""
    steps = 6
    for s in range(steps, 0, -1):
        f = s / steps
        rr = 13.856 * scale * f
        rx = 12 * scale * f
        col_s = blend(blend(col, (0, 0, 0, 255), center_dark), col, f)[:3] + (alpha,)
        pts = [(cx * K, (cy - rr) * K), ((cx + rx) * K, (cy - rr / 2) * K), ((cx + rx) * K, (cy + rr / 2) * K),
               (cx * K, (cy + rr) * K), ((cx - rx) * K, (cy + rr / 2) * K), ((cx - rx) * K, (cy - rr / 2) * K)]
        c.d.polygon(pts, fill=col_s)


ICON = {
    'web_shooters': lambda: U.mod_tex('item/web_shooters'),
    'web_cartridge': lambda: U.mod_tex('item/web_cartridge'),
    'empty_web_cartridge': lambda: U.mod_tex('item/empty_web_cartridge'),
    'tech_table': lambda: U.mod_tex('block/tech_table_front'),
    'iron': lambda: U.sprite('item/iron_ingot'),
    'copper': lambda: U.sprite('item/copper_ingot'),
    'redstone': lambda: U.sprite('item/redstone'),
    'string': lambda: U.sprite('item/string'),
    'slime': lambda: U.sprite('item/slime_ball'),
    'gold': lambda: U.sprite('item/gold_ingot'),
    'glowstone': lambda: U.sprite('item/glowstone_dust'),
    'pearl': lambda: U.sprite('item/ender_pearl'),
    'mechanism': lambda: parts.MECHANISM,
    'spring': lambda: parts.SPRING,
    'nozzle': lambda: parts.NOZZLE,
    'board': lambda: parts.BOARD,
    'mod_spring': lambda: parts.MODULES['spring'],
    'mod_drum': lambda: parts.MODULES['drum'],
    'mod_feed': lambda: parts.MODULES['feed'],
    'mod_electro': lambda: parts.MODULES['electro'],
}


def icon(name):
    return ICON[name]()


def tint(im, f):
    r, g, b, a = im.split()
    return Image.merge('RGBA', [ch.point(lambda v: int(v * f)) for ch in (r, g, b)] + [a])


LOCK = ['.www.', 'w...w', 'w...w', 'wwwww', 'ww.ww', 'wwwww']
CHECK = ['......w', '.....ww', 'w...ww.', 'ww.ww..', '.www...', '..w....']
BOX = ['wwwwwww', 'w.....w', 'w.....w', 'w.....w', 'w.....w', 'wwwwwww']


def glyph(c, rows, x, y, col):
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch == 'k':
                c.px.putpixel((x + i, y + j), (20, 28, 30, 255))
            elif ch == 'w':
                c.px.putpixel((x + i, y + j), col)


# ---------------------------------------------------------------- the tree
NODES = {
    # id: (x, y, icon, name, state)  state: unl / rdy / lkd
    'table': (34, 140, 'tech_table', 'Мастерская', 'unl'),
    'mechanism': (78, 100, 'mechanism', 'Механизм', 'unl'),
    'board': (78, 180, 'board', 'Плата', 'unl'),
    'spring': (118, 74, 'spring', 'Пружина', 'unl'),
    'nozzle': (118, 126, 'nozzle', 'Сопло', 'unl'),
    'chip': (118, 206, 'glowstone', 'Микрочип', 'rdy'),
    'empty': (160, 126, 'empty_web_cartridge', 'Пустой картридж', 'unl'),
    'shooters': (200, 100, 'web_shooters', 'Веб-шутеры', 'unl'),
    'cartridge': (200, 152, 'web_cartridge', 'Картридж', 'unl'),
    'mod_spring': (244, 66, 'mod_spring', 'Тугая пружина', 'unl'),
    'mod_drum': (244, 108, 'mod_drum', 'Второй барабан', 'rdy'),
    'mod_feed': (284, 88, 'mod_feed', 'Быстрая подача', 'lkd'),
    'tracer': (160, 206, 'pearl', 'Паучий маячок', 'lkd'),
    'electro': (244, 170, 'mod_electro', 'Электро-сеть', 'lkd'),
}
LINKS = [('table', 'mechanism'), ('table', 'board'), ('mechanism', 'spring'), ('mechanism', 'nozzle'), ('board', 'chip'),
         ('nozzle', 'empty'), ('mechanism', 'shooters'), ('spring', 'shooters'), ('empty', 'shooters'),
         ('empty', 'cartridge'), ('shooters', 'mod_spring'), ('shooters', 'mod_drum'), ('mod_spring', 'mod_feed'),
         ('mod_drum', 'mod_feed'), ('chip', 'tracer'), ('cartridge', 'electro'), ('chip', 'electro')]


def draw_tree(c, selected=None, highlight=(), dim=False, unlocking=None, hovered=None, t=0.35):
    # lines: elbow-free straight links like Fisk, colour = the child's state
    for a, b in LINKS:
        ax, ay = NODES[a][:2]
        bx, by = NODES[b][:2]
        st = NODES[b][4]
        col = {'unl': LINE_UNL, 'rdy': blend(LINE_RDY, RDY_BORDER_B, t), 'lkd': LINE_LKD}[st]
        if highlight and (a in highlight and b in highlight):
            col = HL_BORDER
        if dim and not (a in highlight and b in highlight):
            col = blend(col, BG2, 0.55)
        c.d.line([(ax * K, ay * K), (bx * K, by * K)], fill=col, width=3 * K // 2 + 1)
    for nid, (x, y, ic, name, st) in NODES.items():
        border, fill = {'unl': (UNL_BORDER, UNL_FILL), 'rdy': (blend(RDY_BORDER_A, RDY_BORDER_B, t), RDY_FILL),
                        'lkd': (LKD_BORDER, LKD_FILL)}[st]
        if nid in highlight:
            border, fill = HL_BORDER, HL_FILL
        if dim and nid not in highlight and nid != unlocking:
            border, fill = blend(border, BG2, 0.6), blend(fill, BG2, 0.6)
        s = 1.15 if nid in (hovered, selected) else 1.0
        if nid == selected:
            hexagon(c, x, y, s, blend(WHITE, hx('#918f91'), 0.4))
        hexagon(c, x, y, 0.9 * s, border)
        hexagon(c, x, y, 0.8 * s, fill, center_dark=0.45)
        if nid == unlocking:
            # unlock pulse: expanding faint hex rings + the green turning cyan from the centre
            for k, f in enumerate((1.25, 1.55, 1.9)):
                ring = Image.new('RGBA', c.hi.size, (0, 0, 0, 0))
                rd = ImageDraw.Draw(ring)
                rr, rx = 13.856 * f, 12 * f
                pts = [(x * K, (y - rr) * K), ((x + rx) * K, (y - rr / 2) * K), ((x + rx) * K, (y + rr / 2) * K),
                       (x * K, (y + rr) * K), ((x - rx) * K, (y + rr / 2) * K), ((x - rx) * K, (y - rr / 2) * K)]
                rd.polygon(pts, outline=(173, 244, 255, 150 - k * 45), width=K)
                c.comp(ring.filter(ImageFilter.GaussianBlur(1)))
            hexagon(c, x, y, 0.55, UNL_BORDER)
            hexagon(c, x, y, 0.47, UNL_FILL, center_dark=0.4)
        im = icon(ic)
        if st == 'lkd' and nid not in highlight:
            im = tint(im, 0.45)
        if dim and nid not in highlight and nid != unlocking:
            im = tint(im, 0.5)
        c.px.alpha_composite(im, (x - 8, y - 8))
        if st == 'lkd':
            glyph(c, LOCK, x + 4, y + 3, (200, 215, 220, 255))
        if nid == unlocking:
            # the lock breaking apart
            glyph(c, ['.www.', 'w...w'], x + 9, y - 2, (200, 215, 220, 255))
            glyph(c, ['wwwww', 'ww.ww', 'wwwww'], x + 5, y + 8, (200, 215, 220, 255))


def hover_label(c, nid, frac=1.0):
    x, y, _, name, _ = NODES[nid]
    w = F.width(name) + 4
    x0 = x + 13
    hgrad(c, x0, y - 6, x0 + w * frac + w / 3, y + 6, TEXT, TEXT, a0=150, a1=0)
    c.flush_px()
    s = name[:max(1, round(len(name) * frac))]
    F.draw(c.px, s, x0 + 2, y - 4, WHITE, shadow=False)
    F.draw(c.px, s, x0 + 3, y - 3, (63, 102, 112, 255), shadow=False)
    F.draw(c.px, s, x0 + 2, y - 4, WHITE, shadow=False)


def toolbar(c, search='', active=0):
    """Top-left: overview buttons (all / gadgets / parts), back, search — like Fisk's database."""
    names = [['wwwwwww', 'w.w.w.w', 'wwwwwww', 'w.w.w.w', 'wwwwwww', 'w.w.w.w', 'wwwwwww'],
             ['..www..', '.w...w.', 'www.www', 'w.w.w.w', 'www.www', '.w...w.', '..www..'],
             ['...w...', '..www..', '.wwwww.', 'www.www', '.wwwww.', '..www..', '...w...']]
    x = 8
    for i, rows in enumerate(names):
        col = TEXT if i == active else (63, 102, 112, 255)
        c.d.rectangle([x * K, 8 * K, (x + 13) * K, 21 * K], outline=col[:3] + (255,), width=K)
        if i == active:
            c.d.rectangle([x * K + K, 8 * K + K, (x + 13) * K - K, 21 * K - K], fill=(173, 244, 255, 40))
        glyph(c, rows, x + 3, 11, col)
        x += 17
    # back arrow
    c.d.rectangle([(x + 6) * K, 8 * K, (x + 19) * K, 21 * K], outline=(63, 102, 112, 255), width=K)
    glyph(c, ['..w....', '.w.....', 'wwwwww.', '.w....w', '..w...w', '......w'], x + 9, 11, (95, 151, 163, 255))
    x += 25
    # search box
    c.d.rectangle([x * K, 8 * K, (x + 110) * K, 21 * K], fill=(14, 22, 24, 200), outline=(95, 151, 163, 255), width=K)
    glyph(c, ['.www.', 'w...w', 'w...w', '.www.', '....w', '.....'], x + 3, 11, TEXT)
    if search:
        n = F.draw(c.px, search, x + 12, 11, TEXT, shadow=False)
        c.d.rectangle([(x + 13 + n) * K, 11 * K, (x + 14 + n) * K, 19 * K], fill=TEXT)
    else:
        F.draw(c.px, 'Поиск технологий...', x + 12, 11, (95, 151, 163, 255), shadow=False)


def counter(c, text, right):
    w = F.width(text)
    F.draw(c.px, text, right - w, 11, TEXT, shadow=False)


def sidebar(c, flipped=False, open_=1.0):
    x0 = SW - 160 * open_
    if flipped:
        vgrad(c, x0, 0, SW, SH, BG2, BG1)
    else:
        vgrad(c, x0, 0, SW, SH, BG1, BG2)
    hgrad(c, x0 - 3, 0, x0, SH, (0, 0, 0, 255), (0, 0, 0, 255), a0=0, a1=150)
    c.d.line([(x0 * K, 0), (x0 * K, SH * K)], fill=(173, 244, 255, 90), width=1)
    return int(x0)


def big_button(c, x, y, w, h, text, col=TEXT, glow=0.0, text_col=None):
    c.d.rectangle([x * K, y * K, (x + w) * K - 1, (y + h) * K - 1], fill=col[:3] + (60 + int(80 * glow),),
                  outline=col[:3] + (255,), width=K)
    c.flush_px()
    F.center(c.px, text, x + w // 2, y + (h - 8) // 2 + 1, text_col or WHITE, shadow=True)


def objective(c, x, y, text, done, w):
    if done:
        glyph(c, CHECK, x, y, DONE)
        F.draw(c.px, text, x + 10, y, DONE, shadow=False)
    else:
        glyph(c, BOX, x, y, TEXT)
        F.draw(c.px, text, x + 10, y, TEXT, shadow=False)


def price(c, x, y, ic, have, need, w=56):
    c.px.alpha_composite(icon(ic) if isinstance(ic, str) else ic, (x, y - 4))
    t = f'{have}/{need}'
    F.draw(c.px, t, x + 19, y, DONE if have >= need else TEXT, shadow=False)
    # segmented bar (Fisk-style progress pips)
    bx, by = x + 19, y + 10
    for i in range(need if need <= 10 else 10):
        filled = (have / need) * (need if need <= 10 else 10) > i
        c.d.rectangle([(bx + i * 3) * K, by * K, (bx + i * 3 + 2) * K - 1, (by + 2) * K - 1],
                      fill=(DONE if have >= need else TEXT)[:3] + (255 if filled else 50,))


def xp_icon():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for (x, y) in [(6, 4), (7, 4), (8, 4), (5, 5), (9, 5), (4, 6), (10, 6), (4, 7), (10, 7), (4, 8), (10, 8), (5, 9), (9, 9),
                   (6, 10), (7, 10), (8, 10)]:
        im.putpixel((x + 1, y + 1), (60, 120, 20, 255))
    for x in range(5, 10):
        for y in range(5, 10):
            if (x - 7) ** 2 + (y - 7) ** 2 <= 5:
                im.putpixel((x + 1, y + 1), (180, 255, 90, 255))
    im.putpixel((7, 7), (255, 255, 220, 255))
    return im


def wrap(text, width):
    words, lines, cur = text.split(), [], ''
    for w in words:
        t = (cur + ' ' + w).strip()
        if F.width(t) > width and cur:
            lines.append(cur)
            cur = w
        else:
            cur = t
    if cur:
        lines.append(cur)
    return lines


def hint(c, text):
    F.draw(c.px, text, 8, SH - 12, (95, 151, 163, 255), shadow=False)


# ---------------------------------------------------------------- 1: tree + description
def screen_tree():
    c = Canvas()
    background(c)
    draw_tree(c, selected='mod_drum', hovered='mod_drum', t=0.3)
    toolbar(c, active=0)
    counter(c, 'Изучено 9 / 14', 312)
    hint(c, 'ЛКМ — выбрать   тащи — двигать   колесо — масштаб')
    x0 = sidebar(c)
    w = 160
    big_button(c, x0 + 10, 0, w - 20, 16, 'ИЗУЧИТЬ', col=RDY_BORDER_A, glow=0.6)
    c.flush_px()
    F.center(c.px, 'Второй барабан', x0 + w // 2, 24, TEXT, shadow=False)
    F.center(c.px, 'Второй барабан', x0 + w // 2 + 1, 24, TEXT, shadow=False)
    F.center(c.px, 'Модуль веб-шутеров', x0 + w // 2, 35, TEXT_D, shadow=False)
    y = 50
    for line in wrap('Ещё одно гнездо под картридж: веб-шутеры держат 3 картриджа вместо 2.', w - 20):
        F.draw(c.px, line, x0 + 10, y, TEXT, shadow=False)
        y += 10
    y += 6
    F.draw(c.px, 'Задания', x0 + 10, y, TEXT_D, shadow=False)
    y += 12
    objective(c, x0 + 10, y, 'Собери веб-шутеры', True, w); y += 11
    objective(c, x0 + 10, y, 'Потрать 2 картриджа', True, w); y += 11
    objective(c, x0 + 10, y, 'Пролети на паутине', False, w); y += 10
    F.draw(c.px, '300 блоков (184/300)', x0 + 20, y, TEXT_D, shadow=False); y += 16
    F.draw(c.px, 'Цена', x0 + 10, y, TEXT_D, shadow=False)
    y += 14
    price(c, x0 + 10, y, xp_icon(), 8, 8)
    price(c, x0 + 82, y, 'iron', 4, 4)
    y += 20
    price(c, x0 + 10, y, 'copper', 2, 2)
    price(c, x0 + 82, y, 'redstone', 2, 2)
    # preview / assembly tab at the bottom (Fisk: scroll or click to flip the sidebar)
    c.d.rectangle([x0 * K, (SH - 14) * K, SW * K, SH * K], fill=(173, 244, 255, 45))
    c.d.line([(x0 * K, (SH - 14) * K), (SW * K, (SH - 14) * K)], fill=(173, 244, 255, 160), width=K)
    c.flush_px()
    F.center(c.px, '^  Сборка  ^', x0 + w // 2, SH - 11, TEXT, shadow=False)
    c.save('tech_table_v2_1_tree')


# ---------------------------------------------------------------- 2: assembly (flipped sidebar)
def screen_assembly():
    c = Canvas()
    background(c)
    draw_tree(c, selected='shooters', hovered='cartridge', t=0.7)
    toolbar(c, active=1)
    counter(c, 'Изучено 9 / 14', 312)
    hover_label(c, 'cartridge', 0.8)
    x0 = sidebar(c, flipped=True)
    w = 160
    c.d.rectangle([x0 * K, 0, SW * K, 14 * K], fill=(173, 244, 255, 45))
    c.d.line([(x0 * K, 14 * K), (SW * K, 14 * K)], fill=(173, 244, 255, 160), width=K)
    c.flush_px()
    F.center(c.px, 'v  Описание  v', x0 + w // 2, 3, TEXT, shadow=False)
    # hologram: projector cone + the gadget large
    cx, cy = x0 + w // 2, 70
    cone = Image.new('RGBA', c.hi.size, (0, 0, 0, 0))
    cd = ImageDraw.Draw(cone)
    cd.polygon([((cx - 8) * K, 112 * K), ((cx + 8) * K, 112 * K), ((cx + 46) * K, 30 * K), ((cx - 46) * K, 30 * K)],
               fill=(173, 244, 255, 38))
    cd.ellipse([(cx - 34) * K, 104 * K, (cx + 34) * K, 118 * K], outline=(173, 244, 255, 200), width=K)
    cd.ellipse([(cx - 22) * K, 108 * K, (cx + 22) * K, 115 * K], fill=(173, 244, 255, 70))
    c.comp(cone.filter(ImageFilter.GaussianBlur(2)))
    c.comp(cone)
    g = icon('web_shooters').resize((64, 64), Image.NEAREST)
    holo = Image.new('RGBA', g.size, (0, 0, 0, 0))
    a = g.split()[3]
    tinted = Image.merge('RGBA', [g.split()[0].point(lambda v: int(v * 0.6 + 70)), g.split()[1].point(lambda v: int(v * 0.8 + 60)),
                                  g.split()[2].point(lambda v: min(255, int(v * 0.9 + 90))), a])
    holo.alpha_composite(tinted)
    for yy in range(0, 64, 2):
        for xx in range(64):
            p = holo.getpixel((xx, yy))
            if p[3]:
                holo.putpixel((xx, yy), (p[0], p[1], p[2], 200))
    c.flush_px()
    c.comp(holo.resize((64 * K, 64 * K), Image.NEAREST), ((cx - 32) * K, 36 * K))
    F.center(c.px, 'Веб-шутеры', cx, 20, WHITE, shadow=True)
    F.draw(c.px, '360°', x0 + w - 26, 20, TEXT_D, shadow=False)
    y = 124
    F.draw(c.px, 'Детали', x0 + 10, y, TEXT_D, shadow=False)
    F.draw(c.px, 'есть / нужно', x0 + w - 10 - F.width('есть / нужно'), y, TEXT_D, shadow=False)
    y += 12
    rows = [('mechanism', 'Механизм', 1, 1), ('spring', 'Пружина', 1, 1), ('nozzle', 'Сопло', 0, 1), ('iron', 'Железо', 9, 2)]
    for ic, name, have, need in rows:
        c.d.rectangle([(x0 + 8) * K, (y - 2) * K, (x0 + w - 8) * K, (y + 16) * K], fill=(14, 22, 24, 120))
        c.px.alpha_composite(icon(ic), (x0 + 10, y - 1))
        F.draw(c.px, name, x0 + 30, y + 3, TEXT, shadow=False)
        t = f'{have}/{need}'
        F.draw(c.px, t, x0 + w - 12 - F.width(t), y + 3, DONE if have >= need else hx('#ff7a6a'), shadow=False)
        y += 20
    y += 2
    F.draw(c.px, 'Сопло: собрать за 1 железо', x0 + 10, y, hx('#ff9b8f'), shadow=False)
    y += 14
    big_button(c, x0 + 10, y, 70, 16, 'СОБРАТЬ', col=(95, 151, 163, 255))
    # progress ring + result slot
    c.d.rectangle([(x0 + 86) * K, (y + 6) * K, (x0 + 124) * K, (y + 10) * K], outline=(95, 151, 163, 255), width=K)
    c.d.rectangle([(x0 + 128) * K, (y - 1) * K, (x0 + 146) * K, (y + 17) * K], fill=(14, 22, 24, 200),
                  outline=TEXT[:3] + (255,), width=K)
    c.flush_px()
    c.px.alpha_composite(tint(icon('web_shooters'), 0.35), (x0 + 129, y))
    hint(c, 'Колесо на панели — описание / сборка')
    c.save('tech_table_v2_2_assembly')


# ---------------------------------------------------------------- 3: search + unlocking
def screen_research():
    c = Canvas()
    background(c)
    hl = {'web_shooters', 'empty', 'cartridge', 'shooters', 'electro', 'mod_drum', 'mod_spring', 'mod_feed'}
    draw_tree(c, selected='chip', highlight=hl, dim=True, unlocking='chip', t=0.5)
    toolbar(c, search='паут', active=0)
    counter(c, 'Найдено: 7', 312)
    x0 = sidebar(c)
    w = 160
    big_button(c, x0 + 10, 0, w - 20, 16, 'ИЗУЧЕНИЕ...', col=TEXT, glow=1.0)
    # progress sweeping under the button
    c.d.rectangle([(x0 + 10) * K, 16 * K, (x0 + 10 + (w - 20) * 0.62) * K, 18 * K], fill=DONE)
    c.flush_px()
    F.center(c.px, 'Микрочип', x0 + w // 2, 26, TEXT, shadow=False)
    F.center(c.px, 'Микрочип', x0 + w // 2 + 1, 26, TEXT, shadow=False)
    F.center(c.px, 'Деталь · электроника', x0 + w // 2, 37, TEXT_D, shadow=False)
    y = 52
    for line in wrap('Мозг гаджетов: нужен маячку, дронам и электро-паутине.', w - 20):
        F.draw(c.px, line, x0 + 10, y, TEXT, shadow=False)
        y += 10
    y += 6
    F.draw(c.px, 'Задания', x0 + 10, y, TEXT_D, shadow=False); y += 12
    objective(c, x0 + 10, y, 'Собери плату', True, w); y += 11
    objective(c, x0 + 10, y, 'Найди светокамень', True, w); y += 16
    F.draw(c.px, 'Цена', x0 + 10, y, TEXT_D, shadow=False); y += 14
    price(c, x0 + 10, y, xp_icon(), 12, 12)
    price(c, x0 + 82, y, 'glowstone', 4, 4)
    y += 20
    price(c, x0 + 10, y, 'gold', 2, 2)
    # particles flying from the price into the node (unlock animation)
    nx, ny = NODES['chip'][:2]
    sx, sy = x0 + 20, y - 10
    for i in range(14):
        tt = i / 13
        px = sx + (nx - sx) * tt
        py = sy + (ny - sy) * tt - math.sin(tt * math.pi) * 40
        r = 1.6 - tt
        c.d.ellipse([(px - r) * K, (py - r) * K, (px + r) * K, (py + r) * K], fill=(173, 244, 255, int(220 - 120 * tt)))
    y += 26
    for line in wrap('Поиск подсвечивает подходящие технологии и путь к ним.', w - 20):
        F.draw(c.px, line, x0 + 10, y, TEXT_D, shadow=False)
        y += 10
    hint(c, 'Жёлтое — найдено поиском')
    c.save('tech_table_v2_3_research')


screen_tree()
screen_assembly()
screen_research()
