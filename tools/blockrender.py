"""Tiny Minecraft JSON block-model renderer (orthographic, z-buffered, MC face shading).

Usage as a module: render(model_path_or_dict, texture_roots, size, yaw_deg, y_rot) -> PIL.Image
"""
import json
import math
import os

import numpy as np
from PIL import Image

SHADE = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "east": 0.6, "west": 0.6}


def load_model(path_or_dict, model_roots):
    m = path_or_dict if isinstance(path_or_dict, dict) else json.load(open(path_or_dict))
    textures = dict(m.get("textures", {}))
    elements = m.get("elements")
    parent = m.get("parent")
    depth = 0
    while elements is None and parent and depth < 10:
        p = resolve(parent, model_roots, "models", ".json")
        if not p:
            break
        pm = json.load(open(p))
        for k, v in pm.get("textures", {}).items():
            textures.setdefault(k, v)
        elements = pm.get("elements")
        parent = pm.get("parent")
        depth += 1
    return elements or [], textures


def resolve(ref, roots, kind, ext):
    ns, _, path = ref.partition(":") if ":" in ref else ("minecraft", ":", ref)
    for r in roots:
        p = os.path.join(r, "assets", ns, kind, path + ext)
        if os.path.exists(p):
            return p
    return None


_tex_cache = {}


def texture(ref, textures, roots):
    seen = 0
    while ref.startswith("#") and seen < 10:
        ref = textures.get(ref[1:], "missing")
        seen += 1
    if ref not in _tex_cache:
        p = resolve(ref, roots, "textures", ".png")
        if p:
            im = Image.open(p).convert("RGBA")
            w, h = im.size
            im = im.crop((0, 0, w, w))  # animated textures: first frame
            _tex_cache[ref] = np.asarray(im).astype(np.float32)
        else:
            a = np.zeros((16, 16, 4), np.float32)
            a[..., 3] = 255
            a[::2, ::2, 0] = 255
            a[1::2, 1::2, 2] = 255
            _tex_cache[ref] = a
    return _tex_cache[ref]


def default_uv(face, f, t):
    if face == "north":
        return [16 - t[0], 16 - t[1], 16 - f[0], 16 - f[1]]
    if face == "south":
        return [f[0], 16 - t[1], t[0], 16 - f[1]]
    if face == "east":
        return [16 - t[2], 16 - t[1], 16 - f[2], 16 - f[1]]
    if face == "west":
        return [f[2], 16 - t[1], t[2], 16 - f[1]]
    if face == "up":
        return [f[0], f[2], t[0], t[2]]
    return [f[0], 16 - t[2], t[0], 16 - f[2]]


def face_corners(face, f, t):
    """origin (texture top-left), end of u axis, end of v axis."""
    x0, y0, z0 = f
    x1, y1, z1 = t
    if face == "north":
        return (x1, y1, z0), (x0, y1, z0), (x1, y0, z0)
    if face == "south":
        return (x0, y1, z1), (x1, y1, z1), (x0, y0, z1)
    if face == "east":
        return (x1, y1, z1), (x1, y1, z0), (x1, y0, z1)
    if face == "west":
        return (x0, y1, z0), (x0, y1, z1), (x0, y0, z0)
    if face == "up":
        return (x0, y1, z0), (x1, y1, z0), (x0, y1, z1)
    return (x0, y0, z1), (x1, y0, z1), (x0, y0, z0)


def rot_point(p, origin, axis, angle):
    a = math.radians(angle)
    c, s = math.cos(a), math.sin(a)
    x, y, z = p[0] - origin[0], p[1] - origin[1], p[2] - origin[2]
    if axis == "y":
        x, z = x * c + z * s, -x * s + z * c
    elif axis == "x":
        y, z = y * c - z * s, y * s + z * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return (x + origin[0], y + origin[1], z + origin[2])


def block_rot_y(p, deg):
    # blockstate "y": rotates the model clockwise seen from above (north -> east for 90)
    for _ in range((deg // 90) % 4):
        p = (16 - p[2], p[1], p[0])
    return p


FACE_DIR = {"north": "north", "south": "south", "east": "east", "west": "west", "up": "up", "down": "down"}
ROT_FACE = {"north": "east", "east": "south", "south": "west", "west": "north", "up": "up", "down": "down"}


def render(model, roots, size=512, yaw=225.0, pitch=30.0, y_rot=0, scale=None, bg=(0, 0, 0, 0), offset_y=0.0):
    elements, textures = load_model(model, roots)
    # camera
    yaw_r, pitch_r = math.radians(yaw), math.radians(pitch)
    fwd = np.array([math.sin(yaw_r) * math.cos(pitch_r), -math.sin(pitch_r), -math.cos(yaw_r) * math.cos(pitch_r)])
    fwd /= np.linalg.norm(fwd)
    up = np.array([0, 1.0, 0])
    right = np.cross(fwd, up)
    right /= np.linalg.norm(right)
    sup = np.cross(right, fwd)
    if scale is None:
        scale = size / 32.0
    center = np.array([8.0, 8.0 + offset_y, 8.0])

    color = np.zeros((size, size, 3), np.float32)
    color[...] = bg[:3]
    alpha = np.zeros((size, size), np.float32)
    zbuf = np.full((size, size), np.inf, np.float32)

    def proj(p):
        v = np.array(p, dtype=np.float64) - center
        return np.array([size / 2 + v.dot(right) * scale, size / 2 - v.dot(sup) * scale]), v.dot(fwd)

    for el in elements:
        f, t = el["from"], el["to"]
        rot = el.get("rotation")
        for fname, fd in el.get("faces", {}).items():
            o, ue, ve = face_corners(fname, f, t)
            pts = [o, ue, ve]
            if rot:
                pts = [rot_point(p, rot["origin"], rot["axis"], rot["angle"]) for p in pts]
            pts = [block_rot_y(p, y_rot) for p in pts]
            sp = [proj(p) for p in pts]
            p0, d0 = sp[0]
            pu, du = sp[1][0] - p0, sp[1][1] - d0
            pv, dv = sp[2][0] - p0, sp[2][1] - d0
            n3 = np.cross(np.array(pts[1]) - np.array(pts[0]), np.array(pts[2]) - np.array(pts[0]))
            if np.dot(n3, fwd) <= 0:  # back-face (normal points away from camera... sign check below)
                pass
            det = pu[0] * pv[1] - pu[1] * pv[0]
            if abs(det) < 1e-6:
                continue
            fdir = fname
            for _ in range((y_rot // 90) % 4):
                fdir = ROT_FACE[fdir]
            shade = SHADE[fdir]
            tex = texture(fd["texture"], textures, roots)
            th, tw = tex.shape[:2]
            uv = fd.get("uv", default_uv(fname, f, t))
            uvrot = fd.get("rotation", 0)
            xs = [p0[0], p0[0] + pu[0], p0[0] + pv[0], p0[0] + pu[0] + pv[0]]
            ys = [p0[1], p0[1] + pu[1], p0[1] + pv[1], p0[1] + pu[1] + pv[1]]
            xa, xb = max(int(math.floor(min(xs))), 0), min(int(math.ceil(max(xs))), size - 1)
            ya, yb = max(int(math.floor(min(ys))), 0), min(int(math.ceil(max(ys))), size - 1)
            if xa > xb or ya > yb:
                continue
            gx, gy = np.meshgrid(np.arange(xa, xb + 1) + 0.5, np.arange(ya, yb + 1) + 0.5)
            qx, qy = gx - p0[0], gy - p0[1]
            s = (qx * pv[1] - qy * pv[0]) / det
            tt = (pu[0] * qy - pu[1] * qx) / det
            inside = (s >= 0) & (s < 1) & (tt >= 0) & (tt < 1)
            if not inside.any():
                continue
            depth = d0 + s * du + tt * dv
            ss, ts = s, tt
            if uvrot == 90:
                ss, ts = tt, 1 - s
            elif uvrot == 180:
                ss, ts = 1 - s, 1 - tt
            elif uvrot == 270:
                ss, ts = 1 - tt, s
            u = uv[0] + ss * (uv[2] - uv[0])
            v = uv[1] + ts * (uv[3] - uv[1])
            ti = np.clip((u / 16.0 * tw).astype(int), 0, tw - 1)
            tj = np.clip((v / 16.0 * th).astype(int), 0, th - 1)
            texel = tex[tj, ti]
            sub = (slice(ya, yb + 1), slice(xa, xb + 1))
            vis = inside & (texel[..., 3] > 127) & (depth < zbuf[sub])
            if not vis.any():
                continue
            zbuf[sub][vis] = depth[vis]
            color[sub][vis] = texel[..., :3][vis] * shade
            alpha[sub][vis] = 255
    out = np.dstack([np.clip(color, 0, 255), alpha]).astype(np.uint8)
    img = Image.fromarray(out, "RGBA")
    if bg[3] > 0:
        back = Image.new("RGBA", img.size, bg)
        back.alpha_composite(img)
        img = back
    return img
