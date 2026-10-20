# Dev tools (Python 3 + Pillow + numpy)

- `blockrender.py` — renders vanilla-format JSON block models (isometric, MC face shading) for quick previews
  without starting the game. Needs vanilla textures in a folder with `assets/minecraft/textures/...`
  (get them with the `mc-reference.yml` workflow).
- `gen_table_textures.py`, `gen_table_models.py` — tailoring table concept textures/models
  (output: `docs/concepts/tailoring_table/assets`). Not used in the mod any more.
- `gen_fabrics.py <repo> <vanilla block textures dir>` — fabric icons (palette from vanilla wool), item models,
  recipes, `fabrics` tag.
- `gen_spider_textures.py <resources dir>` — placeholder Spider-Man suit textures.
- `gen_tech_table.py <out root>` — tech tailoring table concepts T1-T3 (not used any more).
- `gen_suit_creator.py <resources root>` — Suit Creator block model + textures (the block used in the mod).
- `gen_spider_variants.py <resources root>` — alternative Spider-Man suits (textures + item icons).
