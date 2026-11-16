"""18x18 icons for every ability: the skill tree nodes and the powers menu Keys tab
(textures/gui/sprites/ability/*.png).

Built in the style of the owner's hand-made power icons (red spider + yellow "sense" marks, vanilla effect-icon
shading): the owner's spider_sense / wall_climbing / night_vision pixels and vanilla mob_effect / item textures are
cut, recoloured and combined. The owner's three icons themselves are NOT written (danger_sense / night vision nodes
use them directly).

usage: python3 tools/gen_ability_icons.py <resources dir> <vanilla assets/minecraft/textures dir>
"""
import colorsys
import os
import sys

from PIL import Image

RES = sys.argv[1]
VAN = sys.argv[2]
A = os.path.join(RES, "assets", "powers_unleashed", "textures", "gui", "sprites")
OUT = os.path.join(A, "ability")
os.makedirs(OUT, exist_ok=True)

YELLOWS = {(0xff, 0xf4, 0x89), (0xee, 0xe0, 0x50), (0xd6, 0xb7, 0x47), (0x8f, 0x6a, 0x1a)}
Y_HI, Y_MID, Y_LO, Y_DARK = (0xff, 0xf4, 0x89, 255), (0xee, 0xe0, 0x50, 255), (0xd6, 0xb7, 0x47, 255), (0x8f, 0x6a, 0x1a, 255)


def load(path):
    return Image.open(path).convert("RGBA")


def owner(name):
    return load(os.path.join(A, "power", name + ".png"))


def vanilla(rel):
    im = load(os.path.join(VAN, rel + ".png"))
    return im.crop((0, 0, im.width, im.width))  # first frame of animated strips


def blank():
    return Image.new("RGBA", (18, 18), (0, 0, 0, 0))


def split_sense():
    """The owner's spider_sense: (spider only, yellow marks only)."""
    im = owner("spider_sense")
    spider, marks = blank(), blank()
    for y in range(18):
        for x in range(18):
            p = im.getpixel((x, y))
            if p[3] == 0:
                continue
            (marks if p[:3] in YELLOWS else spider).putpixel((x, y), p)
    return spider, marks


def paste(dst, src, dx=0, dy=0):
    for y in range(src.height):
        for x in range(src.width):
            p = src.getpixel((x, y))
            if p[3] and 0 <= x + dx < 18 and 0 <= y + dy < 18:
                dst.putpixel((x + dx, y + dy), p)
    return dst


def erase(im, pts):
    for x, y in pts:
        im.putpixel((x, y), (0, 0, 0, 0))


def _Y(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def shift(c, hue, sat_mul=1.0, lum_mul=1.0, dark_hue=0.0, sat_cap=1.0):
    """Luminance-preserving hue shift (vanilla-style recolour)."""
    if c[:3] == (255, 255, 255):
        return c
    h, s, v = colorsys.rgb_to_hsv(*[x / 255 for x in c[:3]])
    target = min(255, _Y(c) * lum_mul)
    hh = (hue / 360 + dark_hue / 360 * (1 - v)) % 1
    s = min(sat_cap, s * sat_mul)
    while True:
        lo, hi = 0.0, 1.0
        for _ in range(30):
            m = (lo + hi) / 2
            if _Y([x * 255 for x in colorsys.hsv_to_rgb(hh, s, m)]) < target:
                lo = m
            else:
                hi = m
        cc = tuple(round(x * 255) for x in colorsys.hsv_to_rgb(hh, s, (lo + hi) / 2))
        if _Y(cc) >= target - 1.5 or s < 0.02:
            return cc + (c[3],)
        s *= 0.9


def recolor(im, hue, keep=lambda p: False, **kw):
    out = im.copy()
    for y in range(im.height):
        for x in range(im.width):
            p = im.getpixel((x, y))
            if p[3] and not keep(p):
                out.putpixel((x, y), shift(p, hue, **kw))
    return out


def save(im, name):
    assert im.size == (18, 18), name
    im.save(os.path.join(OUT, name + ".png"))


SPIDER, MARKS = split_sense()


def small_spider():
    """The owner's spider body without the outer legs (x 5..12, y 5..15)."""
    return SPIDER.crop((5, 5, 13, 16))


def chevrons_up(im, x, y, n=2):
    """The owner's yellow climbing chevrons (from wall_climbing)."""
    for i in range(n):
        yy = y + i * 5
        im.putpixel((x + 1, yy), Y_HI)
        im.putpixel((x, yy + 1), Y_MID)
        im.putpixel((x + 2, yy + 1), Y_DARK)
        im.putpixel((x - 1, yy + 2), Y_LO)
        im.putpixel((x + 3, yy + 2), Y_LO)


def streaks(im, rows, x0, length):
    """Grey speed streaks (vanilla speed icon's colours)."""
    tones = [(0xe0, 0xe0, 0xe0, 255), (0xa8, 0xa8, 0xa8, 255), (0x6a, 0x6a, 0x6a, 255)]
    for r in rows:
        for i in range(length):
            im.putpixel((x0 + i, r), tones[min(2, (length - 1 - i) * 3 // max(1, length))])


# ------------------------------------------------------------------------------------------------- agility
# roll: the vanilla wind swirl in the owner's yellow (a tumble) with the spider in the middle
roll = recolor(vanilla("mob_effect/wind_charged"), 52, sat_mul=4.0, lum_mul=1.0, dark_hue=-14, sat_cap=0.8)
save(roll, "roll")

# flips: the spider upside down between two curling chevron arcs
flips = blank()
paste(flips, SPIDER.rotate(180), 0, 0)
erase(flips, [(x, y) for y in range(18) for x in range(18) if y < 2])
for (x, y, c) in [(1, 4, Y_LO), (1, 3, Y_MID), (2, 2, Y_MID), (3, 1, Y_HI), (4, 1, Y_HI), (5, 1, Y_MID), (6, 2, Y_LO),
                  (5, 3, Y_LO), (7, 3, Y_DARK),
                  (16, 13, Y_LO), (16, 14, Y_MID), (15, 15, Y_MID), (14, 16, Y_HI), (13, 16, Y_HI), (12, 16, Y_MID),
                  (11, 15, Y_LO), (12, 14, Y_LO), (10, 14, Y_DARK)]:
    flips.putpixel((x, y), c)
save(flips, "flips")

# acrobat: the spider leaping with a star burst
acro = blank()
paste(acro, SPIDER, 0, 1)
erase(acro, [(x, 17) for x in range(18)])
for (x, y, c) in [(14, 0, Y_HI), (14, 1, Y_MID), (13, 1, Y_MID), (15, 1, Y_MID), (14, 2, Y_LO), (12, 1, Y_LO), (16, 1, Y_LO),
                  (14, 3, Y_DARK), (2, 1, Y_MID), (2, 2, Y_LO), (1, 2, Y_LO), (3, 2, Y_LO)]:
    acro.putpixel((x, y), c)
save(acro, "acrobat")

# quick crawl: the owner's wall climber with speed streaks instead of the chevrons
quick = owner("wall_climbing")
erase(quick, [(x, y) for y in range(18) for x in range(13, 18)])
chevrons_up(quick, 14, 2, 3)
save(quick, "quick_crawl")

# ceilings: the wall turned into a ceiling, the spider hangs under it
ceil = owner("wall_climbing").crop((0, 0, 13, 17)).rotate(-90, expand=True)  # 17 x 13, wall on top
c2 = blank()
paste(c2, ceil, 0, 0)
for (x, y, c) in [(4, 14, Y_LO), (5, 15, Y_MID), (6, 14, Y_LO), (11, 14, Y_LO), (12, 15, Y_MID), (13, 14, Y_LO)]:
    c2.putpixel((x, y), c)
save(c2, "ceilings")

# spider jump: vanilla jump boost arrows in spider red
jump = recolor(vanilla("mob_effect/jump_boost"), 352, sat_mul=6.0, lum_mul=0.8, dark_hue=-12, sat_cap=0.85)
for y in range(18):
    for x in range(18):
        p = jump.getpixel((x, y))
        if p[3] and colorsys.rgb_to_hsv(*[v / 255 for v in p[:3]])[1] < 0.3:
            yy = _Y(p)
            jump.putpixel((x, y), Y_HI if yy > 215 else Y_MID if yy > 170 else Y_LO if yy > 120 else Y_DARK)  # lower arrow: yellow
save(jump, "spider_jump")

# soft landing: the vanilla slow falling feathers, warmed to the owner's yellow-white, with a down chevron
soft = recolor(vanilla("mob_effect/slow_falling"), 48, sat_mul=1.6, lum_mul=1.0, sat_cap=0.35)
save(soft, "soft_landing")

# ------------------------------------------------------------------------------------------------- sense
# projectile sense: a vanilla arrow flying at the sense marks
proj = blank()
arrow = vanilla("item/arrow")
paste(proj, arrow, 1, 1)
for (x, y) in [(0, 4), (1, 4), (16, 4), (17, 4), (2, 1), (3, 2), (3, 3), (15, 1), (14, 2), (14, 3)]:
    pass
paste(proj, MARKS.crop((0, 0, 18, 5)), 0, 12)
save(proj, "projectile_sense")

# wide sense: the spider small in the middle, sense marks on all four sides
wide = blank()
paste(wide, small_spider(), 5, 4)
paste(wide, MARKS, 0, 0)
paste(wide, MARKS.crop((0, 0, 18, 5)).transpose(Image.FLIP_TOP_BOTTOM), 0, 13)
save(wide, "wide_sense")

# dodge: the spider sliding aside, leaving speed streaks
dodge = blank()
paste(dodge, SPIDER, 3, 0)
streaks(dodge, (7, 10, 13), 0, 4)
save(dodge, "dodge")

# reflexes: the spider with the owner's 5x8 spider-sense bolt
refl = blank()
paste(refl, SPIDER, -2, 1)
bolt = load(os.path.join(RES, "assets", "powers_unleashed", "textures", "entity", "power", "spider_sense_bolt.png"))
paste(refl, bolt, 12, 1)
paste(refl, bolt, 13, 9)
save(refl, "reflexes")

# sixth sense: the owner's night-vision eye turned red, with the sense marks over it
six = recolor(owner("night_vision"), 350, keep=lambda p: p[:3] in YELLOWS or p[:3] == (255, 255, 255),
              sat_mul=1.0, lum_mul=1.15, dark_hue=-10)
paste(six, MARKS.crop((0, 0, 18, 5)), 0, 0)
save(six, "sixth_sense")

# ------------------------------------------------------------------------------------------------- combat
# strength: the vanilla strength sword, spider red
strength = recolor(vanilla("mob_effect/strength"), 352, sat_mul=5.0, lum_mul=0.9, dark_hue=-10, sat_cap=0.8)
save(strength, "strength")

# strength II: two of them crossed
s2 = strength.copy()
paste(s2, strength.transpose(Image.FLIP_LEFT_RIGHT), 0, 0)
save(s2, "strength_2")

# heavy punch: the red sword with a yellow impact burst at its tip
heavy = strength.copy()
for (x, y, c) in [(14, 0, Y_HI), (16, 1, Y_MID), (17, 3, Y_LO), (12, 0, Y_LO), (16, 4, Y_DARK), (11, 1, Y_DARK)]:
    heavy.putpixel((x, y), c)
paste(heavy, bolt.crop((0, 0, 5, 5)), 12, 3)
save(heavy, "heavy_punch")

# tough body: the vanilla resistance shield in spider red
tough = recolor(vanilla("mob_effect/resistance"), 352, sat_mul=4.0, lum_mul=0.85, dark_hue=-10, sat_cap=0.75)
save(tough, "tough_body")

# spider armour: the vanilla absorption shield in dark navy with the owner's spider on it
armor = recolor(vanilla("mob_effect/absorption"), 228, sat_mul=1.4, lum_mul=0.55, sat_cap=0.7)
paste(armor, small_spider(), 5, 4)
save(armor, "spider_armor")

# regeneration: the vanilla heart with the owner's yellow sparkles
regen = vanilla("mob_effect/regeneration").copy()
for (x, y, c) in [(15, 0, Y_HI), (14, 1, Y_MID), (15, 1, Y_HI), (16, 1, Y_MID), (15, 2, Y_LO),
                  (2, 14, Y_MID), (1, 15, Y_LO), (2, 15, Y_HI), (3, 15, Y_LO), (2, 16, Y_DARK)]:
    regen.putpixel((x, y), c)
save(regen, "regeneration")

# hero's body: the vanilla hero-of-the-village pedestal with a golden gem
hero = recolor(vanilla("mob_effect/hero_of_the_village"), 48, keep=lambda p: p[0] > p[1] + 40,
               sat_mul=1.0, lum_mul=1.1, dark_hue=-14)
save(hero, "hero_body")

# ------------------------------------------------------------------------------------------------- key abilities
# web shot: a cobweb flying right with streaks
shot = blank()
paste(shot, load(os.path.join(RES, "assets", "powers_unleashed", "textures", "entity", "web", "web.png")), 3, 1)
streaks(shot, (5, 9, 13), 0, 4)
save(shot, "web_shot")

# web zip: a string line to the corner and the owner's chevrons along it
zipi = blank()
paste(zipi, vanilla("item/string"), 1, 1)
chevrons_up(zipi, 13, 9, 2)
save(zipi, "web_zip")

# web swing: vanilla weaving (a web with a spider) with the owner's red spider hanging on a thread
swing = vanilla("mob_effect/weaving").copy()
erase(swing, [(x, y) for y in range(5, 15) for x in range(4, 14) if swing.getpixel((x, y))[:3] not in ()
              and sum(swing.getpixel((x, y))[:3]) < 200])
paste(swing, small_spider(), 5, 6)
save(swing, "web_swing")

# ability wheel: a ring of three segments (blue = on, grey = off) like the in-game wheel
wheel = blank()
cx = cy = 8.5
import math
for y in range(18):
    for x in range(18):
        d = math.hypot(x + 0.5 - 9, y + 0.5 - 9)
        if 4.5 <= d <= 8.4:
            ang = (math.degrees(math.atan2(y + 0.5 - 9, x + 0.5 - 9)) + 90) % 360
            if min(ang % 120, 120 - ang % 120) < 7:
                continue
            seg = int(ang // 120)
            base = [(0x59, 0xe4, 0xff), (0x59, 0xe4, 0xff), (0x8a, 0x92, 0x9c)][seg]
            edge = d > 7.4 or d < 5.5
            light = (x + y) < 17
            f = 1.15 if light else 0.8
            if edge:
                f *= 0.6
            c = tuple(max(0, min(255, int(v * f))) for v in base)
            wheel.putpixel((x, y), c + (255,))
paste(wheel, small_spider().crop((1, 2, 7, 9)), 6, 5)
save(wheel, "ability_wheel")

save(owner("spider_sense"), "unknown")
print("ok")
