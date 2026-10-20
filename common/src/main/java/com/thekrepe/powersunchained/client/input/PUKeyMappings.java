package com.thekrepe.powersunchained.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.thekrepe.powersunchained.Constants;
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

    public static final List<KeyMapping> ALL = List.of(ABILITY_1, ABILITY_2, ABILITY_3, ABILITY_4, ABILITY_5);

    private PUKeyMappings() {
    }

    private static KeyMapping key(int index, int defaultKey) {
        return new KeyMapping("key." + Constants.MOD_ID + ".ability_" + index, defaultKey, CATEGORY);
    }
}
