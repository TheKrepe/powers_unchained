from screens import *

img, GX, GY = frame('Каталог')
catalog(img, GX, GY)

HX, HY, HW, HH = GX + 138, GY + 20, 150, 128
hologram(img, HX, HY, HW, HH, 'spider_man', 206)
by = HY + HH + 3
U.button(img, F, HX, by, 32, 14, 'Стив', 'hover', hx('#ffe2a8'))
U.button(img, F, HX + 33, by, 36, 14, 'Алекс')
U.button(img, F, HX + 72, by, 18, 14, '')
img.alpha_composite(U.sprite('item/compass_00'), (HX + 73, by - 1))
for i, p in enumerate(('mask', 'chestplate', 'pants', 'boots')):
    U.inset(img, HX + 93 + i * 14, by, 14, 14, hx('#2a3038'))
    ic = item('spider_man_' + p).resize((12, 12), Image.NEAREST)
    img.alpha_composite(ic, (HX + 94 + i * 14, by + 1))

RX, RW = GX + 294, 138
F.draw(img, 'Чертёж: маска', RX, GY + 21, GREY, shadow=False)
for i, p in enumerate(('mask', 'chestplate', 'pants', 'boots')):
    sx = RX + i * 22
    U.slot(img, sx, GY + 32)
    U.icon(img, item('spider_man_' + p), sx + 1, GY + 33)
    if i == 0:
        U.outline(img, sx - 1, GY + 31, 20, 20, AMBER)
U.button(img, F, RX + 90, GY + 33, 46, 16, 'Весь')
U.monitor(img, RX, GY + 54, RW, 62)
material_row(img, RX + 2, GY + 57, RW - 4, item('red_fabric'), 'Красная ткань', 12, 6)
material_row(img, RX + 2, GY + 76, RW - 4, item('black_fabric'), 'Чёрная ткань', 3, 2)
material_row(img, RX + 2, GY + 95, RW - 4, U.sprite('item/string'), 'Нить', 0, 2)
F.draw(img, 'Защита', RX, GY + 120, GREY, shadow=False)
armor_icons(img, RX + 40, GY + 120, 3)
F.draw(img, 'Прочн. 18', RX + 72, GY + 120, GREY, shadow=False)
cx = RX
for n, c in (('Чутьё', hx('#7a2b8c')), ('Лазание', hx('#2b6e8c')), ('Ловкость', hx('#8c6a2b'))):
    cx += U.chip(img, F, cx, GY + 132, n, c) + 3
U.button(img, F, RX, GY + 151, 52, 16, 'Сшить', 'disabled')
U.inset(img, RX + 56, GY + 156, 52, 6, hx('#1d1f24'))
U.slot(img, RX + 114, GY + 150)
U.icon(img, item('spider_man_mask'), RX + 115, GY + 151, dark=True)
F.draw(img, 'Не хватает: нить ×2', RX, GY + 171, hx('#b03a2e'), shadow=False)

# scan slot (bottom of the centre column)
F.draw(img, 'Скан', GX + 138, GY + 172, GREY, shadow=False)
U.monitor(img, GX + 138, GY + 182, 72, 66)
U.slot(img, GX + 165, GY + 192)
U.fill(img, GX + 141, GY + 214, 66, 1, (89, 228, 255, 80))
F.center(img, 'Положи', GX + 174, GY + 222, LGREY)
F.center(img, 'находку', GX + 174, GY + 233, LGREY)

inventory(img, GX + 216, GY + 172)
finish(img, 'suit_creator_1_catalog')
