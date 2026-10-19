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

public class ItemOpenTimerValue extends IntegerValue{

    public static final MapCodec<ItemOpenTimerValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PlayerSlot.CODEC.optionalFieldOf("slot").forGetter(v -> v.slot),
            modifyFunctionCodec()
    ).apply(instance, ItemOpenTimerValue::new));

    private final Optional<PlayerSlot> slot;

    public ItemOpenTimerValue(Optional<PlayerSlot> slot, String molang) {
        super(molang);
        this.slot = slot;
    }

    @Override
    public int getInteger(DataContext context) {
        if (this.slot.isPresent()) {
            var entity = context.getLivingEntity();

            if (entity == null) {
                return 0;
            }

            for (ItemStack stack : this.slot.get().getItems(entity)) {
                if (stack.has(PalladiumDataComponents.Items.OPEN_TIMER)) {
                    return stack.getOrDefault(PalladiumDataComponents.Items.OPEN_TIMER, 0);
                }
            }

            return 0;
        } else {
            return context.getOrDefault(DataContextKeys.ITEM_OPEN_TIMER, 0);
        }
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.ITEM_OPEN_TIMER.get();
    }

    public static class Serializer extends IntSerializer<ItemOpenTimerValue> {

        @Override
        public MapCodec<ItemOpenTimerValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, ItemOpenTimerValue> builder, HolderLookup.Provider provider) {
            builder.setName("Item Open Timer").setDescription("Returns of the timer for the opening process of an item.")
                    .addOptional("slot", TYPE_PLAYER_SLOT, "Slot to check for. If none specified, it will use the slot from the current context (if given).")
                    .addExampleObject(new ItemOpenTimerValue(Optional.empty(), ""))
                    .addExampleObject(new ItemOpenTimerValue(Optional.of(PlayerSlot.get(EquipmentSlot.CHEST)), ""))
                    .addExampleObject(new ItemOpenTimerValue(Optional.of(PlayerSlot.getCuriosSlot("head")), ""));
        }
    }
}
