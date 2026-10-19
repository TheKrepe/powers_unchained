package net.threetag.palladium.power.provider;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.power.EntityPowerHandler;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.PowerCollector;
import net.threetag.palladium.power.PowerValidator;

import java.util.Objects;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class ItemPowerProvider extends PowerProvider {

    @SubscribeEvent
    static void onEquipmentChange(LivingEquipmentChangeEvent e) {
        var handler = PalladiumEntityData.get(e.getEntity(), PalladiumEntityDataTypes.POWER_HANDLER.get());
        if (handler != null) {
            handler.markDirty();
        }
    }

    @Override
    public void providePowers(LivingEntity entity, EntityPowerHandler handler, PowerCollector collector) {
        for (PlayerSlot slot : PlayerSlot.values(entity.level())) {
            for (int i = 0; i < slot.getSize(entity); i++) {
                var stack = slot.getItem(entity, i);

                if (stack.has(PalladiumDataComponents.Items.POWERS.get())) {
                    var itemPowers = stack.get(PalladiumDataComponents.Items.POWERS.get());
                    final int slotIndex = i;

                    for (Holder<Power> power : Objects.requireNonNull(itemPowers).forSlot(slot)) {
                        collector.addPower(power, PowerProviders.PRIORITY_ITEMS, () -> new Validator(slot, slotIndex, power));
                    }
                }
            }
        }
    }

    public record Validator(PlayerSlot slot, int index, Holder<Power> powerHolder) implements PowerValidator {

        @Override
        public boolean stillValid(LivingEntity entity, Holder<Power> power) {
            var itemPowers = this.slot.getItem(entity, this.index).get(PalladiumDataComponents.Items.POWERS.get());
            return itemPowers != null && itemPowers.contains(this.slot, this.powerHolder);
        }
    }

}
