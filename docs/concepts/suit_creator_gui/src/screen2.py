from screens import *

img, GX, GY = frame('Досье')
catalog(img, GX, GY, query='тех', chips=('Тех',), selected='Продвинутый', only=('Продвинутый',), locked_rows=0)
# no other matches hint
F.draw(img, 'Найдено: 1', GX + 12, GY + 112, LGREY, shadow=False)

HX, HY, HW, HH = GX + 138, GY + 20, 150, 226
hologram(img, HX, HY, HW, HH, 'spider_man_advanced_2', 160, variant='Вариант 1 / 1', size=150)
by = HY + HH - 36
# pose buttons over the hologram
for i, n in enumerate(('Стоя', 'Бег', 'Вис')):
    U.button(img, F, HX + 6 + i * 46, HY + 18, 44, 14, n, 'hover' if i == 2 else 'normal', hx('#ffe2a8') if i == 2 else WHITE)

RX, RW = GX + 294, 138
U.monitor(img, RX, GY + 20, RW, 226)
y = GY + 25
F.draw(img, 'Продвинутый', RX + 5, y, hx('#ffe2a8'))
F.draw(img, 'костюм 2.0', RX + 5, y + 10, hx('#ffe2a8'))
y += 24
cx = RX + 5
for t in ('Игры', 'Тех'):
    cx += U.chip(img, F, cx, y, t, TAG[t]) + 3
U.chip(img, F, cx, y, '★ Ур. 3', hx('#a8661a'))
y += 16
for line in ('Наноткань и тёмная', 'полимерная броня.', 'Лёгкий, но прочный —', 'для боя в городе.'):
    F.draw(img, line, RX + 5, y, hx('#c9d3dc'), shadow=False)
    y += 10
y += 4
U.fill(img, RX + 4, y, RW - 8, 1, (89, 228, 255, 60))
y += 4


def stat(name, v, c):
    global y
    F.draw(img, name, RX + 5, y, LGREY, shadow=False)
    for i in range(8):
        U.fill(img, RX + 66 + i * 8, y + 1, 6, 6, c if i < v else (255, 255, 255, 30))
    y += 11


stat('Защита', 7, hx('#d9dde2'))
stat('Ловкость', 8, hx('#7cff6b'))
stat('Скрытн.', 4, hx('#8a5bd6'))
stat('Прочн.', 6, hx('#ffb13b'))
y += 3
U.fill(img, RX + 4, y, RW - 8, 1, (89, 228, 255, 60))
y += 5
F.draw(img, 'Способности', RX + 5, y, CYAN)
y += 12
for key, name in (('C', 'Выстрел паутиной'), ('V', 'Рывок на паутине'), ('B', 'Полёт на паутине')):
    U.fill(img, RX + 5, y - 1, 11, 11, (0, 0, 0, 140))
    F.center(img, key, RX + 11, y + 1, WHITE)
    F.draw(img, name, RX + 20, y + 1, hx('#dfe6ec'), shadow=False)
    y += 13
y += 2
F.draw(img, 'Нужны веб-шутеры', RX + 5, y, hx('#6b737b'), shadow=False)
img.alpha_composite(item('web_shooters'), (RX + RW - 21, y - 5))

finish(img, 'suit_creator_2_dossier')
