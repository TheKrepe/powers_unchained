package com.star_1mp.powersunchained.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.star_1mp.powersunchained.Constants;
import java.util.List;
import net.minecraft.client.KeyMapping;

/**
 * Ability keys, in their own "Powers Unchained" category of the controls screen. What a key does depends on the
 * player's suit / powers / gadgets; without any, the keys do nothing.
 */
public final class PUKeyMappings {

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Constants.id("abilities"));

    public static final KeyMapping ABILITY_1 = key(1, InputConstants.KEY_C);
    public static final KeyMapping ABILITY_2 = key(2, InputConstants.KEY_V);
    public static final KeyMapping ABILITY_3 = key(3, InputConstants.KEY_B);
    public static final KeyMapping ABILITY_4 = key(4, InputConstants.KEY_M);
    public static final KeyMapping ABILITY_5 = key(5, InputConstants.KEY_COMMA);

    /** The ability keys, in HUD order. */
    public static final List<KeyMapping> ALL = List.of(ABILITY_1, ABILITY_2, ABILITY_3, ABILITY_4, ABILITY_5);

    /** Opens the HUD settings. */
    public static final KeyMapping HUD_SETTINGS = new KeyMapping("key." + Constants.MOD_ID + ".hud_settings", InputConstants.KEY_H, CATEGORY);

    /** Hold to show the ability wheel (switch superpowers on / off). */
    public static final KeyMapping ABILITY_WHEEL = new KeyMapping("key." + Constants.MOD_ID + ".ability_wheel", InputConstants.KEY_R, CATEGORY);

    /** Dodge roll on the ground, front flip in the air. */
    public static final KeyMapping ROLL = new KeyMapping("key." + Constants.MOD_ID + ".roll", InputConstants.KEY_Z, CATEGORY);

    /** Every key of the mod, to register. */
    public static final List<KeyMapping> REGISTER = List.of(ABILITY_1, ABILITY_2, ABILITY_3, ABILITY_4, ABILITY_5, ABILITY_WHEEL, ROLL,
            HUD_SETTINGS);

    private PUKeyMappings() {
    }

    private static KeyMapping key(int index, int defaultKey) {
        return new KeyMapping("key." + Constants.MOD_ID + ".ability_" + index, defaultKey, CATEGORY);
    }
}
