package com.star_1mp.powersunchained.item;

import com.star_1mp.powersunchained.registry.PUDataComponents;
import com.star_1mp.powersunchained.suit.Suit;
import com.star_1mp.powersunchained.suit.SuitUnlocks;
import com.star_1mp.powersunchained.suit.Suits;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

/**
 * Suit fragment: a scrap of a suit's fabric with its data, found in chests. Scanned in the Suit Creator's scanner;
 * enough fragments of one suit unlock it. Which suit is stored in {@code powers_unchained:suit_fragment}; the name
 * stays hidden ("???") until that suit is unlocked.
 */
public class SuitFragmentItem extends Item {

    public SuitFragmentItem(Properties properties) {
        super(properties);
    }

    public static ItemStack of(Suit suit) {
        ItemStack stack = new ItemStack(com.star_1mp.powersunchained.registry.PUItems.SUIT_FRAGMENT.get());
        stack.set(PUDataComponents.SUIT_FRAGMENT.get(), suit.id());
        return stack;
    }

    public static @Nullable Suit suit(ItemStack stack) {
        if (!(stack.getItem() instanceof SuitFragmentItem)) {
            return null;
        }
        Identifier id = stack.get(PUDataComponents.SUIT_FRAGMENT.get());
        return id == null ? null : Suits.ALL.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        Suit suit = suit(stack);
        Player player = clientPlayer();
        if (suit != null && player != null && SuitUnlocks.isUnlocked(player, suit)) {
            builder.accept(Component.translatable("tooltip.powers_unchained.suit_fragment.known",
                    Component.translatable("suit_group.powers_unchained." + com.star_1mp.powersunchained.suit.SuitCatalog.info(suit).group()), Component.translatable(Suits.variantNameKey(suit)))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            builder.accept(Component.translatable("tooltip.powers_unchained.suit_fragment.unknown").withStyle(ChatFormatting.DARK_AQUA));
        }
        builder.accept(Component.translatable("tooltip.powers_unchained.suit_fragment.hint").withStyle(ChatFormatting.DARK_GRAY));
    }

    /** Set by the client: the local player (tooltips show the suit's name once that player unlocked it). */
    public static java.util.function.Supplier<@Nullable Player> localPlayer = () -> null;

    private static @Nullable Player clientPlayer() {
        return localPlayer.get();
    }
}
