package net.threetag.palladium.power.provider;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.item.suitset.EntitySuitSetHandler;
import net.threetag.palladium.item.suitset.SuitSet;
import net.threetag.palladium.item.suitset.SuitSetPowersManager;
import net.threetag.palladium.power.EntityPowerHandler;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.PowerCollector;
import net.threetag.palladium.power.PowerValidator;

public class SuitSetPowerProvider extends PowerProvider {

    @Override
    public void providePowers(LivingEntity entity, EntityPowerHandler handler, PowerCollector collector) {
        var suitSets = EntitySuitSetHandler.get(entity);

        for (SuitSet suitSet : suitSets.getSuitSets()) {
            HolderSet<Power> powers = SuitSetPowersManager.INSTANCE.getPowers(suitSet);

            if (powers != null) {
                for (Holder<Power> power : powers) {
                    collector.addPower(power, PowerProviders.PRIORITY_SUITS, () -> new Validator(suitSet));
                }
            }
        }
    }

    public record Validator(SuitSet suitSet) implements PowerValidator {

        @Override
        public boolean stillValid(LivingEntity entity, Holder<Power> power) {
            var handler = EntitySuitSetHandler.get(entity);
            return handler != null && handler.getSuitSets().contains(this.suitSet);
        }
    }
}
