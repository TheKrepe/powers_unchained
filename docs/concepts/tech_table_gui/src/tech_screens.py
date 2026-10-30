"""Tech Table GUI concept mockups (vanilla font / sprites via ../../suit_creator_gui/src/mc_ui.py).

python3 tech_screens.py <out dir>  ->  tech_table_1_blueprints.png, _2_modules.png, _3_research.png (x3 scale)
"""
import math
import os
import sys
from PIL import Image, ImageFilter

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', '..', 'suit_creator_gui', 'src'))
import mc_ui as U  # noqa: E402
import parts  # noqa: E402

F = U.Font()
hx = U.hx
OUT = sys.argv[1] if len(sys.argv) > 1 else '.'
SW, SH = 480, 270

COPPER, COPPER_D = hx('#ff9a52'), hx('#9c5426')
PAPER, PAPER_L, PAPER_LL = hx('#1d3f73'), hx('#2b5592'), hx('#3d6db0')
INK, INK_D = hx('#e9f3ff'), hx('#9fc2ee')
GREY, LGREY, WHITE = hx('#404040'), hx('#9aa4ad'), hx('#ffffff')
RED, GREEN, CYAN = hx('#ff5a4a'), hx('#7cff6b'), hx('#59e4ff')


def mod_item(name):
    return U.mod_tex('item/' + name)


def vanilla(name):
    return U.sprite('item/' + name)


ICON = {
    'web_shooters': lambda: mod_item('web_shooters'),
    'web_cartridge': lambda: mod_item('web_cartridge'),
    'empty_web_cartridge': lambda: mod_item('empty_web_cartridge'),
    'iron': lambda: vanilla('iron_ingot'),
    'copper': lambda: vanilla('copper_ingot'),
    'redstone': lambda: vanilla('redstone'),
    'string': lambda: vanilla('string'),
    'slime': lambda: vanilla('slime_ball'),
    'gold': lambda: vanilla('gold_ingot'),
    'glowstone': lambda: vanilla('glowstone_dust'),
    'pearl': lambda: vanilla('ender_pearl'),
    'membrane': lambda: vanilla('phantom_membrane'),
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


def background():
    img = Image.new('RGBA', (SW, SH))
    for y in range(SH):
        c = int(30 + 10 * y / SH)
        U.fill(img, 0, y, SW, 1, (c + 4, c + 2, c, 255))
    glow = Image.new('RGBA', (SW, SH), (0, 0, 0, 0))
    for r in range(90, 0, -6):
        U.fill(glow, SW // 2 - r * 2, SH - 40 - r // 2, r * 4, r, (255, 140, 70, 5))
    img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(12)))
    U.fill(img, 0, 0, SW, SH, (0, 0, 0, 120))
    return img


def frame(tab, counter='● 3 / 7'):
    img = background()
    GX, GY, GW, GH = 20, 7, 440, 256
    U.panel(img, GX, GY, GW, GH)
    F.draw(img, 'Технический стол', GX + 8, GY + 6, GREY, shadow=False)
    U.monitor(img, GX + 352, GY + 3, 80, 13, scan=False)
    F.draw(img, counter, GX + 358, GY + 6, COPPER, shadow=False)
    F.draw(img, 'ЧРТ', GX + 410, GY + 6, COPPER_D, shadow=False)
    tx = GX + 136
    for name in ('Чертежи', 'Модули', 'Изучение'):
        sel = name == tab
        w = F.width(name) + 12
        U.button(img, F, tx, GY + 3, w, 14, name, 'hover' if sel else 'normal', hx('#ffd9b8') if sel else WHITE)
        if sel:
            U.fill(img, tx + 2, GY + 15, w - 4, 1, COPPER)
        tx += w + 2
    return img, GX, GY


def blueprint(img, x, y, w, h, title, number):
    """Blueprint paper: deep blue, fine grid, stronger lines every 8 px, white title block."""
    U.inset(img, x, y, w, h, PAPER)
    for gx in range(x + 1, x + w - 1):
        if (gx - x) % 8 == 0:
            U.fill(img, gx, y + 1, 1, h - 2, PAPER_LL)
        elif (gx - x) % 4 == 0:
            U.fill(img, gx, y + 1, 1, h - 2, PAPER_L)
    for gy in range(y + 1, y + h - 1):
        if (gy - y) % 8 == 0:
            U.fill(img, x + 1, gy, w - 2, 1, PAPER_LL)
        elif (gy - y) % 4 == 0:
            U.fill(img, x + 1, gy, w - 2, 1, PAPER_L)
    U.outline(img, x + 3, y + 3, w - 6, h - 6, INK_D)
    # title block in the corner, like a technical drawing
    tw = F.width(title) + 8
    U.fill(img, x + 3, y + 3, tw, 12, PAPER)
    U.outline(img, x + 3, y + 3, tw, 12, INK_D)
    F.draw(img, title, x + 7, y + 5, INK, shadow=False)
    nw = F.width(number) + 8
    U.fill(img, x + w - 3 - nw, y + h - 15, nw, 12, PAPER)
    U.outline(img, x + w - 3 - nw, y + h - 15, nw, 12, INK_D)
    F.draw(img, number, x + w + 1 - nw, y + h - 13, INK_D, shadow=False)


def dashed(img, x0, y0, x1, y1, c=INK_D):
    n = max(abs(x1 - x0), abs(y1 - y0))
    for i in range(0, n + 1):
        if (i // 2) % 2 == 0:
            px = x0 + round((x1 - x0) * i / max(1, n))
            py = y0 + round((y1 - y0) * i / max(1, n))
            U.fill(img, px, py, 1, 1, c)


def part_slot(img, x, y, name, filled=True, label=None, label_side='right', ok=True):
    """A component socket on the blueprint: white frame, ghost icon when empty."""
    U.fill(img, x, y, 18, 18, (10, 25, 50, 200))
    U.outline(img, x, y, 18, 18, INK if filled else INK_D)
    ic = icon(name)
    if filled:
        U.icon(img, ic, x + 1, y + 1)
    else:
        a = ic.split()[3]
        ghost = Image.new('RGBA', ic.size, (159, 194, 238, 0))
        ghost.putalpha(a.point(lambda v: 90 if v > 0 else 0))
        img.alpha_composite(ghost, (x + 1, y + 1))
    if label:
        lx = x + 21 if label_side == 'right' else x - 3 - F.width(label)
        F.draw(img, label, lx, y + 5, INK if ok else hx('#ff9b8f'), shadow=False)


def big(img, name, cx, cy, scale=3, ghost=False):
    ic = icon(name).resize((16 * scale, 16 * scale), Image.NEAREST)
    if ghost:
        a = ic.split()[3]
        g = Image.new('RGBA', ic.size, (233, 243, 255, 0))
        g.putalpha(a.point(lambda v: 70 if v > 0 else 0))
        ic = g
    # soft shadow
    sh = Image.new('RGBA', ic.size, (0, 0, 0, 0))
    sh.putalpha(ic.split()[3].point(lambda v: 90 if v > 0 else 0))
    img.alpha_composite(sh.filter(ImageFilter.GaussianBlur(2)), (cx - ic.width // 2 + 2, cy - ic.height // 2 + 3))
    img.alpha_composite(ic, (cx - ic.width // 2, cy - ic.height // 2))


def card(img, x, y, w, ic, name, sub, selected=False, locked=False, new=False):
    h = 24
    U.fill(img, x, y, w, h, (255, 255, 255, 10) if not selected else (255, 154, 82, 45))
    if selected:
        U.outline(img, x, y, w, h, COPPER)
    U.inset(img, x + 3, y + 3, 18, 18, hx('#2a3038'))
    if locked:
        U.icon(img, icon(ic), x + 4, y + 4, dark=True)
        F.draw(img, name, x + 25, y + 3, hx('#8a929a'))
        F.draw(img, sub, x + 25, y + 13, hx('#5c646e'), shadow=False)
        lock = U.sprite('gui/sprites/widget/locked_button')
        img.alpha_composite(lock.crop((3, 3, lock.width - 3, lock.height - 3)), (x + w - 16, y + 7))
        return
    U.icon(img, icon(ic), x + 4, y + 4)
    F.draw(img, name, x + 25, y + 3, WHITE if not selected else hx('#ffd9b8'))
    F.draw(img, sub, x + 25, y + 13, LGREY, shadow=False)
    if new:
        U.fill(img, x + w - 18, y + 4, 14, 9, hx('#2e6b3a'))
        F.draw(img, '!', x + w - 12, y + 5, GREEN, shadow=False)


def group(img, x, y, w, text, count, open_=True):
    U.fill(img, x, y, w, 11, (255, 255, 255, 18))
    F.draw(img, ('▼ ' if open_ else '▶ ') + text, x + 3, y + 2, COPPER, shadow=False)
    F.draw(img, str(count), x + w - 3 - F.width(str(count)), y + 2, LGREY, shadow=False)


def search(img, x, y, w, text):
    U.nine(img, U.sprite('gui/sprites/widget/text_field'), x, y, w, 14, 1)
    s = U.sprite('gui/sprites/icon/search')
    img.alpha_composite(s, (x + 3, y + (14 - s.height) // 2))
    F.draw(img, text, x + 16, y + 3, hx('#707070'), shadow=False)


def material(img, x, y, w, ic, name, have, need):
    U.icon(img, icon(ic), x + 2, y)
    F.draw(img, name, x + 21, y + 4, hx('#dfe6ec'))
    t = f'{have}/{need}'
    F.draw(img, t, x + w - 4 - F.width(t), y + 4, GREEN if have >= need else RED)


def inventory(img, x, y, items=()):
    F.draw(img, 'Инвентарь', x, y, GREY, shadow=False)
    for r in range(3):
        for c in range(9):
            U.slot(img, x + c * 18, y + 10 + r * 18)
    for c in range(9):
        U.slot(img, x + c * 18, y + 68)
    for (c, r, name, n) in items:
        yy = y + 10 + r * 18 if r < 3 else y + 68
        U.icon(img, icon(name), x + c * 18 + 1, yy + 1)
        if n > 1:
            F.draw(img, str(n), x + c * 18 + 17 - F.width(str(n)), yy + 9, WHITE)


def progress(img, x, y, w, p, color=COPPER):
    U.inset(img, x, y, w, 6, hx('#1d1f24'))
    U.fill(img, x + 1, y + 1, int((w - 2) * p), 4, color)


def finish(img, name):
    os.makedirs(OUT, exist_ok=True)
    img.resize((SW * 3, SH * 3), Image.NEAREST).save(os.path.join(OUT, name + '.png'))


INV = [(0, 0, 'iron', 9), (1, 0, 'copper', 4), (2, 0, 'redstone', 1), (3, 0, 'string', 6), (4, 0, 'slime', 3),
       (0, 3, 'web_shooters', 1), (1, 3, 'web_cartridge', 2)]


# ---------------------------------------------------------------- 1: blueprints
def screen_blueprints():
    img, GX, GY = frame('Чертежи')
    LX = GX + 8
    search(img, LX, GY + 20, 124, 'Поиск...')
    U.monitor(img, LX, GY + 37, 124, 213)
    ly = GY + 40
    group(img, LX + 2, ly, 112, 'Человек-Паук', 4); ly += 13
    card(img, LX + 2, ly, 112, 'web_shooters', 'Веб-шутеры', 'Гаджет', selected=True); ly += 26
    card(img, LX + 2, ly, 112, 'empty_web_cartridge', 'Пуст. картридж', 'Расходник'); ly += 26
    card(img, LX + 2, ly, 112, 'web_cartridge', 'Картридж', 'Расходник'); ly += 26
    card(img, LX + 2, ly, 112, 'pearl', 'Маячок', 'Нужно изучить', locked=True); ly += 26
    group(img, LX + 2, ly, 112, 'Детали', 4); ly += 13
    card(img, LX + 2, ly, 112, 'mechanism', 'Механизм', 'Деталь'); ly += 26
    card(img, LX + 2, ly, 112, 'spring', 'Пружина', 'Деталь'); ly += 26
    card(img, LX + 2, ly, 112, 'nozzle', 'Сопло', 'Деталь', new=True); ly += 26
    U.nine(img, U.sprite('gui/sprites/widget/scroller_background'), LX + 117, GY + 39, 6, 209, 1)
    U.nine(img, U.sprite('gui/sprites/widget/scroller'), LX + 117, GY + 39, 6, 60, 1)

    # the blueprint: exploded view of the gadget, sockets for its parts
    BX, BY, BW, BH = GX + 138, GY + 20, 150, 150
    blueprint(img, BX, BY, BW, BH, 'ЧЕРТЁЖ: ВЕБ-ШУТЕРЫ', '#01')
    cx, cy = BX + BW // 2, BY + 80
    big(img, 'web_shooters', cx, cy, 3)
    # dimension line under the gadget
    U.fill(img, cx - 24, cy + 32, 48, 1, INK_D)
    U.fill(img, cx - 24, cy + 29, 1, 7, INK_D); U.fill(img, cx + 23, cy + 29, 1, 7, INK_D)
    F.center(img, '12 см', cx, cy + 35, INK_D, shadow=False)
    sockets = [((BX + 8, BY + 24), 'mechanism', 'Механизм', True), ((BX + BW - 26, BY + 24), 'nozzle', 'Сопло', False),
               ((BX + 8, BY + 104), 'spring', 'Пружина', True), ((BX + BW - 26, BY + 104), 'iron', 'Корпус ×2', True)]
    for (sx, sy), name, label, filled in sockets:
        dashed(img, sx + 9, sy + 9, cx + (-12 if sx < cx else 12), cy + (-10 if sy < cy else 10))
        part_slot(img, sx, sy, name, filled)
        side = 'right' if sx < cx else 'left'
        lx = sx + 9 - F.width(label) // 2
        F.draw(img, label, max(BX + 5, min(lx, BX + BW - 5 - F.width(label))), sy + (20 if sy > cy else -10),
               INK if filled else hx('#ff9b8f'), shadow=False)

    # right: materials, stats, assemble
    RX, RW = GX + 294, 138
    F.draw(img, 'Веб-шутеры', RX, GY + 21, GREY, shadow=False)
    F.draw(img, 'Гаджет · Человек-Паук', RX, GY + 31, hx('#707070'), shadow=False)
    U.monitor(img, RX, GY + 43, RW, 82)
    material(img, RX + 2, GY + 46, RW - 4, 'mechanism', 'Механизм', 1, 1)
    material(img, RX + 2, GY + 65, RW - 4, 'nozzle', 'Сопло', 0, 1)
    material(img, RX + 2, GY + 84, RW - 4, 'spring', 'Пружина', 1, 1)
    material(img, RX + 2, GY + 103, RW - 4, 'iron', 'Железо', 9, 2)
    F.draw(img, 'Картриджей: 2', RX, GY + 129, GREY, shadow=False)
    F.draw(img, 'Выстрелов: 128', RX, GY + 140, GREY, shadow=False)
    U.button(img, F, RX, GY + 154, 60, 16, 'СОБРАТЬ', 'disabled')
    progress(img, RX + 64, GY + 159, 46, 0.0)
    U.slot(img, RX + 116, GY + 153)
    U.icon(img, icon('web_shooters'), RX + 117, GY + 154, dark=True)

    inventory(img, GX + 216, GY + 172, INV)
    # quick-craft hint for the missing part
    U.monitor(img, GX + 138, GY + 174, 72, 76)
    F.draw(img, 'Сборка 3/4', GX + 142, GY + 178, COPPER, shadow=False)
    progress(img, GX + 142, GY + 189, 64, 0.75)
    F.draw(img, 'Не хватает:', GX + 142, GY + 198, hx('#ff9b8f'), shadow=False)
    U.inset(img, GX + 142, GY + 209, 18, 18, hx('#2a3038'))
    U.icon(img, icon('nozzle'), GX + 143, GY + 210)
    F.draw(img, 'Сопло', GX + 164, GY + 214, WHITE)
    U.button(img, F, GX + 142, GY + 232, 64, 14, 'К чертежу', 'normal')
    finish(img, 'tech_table_1_blueprints')


# ---------------------------------------------------------------- 2: modules
def screen_modules():
    img, GX, GY = frame('Модули')
    LX = GX + 8
    F.draw(img, 'Модули веб-шутеров', LX, GY + 22, GREY, shadow=False)
    U.monitor(img, LX, GY + 33, 124, 217)
    ly = GY + 36
    group(img, LX + 2, ly, 112, 'Установлено', 2); ly += 13
    card(img, LX + 2, ly, 112, 'mod_spring', 'Тугая пружина', 'Дальность +25%'); ly += 26
    card(img, LX + 2, ly, 112, 'mod_feed', 'Быстрая подача', 'Перезарядка −30%'); ly += 26
    group(img, LX + 2, ly, 112, 'Доступно', 2); ly += 13
    card(img, LX + 2, ly, 112, 'mod_drum', 'Второй барабан', '+1 картридж', selected=True); ly += 26
    card(img, LX + 2, ly, 112, 'mod_electro', 'Электро-сеть', 'Нужно изучить', locked=True); ly += 26

    BX, BY, BW, BH = GX + 138, GY + 20, 150, 150
    blueprint(img, BX, BY, BW, BH, 'МОДЕРНИЗАЦИЯ', '#01-М')
    cx, cy = BX + BW // 2, BY + 78
    # the gadget in the middle (a real slot), module sockets round it
    U.fill(img, cx - 14, cy - 14, 28, 28, (10, 25, 50, 220))
    U.outline(img, cx - 14, cy - 14, 28, 28, INK)
    U.icon(img, icon('web_shooters').resize((24, 24), Image.NEAREST), cx - 12, cy - 12)
    ring = [(-50, -40, 'mod_spring', True), (50, -40, 'mod_feed', True), (-50, 40, 'mod_drum', False), (50, 40, None, False)]
    for dx, dy, name, filled in ring:
        sx, sy = cx + dx - 9, cy + dy - 9
        dashed(img, cx + (14 if dx > 0 else -14), cy + (8 if dy > 0 else -8), sx + 9, sy + 9, INK if filled else INK_D)
        if name:
            part_slot(img, sx, sy, name, filled)
            if not filled:
                U.outline(img, sx - 1, sy - 1, 20, 20, COPPER)
        else:
            U.fill(img, sx, sy, 18, 18, (10, 25, 50, 160))
            U.outline(img, sx, sy, 18, 18, INK_D)
            F.center(img, '+', sx + 9, sy + 5, INK_D, shadow=False)
    F.draw(img, 'Слоты: 3 из 4', BX + 7, BY + BH - 13, INK_D, shadow=False)

    RX, RW = GX + 294, 138
    F.draw(img, 'Второй барабан', RX, GY + 21, GREY, shadow=False)
    F.draw(img, 'Модуль · веб-шутеры', RX, GY + 31, hx('#707070'), shadow=False)
    U.monitor(img, RX, GY + 43, RW, 74)
    rows = [('Картриджей', '2', '3', True), ('Дальность', '60', '60', None), ('Перезарядка', '28', '28', None),
            ('Вес', '1.0', '1.2', False)]
    for i, (name, a, b, better) in enumerate(rows):
        yy = GY + 47 + i * 17
        F.draw(img, name, RX + 4, yy, hx('#dfe6ec'))
        t = f'{a} → {b}' if a != b else a
        col = GREEN if better else (RED if better is False else LGREY)
        F.draw(img, t, RX + RW - 4 - F.width(t), yy, col)
    F.draw(img, 'Материалы:', RX, GY + 122, GREY, shadow=False)
    for i, (n, c) in enumerate((('iron', 3), ('copper', 2), ('redstone', 2))):
        U.slot(img, RX + i * 20, GY + 133)
        U.icon(img, icon(n), RX + i * 20 + 1, GY + 134)
        F.draw(img, str(c), RX + i * 20 + 17 - F.width(str(c)), GY + 142, WHITE)
    U.button(img, F, RX, GY + 156, 80, 16, 'УСТАНОВИТЬ', 'hover', hx('#ffd9b8'))
    U.button(img, F, RX + 84, GY + 156, 54, 16, 'Снять', 'normal')
    inventory(img, GX + 216, GY + 172, INV)
    U.monitor(img, GX + 138, GY + 182, 72, 68)
    F.draw(img, 'Гаджет', GX + 142, GY + 186, LGREY, shadow=False)
    U.slot(img, GX + 165, GY + 200)
    U.icon(img, icon('web_shooters'), GX + 166, GY + 201)
    F.center(img, 'Положи', GX + 174, GY + 223, LGREY, shadow=False)
    F.center(img, 'гаджет', GX + 174, GY + 234, LGREY, shadow=False)
    finish(img, 'tech_table_2_modules')


# ---------------------------------------------------------------- 3: research
def screen_research():
    img, GX, GY = frame('Изучение')
    LX = GX + 8
    F.draw(img, 'Изучение находки', LX, GY + 22, GREY, shadow=False)
    U.monitor(img, LX, GY + 33, 124, 217)
    U.slot(img, LX + 53, GY + 44)
    U.icon(img, icon('pearl'), LX + 54, GY + 45)
    F.center(img, 'Странный маячок', LX + 62, GY + 66, WHITE)
    F.center(img, 'Найден в данже', LX + 62, GY + 77, LGREY, shadow=False)
    U.fill(img, LX + 4, GY + 90, 116, 1, (255, 154, 82, 90))
    F.draw(img, 'Изучение 64%', LX + 6, GY + 96, COPPER)
    progress(img, LX + 6, GY + 108, 112, 0.64)
    F.draw(img, 'Находка не тратится', LX + 6, GY + 120, LGREY, shadow=False)
    F.draw(img, 'или за опыт:', LX + 6, GY + 136, LGREY, shadow=False)
    U.button(img, F, LX + 6, GY + 149, 112, 16, 'ИЗУЧИТЬ — 8 ур.', 'normal', hx('#b6ff8a'))
    U.fill(img, LX + 4, GY + 174, 116, 1, (255, 154, 82, 90))
    F.draw(img, 'Откроется:', LX + 6, GY + 180, LGREY, shadow=False)
    U.inset(img, LX + 6, GY + 192, 18, 18, hx('#2a3038'))
    U.icon(img, icon('pearl'), LX + 7, GY + 193, dark=True)
    F.draw(img, 'Чертёж маячка', LX + 28, GY + 192, WHITE)
    F.draw(img, '+ Плата', LX + 28, GY + 202, LGREY, shadow=False)
    F.draw(img, 'Доп. детали: 2 из 4', LX + 6, GY + 220, hx('#707070'), shadow=False)

    BX, BY, BW, BH = GX + 138, GY + 20, 150, 150
    blueprint(img, BX, BY, BW, BH, 'РАСШИФРОВКА…', '#07')
    cx, cy = BX + BW // 2, BY + 78
    # exploded view: parts drift out of the found gadget, the unknown ones are still ghosts
    big(img, 'pearl', cx, cy, 2, ghost=False)
    spots = [(-48, -42, 'board', True), (46, -40, 'glowstone', True), (-50, 36, 'mechanism', False), (48, 38, 'membrane', False)]
    for dx, dy, name, known in spots:
        sx, sy = cx + dx - 9, cy + dy - 9
        dashed(img, cx + (10 if dx > 0 else -10), cy + (8 if dy > 0 else -8), sx + 9, sy + 9)
        part_slot(img, sx, sy, name, filled=known)
        if not known:
            F.center(img, '?', sx + 9, sy + 5, INK, shadow=False)
    # scan beam sweeping across the paper
    beam = Image.new('RGBA', img.size, (0, 0, 0, 0))
    yy = BY + 58
    for k in range(10):
        U.fill(beam, BX + 2, yy + k, BW - 4, 1, (255, 154, 82, 120 - k * 12))
    img.alpha_composite(beam)
    U.fill(img, BX + 2, yy, BW - 4, 1, COPPER)

    RX, RW = GX + 294, 138
    F.draw(img, 'Открытые чертежи', RX, GY + 21, GREY, shadow=False)
    U.monitor(img, RX, GY + 32, RW, 138)
    rows = [('web_shooters', 'Веб-шутеры', True), ('web_cartridge', 'Картридж', True), ('mechanism', 'Механизм', True),
            ('pearl', 'Паучий маячок', False), ('mod_electro', 'Электро-паутина', False)]
    for i, (n, name, ok) in enumerate(rows):
        yy = GY + 35 + i * 26
        U.inset(img, RX + 3, yy, 18, 18, hx('#2a3038'))
        U.icon(img, icon(n), RX + 4, yy + 1, dark=not ok)
        F.draw(img, name if ok else '???', RX + 25, yy + 1, WHITE if ok else hx('#8a929a'))
        F.draw(img, 'Открыт' if ok else 'Найди и изучи', RX + 25, yy + 10, GREEN if ok else hx('#5c646e'), shadow=False)
    inventory(img, GX + 216, GY + 172, INV + [(5, 0, 'pearl', 1)])
    U.monitor(img, GX + 138, GY + 182, 72, 68)
    F.draw(img, 'Подсказка', GX + 142, GY + 186, COPPER_D, shadow=False)
    for i, line in enumerate(('Неизвестное', 'можно', 'изучить', 'здесь')):
        F.draw(img, line, GX + 142, GY + 198 + i * 11, LGREY, shadow=False)
    finish(img, 'tech_table_3_research')


screen_blueprints()
screen_modules()
screen_research()
