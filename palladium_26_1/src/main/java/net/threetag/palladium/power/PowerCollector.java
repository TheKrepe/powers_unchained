package net.threetag.palladium.power;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PowerCollector {

    private final LivingEntity entity;
    private final EntityPowerHandler handler;
    private final Map<Identifier, PowerInstance> toRemove;
    private final List<PowerInstanceCache> powerInstances = new ArrayList<>();

    public PowerCollector(LivingEntity entity, EntityPowerHandler handler, Map<Identifier, PowerInstance> toRemove) {
        this.entity = entity;
        this.handler = handler;
        this.toRemove = toRemove;
    }

    public void addPower(Holder<Power> power, int priority, Supplier<PowerValidator> validatorSupplier) {
        if (power == null) {
            return;
        }

        var powerId = power.unwrapKey().orElseThrow().identifier();
        PowerInstance found = this.toRemove.get(powerId);

        if (found != null) {
            found.switchValidator(validatorSupplier.get());
            found.setPriority(priority);
            this.toRemove.remove(powerId);
            return;
        }

        if (!this.handler.hasPower(powerId)) {
            this.powerInstances.add(new PowerInstanceCache(power, validatorSupplier.get(), priority));
        } else {
            this.handler.getPowerInstance(powerId).setPriority(priority);
        }
    }

    public List<PowerInstanceCache> getAdded() {
        return this.powerInstances;
    }

    public record PowerInstanceCache(Holder<Power> power, PowerValidator validator, int priority) {

        public PowerInstance make(LivingEntity entity, CompoundTag compoundTag) {
            return new PowerInstance(entity, this.power, this.validator, this.priority, compoundTag.getCompoundOrEmpty(this.power.unwrapKey().orElseThrow().identifier().toString()));
        }

    }
}
