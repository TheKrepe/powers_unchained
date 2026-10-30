package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.registry.PUDataComponents;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Looks (designs) of gadgets for the Tech Table's Customization tab: data component {@code powers_unchained:gadget_skin}
 * (missing = {@link #DEFAULT}). Looks are unlocked once per player (materials) in {@code TechProgress.skins}.
 *
 * @param id      skin id, unique per gadget
 * @param cost    materials to unlock it (null = unlocked by something else, see {@code hint})
 * @param hint    lang key of how it is unlocked (shown when {@code cost} is null)
 */
public record GadgetSkins(String id, @Nullable TechCost cost, @Nullable String hint) {

    public static final String DEFAULT = "default";

    /** Owner's looks: Advanced (Marvel's Spider-Man game), Brand New Day (Spider-Man: Brand New Day film). */
    public static final String ADVANCED = "advanced";
    public static final String BRAND_NEW_DAY = "brand_new_day";

    /**
     * Looks of the web-shooters (the first is the default). Each changes the wrists (textures/gadget/web_shooters_<id>)
     * AND the item icon (items/web_shooters.json selects on the component).
     */
    public static final List<GadgetSkins> WEB_SHOOTERS = List.of(
            new GadgetSkins(DEFAULT, null, null),
            new GadgetSkins(ADVANCED, TechCost.of(sup(Items.IRON_INGOT), 4, sup(Items.WHITE_DYE), 2, sup(Items.RED_DYE), 2), null),
            new GadgetSkins(BRAND_NEW_DAY, TechCost.of(sup(Items.IRON_INGOT), 4, sup(Items.BLUE_DYE), 2, sup(Items.RED_DYE), 2), null));

    private static java.util.function.Supplier<net.minecraft.world.item.Item> sup(net.minecraft.world.item.Item item) {
        return () -> item;
    }

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
