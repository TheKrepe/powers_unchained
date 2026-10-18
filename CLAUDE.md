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

## Building
The Claude sandbox usually cannot reach maven.neoforged.net / maven.fabricmc.net, so it cannot run Gradle.
CI (`.github/workflows/build.yml`) builds every push and uploads jars — check its result after pushing.
