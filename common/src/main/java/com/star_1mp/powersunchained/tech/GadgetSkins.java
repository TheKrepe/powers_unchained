package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.registry.PUDataComponents;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Looks (designs) of gadgets for the Tech Table's Customization tab: data component {@code powers_unchained:gadget_skin}
 * (missing = {@link #DEFAULT}). For now every gadget only has the default look; new looks are added to {@link #WEB_SHOOTERS}
 * with how they are unlocked.
 *
 * @param id      skin id, unique per gadget
 * @param cost    materials to unlock it (null = unlocked by something else, see {@code hint})
 * @param hint    lang key of how it is unlocked (shown when {@code cost} is null)
 */
public record GadgetSkins(String id, @Nullable TechCost cost, @Nullable String hint) {

    public static final String DEFAULT = "default";

    /** Looks of the web-shooters (the first is the default). */
    public static final List<GadgetSkins> WEB_SHOOTERS = List.of(new GadgetSkins(DEFAULT, null, null));

    public Component displayName() {
        return Component.translatable("skin." + Constants.MOD_ID + ".web_shooters." + this.id);
    }

    public static String of(ItemStack stack) {
        return stack.getOrDefault(PUDataComponents.GADGET_SKIN.get(), DEFAULT);
    }

    public static void set(ItemStack stack, String skin) {
        if (DEFAULT.equals(skin)) {
            stack.remove(PUDataComponents.GADGET_SKIN.get());
        } else {
            stack.set(PUDataComponents.GADGET_SKIN.get(), skin);
        }
    }
}
