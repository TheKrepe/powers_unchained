package net.threetag.palladium.item.upgrade;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.function.Consumer;
import java.util.stream.Stream;

public record ItemUpgrades(HolderSet<ItemUpgrade> upgrades) implements TooltipProvider {

    public static final ItemUpgrades EMPTY = new ItemUpgrades(HolderSet.empty());
    private static final String TOOLTIP_HEADER_KEY = Util.makeDescriptionId("item", Palladium.id("upgrades"));

    public static final Codec<ItemUpgrades> CODEC = RegistryCodecs.homogeneousList(PalladiumRegistryKeys.ITEM_UPGRADE)
            .xmap(ItemUpgrades::new, ItemUpgrades::upgrades);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemUpgrades> STREAM_CODEC = ByteBufCodecs.holderSet(PalladiumRegistryKeys.ITEM_UPGRADE)
            .map(ItemUpgrades::new, ItemUpgrades::upgrades);

    public boolean has(Holder<ItemUpgrade> upgrade) {
        return this.upgrades.contains(upgrade);
    }

    public ItemUpgrades with(Holder<ItemUpgrade> upgrade) {
        return new ItemUpgrades(HolderSet.direct(Stream.concat(this.upgrades.stream(), Stream.of(upgrade)).toList()));
    }

    public static ItemUpgrades get(ItemStack stack) {
        return stack.getOrDefault(PalladiumDataComponents.Items.ITEM_UPGRADES.get(), EMPTY);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        if (this.upgrades.size() == 0) {
            return;
        }

        consumer.accept(Component.translatable(TOOLTIP_HEADER_KEY).withStyle(ChatFormatting.GRAY));
        for (Holder<ItemUpgrade> holder : this.upgrades) {
            holder.unwrapKey().ifPresent(key ->
                    consumer.accept(CommonComponents.space().append(ItemUpgrade.getName(key)).withStyle(ChatFormatting.BLUE)));
        }
    }

}
