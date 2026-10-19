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
- `gh` works only through the REST API (`gh api ...`); GraphQL-based commands (`gh run list`, `gh workflow run`) fail.

## Content so far
- Suits: `suit/Suit` + `suit/Suits` (definitions), `item/SuitPieceItem` (armor-like item without a vanilla
  equipment asset, so vanilla does not draw it). Rendering in `client/suit/`: `SuitLayer` (added to player and
  mannequin renderers), `SuitModel` (player-skin-shaped model, one instance per piece), `SuitModelLayers`
  (inflate 0.05 suit / 0.1 boots, wide + slim), `SuitRendering.extract` (render-state modifier:
  copies suit pieces into the state — vanilla only copies real armor —, hides the skin's outer layer under
  worn pieces and the flat head item), `SuitFirstPersonRenderer` (first-person sleeve).
- Suit textures use the 64x64 player skin layout: `textures/suit/<suit>/<suit>.png`, `<suit>_slim.png`
  (3px arms), optional `<suit>_boots.png`.
