import math
import sys
from PIL import Image, ImageFilter
import mc_ui as U
import doll3d
import blockrender

F = U.Font()
hx = U.hx
AMBER, AMBER_D = hx('#ffb13b'), hx('#a8661a')
CYAN, CYAN_D = hx('#59e4ff'), hx('#1f7f99')
GREY, LGREY, DARK = hx('#404040'), hx('#9aa4ad'), hx('#12161b')
WHITE, RED, GREEN = hx('#ffffff'), hx('#ff5a4a'), hx('#7cff6b')
SW, SH = 480, 270


def background():
    img = Image.new('RGBA', (SW, SH))
    # a blurred workshop: dark wall, the Suit Creator's amber glow
    for y in range(SH):
        c = int(34 + 10 * y / SH)
        U.fill(img, 0, y, SW, 1, (c, c + 2, c + 6, 255))
    glow = Image.new('RGBA', (SW, SH), (0, 0, 0, 0))
    for r in range(90, 0, -6):
        U.fill(glow, SW // 2 - r * 2, SH - 40 - r // 2, r * 4, r, (255, 160, 50, 6))
    img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(12)))
    U.fill(img, 0, 0, SW, SH, (0, 0, 0, 120))
    return img


def doll(suit, size, yaw=200, slim=False):
    ref = doll3d.skin_for(suit, slim)
    m = doll3d.player_model(ref, slim)
    return blockrender.render(m, [doll3d.HERE + '/tex'], size=size, yaw=yaw, pitch=10, scale=size / 40, offset_y=8)


def item(name):
    try:
        return U.mod_tex('item/' + name)
    except FileNotFoundError:
        return U.sprite('item/' + name)


def hologram(img, x, y, w, h, suit, yaw=200, label='ГОЛОГРАММА', variant='Классика  1 / 5', size=104, ghost=False):
    U.monitor(img, x, y, w, h)
    # perspective grid floor
    floor_y = y + h - 34
    for i in range(0, 7):
        yy = floor_y + int(i * i * 0.75)
        if yy < y + h - 2:
            U.fill(img, x + 2, yy, w - 4, 1, (89, 228, 255, 26 + i * 4))
    cx = x + w // 2
    for k in range(-6, 7):
        for t in range(0, 30):
            px = cx + int(k * (6 + t * 0.9))
            py = floor_y + t
            if x + 2 <= px < x + w - 2 and py < y + h - 2:
                U.fill(img, px, py, 1, 1, (89, 228, 255, 30))
    # pedestal glow ring
    ring = Image.new('RGBA', img.size, (0, 0, 0, 0))
    for a in range(0, 360, 2):
        px = cx + int(30 * math.cos(math.radians(a)))
        py = floor_y + 12 + int(7 * math.sin(math.radians(a)))
        U.fill(ring, px, py, 2, 1, (89, 228, 255, 210))
    img.alpha_composite(ring.filter(ImageFilter.GaussianBlur(1.6)))
    img.alpha_composite(ring)
    # light beam
    beam = Image.new('RGBA', img.size, (0, 0, 0, 0))
    for yy in range(y + 18, floor_y + 12):
        k = (yy - y - 18) / max(1, floor_y + 12 - y - 18)
        half = int(14 + 16 * k)
        U.fill(beam, cx - half, yy, half * 2, 1, (89, 228, 255, int(10 + 22 * k)))
    img.alpha_composite(beam)
    d = doll(suit, size, yaw)
    bb = d.getbbox()
    d = d.crop(bb)
    if ghost:
        # locked suit: a cyan wireframe-ish silhouette
        a = d.split()[3]
        lum = d.convert('L')
        g = Image.new('RGBA', d.size, (89, 228, 255, 0))
        px, al, lp = g.load(), a.load(), lum.load()
        for yy in range(d.height):
            for xx in range(d.width):
                if al[xx, yy] > 0:
                    edge = any(al[min(d.width - 1, max(0, xx + dx)), min(d.height - 1, max(0, yy + dy))] == 0
                               for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                    px[xx, yy] = (89, 228, 255, 230 if edge else (40 + lp[xx, yy] // 5 if (yy // 2) % 2 == 0 else 25))
        d = g
    img.alpha_composite(d, (cx - d.width // 2, floor_y + 10 - d.height))
    U.brackets(img, x + 3, y + 3, w - 6, h - 6, CYAN, 6)
    F.draw(img, label, x + 7, y + 6, CYAN_D, shadow=False)
    F.draw(img, '360°', x + w - 7 - F.width('360°'), y + 6, CYAN_D, shadow=False)
    # rotate arrows + variant switcher
    pb, pf = U.sprite('gui/sprites/widget/page_backward'), U.sprite('gui/sprites/widget/page_forward')
    img.alpha_composite(pb, (x + 6, y + h - 17))
    img.alpha_composite(pf, (x + w - 6 - pf.width, y + h - 17))
    F.center(img, variant, cx, y + h - 14, WHITE)


def suit_card(img, x, y, w, icon, name, sub, tags, selected=False, locked=False, fav=False):
    h = 24
    U.fill(img, x, y, w, h, (255, 255, 255, 10) if not selected else (255, 177, 59, 40))
    if selected:
        U.outline(img, x, y, w, h, AMBER)
    U.inset(img, x + 3, y + 3, 18, 18, hx('#2a3038'))
    if locked:
        U.icon(img, icon, x + 4, y + 4, dark=True)
        F.draw(img, '???', x + 25, y + 3, hx('#6b737b'))
        F.draw(img, sub, x + 25, y + 13, hx('#555c63'), shadow=False)
        lock = U.sprite('gui/sprites/widget/locked_button')
        img.alpha_composite(lock.crop((3, 3, lock.width - 3, lock.height - 3)), (x + w - 16, y + 7))
        return
    U.icon(img, icon, x + 4, y + 4)
    F.draw(img, name, x + 25, y + 3, WHITE if not selected else hx('#ffe2a8'))
    F.draw(img, sub, x + 25, y + 13, LGREY, shadow=False)
    # tag colours as a stripe on the card's left edge
    for i, c in enumerate(tags):
        seg = (h - 2) // max(1, len(tags))
        U.fill(img, x + 1, y + 1 + i * seg, 1, seg, c)
    if fav:
        star = Image.new('RGBA', (5, 5), (0, 0, 0, 0))
        for (px, py) in ((2, 0), (1, 1), (2, 1), (3, 1), (0, 2), (1, 2), (2, 2), (3, 2), (4, 2), (1, 3), (3, 3), (0, 4), (4, 4)):
            star.putpixel((px, py), AMBER)
        img.alpha_composite(star, (x + w - 9, y + 4))


def group_header(img, x, y, w, text, count, open_=True):
    U.fill(img, x, y, w, 11, (255, 255, 255, 18))
    F.draw(img, ('▼ ' if open_ else '▶ ') + text, x + 3, y + 2, AMBER, shadow=False)
    F.draw(img, str(count), x + w - 3 - F.width(str(count)), y + 2, LGREY, shadow=False)


def search(img, x, y, w, text, placeholder=True):
    U.nine(img, U.sprite('gui/sprites/widget/text_field_highlighted' if not placeholder else 'gui/sprites/widget/text_field'), x, y, w, 14, 1)
    s = U.sprite('gui/sprites/icon/search')
    img.alpha_composite(s, (x + 3, y + (14 - s.height) // 2))
    if placeholder:
        F.draw(img, text, x + 16, y + 3, hx('#707070'), shadow=False)
    else:
        n = F.draw(img, text, x + 16, y + 3, hx('#e0e0e0'))
        U.fill(img, x + 17 + n, y + 3, 1, 9, hx('#d0d0d0'))


TAG = {'Классика': hx('#c8392f'), 'Комиксы': hx('#d98b2b'), 'Тех': hx('#2fa6c8'), 'Фильм': hx('#8a5bd6'),
       'Игры': hx('#3fae5a'), 'Стелс': hx('#4b4f57')}


def inventory(img, x, y):
    F.draw(img, 'Инвентарь', x, y, GREY, shadow=False)
    for r in range(3):
        for c in range(9):
            U.slot(img, x + c * 18, y + 10 + r * 18)
    for c in range(9):
        U.slot(img, x + c * 18, y + 68 + 0)
    # some fabrics in the inventory
    for (c, r, name, n) in ((0, 0, 'red_fabric', 12), (1, 0, 'blue_fabric', 9), (2, 0, 'black_fabric', 3), (3, 0, 'white_fabric', 5)):
        U.icon(img, item(name), x + c * 18 + 1, y + 10 + r * 18 + 1)
        F.draw(img, str(n), x + c * 18 + 17 - F.width(str(n)), y + 10 + r * 18 + 9, WHITE)
    for (c, name) in ((0, 'web_shooters'), (1, 'web_cartridge')):
        U.icon(img, item(name), x + c * 18 + 1, y + 69)


def material_row(img, x, y, w, icon, name, have, need):
    U.icon(img, icon, x + 2, y)
    F.draw(img, name, x + 21, y + 4, hx('#dfe6ec'))
    t = f'{have}/{need}'
    F.draw(img, t, x + w - 4 - F.width(t), y + 4, GREEN if have >= need else RED)


def armor_icons(img, x, y, n):
    full = U.sprite('gui/sprites/hud/armor_full')
    for i in range(n):
        img.alpha_composite(full, (x + i * 8, y))


def frame(tab):
    img = background()
    GX, GY, GW, GH = 20, 7, 440, 256
    U.panel(img, GX, GY, GW, GH)
    F.draw(img, 'Создатель костюмов', GX + 8, GY + 6, GREY, shadow=False)
    U.monitor(img, GX + 352, GY + 3, 80, 13, scan=False)
    F.draw(img, '● 5 / 9', GX + 358, GY + 6, AMBER, shadow=False)
    F.draw(img, 'БД', GX + 412, GY + 6, AMBER_D, shadow=False)
    tx = GX + 148
    for name in ('Каталог', 'Досье', 'Сканер'):
        sel = name == tab
        w = F.width(name) + 12
        U.button(img, F, tx, GY + 3, w, 14, name, 'hover' if sel else 'normal', hx('#ffe2a8') if sel else WHITE)
        if sel:
            U.fill(img, tx + 2, GY + 15, w - 4, 1, AMBER)
        tx += w + 2
    return img, GX, GY


CARDS = [
    ('spider_man_mask', 'Классический', 'Комиксы · 1962', ['Классика', 'Комиксы'], True),
    ('spider_man_first_appearance_mask', '1-е появление', 'Комиксы · 1962', ['Комиксы'], False),
    ('spider_man_advanced_2_mask', 'Продвинутый', 'Игры · Тех', ['Игры', 'Тех'], True),
    ('spider_man_brand_new_day_mask', 'Новый день', 'Комиксы · 2008', ['Комиксы'], False),
    ('spider_man_raimi_mask', 'Рэйми', 'Фильм · 2002', ['Фильм', 'Классика'], False),
]


def catalog(img, GX, GY, query=None, chips=('Все',), selected='Классический', only=None, locked_rows=1, open_locked=False):
    LX = GX + 8
    search(img, LX, GY + 20, 98, query or 'Поиск...', placeholder=query is None)
    fb = U.sprite('gui/sprites/recipe_book/filter_enabled')
    img.alpha_composite(fb, (LX + 99, GY + 19))
    cx = LX
    for name in ('Все', 'Классика', 'Тех'):
        cx += U.chip(img, F, cx, GY + 38, name, TAG.get(name, hx('#5b6168')), selected=name in chips) + 3
    cx = LX
    for name in ('Фильм', 'Комиксы', 'Игры'):
        cx += U.chip(img, F, cx, GY + 52, name, TAG[name], selected=name in chips) + 3
    U.monitor(img, LX, GY + 67, 124, 183)
    ly = GY + 70
    rows = [c for c in CARDS if only is None or c[1] in only]
    group_header(img, LX + 2, ly, 112, 'Человек-паук', len(rows))
    ly += 13
    for icon_name, name, sub, tags, fav in rows:
        suit_card(img, LX + 2, ly, 112, item(icon_name), name, sub, [TAG[t] for t in tags], name == selected, False, fav)
        ly += 26
    if locked_rows:
        group_header(img, LX + 2, ly, 112, '???', 4, open_=open_locked)
        ly += 13
        for i in range(locked_rows):
            suit_card(img, LX + 2, ly, 112, item('spider_man_raimi_mask'), '', 'Нужен скан', [], locked=True)
            ly += 26
    U.nine(img, U.sprite('gui/sprites/widget/scroller_background'), LX + 117, GY + 69, 6, 179, 1)
    U.nine(img, U.sprite('gui/sprites/widget/scroller'), LX + 117, GY + 69, 6, 50, 1)


def finish(img, name):
    img.resize((SW * 3, SH * 3), Image.NEAREST).save(f'/tmp/claude-0/-home-claude/d1a7857d-e1c0-50d7-96f4-5fbbc1adfa14/scratchpad/sc/{name}.png')
