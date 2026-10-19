"""Generates JSON block models for the tailoring table concepts. Front of every model faces north."""
import json
import os
import sys

OUT = os.path.join(sys.argv[1], "assets", "powers_unchained", "models", "block")
os.makedirs(OUT, exist_ok=True)

FACES = ("north", "south", "east", "west", "up", "down")


def box(f, t, tex, faces=FACES, uv=None, rot=None, cull=None, tint=None):
    """tex: '#name' for all faces or dict face->'#name'. uv: dict face->[u0,v0,u1,v1]."""
    el = {"from": list(f), "to": list(t), "faces": {}}
    for face in faces:
        t_ = tex[face] if isinstance(tex, dict) else tex
        if t_ is None:
            continue
        fd = {"texture": t_}
        if uv and face in uv:
            fd["uv"] = uv[face]
        if cull and face in cull:
            fd["cullface"] = cull[face]
        el["faces"][face] = fd
    if rot:
        el["rotation"] = rot
    return el


def model(name, textures, elements, particle):
    textures = dict(textures)
    textures["particle"] = particle
    m = {"parent": "minecraft:block/block", "textures": textures, "elements": elements}
    json.dump(m, open(os.path.join(OUT, name + ".json"), "w"), indent=2)


def spool(x, y, z, idx, h=3):
    """2x h x2 thread spool using strip idx (0 red, 1 blue, 2 yellow, 3 green) of the spools texture."""
    u = idx * 4
    side = [u + 1, 16 - h * 16 // 5 if False else 2, u + 3, 14]
    return box((x, y, z), (x + 2, y + h, z + 2),
               {"north": "#spools", "south": "#spools", "east": "#spools", "west": "#spools",
                "up": "#spool_top", "down": "#spool_top"},
               uv={f: [u + 1, 0, u + 3, 16] for f in ("north", "south", "east", "west")} | {"up": [6, 6, 10, 10], "down": [6, 6, 10, 10]})


# =============================================================================================
# A. Sewing machine table: low spruce table with a cast-iron treadle and a black & gold
#    sewing machine on top, red cloth draped over the front edge.
# =============================================================================================
def concept_a():
    T = {
        "top": "minecraft:block/spruce_planks",
        "leg": "minecraft:block/stripped_spruce_log",
        "iron": "minecraft:block/anvil",
        "machine": "powers_unchained:block/sewing_machine",
        "spools": "powers_unchained:block/thread_spools",
        "spool_top": "minecraft:block/oak_log_top",
        "cloth": "minecraft:block/red_wool",
        "drawer": "minecraft:block/dark_oak_planks",
    }
    E = []
    # table top and apron
    E.append(box((0, 8, 0), (16, 10, 16), "#top"))
    E.append(box((1, 6, 1), (15, 8, 15), "#drawer", faces=("north", "south", "east", "west", "down")))
    # legs
    for x, z in ((1, 1), (13, 1), (1, 13), (13, 13)):
        E.append(box((x, 0, z), (x + 2, 6, z + 2), "#leg", cull={"down": "down"}))
    # treadle frame + pedal
    E.append(box((3, 2, 2), (13, 3, 3), "#iron", faces=("north", "south", "up", "down")))
    E.append(box((3, 2, 13), (13, 3, 14), "#iron", faces=("north", "south", "up", "down")))
    E.append(box((4, 1, 4), (12, 2, 12), "#iron"))
    # sewing machine: bed, pillar, arm, head, needle, hand wheel
    m = "#machine"
    E.append(box((3, 10, 5), (13, 11, 11), m, uv={f: [0, 1, 10, 2] for f in ("north", "south", "east", "west")} | {"up": [0, 0, 10, 3], "down": [0, 0, 10, 3]}))
    E.append(box((10, 11, 6), (13, 14, 10), m, uv={"north": [0, 1, 3, 4], "south": [0, 1, 3, 4], "east": [0, 1, 4, 4], "west": [0, 1, 4, 4]}))
    E.append(box((4, 14, 6), (13, 16, 10), m, uv={"north": [0, 4, 9, 6], "south": [0, 4, 9, 6], "east": [0, 1, 4, 3], "west": [0, 1, 4, 3], "up": [0, 0, 9, 4], "down": [0, 0, 9, 4]}))
    E.append(box((3, 12, 6), (5, 16, 10), m, uv={"north": [1, 8, 3, 12], "south": [0, 0, 2, 4], "west": [0, 8, 4, 12], "east": [0, 0, 4, 4], "down": [0, 0, 2, 4], "up": [0, 0, 2, 4]}))
    E.append(box((3.5, 11, 7.5), (4.5, 12, 8.5), "#machine", uv={f: [0, 12, 1, 13] for f in FACES}))
    E.append(box((13, 11.5, 6.5), (14, 15.5, 9.5), m, uv={"east": [0, 12, 3, 16], "west": [0, 12, 3, 16], "north": [0, 12, 1, 16], "south": [0, 12, 1, 16], "up": [0, 12, 1, 15], "down": [0, 12, 1, 15]}))
    # spool on top of the arm
    E.append(spool(8, 16, 7, 0, h=2))
    # draped red cloth (top part + part hanging over the front edge)
    E.append(box((0.5, 10, 0.5), (6, 10.5, 6), "#cloth"))
    E.append(box((0.5, 5, 0), (6, 10.5, 0.5), "#cloth"))
    model("concept_a_sewing_table", T, E, "minecraft:block/spruce_planks")


# =============================================================================================
# B. Tailor's workbench: full block like vanilla job-site blocks. Drawer front with brass
#    knobs, thread-spool cubbies on the sides, cutting mat on top with a fabric roll,
#    shears and a pin cushion.
# =============================================================================================
def concept_b():
    T = {
        "front": "powers_unchained:block/tailoring_workbench_front",
        "side": "powers_unchained:block/tailoring_workbench_side",
        "back": "minecraft:block/dark_oak_planks",
        "mat": "powers_unchained:block/tailoring_cutting_mat",
        "bottom": "minecraft:block/dark_oak_planks",
        "roll": "minecraft:block/light_blue_wool",
        "roll_end": "minecraft:block/stripped_oak_log_top",
        "iron": "minecraft:block/iron_block",
        "handle": "minecraft:block/red_wool",
        "pin": "minecraft:block/red_wool",
    }
    E = []
    E.append(box((0, 0, 0), (16, 16, 16),
                 {"north": "#front", "south": "#back", "east": "#side", "west": "#side", "up": "#mat", "down": "#bottom"},
                 cull={f: f for f in FACES if f != "up"}))
    # fabric roll lying across the back of the top
    E.append(box((1.5, 16, 10), (14.5, 18.5, 12.5), {"north": "#roll", "south": "#roll", "up": "#roll", "down": "#roll", "east": "#roll_end", "west": "#roll_end"}))
    # shears (two blades + red handles)
    E.append(box((9, 16, 3), (13, 16.5, 3.75), "#iron"))
    E.append(box((9, 16, 4.25), (13, 16.5, 5), "#iron"))
    E.append(box((12.5, 16, 2.5), (14.5, 16.75, 5.5), "#handle"))
    # pin cushion
    E.append(box((2.5, 16, 3), (5.5, 17.5, 6), "#pin"))
    E.append(box((3.5, 17.5, 4), (3.75, 18.25, 4.25), "#iron"))
    E.append(box((4.25, 17.5, 4.75), (4.5, 18.25, 5), "#iron"))
    model("concept_b_tailor_workbench", T, E, "minecraft:block/dark_oak_planks")


# =============================================================================================
# C. Dress form station: dark-oak cabinet with a tailor's dummy on top and a measuring tape
#    hanging from its shoulders.
# =============================================================================================
def concept_c():
    T = {
        "cab": "minecraft:block/dark_oak_planks",
        "trim": "minecraft:block/spruce_planks",
        "pole": "minecraft:block/dark_oak_log",
        "form": "powers_unchained:block/dress_form",
        "tape": "powers_unchained:block/measuring_tape",
        "spools": "powers_unchained:block/thread_spools",
        "spool_top": "minecraft:block/oak_log_top",
    }
    E = []
    E.append(box((1, 0, 1), (15, 5, 15), "#cab", cull={"down": "down"}))
    E.append(box((0.5, 5, 0.5), (15.5, 6, 15.5), "#trim"))
    E.append(box((7, 6, 7), (9, 8, 9), "#pole"))
    E.append(box((4, 8, 5), (12, 11, 11), "#form"))                 # hips
    E.append(box((4.5, 11, 5.5), (11.5, 13, 10.5), "#form"))        # waist
    E.append(box((3.5, 13, 5), (12.5, 16, 11), "#form"))            # chest/shoulders
    E.append(box((7, 16, 7), (9, 17, 9), "#pole"))                  # neck knob
    E.append(box((5, 9, 4.75), (6, 16.1, 5), "#tape", uv={"north": [0, 0, 1, 16], "south": [0, 0, 1, 16]}))
    E.append(box((10, 9, 4.75), (11, 16.1, 5), "#tape", uv={"north": [0, 0, 1, 16], "south": [0, 0, 1, 16]}))
    E.append(box((6.5, 15.5, 6.5), (9.5, 16.3, 9.5), "#tape", uv={f: [0, 0, 3, 1] for f in FACES}))
    E.append(spool(2, 6, 2, 1))
    E.append(spool(12, 6, 2, 3))
    model("concept_c_dress_form", T, E, "minecraft:block/dark_oak_planks")


# =============================================================================================
# D. Pattern drafting board: lectern-like slanted board with a suit pattern sheet, on a spruce
#    stand with thread spools around the base.
# =============================================================================================
def concept_d():
    T = {
        "base": "minecraft:block/spruce_planks",
        "post": "minecraft:block/stripped_spruce_log",
        "board": "minecraft:block/spruce_planks",
        "pattern": "powers_unchained:block/suit_pattern",
        "spools": "powers_unchained:block/thread_spools",
        "spool_top": "minecraft:block/oak_log_top",
        "iron": "minecraft:block/iron_block",
    }
    E = []
    E.append(box((1, 0, 1), (15, 2, 15), "#base", cull={"down": "down"}))
    E.append(box((5, 2, 6), (11, 10, 10), "#post"))
    rot = {"origin": [8, 11, 8], "axis": "x", "angle": -22.5}
    E.append(box((0, 10, 1), (16, 12, 15), {"north": "#board", "south": "#board", "east": "#board", "west": "#board", "up": "#pattern", "down": "#board"}, rot=rot))
    E.append(box((0, 9, 1), (16, 12, 2), "#board", rot=rot))      # lip that holds the sheet
    E.append(spool(2, 2, 2, 0))
    E.append(spool(12, 2, 2, 2))
    E.append(spool(12, 2, 12, 1))
    E.append(box((2, 2, 12), (4, 3, 14), "#iron"))                 # thimble-ish box
    model("concept_d_pattern_board", T, E, "minecraft:block/spruce_planks")


concept_a()
concept_b()
concept_c()
concept_d()
print("ok")
