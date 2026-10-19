package net.threetag.palladium.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.loading.FMLPaths;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.client.gui.screen.abilitybar.AbilityBarSprites;
import net.threetag.palladium.power.ability.AbilityColor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class AbilityBarTextureConverter implements ResourceManagerReloadListener {

    public static final Identifier ID = Palladium.id("ability_bar_texture_converter");
    public static final AbilityBarTextureConverter INSTANCE = new AbilityBarTextureConverter();

    private static final Path OUTPUT_FOLDER = FMLPaths.GAMEDIR.get().resolve("palladium").resolve("converted").resolve("ability_bars");
    private static final String TEXTURES_PREFIX = "textures/";
    private static final String SPRITES_PREFIX = "textures/gui/sprites/";
    private static final String SUFFIX = ".png";
    private static final Map<String, Region> REGIONS = new LinkedHashMap<>();

    static {
        REGIONS.put(AbilityBarSprites.POWER_INDICATOR_BOTTOM_RIGHT, new Region(0, 0, 52, 28));
        REGIONS.put(AbilityBarSprites.POWER_INDICATOR_BOTTOM_LEFT, new Region(52, 0, 52, 28));
        REGIONS.put(AbilityBarSprites.POWER_INDICATOR_TOP_RIGHT, new Region(0, 28, 52, 28));
        REGIONS.put(AbilityBarSprites.POWER_INDICATOR_TOP_LEFT, new Region(52, 28, 52, 28));
        REGIONS.put(AbilityBarSprites.ABILITY_LIST, new Region(0, 56, 24, 112));
        REGIONS.put(AbilityBarSprites.SINGLE_ABILITY, new Region(0, 168, 24, 24));
        REGIONS.put(AbilityBarSprites.SLOT_DISABLED, new Region(24, 56, 18, 18));
        REGIONS.put(AbilityBarSprites.SLOT_ENABLED, new Region(42, 56, 18, 18));
        REGIONS.put(AbilityBarSprites.SLOT_EMPTY, new Region(60, 56, 18, 18));
        REGIONS.put(AbilityBarSprites.SLOT_LOCKED, new Region(24, 74, 18, 18));
        REGIONS.put(AbilityBarSprites.SLOT_LOCK, new Region(42, 74, 18, 18));
        REGIONS.put(AbilityBarSprites.SLOT_COOLDOWN, new Region(60, 74, 18, 18));
        REGIONS.put(AbilityBarSprites.SWITCH_KEY_FORWARD, new Region(78, 56, 8, 8));
        REGIONS.put(AbilityBarSprites.SWITCH_KEY_BACKWARD, new Region(78, 64, 8, 8));
        REGIONS.put(AbilityBarSprites.KEY_MOUSE_LEFT, new Region(24, 92, 5, 7));
        REGIONS.put(AbilityBarSprites.KEY_MOUSE_RIGHT, new Region(29, 92, 5, 7));
        REGIONS.put(AbilityBarSprites.KEY_MOUSE_MIDDLE, new Region(34, 92, 5, 7));
        REGIONS.put(AbilityBarSprites.KEY_JUMP, new Region(39, 92, 10, 5));
        REGIONS.put(AbilityBarSprites.ENERGY_BAR, new Region(152, 0, 10, 112));
        REGIONS.put(AbilityBarSprites.ENERGY_BAR_FILL, new Region(162, 0, 4, 106));

        var colors = AbilityColor.values();
        for (int i = 0; i < colors.length; i++) {
            REGIONS.put(AbilityBarSprites.slotName(colors[i]), new Region(104 + 24 * (i / 8), 24 * (i % 8), 24, 24));
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        var textures = resourceManager.listResources("textures", id ->
                id.getPath().endsWith(SUFFIX) && id.getPath().contains("ability_bar") && !id.getPath().startsWith(SPRITES_PREFIX));

        for (Map.Entry<Identifier, Resource> entry : textures.entrySet()) {
            try {
                convert(entry.getKey(), entry.getValue());
            } catch (IOException e) {
                AddonPackLog.error(e, "Failed to convert legacy ability bar texture {}", entry.getKey());
            }
        }
    }

    private static void convert(Identifier id, Resource resource) throws IOException {
        try (InputStream inputStream = resource.open(); NativeImage image = NativeImage.read(inputStream)) {
            if (image.getWidth() < 166 || image.getHeight() < 192) {
                AddonPackLog.warning("Skipping ability bar texture {}: expected at least 166x192 pixels, got {}x{}", id, image.getWidth(), image.getHeight());
                return;
            }

            String name = id.getPath().substring(TEXTURES_PREFIX.length(), id.getPath().length() - SUFFIX.length());
            Path folder = OUTPUT_FOLDER.resolve(id.getNamespace()).resolve(name);
            Files.createDirectories(folder);

            for (Map.Entry<String, Region> entry : REGIONS.entrySet()) {
                Region region = entry.getValue();

                try (NativeImage sprite = new NativeImage(region.width(), region.height(), false)) {
                    for (int y = 0; y < region.height(); y++) {
                        for (int x = 0; x < region.width(); x++) {
                            sprite.setPixel(x, y, image.getPixel(region.x() + x, region.y() + y));
                        }
                    }

                    sprite.writeToFile(folder.resolve(entry.getKey() + SUFFIX));
                }
            }

            AddonPackLog.info("Converted legacy ability bar texture {} into sprites at {}. Move that folder to assets/{}/textures/gui/sprites/ and set \"ability_bar_textures\": \"{}:<folder>\" in the power renderer",
                    id, folder, id.getNamespace(), id.getNamespace());
        }
    }

    public record Region(int x, int y, int width, int height) {

    }
}
