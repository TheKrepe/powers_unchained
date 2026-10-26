"""Spider-Man (Marvel Midnight) — the 2026 "Midnight Spider-Man" comic look, Minecraft skin style.

Peter is fused with a huge spider: the chestplate is his pale bare torso with the spider's black body in the middle of
the chest (glowing eyes, red hourglass), its legs spreading over the whole torso and wrapping round the sides to the
back (leg joints stand out on the outer layer), leg tips gripping the shoulders, dark veins on pale bare arms, and
boxing hand wraps as gloves. Red hood with goggles and a dark
bandana as the mask, dark cargo pants with a belt, black sneakers with red laces.

Writes textures/suit/spider_man_marvel_midnight/*.png (64x64 player-skin layout, per piece) and the item icons.
Usage: python3 tools/gen_spider_midnight.py <resources root>
"""
import os
import random
import sys

from PIL import Image

ROOT = sys.argv[1]
TEX = os.path.join(ROOT, "assets", "powers_unchained", "textures")
NAME = "spider_man_marvel_midnight"
rnd = random.Random(2026)


def hx(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c[:3]) + (255,)


def noise(c, amount=0.06):
    return shade(c, 1.0 + rnd.uniform(-amount, amount))


# chitin
C0, C1, C2, C3, C4 = hx("#0c0910"), hx("#191320"), hx("#251c2e"), hx("#3a2b4a"), hx("#56406e")
# reds
R1, R2, R3, R4 = hx("#6e0c14"), hx("#ad1620"), hx("#e3302c"), hx("#ff7040")
VEIN = hx("#5c0c1a")
# hood
H1, H2, H3, HD = hx("#a3161e"), hx("#c22128"), hx("#7a1016"), hx("#2e0709")
# goggles / bandana
GK, GL1, GL2, GLH = hx("#232327"), hx("#ff5a1e"), hx("#b81e0c"), hx("#ffc09a")
B1, B2 = hx("#26262c"), hx("#34343c")
# hand wraps
W1, W2, W3 = hx("#dcd6cc"), hx("#bcb4a8"), hx("#8c867a")
# pants / boots
P1, P2, P3 = hx("#2b2b33"), hx("#36363f"), hx("#1c1c22")
BELT, BUCKLE = hx("#17171b"), hx("#9a958a")
K1, K2, SOLE, SOLE2 = hx("#16161a"), hx("#26262c"), hx("#8c877c"), hx("#5e5a52")


def cube(img, u, v, w, h, d, paint):
    """Paints the six faces of a skin cube: paint(face, x, y, fw, fh) -> colour or None."""
    faces = {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }
    px = img.load()
    for face, (fx, fy, fw, fh) in faces.items():
        for y in range(fh):
            for x in range(fw):
                c = paint(face, x, y, fw, fh)
                if c is not None:
                    px[fx + x, fy + y] = c


# ---------------------------------------------------------------- mask: red hood, goggles, bandana
FACE = [
    "DDDDDDDD",
    "HDDDDDDH",
    "HKKKKKKH",
    "HLlKKLlH",
    "HMMKKMMH",
    "BBBBBBBB",
    "BbBBbBBb",
    "BBBBBBBB",
]
FACE_PAL = {"D": HD, "H": H1, "K": GK, "L": GLH, "l": GL1, "M": GL2, "B": B1, "b": B2}


def mask_base(face, x, y, w, h):
    if face == "front":
        return noise(FACE_PAL[FACE[y][x]], 0.03)
    if face == "bottom":
        return B1
    if face == "back":
        # hood seam down the middle
        return noise(H3 if x in (3, 4) and y < 6 else H1)
    if face == "top":
        return noise(H2 if (x + y) % 5 == 0 else H1)
    # sides: hood, bandana knot at the back of the lower half
    if y >= 5:
        return noise(B1 if (face == "left" and x < 2) or (face == "right" and x > 5) else H1)
    return noise(H1)


def mask_hat(face, x, y, w, h):
    """The hood's volume on the outer layer; open in front (only the rim and the sides frame the face)."""
    if face == "front":
        if y == 0:
            return H2
        if x in (0, 7) and y <= 5:
            return H3 if y > 3 else H1
        return None
    if face == "bottom":
        return None
    if face == "back":
        return noise(H1 if y < 7 else H3) if not (x in (3, 4) and y < 7) else noise(H3)
    if face == "top":
        return noise(H2 if (x * 3 + y) % 7 == 0 else H1)
    # sides: folds of the hood, shadowed towards the bottom
    c = H1 if y < 5 else H3
    if (x + y * 2) % 6 == 0:
        c = H2
    return noise(c) if y < 7 else None


# ---------------------------------------------------------------- chestplate: the spider fused into the chest
# Peter is shirtless and pale, the spider's black body sits in the middle of the chest and its legs spread over the
# whole torso (and wrap round the sides to the back), veins run out from it. Left half of the front (cols 0-3),
# mirrored for the right half.
FLESH, FLESH2, FLESH3 = hx("#b89a8b"), hx("#a3877b"), hx("#8b6f66")
V2 = hx("#7a2a3a")
FRONT_HALF = [
    "kk..",
    ".kkK",
    "k.KE",
    "kkKh",
    ".v.K",
    "..Kh",
    "kkKr",
    "..Kr",
    ".kKR",
    "k.Kr",
    "k.vK",
    ".v..",
]
SPIDER_PAL = {"k": C1, "K": C2, "h": C4, "E": R4, "r": R2, "R": R3, "v": VEIN}
LEG_ROWS = (0, 3, 6, 9)          # rows where the legs leave the front and wrap round the body
KNUCKLES = ((0, 0), (0, 3), (0, 6), (1, 8))


def flesh(x, y):
    c = FLESH if (x + y) % 4 else FLESH2
    return noise(c, 0.04)


def torso_base(face, x, y, w, h):
    if face == "front":
        ch = FRONT_HALF[y][x if x < 4 else 7 - x]
        return noise(SPIDER_PAL[ch], 0.04) if ch in SPIDER_PAL else flesh(x, y)
    if face in ("right", "left"):
        if y in LEG_ROWS:
            return R2 if x == 2 else noise(C1)          # the legs wrapping round, a red joint
        if y in (1, 7) and x == 1:
            return VEIN
        return flesh(x, y)
    if face == "back":
        # leg tips reach round to the spine, veins in between
        for row in LEG_ROWS:
            if y == row and (x <= 1 or x >= 6):
                return noise(C1)
            if y == row and x in (2, 5):
                return R2
        if x in (3, 4) and y % 3 == 1:
            return V2
        return flesh(x, y) if y < 11 else noise(FLESH3)
    if face == "top":
        return noise(C1) if x in (0, 7) else flesh(x, y)
    return noise(FLESH3)


def torso_jacket(face, x, y, w, h):
    """The spider's leg joints stand out from the body (outer layer)."""
    if face == "front":
        if (x, y) in KNUCKLES or (7 - x, y) in KNUCKLES:
            return noise(C2)
        if y in (2, 3) and x in (3, 4):
            return R4 if y == 2 else None              # glowing eyes stand out a little
        return None
    if face in ("right", "left") and y in LEG_ROWS and x == 0:
        return noise(C2)
    return None


def arm_base(outer_face):
    def paint(face, x, y, w, h):
        if face == "top":
            return noise(C1) if x == 0 else flesh(x, y)
        if face == "bottom":
            return noise(W2)
        if y >= 8:
            # boxing hand wraps (the gloves)
            if y == 8:
                return W3
            c = W2 if (x + y) % 3 == 0 else W1
            if face == "front" and y >= 10 and x in (1, 2):
                c = R1 if (x + y) % 2 == 0 else c         # blood on the knuckles
            return noise(c, 0.04)
        # the spider's front leg tips grip the shoulder
        if y == 0 and face in (outer_face, "front", "back"):
            return noise(C1)
        if y == 1 and face == outer_face and x in (1, 2):
            return noise(C1)
        if y == 2 and face == outer_face and x == 1:
            return R2
        # pale bare arm with dark veins
        if face in (outer_face, "front") and ((y == 3 and x == 1) or (y == 4 and x == 2) or (y == 5 and x == 2)
                                              or (y == 6 and x == 1 and face == outer_face)):
            return VEIN
        return flesh(x, y) if y < 7 else noise(FLESH2)
    return paint


def arm_sleeve(outer_face):
    def paint(face, x, y, w, h):
        if face in ("top", "bottom"):
            return None
        if y == 0 and face == outer_face and x in (1, 2):
            return noise(C2)                              # claw knuckles on the shoulder
        if y == 8 and (x + (1 if face in ("front", "back") else 0)) % 2 == 0:
            return W1                                     # the wrap's thick top edge
        return None
    return paint


# ---------------------------------------------------------------- pants: dark cargo pants + belt on the torso
def leg_pants(outer_face):
    def paint(face, x, y, w, h):
        if face == "top":
            return None
        if face == "bottom":
            return P3
        c = P1
        if face == outer_face and 4 <= y <= 6 and 1 <= x <= 2:
            c = P2 if not (y == 4 or x == 1) else P3          # cargo pocket
        if face == "front" and y in (6, 7):
            c = P2                                             # knee
        if y == 0 and face == "front":
            c = P3
        return noise(c, 0.05)
    return paint


def torso_belt(face, x, y, w, h):
    if face in ("front", "back", "left", "right") and y == h - 1:
        if face == "front" and x in (3, 4):
            return BUCKLE
        return BELT
    return None


# ---------------------------------------------------------------- boots: black sneakers, red laces
def leg_boots(face, x, y, w, h):
    if face == "top":
        return None
    if face == "bottom":
        return SOLE2
    if y < 8:
        return None
    if y == 11:
        return SOLE if face != "back" else SOLE2
    if face == "front" and y in (8, 9, 10) and x in (1, 2):
        return R2 if (x + y) % 2 == 0 else K1                  # laces
    if y == 8:
        return K2
    return noise(K1, 0.05)


def blank():
    return Image.new("RGBA", (64, 64), (0, 0, 0, 0))


def chest(slim):
    img = blank()
    aw = 3 if slim else 4
    cube(img, 16, 16, 8, 12, 4, torso_base)
    cube(img, 16, 32, 8, 12, 4, torso_jacket)
    cube(img, 40, 16, aw, 12, 4, arm_base("right"))
    cube(img, 40, 32, aw, 12, 4, arm_sleeve("right"))
    cube(img, 32, 48, aw, 12, 4, arm_base("left"))
    cube(img, 48, 48, aw, 12, 4, arm_sleeve("left"))
    return img


def main():
    out = os.path.join(TEX, "suit", NAME)
    os.makedirs(out, exist_ok=True)
    mask = blank()
    cube(mask, 0, 0, 8, 8, 8, mask_base)
    cube(mask, 32, 0, 8, 8, 8, mask_hat)
    mask.save(os.path.join(out, NAME + "_mask.png"))
    chest(False).save(os.path.join(out, NAME + "_chestplate.png"))
    chest(True).save(os.path.join(out, NAME + "_chestplate_slim.png"))
    pants = blank()
    cube(pants, 0, 16, 4, 12, 4, leg_pants("right"))
    cube(pants, 16, 48, 4, 12, 4, leg_pants("left"))
    cube(pants, 16, 16, 8, 12, 4, torso_belt)
    pants.save(os.path.join(out, NAME + "_pants.png"))
    boots = blank()
    cube(boots, 0, 16, 4, 12, 4, leg_boots)
    cube(boots, 16, 48, 4, 12, 4, leg_boots)
    boots.save(os.path.join(out, NAME + "_boots.png"))
    icons()
    print("midnight textures written")


# ---------------------------------------------------------------- item icons (16x16)
ICON_MASK = [
    "................",
    ".....hhhhhh.....",
    "....hHHHHHHh....",
    "...hHHHHHHHHh...",
    "...hHDDDDDDHh...",
    "...hHKKKKKKHh...",
    "...hHLlKKLlHh...",
    "...hHMMKKMMHh...",
    "...hHBBBBBBHh...",
    "...hHBbBBbBHh...",
    "...hhBBBBBBhh...",
    "....hhBBBBhh....",
    ".....hhhhhh.....",
    "................",
    "................",
    "................",
]
ICON_CHEST = [
    "................",
    "..ffff....ffff..",
    ".fcfffc..cfffcf.",
    ".ffcffKyyKffcff.",
    ".fffcfKEEKfcfff.",
    ".ffffcKAAKcffff.",
    "..ff.cKAAKc.ff..",
    "..ff.cKArKc.ff..",
    "..ww.cKrRKc.ww..",
    "..WW.fKArKf.WW..",
    "..WW.cfKKfc.WW..",
    ".....ffcffcf....",
    ".....ffffff.....",
    "................",
    "................",
    "................",
]
ICON_PANTS = [
    "................",
    "...ZZZZZZZZZZ...",
    "...PPPPPPPPPP...",
    "...PPPPPPPPPP...",
    "...PPPPPPPPPP...",
    "...PPPP..PPPP...",
    "...PQQP..PQQP...",
    "...PQQP..PQQP...",
    "...PPPP..PPPP...",
    "...pPPP..PPPp...",
    "...PPPP..PPPP...",
    "...pppp..pppp...",
    "................",
    "................",
    "................",
    "................",
]
ICON_BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "..kkkk....kkkk..",
    "..KrKK....KKrK..",
    "..KKrK....KrKK..",
    "..KrKK....KKrK..",
    "..KKKKk..kKKKK..",
    "..KKKKKk.KKKKKK.",
    "..KKKKKK.KKKKKK.",
    "..SSSSSS.SSSSSS.",
    "................",
    "................",
    "................",
    "................",
]
ICON_PAL = {
    "h": H3, "H": H1, "D": HD, "K": GK, "L": GLH, "l": GL1, "M": GL2, "B": B1, "b": B2,
    "c": C1, "C": C2, "a": C4, "f": hx("#b89a8b"), "y": R4, "E": R3, "A": C3, "r": R2, "R": R1, "w": W1, "W": W2,
    "Z": BELT, "P": P1, "p": P3, "Q": P2,
}
ICON_PAL_BOOTS = {"k": K2, "K": K1, "r": R2, "S": SOLE}


def icon(rows, pal, name):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in pal:
                img.putpixel((x, y), pal[ch])
    img.save(os.path.join(TEX, "item", name + ".png"))


def icons():
    icon(ICON_MASK, ICON_PAL, NAME + "_mask")
    icon(ICON_CHEST, ICON_PAL, NAME + "_chestplate")
    icon(ICON_PANTS, ICON_PAL, NAME + "_pants")
    icon(ICON_BOOTS, ICON_PAL_BOOTS, NAME + "_boots")


if __name__ == "__main__":
    main()
