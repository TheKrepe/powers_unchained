"""Splits a full suit texture (player-skin layout, 64x64) into one texture per piece:
<suit>_mask.png, <suit>_chestplate.png, <suit>_chestplate_slim.png, <suit>_pants.png (+ existing <suit>_boots.png).

Usage: python3 suit_textures.py <suit texture dir> [...]   (expects <suit>.png and <suit>_slim.png in each dir,
removes them afterwards). The generators call split() directly.
"""
import os
import sys

from PIL import Image

# (x0, y0, x1, y1) regions of the 64x64 skin layout, base layer + outer layer
HEAD = [(0, 0, 32, 16), (32, 0, 64, 16)]
CHEST = [(16, 16, 40, 32), (16, 32, 40, 48),      # body + jacket
         (40, 16, 56, 32), (40, 32, 56, 48),      # right arm + sleeve
         (32, 48, 48, 64), (48, 48, 64, 64)]      # left arm + sleeve
LEGS = [(0, 16, 16, 32), (0, 32, 16, 48),         # right leg + pants
        (16, 48, 32, 64), (0, 48, 16, 64)]        # left leg + pants


def extract(src, regions):
    out = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for r in regions:
        out.paste(src.crop(r), r[:2])
    return out


def split(directory, name, remove=True):
    wide_path = os.path.join(directory, name + ".png")
    slim_path = os.path.join(directory, name + "_slim.png")
    wide = Image.open(wide_path).convert("RGBA")
    slim = Image.open(slim_path).convert("RGBA")
    extract(wide, HEAD).save(os.path.join(directory, name + "_mask.png"))
    extract(wide, CHEST).save(os.path.join(directory, name + "_chestplate.png"))
    extract(slim, CHEST).save(os.path.join(directory, name + "_chestplate_slim.png"))
    extract(wide, LEGS).save(os.path.join(directory, name + "_pants.png"))
    if remove:
        os.remove(wide_path)
        os.remove(slim_path)


if __name__ == "__main__":
    for d in sys.argv[1:]:
        split(d, os.path.basename(os.path.normpath(d)))
    print("ok")
