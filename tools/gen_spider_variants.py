"""Generates the alternative Spider-Man suits (player-skin UV layout, wide + slim + boots) and their item icons.

Usage: python3 gen_spider_variants.py <resources root>

Every suit is described by a painter: color(part, face, u, v, w, h) for each pixel of each cube face, where
(u, v) are face-local coordinates (u left->right as laid out in the skin texture, v top->bottom).
"""
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import suit_textures  # noqa: E402

OUT = sys.argv[1]
T = (0, 0, 0, 0)


def rgb(h):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255)


def cube(ox, oy, w, h, d):
    return {
        "top": (ox + d, oy, w, d),
        "bottom": (ox + d + w, oy, w, d),
        "right": (ox, oy + d, d, h),
        "front": (ox + d, oy + d, w, h),
        "left": (ox + d + w, oy + d, d, h),
        "back": (ox + d + w + d, oy + d, w, h),
    }


# parts: name -> (uv origin, w, h, d, side)  side: which face is the outer one
PARTS = {
    "head": ((0, 0), 8, 8, 8, None),
    "body": ((16, 16), 8, 12, 4, None),
    "right_arm": ((40, 16), 4, 12, 4, "right"),
    "left_arm": ((32, 48), 4, 12, 4, "left"),
    "right_leg": ((0, 16), 4, 12, 4, "right"),
    "left_leg": ((16, 48), 4, 12, 4, "left"),
}


def mirror_rows(halves):
    return [h + h[::-1] for h in halves]


def stamp(rows, u, v, x0=0, y0=0):
    """Char of a pixel map at face coords, or None."""
    y, x = v - y0, u - x0
    if 0 <= y < len(rows) and 0 <= x < len(rows[y]):
        ch = rows[y][x]
        return None if ch == "." else ch
    return None


class Painter:
    web_period = 6

    def web(self, u, v, ou=0, ov=0):
        a, b = u + ou, v + ov
        p = self.web_period
        return (a + b) % p == 0 or (a - b) % p == 0

    def red(self, u, v, ou=0, ov=0):
        return self.WEB if self.web(u, v, ou, ov) else self.RED

    def blue(self, u, v):
        return self.BLUE_D if (u + v) % 7 == 0 else self.BLUE

    # --- default layout: classic comic suit (red head/chest/gloves/boots, blue sides/arms/legs) -----------------
    def head(self, face, u, v, w, h):
        if face == "front":
            ch = stamp(self.EYES, u, v)
            if ch:
                return self.PAL[ch]
        return self.red(u, v, 0 if face != "back" else 3)

    def body(self, face, u, v, w, h):
        if face == "front":
            ch = stamp(self.EMBLEM, u, v)
            if ch:
                return self.PAL[ch]
        if face == "back":
            ch = stamp(self.BACK_EMBLEM, u, v)
            if ch:
                return self.PAL[ch]
        if face in ("front", "back"):
            if v < 6 or 2 <= u <= 5:
                return self.red(u, v, 2 if face == "back" else 0)
            return self.blue(u, v)
        if face in ("right", "left"):
            return self.red(u, v) if v < 4 else self.blue(u, v)
        if face == "top":
            return self.red(u, v)
        return self.blue(u, v)

    def arm(self, face, u, v, w, h, outer):
        if face == "top":
            return self.red(u, v)
        if face == "bottom" or v >= 8:  # gloves
            return self.red(u, v, 1, 2)
        if face == outer or face in ("front", "back"):
            if v < 3:  # shoulder
                return self.red(u, v, 1, 2)
        return self.blue(u, v)

    def leg(self, face, u, v, w, h, outer):
        return self.blue(u, v)

    def boot(self, face, u, v, w, h, outer):
        if face == "top":
            return None
        if face == "bottom":
            return self.red(u, v)
        if v < 6:
            return None
        if v == 6:
            return self.BOOT_TRIM
        return self.red(u, v, 0, 1)

    # --- texture assembly --------------------------------------------------------------------------------------
    def texture(self, slim, boots=False):
        img = Image.new("RGBA", (64, 64), T)
        for name, ((ox, oy), w, h, d, outer) in PARTS.items():
            if slim and name.endswith("_arm"):
                w = 3
            if boots and not name.endswith("_leg"):
                continue
            for face, (x, y, fw, fh) in cube(ox, oy, w, h, d).items():
                for u in range(fw):
                    for v in range(fh):
                        if name == "head":
                            c = self.head(face, u, v, fw, fh)
                        elif name == "body":
                            c = self.body(face, u, v, fw, fh)
                        elif name.endswith("_arm"):
                            c = self.arm(face, u, v, fw, fh, outer)
                        elif boots:
                            c = self.boot(face, u, v, fw, fh, outer)
                        else:
                            c = self.leg(face, u, v, fw, fh, outer)
                        if c:
                            img.putpixel((x + u, y + v), c)
        return img


# =====================================================================================================================
class FirstAppearance(Painter):
    """Amazing Fantasy #15 (1962): bright red with thin black webs, light blue, huge black-rimmed eyes,
    tiny spider emblem, web 'wings' under the arms."""
    RED = rgb("#d02a2e")
    WEB = rgb("#2a1416")
    BLUE = rgb("#3a6ad6")
    BLUE_D = rgb("#2b52ad")
    BOOT_TRIM = rgb("#2a1416")
    K, W, G = rgb("#141418"), rgb("#f2f2f2"), rgb("#9aa4b4")
    PAL = {"K": K, "W": W, "G": G}
    EYES = [""] + mirror_rows(["....", "KKK.", "KWWK", "KWWK", "KWWK", ".KK."])
    EMBLEM = ["........", "........", "...KK...", "..KKKK..", "...KK...", "..K..K.."]
    BACK_EMBLEM = []

    def arm(self, face, u, v, w, h, outer):
        inner = "left" if outer == "right" else "right"
        if face == inner and 1 <= v <= 8:
            # web wing under the arm
            return self.G if (u + v) % 3 == 0 or u == 0 else self.blue(u, v)
        return super().arm(face, u, v, w, h, outer)


class Advanced2(Painter):
    """Marvel's Spider-Man 2 'Advanced Suit 2.0': red & black, big white spider wrapping over the shoulders,
    angular white eyes, black sides and inner legs."""
    RED = rgb("#c81e2c")
    WEB = rgb("#8e1220")
    BLUE = rgb("#1d1d24")      # black panels
    BLUE_D = rgb("#2c2c36")
    BOOT_TRIM = rgb("#1d1d24")
    K, W = rgb("#111115"), rgb("#f4f4f6")
    PAL = {"K": K, "W": W}
    EYES = [""] + mirror_rows(["....", "KK..", "KWWK", ".KWK", "..KK"])
    EMBLEM = ["W.W..W.W", ".W.WW.W.", "..WWWW..", "...WW...", "..WWWW..", ".W.WW.W.", "W..WW..W", "...WW..."]
    BACK_EMBLEM = ["W..WW..W", ".W.WW.W.", "..WWWW..", "...WW...", "..WWWW..", ".W.WW.W.", "W..WW..W"]

    def body(self, face, u, v, w, h):
        if face == "top":
            # emblem legs run over the shoulders
            return self.W if u in (0, 7) else self.red(u, v)
        if face in ("right", "left"):
            return self.blue(u, v) if v >= 2 else self.red(u, v)
        if face in ("front", "back"):
            ch = stamp(self.EMBLEM if face == "front" else self.BACK_EMBLEM, u, v)
            if ch:
                return self.PAL[ch]
            if v >= 8 and u in (0, 7):
                return self.blue(u, v)
            return self.red(u, v)
        return self.blue(u, v)

    def arm(self, face, u, v, w, h, outer):
        inner = "left" if outer == "right" else "right"
        if face == inner or face == "bottom":
            return self.blue(u, v)
        if face == "back" and 2 <= v <= 9:
            return self.blue(u, v)
        if v == 9:
            return self.K   # glove cuff
        return self.red(u, v, 1, 2)

    def leg(self, face, u, v, w, h, outer):
        inner = "left" if outer == "right" else "right"
        if face in (inner, "back", "bottom"):
            return self.blue(u, v)
        if face == "top":
            return self.red(u, v)
        return self.red(u, v, 2, 0)

    def boot(self, face, u, v, w, h, outer):
        if face == "bottom":
            return self.K
        if face == "top" or v < 7:
            return None
        if v == 11:
            return self.K   # sole
        return self.red(u, v, 0, 1) if face in ("front", outer) else self.blue(u, v)


class BrandNewDay(Painter):
    """Spider-Man: Brand New Day (2026 film): comic-accurate classic, bright red with raised black webbing,
    deep blue, large black spider on the chest, red spider on the back, big black-rimmed eyes."""
    RED = rgb("#d4222c")
    WEB = rgb("#3a0a10")
    BLUE = rgb("#1f3a9e")
    BLUE_D = rgb("#172c7a")
    BOOT_TRIM = rgb("#3a0a10")
    K, W, R = rgb("#121216"), rgb("#f2f2f4"), rgb("#d4222c")
    PAL = {"K": K, "W": W, "R": R}
    EYES = [""] + mirror_rows(["....", "KKK.", "KWWK", "KWWK", ".KKK"])
    EMBLEM = ["........", "K..KK..K", ".K.KK.K.", "..KKKK..", "...KK...", "..KKKK..", ".K.KK.K.", "K..KK..K"]
    BACK_EMBLEM = ["........", "K..KK..K", ".KKRRKK.", "..KRRK..", ".KKRRKK.", "K.KRRK.K", "..KKKK..", ".K....K."]


class Raimi(Painter):
    """Spider-Man (2002, Sam Raimi): deep red with raised silver webbing, dark blue side panels, silver
    reflective eyes, thin black emblem, red arms with blue inner panel."""
    RED = rgb("#a8161f")
    WEB = rgb("#b58a90")
    BLUE = rgb("#1a2c78")
    BLUE_D = rgb("#13205a")
    BOOT_TRIM = rgb("#c9a7aa")
    K, S, s = rgb("#121214"), rgb("#d7dce4"), rgb("#9aa2b0")
    PAL = {"K": K, "S": S, "s": s}
    EYES = [""] + mirror_rows(["....", ".KK.", "KSSK", "KSsK", ".KsK", "..K."])
    EMBLEM = ["........", "..K..K..", "...KK...", ".KKKKKK.", "...KK...", "..K..K..", ".K....K."]
    BACK_EMBLEM = []

    def body(self, face, u, v, w, h):
        if face in ("right", "left"):
            return self.blue(u, v) if v >= 2 else self.red(u, v)
        if face in ("front", "back") and v >= 4 and (u == 0 or u == 7):
            return self.blue(u, v)
        if face in ("front", "back"):
            ch = stamp(self.EMBLEM if face == "front" else self.BACK_EMBLEM, u, v)
            if ch:
                return self.PAL[ch]
            return self.red(u, v, 1 if face == "back" else 0)
        if face == "top":
            return self.red(u, v)
        return self.blue(u, v)

    def arm(self, face, u, v, w, h, outer):
        inner = "left" if outer == "right" else "right"
        if face == inner and v < 9:
            return self.blue(u, v)
        return self.red(u, v, 1, 2)


SUITS = {
    "spider_man_first_appearance": FirstAppearance(),
    "spider_man_advanced_2": Advanced2(),
    "spider_man_brand_new_day": BrandNewDay(),
    "spider_man_raimi": Raimi(),
}


def save(img, *path):
    p = os.path.join(OUT, *path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    img.save(p)


# ---------------------------------------------------------------- item icons ----------------------------------------
MASK = [
    "................",
    ".....KKKKKK.....",
    "....KRrRRrRK....",
    "...KRRrRRrRRK...",
    "..KRrrrrrrrrRK..",
    "..KRRrRRrRRrRK..",
    "..KKKKRrrKKKKK..",
    "..KWWWKRRKWWWK..",
    "..KKWWKrrKWWKK..",
    "..KRKWKRRKWKRK..",
    "..KRRKrRRrKRRK..",
    "...KRrrrrrrRK...",
    "...KRRrRRrRRK...",
    "....KRrRRrRK....",
    ".....KKKKKK.....",
    "................",
]
SUIT = [
    "................",
    "..KKKK....KKKK..",
    ".KRRRRKKKKRRRRK.",
    ".KRrRRRrrRRRrRK.",
    ".KRRrRERREKRrRK.",
    ".KBRRREEEERRRBK.",
    ".KBRrRREERRrRBK.",
    ".KBKRREEEERRKBK.",
    ".KBKRERRREKRKBK.",
    ".KKKBRRrrRRBKKK.",
    "...KBBRRRRBBK...",
    "...KBBRrrRBBK...",
    "...KBBRRRRBBK...",
    "...KBBBRRBBBK...",
    "...KKKKKKKKKK...",
    "................",
]
PANTS = [
    "................",
    "...KKKKKKKKKK...",
    "...KBBBBBBBBK...",
    "...KBbBBBBbBK...",
    "...KBBBKKBBBK...",
    "...KBBBKKBBBK...",
    "...KBbBKKBbBK...",
    "...KBBBKKBBBK...",
    "...KBBBKKBBBK...",
    "...KBBbKKbBBK...",
    "...KBBBKKBBBK...",
    "...KBBBKKBBBK...",
    "...KBbBKKBbBK...",
    "...KBBBKKBBBK...",
    "...KKKKKKKKKK...",
    "................",
]
BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "..KKKKK..KKKKK..",
    "..KRrRK..KRrRK..",
    "..KRRRK..KRRRK..",
    "..KrrrK..KrrrK..",
    "..KRrRK..KRrRK..",
    "..KRRRK..KRRRK..",
    ".KKRrRK..KRrRKK.",
    "KRRRRRK..KRRRRRK",
    "KrrrrrK..KrrrrrK",
    "KRRRRRK..KRRRRRK",
    "KKKKKKK..KKKKKKK",
    "................",
]

ICON_PAL = {
    "spider_man_first_appearance": {"R": "#d02a2e", "r": "#2a1416", "B": "#3a6ad6", "b": "#2b52ad", "W": "#f2f2f2",
                                    "E": "#d02a2e", "K": "#141418"},
    "spider_man_advanced_2": {"R": "#c81e2c", "r": "#8e1220", "B": "#26262e", "b": "#3a3a46", "W": "#f4f4f6",
                              "E": "#f4f4f6", "K": "#0c0c0f"},
    "spider_man_brand_new_day": {"R": "#d4222c", "r": "#3a0a10", "B": "#1f3a9e", "b": "#172c7a", "W": "#f2f2f4",
                                 "E": "#121216", "K": "#121216"},
    "spider_man_raimi": {"R": "#a8161f", "r": "#b58a90", "B": "#1a2c78", "b": "#13205a", "W": "#d7dce4",
                         "E": "#121214", "K": "#121214"},
}


def icon(rows, pal):
    img = Image.new("RGBA", (16, 16), T)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != ".":
                img.putpixel((x, y), rgb(pal[ch]))
    return img


for sid, painter in SUITS.items():
    d = ("assets", "powers_unchained", "textures", "suit", sid)
    save(painter.texture(False), *d, sid + ".png")
    save(painter.texture(True), *d, sid + "_slim.png")
    save(painter.texture(False, boots=True), *d, sid + "_boots.png")
    suit_textures.split(os.path.join(OUT, *d), sid)
    pal = ICON_PAL[sid]
    for piece, rows in (("mask", MASK), ("chestplate", SUIT), ("pants", PANTS), ("boots", BOOTS)):
        if sid == "spider_man_advanced_2" and piece == "pants":
            rows = [r.replace("KK", "RR", 1) if 3 < i < 14 else r for i, r in enumerate(rows)]
        save(icon(rows, pal), "assets", "powers_unchained", "textures", "item", f"{sid}_{piece}.png")
print("ok")
