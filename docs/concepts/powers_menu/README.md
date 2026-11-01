# Powers menu concepts (not implemented yet)

The menu of abilities and progression for EVERY hero of the mod (Spider-Man first, other heroes later). Style = our
blocks (Suit Creator / Tech Table): vanilla panel, dark monitors, copper + cyan accents, 416x236. Layout idea from Alien
Evolution: the player stands in the middle as a hologram, everything else is arranged around them.

- `powers_1_abilities.png` — tab **Силы**: left = the player's powers / suits / gadgets (search, no categories; future
  heroes appear by themselves); centre = the player hologram, every ability is a round socket wired to the body part it
  comes from (sense -> head, web -> wrist, roll -> legs), level pips under it, on / off / locked; under it the power's
  level bar; right = the picked ability (level, description, key, wheel, on/off, "to progression").
- `powers_2_progression.png` — tab **Прокачка**: the player in rings, the power's branches (Web / Agility / Sense)
  radiate out, tiers I-III; right = the picked node: tasks (✔/✘, like the Tech Table), price in power points (ОС,
  top-right counter), required power level, "Изучить".
- `powers_3_keys.png` — tab **Клавиши**: abilities on the left, dragged onto the 5 key sockets under the hologram;
  right = how the HUD will look, "Automatic", HUD settings.
- `powers_4_button.png` — the inventory button (our emblem, 20x18 like the recipe book button) and its places:
  1 by the recipe book (default; creative: on the inventory tab, right of the armour), 2 right of the inventory,
  3 left of the inventory, 4 hidden (key J only). Implemented already (Interface Settings).

Generator: `src/powers_screens.py` (uses `../suit_creator_gui/src/mc_ui.py` + its doll renderer):
`python3 src/powers_screens.py .`
