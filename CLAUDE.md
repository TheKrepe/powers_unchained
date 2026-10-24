# Powers Unchained — notes for Claude

Superhero mod for Minecraft **26.3**, NeoForge + Fabric, built from the official MultiLoader-Template (branch 26.3).
Owner: Star_1mp (GitHub account still TheKrepe — keep repo URLs as they are). Communicate with the owner in Russian.

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
- Mod id `powers_unchained`, base package `com.star_1mp.powersunchained`.
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
  Every run uploads the jars as the artifact `powers_unchained-jars` (zip). WIP builds are NOT releases (owner's
  decision) — never publish GitHub releases. The newest main jar is also force-pushed to branch **`ci-build`**
  (Claude cannot download artifacts; fetch the branch to send the jar/zip to the owner as a file).
- **Owner's rule:** end every reply that changes the mod with the link to the Actions run of the pushed commit
  (after it succeeded), where the `powers_unchained-jars` zip can be downloaded:
  `https://github.com/TheKrepe/powers_unchained/actions/runs/<run id>` (run id via
  `gh api "repos/TheKrepe/powers_unchained/actions/runs?head_sha=<sha>"`).
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
  pickaxe. Menu `menu/SuitCreatorMenu` (inventory slots + scan slot, `ViewSlot`s hidden per tab; scanning ticks in
  `broadcastChanges` once per game tick; buttons: sew = suit index*8 + piece (4 = all), unlock for XP = 4000 + index)
  + screen `client/suitcreator/SuitCreatorScreen` (416x236, drawn procedurally: vanilla panel + dark monitors; tabs
  Catalog / Scanner (owner removed the Dossier tab); tags popup; tag count + unlocked count in the top bar on every tab). Owner's edits:
  ONE "Теги" button + popup (no chip row, no crafting button), no ability tags, no subtitles under names, no
  "missing materials" text, big Sew panel, no scan slot on the Catalog tab, Hologram = `SuitHologram` (own AvatarRenderState, Steve/Alex skin, pose encoded
  in the state id `ID_BASE + pose`, applied from `PlayerModelMixin`). View prefs (tags, favorites, collapsed
  groups) in `config/powers_unchained-suit_creator.json` (`SuitCreatorPrefs`). Suit data: `suit/SuitCatalog`
  (`SuitInfo`: group, `SuitTag`s = owner's list Marvel / DC / First Appearance / Movies / DCU / DCEU / MCU / Games /
  Comics / Animation, stats, abilities, materials per piece, xpCost 20).
  ALL suits start locked (owner): unlock by scanning ANY piece of the suit in the scanner (100 ticks; durability doesn't matter,
  the piece stays; owner removed suit fragments completely) or for 20 XP levels. Locked suits are selectable in the catalog but
  their look stays hidden (owner): "???" group, lock icon, ghost hologram, generic part icons. Progress: `suit/SuitProgress` (set of unlocked ids) in NeoForge
  attachment `PUAttachments.SUIT_PROGRESS` (copyOnDeath, synced to self) via `IGameplayHelper.suitProgress/setSuitProgress`;
  toast `SuitUnlockToast` from `SuitCreatorClient.tick` diffing the synced set.
  Mouse buttons in 26.3 are SDL numbers: left = `InputConstants.MOUSE_BUTTON_LEFT` (1), right = 3 — never 0/1.
  `/suit unlock|lock <suit|all> [targets]`. GUI sprites: `tools/gen_suit_creator_gui.py` (hero emblem after
  the group name: `suit_creator/group/<group>` — every new hero group needs one).
  CI: `client-test.yml` input `start_tick` (JAVA_TOOL_OPTIONS -> `-Dpowers_unchained.ci.start`) skips earlier
  scenes; Suit Creator scenes start at 5760. Model + textures generated by `tools/gen_suit_creator.py`. Older concepts in
  `docs/concepts/tailoring_table`.
- Suit variants: `Suits.VARIANT_GROUPS` (Spider-Man: classic, First Appearance, Advanced 2.0, Brand New Day, Raimi),
  every variant is its own items (`<suit>_mask/_suit/_pants/_boots`, registered from `Suits.ALL` in `PUItems`).
  Suits creative tab lists only the first variant (others are search-only); `client/suit/SuitVariantScroller`
  swaps the whole hovered suit (all 4 pieces) on mouse wheel (NeoForge `ScreenEvent.MouseScrolled.Pre`) and adds
  a soft-blue "Variant n/m: name" tooltip line under the item name, only inside the Suits tab
  (open screen = `Minecraft.getInstance().gui.screen()` in 26.3). All suits share stats: 3/8/6/3 (full armor bar), no
  toughness, durability 18. Variant textures/icons: `tools/gen_spider_variants.py`.
- Command `/suit give <suit> [targets]` (`command/SuitCommand`, registered via NeoForge `RegisterCommandsEvent`): gives
  the full set, suit names tab-complete, permission level 2.
- Web-Shooters (`item/WebShootersItem`): worn in the Curios `bracelet` slot (data-only: `data/curios/tags/item/
  bracelet.json` + `data/powers_unchained/curios/entities`); drawn skin-style like a suit piece
  (`client/gadget/WebShootersModel`, PlayerModel at boots inflate 0.1 so it shows over a chestplate; hand-made
  textures `textures/gadget/web_shooters(_slim).png`) via `neoforge/.../compat/curios/CuriosClientCompat`
  (ICurioRenderer, only class-loaded with Curios). Curios = optional dep (`compileOnly` API + `localRuntime`,
  `curios_version`). Without Curios, web-shooters anywhere in the inventory count. Fabric later: Trinkets.
  Shooter pixels live on the arms' OUTER layer (`tools/web_shooters_to_outer_layer.py`) so skin sleeves don't hide them.
- Ability keys (`client/input/PUKeyMappings`, category "Powers Unchained"): Ability 1-5 = C, V, B, M, comma.
- Web cartridges (`web/WebCartridges`, `item/WebCartridgeItem`, `empty_web_cartridge`): web-shooters hold 2 cartridges in
  data component `powers_unchained:web_cartridges` (list of fluid ints, 0 = used up), loaded bundle-style
  (`WebShootersItem` overrideStacked*, like Powerborne Heroes' vial web-shooters) or by using a cartridge while wearing
  them; 128 shots per cartridge (`web_fluid` component, missing = full); creative shoots free; bundle-style tooltip
  `client/web/ClientWebShootersTooltip`. `IGameplayHelper.findAccessory` returns the live worn stack (Curios).
- Web Zip (ability 2, V): `entity/web/WebZipLine` (Fisk pull web: 48 blocks, cooldown 20, pulls to block 5 ticks /
  rams entity 10 ticks; pull applied on the shooter's own client), `network/WebZipPayload`, `WebShooting.zip`.
- Web Zip body animations (`client/web/WebZipAnimation`, our own poses in the spirit of Fisk / Powerborne / Marvel
  Legacy, no copied values): shoot, 3 pulls, 8 release flips, 3 dives, superhero landing; smoothstep keyframes,
  every phase fades; limbs via `PlayerModelMixin`, whole body via `LivingEntityRendererMixin` (after body yaw).
- Web swinging (ability 3, B = mode toggle, like Fisk; owner wants Fisk + Powerborne + Marvel Legacy feel): HOLD right
  click (empty hand) = shoot a rope (retried every 6 ticks while it misses, like Powerborne) and swing while held; let
  go of RMB (or LMB) = release with Powerborne's launch (+0.5 towards the look, lift towards the anchor height) and 40
  ticks of x1.05 horizontal momentum; jump = reel in 0.38/tick, sprint+W = slow reel; 40 ticks on the ground = rope
  dropped. `entity/web/WebSwingLine` (extends WebZipLine: hooks `clientTick` / `whileCaught`; velocity rope like
  Powerborne/ML: predict next pos, remove outward radial velocity, pull back 0.15 x overshoot, drag compensation 1.05,
  W pump 0.06 / strafe 0.03 towards the look, soft/hard speed caps 2.4/3.4, snaps at 10 blocks overshoot), run on the
  local client in `PlayerTickEvent.Pre` via `client/web/WebSwingClient.playerTick`; kind 0 both hands / 1 right / 2
  left picked like Fisk. `network/WebSwingPayload` (SHOOT / RELEASE / DROP), `WebShooting.swingShoot/swingRelease`.
  Swing and web shot modes are exclusive (both RMB). Animations in `WebZipAnimation` (calm air base pose that hides
  vanilla running limbs, hang along the rope via `hangAlongRope`, swing shoot, release tricks, dive, 3 landings only
  after real falls; NO springboard / extra jump animations — owner removed them). Loop sounds `client/web/WebSwingSound`
  (Fisk's whoosh / stress / wind sounds copied from the Fisk port). Rope end while hanging: third person = `WebZipAnimation.hangingWrist`
  (same body rotation + arm pose), first person = the aimed arms turned about the eye towards the anchor
  (`WebArmAnimation.firstPersonHangRotation`; NOTE the first person hands' pose space is the view turned 180° about Y,
  so view-space rotations are mirrored with (-x, y, -z, w) there). Web animations (calm pose, dive, landing) only run
  while "travelling" (a line out / flip / still airborne after one), never on ordinary jumps. Powerborne's swing script for reference:
  jar-reference `modrinth:AGvjxjAT:forge` -> `data/powerborne/kubejs_scripts/spiderman/web_swing.js`; Marvel Legacy:
  `util/SwingManager`.
- No fall damage after web travel: `WebShooting.markFallSafe` (zip, hanging, release) until landing; NeoForge
  `LivingFallEvent` is cancelled while `isFallSafe`.
- Recipes: empty cartridge = iron ingot / glass / iron ingot (column); web cartridge = shapeless empty cartridge + cobweb
  + potion of oozing (NeoForge `neoforge:components` ingredient — Fabric will need its own ingredient).
- Web cocoon: 5 sticky hits within 120 ticks -> `entity/web/WebCocoon` (60 ticks, `WebSlowness.hold` = speed/jump/fly
  -100%), `client/web/WebCocoonRenderer` (tiled box), first-person `client/web/WebOverlay` (head webs / cocoon).
- Webs aim from the wrist at the crosshair's target point (`AbstractWebProjectile.aimPoint`). Head webs lie on the
  head box faces and follow head yaw/pitch (`StickyWeb.isOnHead/headPivot`).
- Ability HUD `client/ability/AbilityHud` (providers per key slot; small vanilla-like key boxes at the left edge).
- `jar-reference.yml` (manual dispatch, inputs `url` + `patterns`) dumps another mod's jar (text files + decompiled
  matching classes) to branch `jar-ref-tmp`.
- Web Shot ability (ability 1 with web-shooters), ported from the owner's Fisk's Superheroes 26.3 port
  (`/home/claude/fiskheroes_26.3`, repo TheKrepe/fiskheroes_26.3, Fisk gave permission): `client/web/WebShooterClient`
  (tap = mode on/off, hold = menu, wheel while holding = web type, right click with EMPTY main hand = fire ->
  `network/ShootWebPayload` -> `web/WebShooting`), `web/WebAmmo` (Fisk equipment rules: 6 shots per type, one back
  every 40 ticks; server + client copies), `client/web/WebShooterHud`. Projectiles `entity/web/AbstractWebProjectile`
  (Fisk SHProjectile + AbstractEntityWeb physics; spawn data as synched entity data instead of NeoForge complex
  spawn), `ImpactWeb` (5 dmg x speed, resets hurt cooldown, bounces), `StickyWeb` (hooks to the hit entity via synced
  HIT_ENTITY, follows it, `web/WebSlowness` speed modifier). Renderer `client/web/WebProjectileRenderer` +
  `WebGeometry` (four-leaved web star, own textures `textures/entity/web/`). Arm animations = Fisk's `web_aim_*`/`web_shoot_*`
  .fsk scripts in Java (`client/web/WebArmAnimation`; data per entity in `web/WebShotTracker`, fed by projectiles
  appearing client-side; rapid fire alternates wrists -> both arms up): third person via
  `mixin/client/PlayerModelMixin` (PlayerModel.setupAnim RETURN, `AvatarRenderState.id`), first person via
  RenderArmEvent (HIGHEST priority, transforms the arm pose). Controls like Fisk's utility belt: hold key = wheel
  (`WebShooterHud`, port of HudElementUtilityBelt), release without scrolling = toggle; key hints after equipping. Sounds copied
  from the Fisk port (`sounds/web_shooters/`), damage type `powers_unchained:web`. Assets: `tools/gen_web_assets.py`.
- Loader services: `IRegistryHelper` (blocks, items, entity types, sounds, tabs), `IGameplayHelper` (accessory
  check, sendToServer). NeoForge glue ticks `WebShooting.tick()` + `WebSlowness.endTick()` on ServerTickEvent.Post.
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
