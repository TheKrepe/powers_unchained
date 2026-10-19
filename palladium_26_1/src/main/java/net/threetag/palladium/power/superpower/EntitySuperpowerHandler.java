package net.threetag.palladium.power.superpower;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.config.PalladiumServerConfig;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.power.Power;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class EntitySuperpowerHandler extends PalladiumEntityData<LivingEntity, EntitySuperpowerHandler> {

    // use CompoundTag codec as alternative, as MC opens non-existing tags as objects instead of arrays, causing the primary codec to scream
    public static final Codec<EntitySuperpowerHandler> CODEC = Codec.withAlternative(
            Power.HOLDER_CODEC.listOf().xmap(EntitySuperpowerHandler::new, s -> s.superpowers),
            CompoundTag.CODEC.xmap(compoundTag -> new EntitySuperpowerHandler(Collections.emptyList()), s -> new CompoundTag()));

    private final List<Holder<Power>> superpowers;

    public EntitySuperpowerHandler(List<Holder<Power>> superpowers) {
        this.superpowers = new ArrayList<>(superpowers);
    }

    @Override
    public Codec<EntitySuperpowerHandler> codec() {
        return CODEC;
    }

    @Override
    public void copyFrom(PalladiumEntityData<LivingEntity, EntitySuperpowerHandler> source) {
        if (source instanceof EntitySuperpowerHandler old) {
            this.superpowers.clear();
            this.superpowers.addAll(old.superpowers);
            this.markPowersDirty();
        }
    }

    public boolean hasFreeSuperpowerSlot() {
        return this.superpowers.stream().filter(h -> h.value().getParentId() == null).count() < PalladiumServerConfig.MAX_SUPERPOWER_SETS.get();
    }

    public boolean canBeAdded(Holder<Power> powerHolder) {
        var power = powerHolder.value();

        if (!this.superpowers.contains(powerHolder)) {
            if (power.getParentId() == null) {
                return this.hasFreeSuperpowerSlot();
            } else {
                return this.superpowers.stream().anyMatch(h -> h.unwrapKey().orElseThrow().identifier().equals(power.getParentId()));
            }
        }

        return false;
    }

    public boolean add(Holder<Power> powerHolder) {
        if (this.canBeAdded(powerHolder)) {
            this.superpowers.add(powerHolder);
            this.markPowersDirty();
            return true;
        } else {
            return false;
        }
    }

    public boolean remove(Holder<Power> powerHolder) {
        boolean removed = this.superpowers.remove(powerHolder);
        if (removed) this.markPowersDirty();
        return removed;
    }

    public boolean remove(Predicate<Holder<Power>> predicate) {
        int i = 0;

        for (Holder<Power> holder : this.getSuperpowers()) {
            if (predicate.test(holder)) {
                this.superpowers.remove(holder);
                i++;
            }
        }

        if (i > 0) this.markPowersDirty();
        return i > 0;
    }

    public void removeAll() {
        this.superpowers.clear();
        this.markPowersDirty();
    }

    private void markPowersDirty() {
        var powerHandler = PalladiumEntityData.get(this.getEntity(), PalladiumEntityDataTypes.POWER_HANDLER.get());
        if (powerHandler != null) {
            powerHandler.markDirty();
        }
    }

    public boolean has(Holder<Power> powerHolder) {
        return this.superpowers.contains(powerHolder);
    }

    public List<Holder<Power>> getSuperpowers() {
        return ImmutableList.copyOf(this.superpowers);
    }

}
