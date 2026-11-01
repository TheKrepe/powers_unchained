"""Powers menu (abilities + progression) concept mockups — NOT implemented yet.

Same kit and style as the Suit Creator / Tech Table screens: vanilla panel, dark monitors, copper + cyan accents.
Layout idea from Alien Evolution: the player stands in the middle, everything else is arranged around them.

python3 powers_screens.py <out dir>
  -> powers_1_abilities.png, powers_2_progression.png, powers_3_keys.png, powers_4_button.png  (x3 scale)
"""
import math
import os
import sys
from PIL import Image, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
SC = os.path.join(HERE, '..', '..', 'suit_creator_gui', 'src')
sys.path.insert(0, SC)
import mc_ui as U  # noqa: E402
import screens as S  # noqa: E402  (doll renderer)

F = U.Font()
hx = U.hx
OUT = sys.argv[1] if len(sys.argv) > 1 else '.'
SW, SH = 480, 270
GX, GY, GW, GH = 32, 17, 416, 236

COPPER, COPPER_D = hx('#ff9a52'), hx('#9c5426')
AMBER = hx('#ffb13b')
GREY, LGREY, DIM, WHITE = hx('#404040'), hx('#9aa3ad'), hx('#5c646e'), hx('#ffffff')
RED, GREEN, CYAN, CYAN_D = hx('#ff5a4a'), hx('#7cfc6a'), hx('#59e4ff'), hx('#2f8fa8')
PURPLE = hx('#c08cff')

MOD = U.MOD + '/textures/'


def tex(path):
    return Image.open(MOD + path + '.png').convert('RGBA')


def item(name):
    return U.sprite('item/' + name)


ICONS = {
    'spider_sense': lambda: tex('gui/sprites/power/spider_sense'),
    'wall_climbing': lambda: tex('gui/sprites/power/wall_climbing'),
    'night_vision': lambda: tex('gui/sprites/power/night_vision'),
    'roll': lambda: roll_icon(),
    'web_shot': lambda: tex('gui/web/impact_web').resize((16, 16), Image.NEAREST),
    'web_zip': lambda: tex('gui/web/sticky_web').resize((16, 16), Image.NEAREST),
    'web_swing': lambda: item('string'),
    'electro': lambda: tex('gui/web/electro_web').resize((16, 16), Image.NEAREST),
    'web_shooters': lambda: tex('item/web_shooters'),
    'lock': lambda: tex('gui/sprites/suit_creator/lock'),
}


def roll_icon():
    """Placeholder roll icon: a curved arrow (3/4 circle) with a head."""
    im = Image.new('RGBA', (16, 16))
    for t in range(40, 330, 3):
        a = math.radians(t)
        for rr in (5.0, 5.8):
            im.putpixel((int(8 + rr * math.cos(a)), int(8 - rr * math.sin(a))), (230, 236, 242, 255))
    for (x, y) in ((12, 9), (13, 9), (14, 9), (13, 10), (13, 8), (12, 10), (14, 8)):
        im.putpixel((x, y), (124, 252, 106, 255))
    return im


def ic(name):
    return ICONS[name]()


# ------------------------------------------------------------------ frame
def background():
    img = Image.new('RGBA', (SW, SH))
    for y in range(SH):
        c = int(30 + 10 * y / SH)
        U.fill(img, 0, y, SW, 1, (c, c + 2, c + 6, 255))
    glow = Image.new('RGBA', (SW, SH), (0, 0, 0, 0))
    for r in range(90, 0, -6):
        U.fill(glow, SW // 2 - r * 2, SH - 40 - r // 2, r * 4, r, (90, 200, 255, 5))
    img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(12)))
    U.fill(img, 0, 0, SW, SH, (0, 0, 0, 120))
    return img


def frame(tab, points=3):
    img = background()
    U.panel(img, GX, GY, GW, GH)
    F.draw(img, 'Способности', GX + 8, GY + 7, GREY, shadow=False)
    tx = GX + 112
    for name in ('Силы', 'Прокачка', 'Клавиши'):
        sel = name == tab
        w = F.width(name) + 14
        U.button(img, F, tx, GY + 4, w, 14, name, 'hover' if sel else 'normal', hx('#ffd9b8') if sel else WHITE)
        if sel:
            U.fill(img, tx + 3, GY + 15, w - 6, 1, COPPER)
        tx += w + 2
    # power points counter (top right), like the Tech Table's counter chip
    text = f'{points} ОС'
    w = F.width(text) + 20
    x = GX + GW - 8 - w
    U.inset(img, x, GY + 4, w, 14, hx('#1d2128'))
    diamond(img, x + 4, GY + 6, AMBER)
    F.draw(img, text, x + 16, GY + 7, AMBER, shadow=False)
    return img


def diamond(img, x, y, c):
    for i in range(5):
        U.fill(img, x + 4 - i, y + i, 1 + 2 * i, 1, c)
        U.fill(img, x + 4 - i, y + 8 - i, 1 + 2 * i, 1, c)


def card(img, x, y, w, icon, name, sub, selected=False, locked=False, tag=None, tag_c=None):
    U.fill(img, x, y, w, 20, (255, 154, 82, 64) if selected else (255, 255, 255, 12))
    if selected:
        U.outline(img, x, y, w, 20, COPPER)
    U.inset(img, x + 2, y + 1, 18, 18, hx('#2a3038'))
    if icon is not None:
        U.icon(img, icon, x + 3, y + 2, dark=locked)
    F.draw(img, name, x + 23, y + 2, hx('#8a929a') if locked else (hx('#ffd9b8') if selected else WHITE))
    if sub:
        F.draw(img, sub, x + 23, y + 11, DIM if locked else LGREY, shadow=False)
    if tag:
        tw = F.width(tag) + 4
        U.fill(img, x + w - tw - 2, y + 2, tw, 9, tag_c)
        F.draw(img, tag, x + w - tw, y + 3, WHITE, shadow=False)
    if locked:
        U.icon(img, ic('lock'), x + w - 11, y + 6, size=8)


def node(img, cx, cy, icon, state='on', size=22, level=None, selected=False):
    """Round ability socket: on = cyan ring, off = grey, locked = dark + lock, selected = copper."""
    r = size // 2
    ring = {'on': CYAN, 'off': hx('#6b7682'), 'locked': hx('#3a4048'), 'new': AMBER}[state]
    for y in range(-r, r + 1):
        for x in range(-r, r + 1):
            d = math.hypot(x, y)
            if d <= r:
                c = ring if d > r - 1.5 else ((18, 26, 34, 255) if state != 'on' else (16, 40, 52, 255))
                U.fill(img, cx + x, cy + y, 1, 1, c)
    if selected:
        for a in range(0, 360, 4):
            t = math.radians(a)
            U.fill(img, int(cx + (r + 2) * math.cos(t)), int(cy + (r + 2) * math.sin(t)), 1, 1, COPPER)
    if icon is not None:
        U.icon(img, icon, cx - 8, cy - 8, dark=state == 'locked')
    if state == 'locked':
        U.icon(img, ic('lock'), cx + r - 6, cy + r - 7, size=8)
    if level:
        cur, mx = level
        for i in range(mx):
            px = cx - (mx * 4 - 1) // 2 + i * 4
            U.fill(img, px, cy + r + 3, 3, 2, AMBER if i < cur else hx('#3a4048'))


def line(img, x0, y0, x1, y1, c, dashed=False):
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(n + 1):
        if dashed and (i // 2) % 2:
            continue
        U.fill(img, round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n), 1, 1, c)


def elbow(img, x0, y0, x1, y1, c):
    """Circuit-style connector: horizontal, then a 45 degree bend, like traces on a board."""
    mx = x1 - (abs(y1 - y0)) * (1 if x1 > x0 else -1)
    line(img, x0, y0, mx, y0, c)
    line(img, mx, y0, x1, y1, c)
    U.fill(img, x0 - 1, y0 - 1, 3, 3, c)


def hologram(img, x, y, w, h, title, yaw=200, size=92, scan=True):
    U.monitor(img, x, y, w, h)
    U.brackets(img, x + 2, y + 2, w - 4, h - 4, CYAN_D)
    F.draw(img, title, x + 6, y + 5, CYAN_D, shadow=False)
    cx = x + w // 2
    feet = y + h - 12
    # beam + pedestal
    for yy in range(y + 16, feet):
        k = (yy - y - 16) / max(1, feet - y - 16)
        half = int(14 + 16 * k)
        U.fill(img, cx - half, yy, half * 2, 1, (89, 228, 255, int(6 + 20 * k)))
    U.fill(img, cx - 32, feet, 64, 1, CYAN)
    U.fill(img, cx - 24, feet + 1, 48, 1, (89, 228, 255, 140))
    U.fill(img, cx - 16, feet + 2, 32, 1, (89, 228, 255, 70))
    d = S.doll('spider_man', size, yaw)
    bb = d.getbbox()
    d = d.crop(bb)
    # a cyan holographic tint
    tint = Image.new('RGBA', d.size, (89, 228, 255, 40))
    tint.putalpha(d.split()[3].point(lambda v: 40 if v else 0))
    d.alpha_composite(tint)
    img.alpha_composite(d, (cx - d.width // 2, feet - d.height))
    return cx, feet - d.height, feet, d.height


def bar(img, x, y, w, p, color, bg=hx('#1d1f24')):
    U.inset(img, x, y, w, 6, bg)
    U.fill(img, x + 1, y + 1, int((w - 2) * p), 4, color)


def wrap(img, text, x, y, w, color, shadow=False):
    words = text.split(' ')
    line_ = ''
    for wd in words:
        t = (line_ + ' ' + wd).strip()
        if F.width(t) > w and line_:
            F.draw(img, line_, x, y, color, shadow)
            y += 10
            line_ = wd
        else:
            line_ = t
    if line_:
        F.draw(img, line_, x, y, color, shadow)
        y += 10
    return y


def key_box(img, x, y, k, active=True):
    w = max(11, F.width(k) + 5)
    U.fill(img, x, y, w, 11, hx('#1d2128') if active else hx('#2a2d33'))
    U.outline(img, x, y, w, 11, LGREY if active else DIM)
    F.draw(img, k, x + (w - F.width(k)) // 2, y + 2, WHITE if active else DIM, shadow=False)
    return w


def finish(img, name):
    os.makedirs(OUT, exist_ok=True)
    img.resize((SW * 3, SH * 3), Image.NEAREST).save(os.path.join(OUT, name + '.png'))
    print('wrote', name)


LX, LY, LW, LH = GX + 8, GY + 21, 128, 207
CX, CW = GX + 140, 162
RX, RW = GX + 306, 102


def power_list(img, selected='spider'):
    U.monitor(img, LX, LY, LW, LH)
    # search, like the Tech Table
    U.inset(img, LX + 3, LY + 3, LW - 6, 14, hx('#000000'))
    U.outline(img, LX + 3, LY + 3, LW - 6, 14, hx('#a0a0a0'))
    F.draw(img, 'Поиск...', LX + 6, LY + 6, hx('#6f7780'), shadow=False)
    y = LY + 22
    card(img, LX + 2, y, LW - 4, tex('gui/sprites/suit_creator/group/spider_man').resize((16, 16), Image.NEAREST),
         'Человек-паук', 'Сила · ур. 4', selected == 'spider')
    y += 22
    card(img, LX + 2, y, LW - 4, ic('web_shooters'), 'Веб-шутеры', 'Гаджет · 3 способн.', selected == 'shooters')
    y += 22
    card(img, LX + 2, y, LW - 4, None, '???', 'Новая сила', locked=True)
    y += 22
    card(img, LX + 2, y, LW - 4, None, '???', 'Новый костюм', locked=True)
    y += 26
    y = wrap(img, 'Силы, костюмы и гаджеты других героев появятся здесь сами, когда игрок их получит.', LX + 6, y, LW - 12, DIM)
    return y


# ------------------------------------------------------------------ 1: abilities around the player
def screen_abilities():
    img = frame('Силы')
    power_list(img)

    # centre: the player as a hologram, abilities wired to the body part they come from
    hx0, hy0, hw, hh = CX, GY + 20, CW, 160
    cx, top, feet, dh = hologram(img, hx0, hy0, hw, hh, 'Человек-паук', yaw=200, size=104)
    head = (cx, top + 8)
    chest = (cx, top + 26)
    lhand = (cx - 13, top + 46)
    rhand = (cx + 13, top + 46)
    legs = (cx, top + 74)
    nodes = [  # (x, y, icon, state, level, anchor, label)
        (hx0 + 20, hy0 + 30, 'spider_sense', 'on', (2, 3), head),
        (hx0 + 20, hy0 + 62, 'wall_climbing', 'on', (1, 3), lhand),
        (hx0 + 20, hy0 + 94, 'night_vision', 'off', (1, 1), head),
        (hx0 + 20, hy0 + 126, 'roll', 'on', (1, 2), legs),
        (hx0 + hw - 20, hy0 + 30, 'web_shot', 'on', (3, 3), rhand),
        (hx0 + hw - 20, hy0 + 62, 'web_zip', 'on', (2, 3), rhand),
        (hx0 + hw - 20, hy0 + 94, 'web_swing', 'on', (1, 3), rhand),
        (hx0 + hw - 20, hy0 + 126, 'electro', 'locked', None, rhand),
    ]
    for (x, y, icn, st, lvl, anchor) in nodes:
        c = CYAN_D if st == 'on' else hx('#3a4652')
        elbow(img, anchor[0], anchor[1], x + (12 if x < cx else -12), y, c)
    for (x, y, icn, st, lvl, anchor) in nodes:
        node(img, x, y, ic(icn), st, level=lvl, selected=icn == 'spider_sense')
    # power level bar under the hologram
    by = hy0 + hh + 4
    U.monitor(img, CX, by, CW, 27, scan=False)
    F.draw(img, 'Сила паука', CX + 5, by + 4, WHITE)
    F.draw(img, 'ур. 4', CX + CW - 5 - F.width('ур. 4'), by + 4, AMBER)
    bar(img, CX + 5, by + 16, CW - 10, 0.62, CYAN)
    F.draw(img, '620/1000', CX + CW - 48, by + 15, LGREY, shadow=False) if False else None

    # right: the selected ability
    U.monitor(img, RX, GY + 20, RW, GH - 28)
    y = GY + 24
    U.icon(img, ic('spider_sense'), RX + 4, y)
    F.draw(img, 'Паучье', RX + 23, y, COPPER)
    F.draw(img, 'чутьё', RX + 23, y + 9, COPPER)
    y += 22
    for i in range(3):
        U.fill(img, RX + 5 + i * 10, y, 8, 3, AMBER if i < 2 else hx('#3a4048'))
    F.draw(img, 'Ур. 2/3', RX + RW - 5 - F.width('Ур. 2/3'), y - 3, AMBER, shadow=False)
    y += 8
    y = wrap(img, 'Предупреждает об опасности: враги, снаряды, крипер, динамит.', RX + 5, y, RW - 10, LGREY) + 4
    F.draw(img, 'Клавиша', RX + 5, y, CYAN_D, shadow=False)
    key_box(img, RX + RW - 18, y - 2, '—')
    y += 12
    F.draw(img, 'Колесо', RX + 5, y, CYAN_D, shadow=False)
    F.draw(img, 'да', RX + RW - 5 - F.width('да'), y, GREEN, shadow=False)
    y += 14
    U.button(img, F, RX + 3, y, RW - 6, 16, 'Выключить')
    y += 20
    U.button(img, F, RX + 3, GY + GH - 28, RW - 6, 16, 'К прокачке', 'hover', hx('#ffd9b8'))
    U.fill(img, RX + 6, GY + GH - 15, RW - 12, 1, COPPER)
    finish(img, 'powers_1_abilities')


# ------------------------------------------------------------------ 2: progression tree around the player
def screen_progression():
    img = frame('Прокачка')
    y = power_list(img)

    # centre: player in a ring, three branches radiating out (Alien Evolution's evolution wheel)
    x0, y0, w, h = CX, GY + 20, CW, GH - 28
    U.monitor(img, x0, y0, w, h)
    U.brackets(img, x0 + 2, y0 + 2, w - 4, h - 4, CYAN_D)
    F.draw(img, 'Древо силы', x0 + 6, y0 + 5, CYAN_D, shadow=False)
    cx, cy = x0 + w // 2, y0 + 120
    # rings
    for rr, a in ((20, 120), (42, 60), (66, 35)):
        for t in range(0, 360, 2):
            r = math.radians(t)
            U.fill(img, int(cx + rr * math.cos(r)), int(cy + rr * math.sin(r)), 1, 1, (89, 228, 255, a))
    d = S.doll('spider_man', 44, 200)
    d = d.crop(d.getbbox())
    img.alpha_composite(d, (cx - d.width // 2, cy - d.height // 2))
    branches = [  # angle, name, colour, tiers (icon, state)
        (-90, 'Паутина', CYAN, [('web_shot', 'on'), ('web_zip', 'on'), ('electro', 'new')]),
        (150, 'Ловкость', GREEN, [('wall_climbing', 'on'), ('roll', 'off'), (None, 'locked')]),
        (30, 'Чутьё', PURPLE, [('spider_sense', 'on'), ('night_vision', 'off'), (None, 'locked')]),
    ]
    for ang, name, col, tiers in branches:
        a = math.radians(ang)
        prev = (cx + 20 * math.cos(a), cy + 20 * math.sin(a))
        for i, (icn, st) in enumerate(tiers):
            rr = 32 + i * 23
            p = (cx + rr * math.cos(a), cy + rr * math.sin(a))
            line(img, int(prev[0]), int(prev[1]), int(p[0]), int(p[1]), col if st == 'on' else hx('#3a4652'), dashed=st != 'on')
            prev = p
        for i, (icn, st) in enumerate(tiers):
            rr = 32 + i * 23
            p = (int(cx + rr * math.cos(a)), int(cy + rr * math.sin(a)))
            node(img, p[0], p[1], ic(icn) if icn else None, st, size=20, selected=(name == 'Паутина' and i == 2))
        lx = int(cx + 78 * math.cos(a))
        ly = int(cy + 78 * math.sin(a)) + (14 if ang > 0 else -26)
        lx = min(max(lx - F.width(name) // 2, x0 + 4), x0 + w - 4 - F.width(name))
        F.draw(img, name, lx, min(max(ly - 4, y0 + 16), y0 + h - 12), col)

    # right: the picked node — cost in power points + a task, like the Tech Table modules
    U.monitor(img, RX, GY + 20, RW, GH - 28)
    yy = GY + 24
    U.icon(img, ic('electro'), RX + 4, yy)
    F.draw(img, 'Электро-', RX + 23, yy, COPPER)
    F.draw(img, 'паутина', RX + 23, yy + 9, COPPER)
    yy += 22
    F.draw(img, 'Паутина · III', RX + 5, yy, CYAN_D, shadow=False)
    yy += 12
    yy = wrap(img, 'Новый тип выстрела: удар током, оглушение, цепная молния.', RX + 5, yy, RW - 10, LGREY) + 4
    F.draw(img, 'Задание:', RX + 5, yy, COPPER_D, shadow=False)
    yy += 11
    yy = wrap(img, '✔ Попасть под молнию в веб-шутерах', RX + 5, yy, RW - 10, GREEN) + 2
    yy = wrap(img, '✘ Оглушить 10 мобов', RX + 5, yy, RW - 10, RED) + 4
    F.draw(img, 'Цена:', RX + 5, yy, COPPER_D, shadow=False)
    F.draw(img, '2 ОС', RX + RW - 5 - F.width('2 ОС'), yy, AMBER, shadow=False)
    diamond(img, RX + RW - 18 - F.width('2 ОС'), yy - 1, AMBER)
    yy += 14
    F.draw(img, 'Нужно: ур. силы 5', RX + 5, yy, RED, shadow=False)
    U.button(img, F, RX + 3, GY + GH - 28, RW - 6, 16, 'Изучить', 'disabled')
    finish(img, 'powers_2_progression')


# ------------------------------------------------------------------ 3: keys / loadout around the player
def screen_keys():
    img = frame('Клавиши')
    # left: every ability the player has, drag them onto the keys
    U.monitor(img, LX, LY, LW, LH)
    U.inset(img, LX + 3, LY + 3, LW - 6, 14, hx('#000000'))
    U.outline(img, LX + 3, LY + 3, LW - 6, 14, hx('#a0a0a0'))
    F.draw(img, 'Поиск...', LX + 6, LY + 6, hx('#6f7780'), shadow=False)
    y = LY + 22
    for icn, name, sub in (('web_shot', 'Выстрел паутиной', 'Веб-шутеры'), ('web_zip', 'Рывок на паутине', 'Веб-шутеры'),
                           ('web_swing', 'Полёт на паутине', 'Веб-шутеры'), ('roll', 'Кувырок', 'Человек-паук'),
                           ('spider_sense', 'Колесо сил', 'Человек-паук')):
        card(img, LX + 2, y, LW - 4, ic(icn), name, sub, selected=name == 'Кувырок')
        y += 22

    # centre: the player with 5 key sockets in an arc under them
    x0, y0, w, h = CX, GY + 20, CW, GH - 28
    cx, top, feet, dh = hologram(img, x0, y0, w, h - 52, 'Набор', yaw=200, size=84)
    keys = [('C', 'web_shot'), ('V', 'web_zip'), ('B', 'web_swing'), ('M', 'roll'), (',', None)]
    for i, (k, icn) in enumerate(keys):
        a = math.radians(200 - i * 55 / 1.0 * 0.4 - 0)  # unused
        sx = x0 + 9 + i * 30
        sy = y0 + h - 46
        U.inset(img, sx, sy, 26, 26, hx('#1a2027'))
        U.outline(img, sx - 1, sy - 1, 28, 28, CYAN if icn else hx('#3a4652'))
        if icn:
            U.icon(img, ic(icn), sx + 5, sy + 5)
        else:
            F.center(img, '+', sx + 13, sy + 9, DIM, shadow=False)
        key_box(img, sx + 7, sy + 29, k, active=icn is not None)
    line(img, x0 + 9 + 3 * 30 + 13, y0 + h - 47, x0 + 9 + 3 * 30 + 13, feet + 4, AMBER, dashed=True)

    # right: HUD preview + options
    U.monitor(img, RX, GY + 20, RW, GH - 28)
    yy = GY + 24
    F.draw(img, 'На экране', RX + 5, yy, CYAN_D, shadow=False)
    yy += 13
    for k, name in (('C', 'Выстрел'), ('V', 'Рывок'), ('B', 'Полёт'), ('M', 'Кувырок')):
        kw = key_box(img, RX + 5, yy, k)
        F.draw(img, name, RX + 9 + kw, yy + 2, WHITE)
        yy += 14
    yy += 6
    U.button(img, F, RX + 3, yy, RW - 6, 16, 'Автоматически')
    yy += 20
    U.button(img, F, RX + 3, yy, RW - 6, 16, 'Настройки HUD')
    yy += 24
    wrap(img, 'Перетащи способность слева на клавишу. ПКМ по клавише — убрать.', RX + 5, yy, RW - 10, DIM)
    finish(img, 'powers_3_keys')


# ------------------------------------------------------------------ 4: the inventory button and where it can be
def screen_button():
    img = background()
    # survival inventory (176x166), the recipe book button + ours next to it
    ix, iy = 40, 52
    U.panel(img, ix, iy, 176, 166)
    F.draw(img, 'Создание', ix + 97, iy + 8, GREY, shadow=False)
    for r in range(3):
        for c in range(9):
            U.slot(img, ix + 7 + c * 18, iy + 83 + r * 18)
    for c in range(9):
        U.slot(img, ix + 7 + c * 18, iy + 141)
    for r in range(4):
        U.slot(img, ix + 7, iy + 7 + r * 18)
    U.slot(img, ix + 76, iy + 61)
    for r in range(2):
        for c in range(2):
            U.slot(img, ix + 97 + c * 18, iy + 17 + r * 18)
    U.slot(img, ix + 153, iy + 27)
    U.inset(img, ix + 25, iy + 7, 51, 72, hx('#000000'))
    d = S.doll('spider_man', 60, 200)
    d = d.crop(d.getbbox())
    img.alpha_composite(d, (ix + 50 - d.width // 2, iy + 76 - d.height))
    rb = Image.open(U.MC + '/textures/gui/sprites/recipe_book/button.png').convert('RGBA')
    img.alpha_composite(rb, (ix + 104, iy + 61))
    ours = tex('gui/sprites/powers_menu/button')
    img.alpha_composite(ours, (ix + 126, iy + 61))  # 1: next to the recipe book (default)
    img.alpha_composite(ours, (ix + 178, iy + 4))   # 2: right of the inventory
    img.alpha_composite(ours, (ix - 22, iy + 4))    # 3: left of the inventory
    for n, (x, y) in enumerate(((ix + 136, iy + 82), (ix + 188, iy + 25), (ix - 12, iy + 25)), 1):
        U.fill(img, x - 5, y - 1, 11, 11, COPPER)
        F.draw(img, str(n), x - 2, y + 1, WHITE, shadow=False)
    F.draw(img, 'Выживание', ix, iy - 12, WHITE)

    # creative inventory tab (195x136): the same three places
    cx0, cy0 = 256, 70
    U.panel(img, cx0, cy0, 195, 136)
    for r in range(3):
        for c in range(9):
            U.slot(img, cx0 + 8 + c * 18, cy0 + 53 + r * 18)
    for c in range(9):
        U.slot(img, cx0 + 8 + c * 18, cy0 + 111)
    for (x, y) in ((54, 5), (54, 32), (108, 5), (108, 32), (35, 19), (172, 111)):
        U.slot(img, cx0 + x, cy0 + y)
    U.inset(img, cx0 + 73, cy0 + 5, 32, 43, hx('#000000'))
    d2 = S.doll('spider_man', 40, 200)
    d2 = d2.crop(d2.getbbox())
    img.alpha_composite(d2, (cx0 + 89 - d2.width // 2, cy0 + 47 - d2.height))
    img.alpha_composite(ours, (cx0 + 151, cy0 + 20))  # 1 in creative: right of the armour, on the inventory tab
    img.alpha_composite(ours, (cx0 + 197, cy0 + 4))
    img.alpha_composite(ours, (cx0 - 22, cy0 + 4))
    for n, (x, y) in enumerate(((cx0 + 161, cy0 + 41), (cx0 + 207, cy0 + 25), (cx0 - 12, cy0 + 25)), 1):
        U.fill(img, x - 5, y - 1, 11, 11, COPPER)
        F.draw(img, str(n), x - 2, y + 1, WHITE, shadow=False)
    F.draw(img, 'Творческий (вкладка инвентаря)', cx0, cy0 - 12, WHITE)

    # legend + the button itself, big
    big = ours.resize((40, 36), Image.NEAREST)
    img.alpha_composite(big, (380, 222))
    big2 = tex('gui/sprites/powers_menu/button_highlighted').resize((40, 36), Image.NEAREST)
    img.alpha_composite(big2, (424, 222))
    yy = 218
    for t in ('1 — у книги рецептов (по умолч.)', '2 — справа от инвентаря', '3 — слева от инвентаря',
              '4 — скрыта (только клавиша J)'):
        F.draw(img, t, 392 - 0, yy, WHITE) if False else None
        yy += 10
    F.draw(img, 'Где кнопка — в настройках мода:', 40, 226, AMBER)
    F.draw(img, '1 у книги рецептов · 2 справа · 3 слева · 4 скрыта', 40, 238, WHITE)
    F.draw(img, 'Клавиша: J', 40, 250, LGREY)
    finish(img, 'powers_4_button')


if __name__ == '__main__':
    screen_abilities()
    screen_progression()
    screen_keys()
    screen_button()
