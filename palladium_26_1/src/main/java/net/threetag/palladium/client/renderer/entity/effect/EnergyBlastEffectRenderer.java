package net.threetag.palladium.client.renderer.entity.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.client.beam.BeamManager;
import net.threetag.palladium.client.renderer.entity.EffectEntityRenderer;
import net.threetag.palladium.entity.EffectEntity;
import net.threetag.palladium.entity.effect.EnergyBlastEffect;
import net.threetag.palladium.entity.effect.EntityEffects;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.BlastAbility;

import java.util.Objects;

public class EnergyBlastEffectRenderer implements EntityEffectRenderer<EnergyBlastEffect> {

    @SuppressWarnings("unchecked")
    @Override
    public void render(EnergyBlastEffect effect, EffectEntityRenderer.EffectEntityRenderState renderState, Entity anchor, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLightIn, boolean isFirstPerson, float partialTicks) {
        if (anchor instanceof AbstractClientPlayer player) {
            var ref = getAbilityReference(renderState.extraData);
            AbilityInstance<?> instance = ref.getInstance(player);

            if (instance != null && instance.getAbility() instanceof BlastAbility) {
                var ability = (AbilityInstance<BlastAbility>) instance;
                var blast = ability.getAbility().getBlast(ability);
                var beam = BeamManager.INSTANCE.get(ability.getAbility().beamId);

                if (blast == null || beam == null) {
                    return;
                }

                float travelled = ability.getAbility().travelledDistance(blast, player.level(), partialTicks);
                beam.submitPieceOnPlayer(player, renderState.position, blast.target(), travelled, ability.getAbility().length,
                        1F, poseStack, submitNodeCollector, packedLightIn, partialTicks);
            }
        }
    }

    public static AbilityReference getAbilityReference(CompoundTag tag) {
        return AbilityReference.parse(tag.getString("ability").orElse(""));
    }

    public static void start(Player player, AbilityReference abilityReference) {
        EffectEntity effectEntity = new EffectEntity(player.level(), player, EntityEffects.ENERGY_BLAST.value());
        EnergyBlastEffect.setAbilityReference(effectEntity, abilityReference);
        Objects.requireNonNull(Minecraft.getInstance().level).addEntity(effectEntity);
    }
}
