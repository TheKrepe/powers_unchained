"""Irradiator screen: 4 concepts for the right part (capsule / sample / result) — NOT implemented yet.

python3 irradiator_screens.py <out dir>  -> irradiator_A..D.png (x3)
"""
import math
import os
import sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, '..', '..', 'suit_creator_gui', 'src'))
import mc_ui as U  # noqa: E402

F = U.Font()
hx = U.hx
OUT = sys.argv[1] if len(sys.argv) > 1 else '.'
W, H = 260, 236
SW, SH = 300, 262
GX, GY = 20, 13
GREEN, GREEN_D, GREEN_DK = hx('#58f05a'), hx('#2c8a3a'), hx('#16391e')
GLASS = hx('#0a140d')
AMBER, RED, GREY, LABEL, WHITE = hx('#ffb13b'), hx('#ff5a4a'), hx('#9aa3ad'), hx('#404040'), hx('#ffffff')
YEL, BLK = hx('#e8c21a'), hx('#1a1a1a')
CAPSULE = U.mod_tex('item/isotope_capsule')
RAD_EYE = U.mod_tex('item/radioactive_spider_eye')
EYE = U.sprite('item/spider_eye')
CART = U.mod_tex('item/empty_web_cartridge')


def fill(img, x, y, w, h, c):
    U.fill(img, GX + x, GY + y, w, h, c)


def px(img, x, y, c):
    U.fill(img, GX + x, GY + y, 1, 1, c)


def text(img, s, x, y, c, shadow=False):
    F.draw(img, s, GX + x, GY + y, c, shadow)


def ctext(img, s, cx, y, c, shadow=False):
    F.center(img, s, GX + cx, GY + y, c, shadow)


def monitor(img, x, y, w, h):
    U.inset(img, GX + x, GY + y, w, h, GLASS)
    for yy in range(y + 2, y + h - 1, 2):
        fill(img, x + 1, yy, w - 2, 1, (60, 90, 110, 22))
    fill(img, x + 1, y + 1, w - 2, 1, (88, 240, 90, 64))
    fill(img, x + 1, y + h - 2, w - 2, 1, GREEN_DK)


def slot(img, x, y):
    U.slot(img, GX + x - 1, GY + y - 1)


def item(img, im, x, y, size=16):
    U.icon(img, im, GX + x, GY + y, size)


def hazard(img, x, y, w, h):
    fill(img, x, y, w, h, BLK)
    for i in range(-h, w, 6):
        for r in range(h):
            for k in range(3):
                xx = i + r + k
                if 0 <= xx < w:
                    px(img, x + xx, y + h - 1 - r, YEL)


def ellipse(img, cx, cy, rx, ry, c, n=64):
    for i in range(n):
        a = 2 * math.pi * i / n
        px(img, cx + round(math.cos(a) * rx), cy + round(math.sin(a) * ry), c)


def spider(img, cx, cy, s=2):
    """A small cave spider seen from the front (pixel art, s = pixel size)."""
    body = hx('#123a3a'); dark = hx('#0b2424'); eye = hx('#d0101a')
    def p(x, y, c, w=1, h=1):
        fill(img, cx + x * s, cy + y * s, w * s, h * s, c)
    p(-5, -3, body, 10, 6); p(-4, -2, dark, 8, 4)
    p(-3, -1, eye); p(2, -1, eye); p(-1, -2, eye); p(0, -2, eye); p(-2, 0, eye); p(1, 0, eye)
    for side in (-1, 1):
        for j, (dx, dy) in enumerate(((7, -4), (8, -1), (8, 2), (7, 4))):
            for t in range(1, 5):
                xx = side * (5 + round(t * (dx - 5) / 4)) if side > 0 else -(5 + round(t * (dx - 5) / 4)) - 1
                yy = round(-1 + t * (dy + 1) / 4)
                p(xx, yy, body)


def base(img, title='Облучатель'):
    U.panel(img, GX, GY, W, H)
    text(img, title, 8, 7, LABEL)
    hazard(img, 124, 7, W - 132, 6)
    for r in range(3):
        for c in range(9):
            slot(img, 49 + c * 18, 152 + r * 18)
    for c in range(9):
        slot(img, 49 + c * 18, 210)


def chamber(img, x, y, w, h, content='spider', sample_slot=False):
    monitor(img, x, y, w, h)
    mid = x + w // 2
    top, bottom = y + 10, y + h - 18
    for yy in range(top, bottom):
        a = int(90 * (1 - (yy - top) / (bottom - top)) + 30)
        fill(img, mid - 30, yy, 60, 1, (88, 240, 90, a // 2))
    fill(img, mid - 34, top, 2, bottom - top, (88, 240, 90, 150))
    fill(img, mid + 32, top, 2, bottom - top, (88, 240, 90, 150))
    fill(img, mid - 30, top + 3, 1, bottom - top - 6, (255, 255, 255, 80))
    ellipse(img, mid, top, 34, 4, (88, 240, 90, 180))
    ellipse(img, mid, bottom, 34, 4, (88, 240, 90, 180))
    fill(img, mid - 36, bottom + 3, 73, 4, hx('#3c4148'))
    fill(img, mid - 36, bottom + 3, 73, 1, hx('#5a616a'))
    if content == 'spider':
        spider(img, mid - 1, (top + bottom) // 2, 3)
    elif content == 'item':
        item(img, EYE, mid - 16, (top + bottom) // 2 - 16, 32)
    if sample_slot:
        slot(img, mid - 8, bottom - 22)
        item(img, EYE, mid - 8, bottom - 22)
        fill(img, mid - 12, bottom - 4, 24, 2, hx('#5a616a'))
    ctext(img, 'Облучение…', mid, y + h - 11, GREEN)


def gauge(img, x, y, w, h, dose=0.37):
    monitor(img, x, y, w, h)
    ox, oy, r = x + w // 2, y + 30, 18
    for i in range(37):
        t = i / 36
        a = math.pi * (1 - t)
        c = GREEN if t < 0.6 else AMBER if t < 0.85 else RED
        px(img, ox + round(math.cos(a) * r), oy - round(math.sin(a) * r), c)
        if i % 6 == 0:
            px(img, ox + round(math.cos(a) * (r - 3)), oy - round(math.sin(a) * (r - 3)), c)
    na = math.pi * (1 - dose)
    for s in range(r - 2):
        px(img, ox + round(math.cos(na) * s), oy - round(math.sin(na) * s), WHITE)
    ctext(img, f'{round(dose * 100)}%', ox, oy + 5, GREEN)
    bx, by, bh = ox - 5, y + 48, h - 56
    U.inset(img, GX + bx, GY + by, 10, bh, hx('#06100a'))
    f = round((bh - 2) * dose)
    for yy in range(f):
        t = yy / max(1, f)
        fill(img, bx + 1, by + bh - 1 - f + yy, 8, 1, (int(184 - 140 * t), int(255 - 117 * t), int(122 - 64 * t), 255))


def button(img, x, y, w, h, s, state='normal'):
    U.button(img, F, GX + x, GY + y, w, h, s, state, hx('#b8ff7a'))


def save(img, name):
    img.resize((img.width * 3, img.height * 3), Image.NEAREST).save(os.path.join(OUT, name))


def new():
    return Image.new('RGBA', (SW, SH), hx('#202428'))


# ---------------------------------------------------------------- A: fuel rod + petri dishes
def variant_a():
    img = new(); base(img)
    chamber(img, 8, 20, 110, 110); gauge(img, 124, 20, 48, 110)
    x, y = 178, 20
    monitor(img, x, y, 74, 110)
    text(img, 'Стержень', x + 4, y + 4, GREEN_D)
    # tall flask with the capsule socket in its neck, isotope level going down
    fx, fy, fw, fh = x + 8, y + 32, 16, 66
    U.outline(img, GX + fx - 1, GY + fy - 1, fw + 2, fh + 2, (88, 240, 90, 160))
    lvl = int(fh * 0.62)
    for yy in range(lvl):
        a = 120 + int(80 * math.sin(yy * 0.5) ** 2)
        fill(img, fx, fy + fh - lvl + yy, fw, 1, (88, 240, 90, a))
    for b in range(5):
        px(img, fx + 4 + (b * 5) % 9, fy + fh - 6 - b * 9, hx('#c8ffb0'))
    slot(img, fx, y + 14); item(img, CAPSULE, fx, y + 14)
    # pipe to the chamber (along the top)
    for xx in range(118, fx):
        if xx % 6 < 4:
            px(img, xx, y + 22, GREEN)
    fill(img, fx + fw // 2, y + 22, 1, 10, GREEN_D)
    # petri dishes: sample -> result
    for (sy, im, lab) in ((y + 30, EYE, 'Образец'), (y + 78, RAD_EYE, 'Итог')):
        ellipse(img, x + 52, sy + 18, 13, 3, hx('#9fd8b0'))
        ellipse(img, x + 52, sy + 18, 11, 2, hx('#4e7a5a'))
        slot(img, x + 44, sy); item(img, im, x + 44, sy)
        ctext(img, lab, x + 52, sy - 10, GREEN_D)
    ctext(img, '☢', x + 52, y + 58, AMBER)
    fill(img, x + 52, y + 52, 1, 5, AMBER); fill(img, x + 52, y + 66, 1, 5, AMBER)
    button(img, 8, 134, 110, 15, 'ОБЛУЧЕНИЕ…', 'disabled')
    save(img, 'irradiator_A_fuel_rod.png')


# ---------------------------------------------------------------- B: control console
def variant_b():
    img = new(); base(img)
    chamber(img, 8, 20, 110, 110); gauge(img, 124, 20, 48, 110)
    x, y = 178, 20
    U.inset(img, GX + x, GY + y, 74, 110, hx('#3a3f46'))
    fill(img, x + 1, y + 1, 72, 1, hx('#5a616a'))
    # rivets
    for (rx, ry) in ((x + 3, y + 3), (x + 69, y + 3), (x + 3, y + 105), (x + 69, y + 105)):
        px(img, rx, ry, hx('#22262b'))
    # capsule = key cartridge with 3 LEDs
    text(img, 'КАРТРИДЖ', x + 6, y + 6, hx('#c9d1d9'))
    fill(img, x + 5, y + 16, 26, 22, hx('#22262b'))
    slot(img, x + 10, y + 19); item(img, CAPSULE, x + 10, y + 19)
    for i, c in enumerate((hx('#5a1d1d'), hx('#6a5a1d'), GREEN)):
        fill(img, x + 36 + i * 9, y + 23, 6, 6, hx('#15181c'))
        fill(img, x + 37 + i * 9, y + 24, 4, 4, c)
    # big red lever / rotary switch
    cx, cy = x + 52, y + 56
    for r in range(11, 0, -1):
        ellipse(img, cx, cy, r, r, hx('#1d2024') if r > 9 else hx('#8a1010') if r > 4 else hx('#d02020'))
    for t in range(12):
        px(img, cx + round(t * 0.55), cy - round(t * 0.85), hx('#f0f0f0'))
    ctext(img, 'ПУСК', cx, y + 70, hx('#ff8a7a'))
    # glove-box ports: sample / result
    for (gx, im, lab) in ((x + 6, EYE, 'Образец'), (x + 6, RAD_EYE, 'Результат')):
        pass
    for i, (im, lab) in enumerate(((EYE, 'Обр.'), (RAD_EYE, 'Рез.'))):
        px0 = x + 8 + i * 0
        py0 = y + 42 + i * 36
        ellipse(img, x + 16, py0 + 8 + 0, 12, 12, hx('#15181c'))
        ellipse(img, x + 16, py0 + 8, 11, 11, hx('#4a5058'))
        slot(img, x + 8, py0); item(img, im, x + 8, py0)
    text(img, 'Образец', x + 4, y + 59, hx('#c9d1d9'))
    text(img, 'Итог', x + 30, y + 98, hx('#c9d1d9'))
    # no bottom button: the lever starts it
    text(img, 'Повернуть рубильник — облучить', 8, 137, LABEL)
    save(img, 'irradiator_B_console.png')


# ---------------------------------------------------------------- C: everything inside the machine + geiger display
def variant_c():
    img = new(); base(img)
    x, y, w, h = 8, 20, 120, 128
    monitor(img, x, y, w, h)
    mid = x + w // 2
    top, bottom = y + 8, y + h - 34
    for yy in range(top, bottom):
        a = int(90 * (1 - (yy - top) / (bottom - top)) + 30)
        fill(img, mid - 30, yy, 60, 1, (88, 240, 90, a // 2))
    fill(img, mid - 34, top, 2, bottom - top, (88, 240, 90, 150))
    fill(img, mid + 32, top, 2, bottom - top, (88, 240, 90, 150))
    ellipse(img, mid, top, 34, 4, (88, 240, 90, 180)); ellipse(img, mid, bottom, 34, 4, (88, 240, 90, 180))
    spider(img, mid - 1, (top + bottom) // 2 - 6, 3)
    # sample slot on a pedestal INSIDE the cylinder
    slot(img, mid - 8, bottom - 20); item(img, EYE, mid - 8, bottom - 20)
    # machine base: capsule inserted from below, result tray on the side
    fill(img, mid - 40, bottom + 4, 81, 22, hx('#3c4148')); fill(img, mid - 40, bottom + 4, 81, 1, hx('#5a616a'))
    hazard(img, mid - 40, bottom + 23, 81, 3)
    slot(img, mid - 30, bottom + 6); item(img, CAPSULE, mid - 30, bottom + 6)
    slot(img, mid + 14, bottom + 6); item(img, RAD_EYE, mid + 14, bottom + 6)
    text(img, '→', mid - 6, bottom + 10, GREEN)
    # gauge + geiger display
    gauge(img, 134, 20, 40, 76, 0.62)
    gx, gy, gw, gh = 180, 20, 72, 128
    monitor(img, gx, gy, gw, gh)
    text(img, 'ГЕЙГЕР', gx + 4, gy + 4, GREEN_D)
    # waveform
    for xx in range(gw - 8):
        v = math.sin(xx * 0.7) * 3 + (7 if xx % 11 == 3 else 0) + (5 if xx % 17 == 9 else 0)
        px(img, gx + 4 + xx, gy + 28 - round(v), GREEN)
    fill(img, gx + 4, gy + 29, gw - 8, 1, GREEN_DK)
    # click dots
    for i in range(12):
        fill(img, gx + 5 + i * 5, gy + 36, 3, 3, GREEN if i in (1, 2, 5, 9, 10) else GREEN_DK)
    log = ['> Доза 62%', '> Образец', '  стабилен', '> Мутация…', '> Внимание:', '  высокая', '  доза']
    for i, s in enumerate(log):
        text(img, s, gx + 4, gy + 46 + i * 10, AMBER if 'Внимание' in s or 'высокая' in s or s.strip() == 'доза' else GREEN)
    button(img, 134, 100, 40, 15, 'ПУСК', 'disabled')
    save(img, 'irradiator_C_inside_geiger.png')


# ---------------------------------------------------------------- D: airlock
def variant_d():
    img = new(); base(img)
    chamber(img, 8, 20, 110, 110); gauge(img, 124, 20, 48, 110)
    x, y = 178, 20
    U.inset(img, GX + x, GY + y, 74, 110, hx('#2a2e34'))
    labels = [('ЗАГРУЗКА', CAPSULE), ('КАМЕРА', EYE), ('ВЫГРУЗКА', RAD_EYE)]
    for i, (lab, im) in enumerate(labels):
        cy = y + 3 + i * 36
        U.inset(img, GX + x + 4, GY + cy, 66, 30, GLASS)
        text(img, lab, x + 8, cy + 3, GREEN_D)
        slot(img, x + 29, cy + 12); item(img, im, x + 29, cy + 12)
        if i < 2:
            # shutters between compartments: closed (hazard) while it works
            hazard(img, x + 4, cy + 30, 66, 6)
    # the lower shutter half open = result ready
    fill(img, x + 4, y + 3 + 2 * 36 - 6, 20, 6, hx('#2a2e34'))
    fill(img, x + 50, y + 3 + 2 * 36 - 6, 20, 6, hx('#2a2e34'))
    for k in range(3):
        px(img, x + 37, y + 3 + 2 * 36 + 13 + k, GREEN)
    button(img, 8, 134, 110, 15, 'ОБЛУЧЕНИЕ…', 'disabled')
    save(img, 'irradiator_D_airlock.png')


if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    variant_a(); variant_b(); variant_c(); variant_d()
    print('ok')
