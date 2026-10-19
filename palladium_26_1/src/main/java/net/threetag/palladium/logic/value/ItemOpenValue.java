package net.threetag.palladium.logic.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ItemOpenValue extends BooleanValue {

    public static final MapCodec<ItemOpenValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PlayerSlot.CODEC.optionalFieldOf("slot").forGetter(v -> v.slot),
            onTrueCodec(), onFalseCodec()
    ).apply(instance, ItemOpenValue::new));

    private final Optional<PlayerSlot> slot;

    public ItemOpenValue(Optional<PlayerSlot> slot, String onTrue, String onFalse) {
        super(onTrue, onFalse);
        this.slot = slot;
    }

    @Override
    public boolean getBoolean(DataContext context) {
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
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.ITEM_OPEN.get();
    }

    public static class Serializer extends BooleanSerializer<ItemOpenValue> {

        @Override
        public MapCodec<ItemOpenValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, ItemOpenValue> builder, HolderLookup.Provider provider) {
            builder.setName("Item Open").setDescription("Checks if an item was opened.")
                    .addOptional("slot", TYPE_PLAYER_SLOT, "Slot to check for. If none specified, it will use the slot from the current context (if given).")
                    .addExampleObject(new ItemOpenValue(Optional.empty(), "opened", "not_opened"))
                    .addExampleObject(new ItemOpenValue(Optional.of(PlayerSlot.get(EquipmentSlot.CHEST)), "opened", "not_opened"))
                    .addExampleObject(new ItemOpenValue(Optional.of(PlayerSlot.getCuriosSlot("head")), "opened", "not_opened"));
        }
    }
}
