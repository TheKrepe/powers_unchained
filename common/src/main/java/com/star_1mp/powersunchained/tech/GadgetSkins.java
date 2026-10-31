package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.registry.PUDataComponents;
import com.star_1mp.powersunchained.suit.Suit;
import com.star_1mp.powersunchained.suit.SuitUnlocks;
import com.star_1mp.powersunchained.suit.Suits;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Looks (designs) of gadgets for the Tech Table's Customization tab: data component {@code powers_unchained:gadget_skin}
 * (missing = {@link #DEFAULT}). Looks are bought PER GADGET (owner, like modules): component
 * {@code powers_unchained:unlocked_gadget_skins}; some also need a suit unlocked in the Suit Creator.
 *
 * @param id      skin id, unique per gadget
 * @param cost    materials to unlock it (null = unlocked by something else, see {@code hint})
 * @param hint    lang key of how it is unlocked (shown when {@code cost} is null)
 * @param requiredSuit task besides the cost (owner): this suit must be unlocked in the Suit Creator, or null
 */
public record GadgetSkins(String id, @Nullable TechCost cost, @Nullable String hint, @Nullable Suit requiredSuit) {

    public static final String DEFAULT = "default";

    /** Owner's looks: Advanced (Marvel's Spider-Man game), Brand New Day (Spider-Man: Brand New Day film). */
    public static final String ADVANCED = "advanced";
    public static final String BRAND_NEW_DAY = "brand_new_day";

    /**
     * Looks of the web-shooters (the first is the default). Each changes the wrists (textures/gadget/web_shooters_ID)
     * AND the item icon (items/web_shooters.json selects on the component).
     */
    public static final List<GadgetSkins> WEB_SHOOTERS = List.of(
            new GadgetSkins(DEFAULT, null, null, null),
            new GadgetSkins(ADVANCED, TechCost.of(sup(Items.IRON_INGOT), 4, fabric(DyeColor.WHITE), 2, fabric(DyeColor.RED), 2), null,
                    Suits.SPIDER_MAN_ADVANCED_2),
            new GadgetSkins(BRAND_NEW_DAY, TechCost.of(sup(Items.IRON_INGOT), 4, fabric(DyeColor.BLUE), 2, fabric(DyeColor.RED), 2), null,
                    Suits.SPIDER_MAN_BRAND_NEW_DAY));

    private static java.util.function.Supplier<net.minecraft.world.item.Item> fabric(DyeColor color) {
        return () -> com.star_1mp.powersunchained.registry.PUItems.FABRICS.get(color).get();
    }

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

    /** Whether this look is bought on this gadget (the default always is; creative web-shooters have every look). */
    public boolean isUnlocked(ItemStack gadget) {
        return DEFAULT.equals(this.id) || com.star_1mp.powersunchained.item.WebShootersItem.isCreative(gadget)
                || gadget.getOrDefault(PUDataComponents.UNLOCKED_SKINS.get(), List.<String>of()).contains(this.id);
    }

    public void unlock(ItemStack gadget) {
        java.util.List<String> list = new java.util.ArrayList<>(gadget.getOrDefault(PUDataComponents.UNLOCKED_SKINS.get(), List.<String>of()));
        if (!list.contains(this.id)) {
            list.add(this.id);
        }
        gadget.set(PUDataComponents.UNLOCKED_SKINS.get(), List.copyOf(list));
    }

    /** The task besides the cost: the required suit is unlocked (Suit Creator). */
    public boolean requirementMet(net.minecraft.world.entity.player.Player player) {
        return this.requiredSuit == null || SuitUnlocks.progress(player).isUnlocked(this.requiredSuit);
    }
}
