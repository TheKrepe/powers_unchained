"""3D player doll (iso) for mockups, via tools/blockrender.py: player model as block-model elements, skin UVs."""
import os, sys, json
sys.path.insert(0, '/home/claude/powers_unchained/tools')
import blockrender
from PIL import Image

RES = '/home/claude/powers_unchained/common/src/main/resources'
HERE = os.path.dirname(os.path.abspath(__file__))


def skin_for(suit, slim=False):
    d = f'{RES}/assets/powers_unleashed/textures/suit/{suit}/'
    out = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    for p in ['pants', 'boots', ('chestplate_slim' if slim else 'chestplate'), 'mask']:
        f = d + f'{suit}_{p}.png'
        if os.path.exists(f):
            out.alpha_composite(Image.open(f).convert('RGBA'))
    os.makedirs(f'{HERE}/tex/assets/doll/textures', exist_ok=True)
    out.save(f'{HERE}/tex/assets/doll/textures/{suit}.png')
    return f'doll:{suit}'


def box(frm, to, uv0, size, tex, inflate=0.0):
    u, v = uv0
    w, h, d = size
    s = 0.25  # 64px texture -> 16 units
    f = [frm[i] - inflate for i in range(3)]
    t = [to[i] + inflate for i in range(3)]
    R = lambda a, b, c, e: [a * s, b * s, c * s, e * s]
    faces = {
        'north': {'uv': R(u + d, v + d, u + d + w, v + d + h), 'texture': tex},
        'east': {'uv': R(u, v + d, u + d, v + d + h), 'texture': tex},
        'south': {'uv': R(u + d + w + d, v + d, u + d + w + d + w, v + d + h), 'texture': tex},
        'west': {'uv': R(u + d + w, v + d, u + d + w + d, v + d + h), 'texture': tex},
        'up': {'uv': R(u + d + w, v + d, u + d, v), 'texture': tex},
        'down': {'uv': R(u + d + w + w, v, u + d + w, v + d), 'texture': tex},
    }
    return {'from': f, 'to': t, 'faces': faces}


def player_model(texref, slim=False, arm_out=0.0):
    a = 3 if slim else 4
    T = '#skin'
    el = []
    # legs
    el += [box([8, 0, 6], [12, 12, 10], (0, 16), (4, 12, 4), T), box([8, 0, 6], [12, 12, 10], (0, 32), (4, 12, 4), T, 0.25)]
    el += [box([4, 0, 6], [8, 12, 10], (16, 48), (4, 12, 4), T), box([4, 0, 6], [8, 12, 10], (0, 48), (4, 12, 4), T, 0.25)]
    # body
    el += [box([4, 12, 6], [12, 24, 10], (16, 16), (8, 12, 4), T), box([4, 12, 6], [12, 24, 10], (16, 32), (8, 12, 4), T, 0.25)]
    # arms
    el += [box([12, 12, 6], [12 + a, 24, 10], (40, 16), (a, 12, 4), T), box([12, 12, 6], [12 + a, 24, 10], (40, 32), (a, 12, 4), T, 0.25)]
    el += [box([4 - a, 12, 6], [4, 24, 10], (32, 48), (a, 12, 4), T), box([4 - a, 12, 6], [4, 24, 10], (48, 48), (a, 12, 4), T, 0.25)]
    # head
    el += [box([4, 24, 4], [12, 32, 12], (0, 0), (8, 8, 8), T), box([4, 24, 4], [12, 32, 12], (32, 0), (8, 8, 8), T, 0.5)]
    return {'textures': {'skin': texref}, 'elements': el}


def render(suit, size=256, yaw=200, pitch=12, slim=False):
    ref = skin_for(suit, slim)
    m = player_model(ref, slim)
    return blockrender.render(m, [f'{HERE}/tex'], size=size, yaw=yaw, pitch=pitch)


if __name__ == '__main__':
    im = render(sys.argv[1] if len(sys.argv) > 1 else 'spider_man', 256, float(sys.argv[2]) if len(sys.argv) > 2 else 200)
    im.save(f'{HERE}/doll_test.png')
