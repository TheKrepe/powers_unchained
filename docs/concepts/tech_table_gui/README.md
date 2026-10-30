# Tech Table GUI concepts (not implemented yet)

## v2 — holographic research tree (`v2/`, current proposal)
In the spirit of Fisk's Superheroes' Suit Database (full-screen teal hologram, hexagonal nodes joined by lines, a
sliding sidebar), drawn by us: `v2/tech_table_v2_1_tree.png` (tree + node description: objectives, price, ИЗУЧИТЬ),
`v2/tech_table_v2_2_assembly.png` (sidebar flipped to assembly: hologram of the gadget, parts have/need, СОБРАТЬ),
`v2/tech_table_v2_3_research.png` (search highlights + the unlock animation). Generator: `src/tech_screens_v2.py`.
Fisk's palette for reference (FabricatorColors): BG #466B73 -> #20282A, text #ADF4FF, ready #A1D69A/#477741,
locked #364447, search highlight #F6E087.

## v1 — workbench with tabs (kept on purpose, may come back)
`tech_table_1_blueprints.png`, `_2_modules.png`, `_3_research.png` — vanilla panel, blueprint-paper centre, copper
accents. Generator: `src/tech_screens.py`.

Both use the Suit Creator mockup kit `../suit_creator_gui/src/mc_ui.py` (vanilla font + sprites); invented part icons
in `src/parts.py`. Run: `python3 src/tech_screens_v2.py v2` / `python3 src/tech_screens.py .`
