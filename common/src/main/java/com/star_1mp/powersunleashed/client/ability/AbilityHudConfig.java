package com.star_1mp.powersunleashed.client.ability;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.platform.Services;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.util.Mth;

/**
 * Player's ability HUD settings (client only), saved to {@code config/powers_unleashed-hud.json}. Edited in
 * {@link AbilityHudConfigScreen}; the defaults are the original layout.
 */
public final class AbilityHudConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE = Constants.MOD_ID + "-hud.json";

    public static final float MIN_SCALE = 0.5F;
    public static final float MAX_SCALE = 2.0F;
    public static final float MIN_OPACITY = 0.1F;

    /** The HUD is shown at all. */
    public boolean enabled = true;
    /** Ability names next to the keys (off = keys only). */
    public boolean showNames = true;
    /** Opacity of the text, 0.1 .. 1. */
    public float textOpacity = 1.0F;
    /** Size multiplier, 0.5 .. 2. */
    public float scale = 1.0F;
    /** Top-left corner as a fraction of the screen size; negative = default place (left edge, middle). */
    public float x = -1.0F;
    public float y = -1.0F;
    /** Order of the abilities on the keys (ability ids; missing ones in the default order), null = default. */
    public java.util.List<String> abilityOrder = null;
    /**
     * The player's own ability set (Ability Settings): one ability id per key, {@link AbilitySlots#NONE} = no ability;
     * null = automatic (every ability the player has, in the default order).
     */
    public java.util.List<String> abilitySlots = null;
    /** Selected ultimate ID, {@link AbilitySlots#NONE} for empty, or null for automatic. */
    public String ultimateAbility = null;

    /** Where the powers menu button sits in the inventory ({@code PowersMenu.ButtonPlace} id). */
    public String menuButton = "recipe_book";

    private static AbilityHudConfig instance;

    public static AbilityHudConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public java.util.List<String> order() {
        return this.abilityOrder != null ? this.abilityOrder : java.util.List.of();
    }

    public boolean hasCustomPosition() {
        return this.x >= 0.0F && this.y >= 0.0F;
    }

    public void reset() {
        AbilityHudConfig defaults = new AbilityHudConfig();
        this.enabled = defaults.enabled;
        this.showNames = defaults.showNames;
        this.textOpacity = defaults.textOpacity;
        this.scale = defaults.scale;
        this.x = defaults.x;
        this.y = defaults.y;
    }

    private void clamp() {
        this.textOpacity = Mth.clamp(this.textOpacity, MIN_OPACITY, 1.0F);
        this.scale = Mth.clamp(this.scale, MIN_SCALE, MAX_SCALE);
        if (this.hasCustomPosition()) {
            this.x = Mth.clamp(this.x, 0.0F, 1.0F);
            this.y = Mth.clamp(this.y, 0.0F, 1.0F);
        }
    }

    private static Path path() {
        return Services.PLATFORM.getConfigDir().resolve(FILE);
    }

    private static AbilityHudConfig load() {
        Path path = path();
        AbilityHudConfig config = null;
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                config = GSON.fromJson(reader, AbilityHudConfig.class);
            } catch (IOException | RuntimeException e) {
                Constants.LOG.warn("Could not read {}, using the default HUD settings", path, e);
            }
        }
        if (config == null) {
            config = new AbilityHudConfig();
        }
        config.clamp();
        return config;
    }

    public void save() {
        this.clamp();
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            Constants.LOG.warn("Could not save {}", path, e);
        }
    }
}
