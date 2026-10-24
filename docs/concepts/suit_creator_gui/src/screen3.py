from screens import *

img, GX, GY = frame('Сканер')
catalog(img, GX, GY, chips=('Все',), selected='', only=('Классический', '1-е появление'), locked_rows=2, open_locked=True)

# centre: scanner
SX, SY, SWd, SHd = GX + 138, GY + 20, 150, 148
U.monitor(img, SX, SY, SWd, SHd)
U.brackets(img, SX + 3, SY + 3, SWd - 6, SHd - 6, AMBER, 6)
F.draw(img, 'СКАНЕР', SX + 7, SY + 6, AMBER_D, shadow=False)
# big item on a scan plate
plate = SX + SWd // 2
U.inset(img, plate - 26, SY + 24, 52, 52, hx('#1b2026'))
big = item('spider_man_raimi_mask').resize((48, 48), Image.NEAREST)
img.alpha_composite(big, (plate - 24, SY + 26))
# laser line + glow
for dy, a in ((0, 230), (-1, 90), (1, 90), (-2, 40), (2, 40)):
    U.fill(img, plate - 30, SY + 52 + dy, 60, 1, (255, 177, 59, a))
U.fill(img, plate - 26, SY + 26, 52, 26, (255, 177, 59, 28))
F.center(img, 'Анализ ткани...', plate, SY + 84, hx('#dfe6ec'))
U.inset(img, SX + 10, SY + 98, SWd - 20, 8, hx('#1d1f24'))
U.fill(img, SX + 11, SY + 99, int((SWd - 22) * 0.64), 6, AMBER)
F.center(img, '64%', plate, SY + 110, AMBER)
F.draw(img, 'Фрагменты', SX + 8, SY + 126, LGREY, shadow=False)
for i in range(3):
    U.slot(img, SX + 72 + i * 20, SY + 122)
    if i < 2:
        U.icon(img, item('red_fabric'), SX + 73 + i * 20, SY + 123)
U.fill(img, SX + 112, SY + 123, 16, 16, (0, 0, 0, 60))

# right: what is being decoded
RX, RW = GX + 294, 138
hologram(img, RX, GY + 20, RW, 148, 'spider_man_raimi', 200, label='???', variant='Расшифровка', size=110, ghost=True)

# toast (top right of the screen, like an advancement)
TX, TY = SW - 168, 4
U.fill(img, TX, TY, 164, 32, hx('#202329'))
U.outline(img, TX, TY, 164, 32, hx('#5a6168'))
U.outline(img, TX + 1, TY + 1, 162, 30, hx('#000000'))
img.alpha_composite(item('spider_man_first_appearance_mask'), (TX + 8, TY + 8))
F.draw(img, 'Новый костюм!', TX + 30, TY + 7, AMBER)
F.draw(img, '1-е появление', TX + 30, TY + 18, WHITE)

F.draw(img, 'Скан', GX + 138, GY + 172, GREY, shadow=False)
U.monitor(img, GX + 138, GY + 182, 72, 66)
U.slot(img, GX + 165, GY + 192)
U.icon(img, item('spider_man_raimi_mask'), GX + 166, GY + 193)
F.center(img, 'Вставлено:', GX + 174, GY + 222, LGREY)
F.center(img, 'находка', GX + 174, GY + 233, AMBER)
inventory(img, GX + 216, GY + 172)
finish(img, 'suit_creator_3_scanner')
