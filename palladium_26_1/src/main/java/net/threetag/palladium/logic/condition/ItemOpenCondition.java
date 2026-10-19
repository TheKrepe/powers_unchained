package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;

import java.util.Optional;

public record ItemOpenCondition(Optional<PlayerSlot> slot) implements Condition {

    public static final MapCodec<ItemOpenCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(PlayerSlot.CODEC.optionalFieldOf("slot").forGetter(ItemOpenCondition::slot))
            .apply(instance, ItemOpenCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOpenCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(PlayerSlot.STREAM_CODEC), ItemOpenCondition::slot,
            ItemOpenCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        if (this.slot.isPresent()) {
            var entity = context.getLivingEntity();

            if (entity == null) {
                return false;
            }

            for (ItemStack stack : this.slot.get().getItems(entity)) {
                if (stack.has(PalladiumDataComponents.Items.OPENED)) {
                    return Boolean.TRUE.equals(stack.get(PalladiumDataComponents.Items.OPENED));
                }
            }

            return false;
        } else {
            return context.getOrDefault(DataContextKeys.ITEM_OPENED, false);
        }
    }

    @Override
    public ConditionSerializer<ItemOpenCondition> getSerializer() {
        return ConditionSerializers.ITEM_OPEN.get();
    }

    public static class Serializer extends ConditionSerializer<ItemOpenCondition> {

        @Override
        public MapCodec<ItemOpenCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, ItemOpenCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Item Open")
                    .setDescription("Checks if an item is opened.")
                    .addOptional("slot", TYPE_PLAYER_SLOT, "Slot to check for. If none specified, it will use the slot from the current context (if given).")
                    .addExampleObject(new ItemOpenCondition(Optional.empty()))
                    .addExampleObject(new ItemOpenCondition(Optional.of(PlayerSlot.get(EquipmentSlot.CHEST))))
                    .addExampleObject(new ItemOpenCondition(Optional.of(PlayerSlot.getCuriosSlot("head"))));
        }
    }
}
