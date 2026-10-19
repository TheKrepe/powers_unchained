package net.threetag.palladium.item.multiverse;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.Optional;
import java.util.function.Consumer;

public record ItemMultiversalVariant(ResourceKey<MultiversalItemVariant> variant) implements TooltipProvider {

    public static final Codec<ItemMultiversalVariant> CODEC = ResourceKey.codec(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT).xmap(ItemMultiversalVariant::new, ItemMultiversalVariant::variant);
    public static final StreamCodec<ByteBuf, ItemMultiversalVariant> STREAM_CODEC = ResourceKey.streamCodec(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT).map(ItemMultiversalVariant::new, ItemMultiversalVariant::variant);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
        MutableComponent tooltip = MultiversalItemVariant.getTitle(this.variant.identifier()).copy();

        if (context.registries() != null) {
            context.registries()
                    .get(this.variant)
                    .map(Holder.Reference::value)
                    .ifPresent(definition -> {
                                if (flag.hasShiftDown()) {
                                    tooltip.append(" (")
                                            .append(Universe.getTitle(definition.universe().identifier()));
                                    context.registries().get(definition.universe())
                                            .map(un -> un.value().description())
                                            .filter(Optional::isPresent)
                                            .ifPresent(desc -> {
                                                tooltip.append(" / ").append(desc.get());
                                            });
                                    tooltip.append(")");
                                }
                            }
                    );
        }

        consumer.accept(tooltip.withStyle(ChatFormatting.GOLD));
    }
}
