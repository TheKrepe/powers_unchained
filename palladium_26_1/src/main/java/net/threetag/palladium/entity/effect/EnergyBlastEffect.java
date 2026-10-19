package net.threetag.palladium.entity.effect;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.client.beam.BeamManager;
import net.threetag.palladium.entity.EffectEntity;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.BlastAbility;

public class EnergyBlastEffect extends EntityEffect {

    private static final int WAIT_FOR_BLAST_TICKS = 20;

    @SuppressWarnings("unchecked")
    @Override
    public void tick(EffectEntity entity, Entity anchor) {
        if (entity.level().isClientSide() && anchor instanceof Player player) {
            var ref = getAbilityReference(entity.getExtraData());
            AbilityInstance<?> instance = ref.getInstance(player);

            if (instance != null && instance.getAbility() instanceof BlastAbility) {
                var ability = (AbilityInstance<BlastAbility>) instance;
                var beam = BeamManager.INSTANCE.get(ability.getAbility().beamId);
                var blast = ability.getAbility().getBlast(ability);

                if (beam == null) {
                    this.stopPlaying(entity);
                    return;
                }

                if (blast == null) {
                    if (entity.tickCount > WAIT_FOR_BLAST_TICKS) {
                        this.stopPlaying(entity);
                    }

                    return;
                }

                if (ability.getAbility().hasPassedTarget(blast, entity.level(), player.getEyePosition().distanceTo(blast.target()), 1F)) {
                    this.stopPlaying(entity);
                }
            } else {
                this.stopPlaying(entity);
            }
        } else {
            this.stopPlaying(entity);
        }
    }

    public static AbilityReference getAbilityReference(CompoundTag tag) {
        return AbilityReference.parse(tag.getString("ability").orElse(""));
    }

    public static void setAbilityReference(EffectEntity entity, AbilityReference abilityReference) {
        entity.changeExtraData(compound -> compound.putString("ability", abilityReference.toString()));
    }
}
