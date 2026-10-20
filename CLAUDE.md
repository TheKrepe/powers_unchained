# Powers Unchained — notes for Claude

Superhero mod for Minecraft **26.3**, NeoForge + Fabric, built from the official MultiLoader-Template (branch 26.3).
Owner: TheKrepe. Communicate with the owner in Russian.

## Current build target
**NeoForge only for now** (owner's decision): `include('fabric')` is commented out in `settings.gradle`.
Do not build or update the `fabric/` module until the owner asks to bring Fabric back. Still keep game logic
in `common/` and loader glue in `neoforge/`, so Fabric can be re-enabled later with minimal work.

## Layout
- `common/` — shared code, vanilla-only (compiled against NeoForm). Put ~all logic here.
- `neoforge/`, `fabric/` — thin loader layers: entry points, registration glue, events, networking glue.
- Loader-specific needs from common go through service interfaces in `common/.../platform/services`
  with implementations registered in each loader's `META-INF/services`.
- All versions live in `gradle.properties`. New expandable properties must also be added to `expandProps`
  in `build-logic/src/main/groovy/multiloader-common.gradle`.

## Conventions
- Mod id `powers_unchained`, base package `com.thekrepe.powersunchained`.
- Use `Constants.id("path")` for identifiers.
- Plan for future MC versions: keep version-sensitive vanilla API usage behind small helper classes, avoid
  mixins when a loader event exists, keep content data-driven (JSON) where practical.
- Prefer no hard third-party dependencies (GeckoLib, Curios, etc.); optional compat only.

## 26.x API notes (differs from 1.20/1.21 tutorials)
- Minecraft is unobfuscated, Mojang names everywhere; Java 25.
- `ResourceLocation` → `Identifier`; armor uses `Equippable` component + `EquipmentAssets`.
- Player data: NeoForge `AttachmentType`; packets: `CustomPacketPayload` + `PayloadRegistrar`.
- Player rendering: `AvatarRenderer` / `AvatarRenderState`, models submitted via `SubmitNodeCollector`.
- `GuiGraphics` → `GuiGraphicsExtractor`, `Screen#render` → `Screen#extractRenderState`.
- 26.3: input moved from GLFW to SDL (use `InputConstants`), features moved to `worldgen/feature`.
- Fabric uses Loom plugin `net.fabricmc.fabric-loom` (no remapping): `implementation`, not `modImplementation`.

## Building & testing (Claude has no local Gradle — everything goes through GitHub Actions)
The Claude sandbox cannot reach maven.neoforged.net / maven.fabricmc.net / Mojang, and cannot download
Actions logs/artifacts. Workarounds that push results to git branches (fetch them with
`git fetch -f origin <branch>:refs/remotes/origin/<branch>`):
- `build.yml` — builds every push. On failure the full Gradle log goes to branch **`ci-log`** (`build.log`).
  On push to main the NeoForge jar is published as the rolling pre-release **`dev`**; the owner's Prism Launcher
  pulls it before each launch with `tools/update-mod.ps1`.
- `client-test.yml` (manual dispatch via REST:
  `gh api -X POST repos/TheKrepe/powers_unchained/actions/workflows/client-test.yml/dispatches -f ref=main`)
  — generates a flat world with a dedicated server, runs the real client on Xvfb + software GL, and the in-mod
  driver `neoforge/.../client/dev/CiClientDriver` (inert without `-Dpowers_unchained.ci=true`) sets up scenes and
  logs `[CI-SHOT] name`; the workflow screenshots the display. Results go to branch **`ci-shots`**
  (`shots/*.png`, `client.log`, `client-latest.log`, crash reports). Use it to check rendering visually.
- `mc-reference.yml` (manual dispatch with input `patterns` = space-separated path regexes) — decompiles matching
  Minecraft classes (Vineflower) / copies NeoForge sources and vanilla assets to branch **`mc-ref-tmp`**.
  Use it to look up exact 26.3 APIs instead of guessing. The branch is force-overwritten each run; Claude cannot
  delete branches through the proxy (the owner can).
- `external-reference.yml` (manual dispatch, input `repos` = `name=url[#branch] ...`) — clones other mods'
  repos (text files only) to branch **`ext-ref-tmp`** for study. git.threetag.net / gitlab.com are not reachable
  from the sandbox directly. Last dump: Palladium (1.20 + `26.1/dev`), PantheonSent, PymTech (1.16.4),
  SpeedsterHeroes (1.11.2), HeroesExpansion / LucraftCore (1.12.2).
- `unpack-upload.yml` — the owner's uploads (rar/zip) can't be opened in the sandbox (no unrar, pip/npm
  registries unreachable): push the archive to branch `upload-tmp`, dispatch this workflow, read the JPEGs from
  `upload-unpacked-tmp`.
- Python tools for previews/asset generation are in `tools/` (block model renderer, texture generators).
- 26.3 block model elements support `"light_emission": 0-15` (glowing parts) and NeoForge `"render_type"`.
- `gh` works only through the REST API (`gh api ...`); GraphQL-based commands (`gh run list`, `gh workflow run`) fail.

## Content so far
- Suits: `suit/Suit` + `suit/Suits` (definitions), `item/SuitPieceItem` (armor-like item without a vanilla
  equipment asset, so vanilla does not draw it). Rendering in `client/suit/`: `SuitLayer` (added to player and
  mannequin renderers), `SuitModel` (player-skin-shaped model, one instance per piece), `SuitModelLayers`
  (inflate 0.05 suit / 0.1 boots, wide + slim), `SuitRendering.extract` (render-state modifier:
  copies suit pieces into the state — vanilla only copies real armor —, hides the skin's outer layer under
  worn pieces and the flat head item), `SuitFirstPersonRenderer` (first-person sleeve).
- Fabrics: `<color>_fabric` for all 16 `DyeColor`s (`PUItems.FABRICS`), tag `powers_unchained:fabrics`;
  recipes 2 wool + stick -> 4, dye + fabric -> recolor (generated by `tools/gen_fabrics.py`).
- Suit Creator (`block/SuitCreatorBlock`, id `suit_creator`): Fisk's Superheroes-inspired steel machine (corner
  pillars, recessed body, glowing amber vent / data screen / side lights via element `light_emission`), metal,
  pickaxe, no menu yet. Model + textures generated by `tools/gen_suit_creator.py`. Older concepts in
  `docs/concepts/tailoring_table`.
- Suit variants: `Suits.VARIANT_GROUPS` (Spider-Man: classic, First Appearance, Advanced 2.0, Brand New Day, Raimi),
  every variant is its own items (`<suit>_mask/_suit/_pants/_boots`, registered from `Suits.ALL` in `PUItems`).
  Suits creative tab lists only the first variant (others are search-only); `client/suit/SuitVariantScroller`
  swaps the whole hovered suit (all 4 pieces) on mouse wheel (NeoForge `ScreenEvent.MouseScrolled.Pre`) and adds
  a "Variant n/m: name" tooltip line under the item name. All suits share stats: 3/8/6/3 (full armor bar), no
  toughness, durability 18. Variant textures/icons: `tools/gen_spider_variants.py`.
- Command `/suit <suit> [targets]` (`command/SuitCommand`, registered via NeoForge `RegisterCommandsEvent`): gives
  the full set, suit names tab-complete, permission level 2.
- Web-Shooters (`item/WebShootersItem`): worn in the Curios `bracelet` slot (data-only: `data/curios/tags/item/
  bracelet.json` + `data/powers_unchained/curios/entities`); drawn skin-style like a suit piece
  (`client/gadget/WebShootersModel`, PlayerModel at boots inflate 0.1 so it shows over a chestplate; hand-made
  textures `textures/gadget/web_shooters(_slim).png`) via `neoforge/.../compat/curios/CuriosClientCompat`
  (ICurioRenderer, only class-loaded with Curios). Curios = optional dep (`compileOnly` API + `localRuntime`,
  `curios_version`). Without Curios, web-shooters anywhere in the inventory count. Fabric later: Trinkets.
  `web_cartridge` item (future ammo, no recipe yet).
- Ability keys (`client/input/PUKeyMappings`, category "Powers Unchained"): Ability 1-5 = C, V, B, M, comma.
- Web Shot ability (ability 1 with web-shooters): `client/web/WebShooterClient` (tap = mode on/off, hold = menu,
  wheel while holding = web type, right click while on = fire -> `network/ShootWebPayload` -> `web/WebShooting`,
  cooldown on the web-shooters item), `client/web/WebShooterHud` (GUI layer). `web/WebType` IMPACT (damage) /
  STICKY (slowness + `IGameplayHelper.setWebbedUntil`, a synced NeoForge attachment). Projectile
  `entity/WebProjectile` (ThrowableItemProjectile, rendered with ThrownItemRenderer from items impact_web /
  sticky_web). Webbed visuals: `client/web/WebbedRendering` (layer on every LivingEntityRenderer, state flag via
  NeoForge render data; first-person arm; screen overlay GUI layer). Assets: `tools/gen_web_assets.py`.
- Loader services: `IRegistryHelper` (blocks, items, entity types, tabs), `IGameplayHelper` (accessory check,
  webbed data, sendToServer).
- Recipe unlocks: `data/powers_unchained/advancement/recipes/...` (Suit Creator unlocks with the copper block recipe).
- Chest pieces are `<suit>_chestplate` (renamed from `_suit`).
- Creative tabs (`PUCreativeTabs`): Suits, Weapons, Items, Blocks;
  ordered with `withTabsBefore`. Tab contents go through `IRegistryHelper.TabOutput` (search-only flag).
- Armor stands: `SuitArmorStandLayer` + `SuitArmorStandModel` (player-skin UV mesh on armor stand pivots, posed like
  vanilla armor; small stands via `HumanoidModel.BABY_TRANSFORMER`); `SuitRendering.extractEquipment` registered
  as a render state modifier for `ArmorStandRenderer`.
- Suit textures: one 64x64 player-skin-layout texture per piece, `textures/suit/<suit>/<suit>_mask.png`,
  `_chestplate.png`, `_chestplate_slim.png` (3px arms), `_pants.png`, `_boots.png` (`tools/suit_textures.py`
  splits a full texture; the generators call it).

## Lessons from Palladium 5 (`26.1/dev`, NeoForge 26.1.2, closest reference to our target)
- Abilities are codec-based (`Codec` dispatch by registry), not property bags. State is split into
  `unlocking` (condition / buyable) and `enabling` (condition / keybind handler) handlers.
- Per-ability runtime state lives in a `PatchedDataComponentMap` on the ability instance; changes are synced to
  tracking players as `DataComponentPatch` packets. Good pattern for our own ability state.
- Conditions/values support MoLang; actions + triggers + costs are separate registries.
- Suits/armor geometry comes from Bedrock-format JSON model layers (Blockbench-editable), wide + slim variants;
  tight suit inflate: base 0.1, overlay 0.35. Render layers copy poses from the parent model by part name and
  snapshot them because model submission is deferred.
- Skin overlay under a suit can be *shrunk* to 0.9 scale (`shrink_player_parts`) instead of hidden, so the
  skin's outer layer stays visible through transparent suit pixels — consider offering both modes.
- Web swinging is implemented as a flight type (`SwingingFlightType`, phases: swing/release/dive/landing,
  client-authoritative for the local player). Other built-ins: wall climbing, grapple, combo attack, multi-jump,
  carry, guard, slide, beams, trails, screen shake, customization (cosmetics), power dampening.
- Palladium 5 is NeoForge-only and depends on player-animation-lib, GeckoLib 5, Curios — all exist for 26.1.
