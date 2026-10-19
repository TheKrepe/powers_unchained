package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ComboAttackAbility extends Ability {

    public static final int FINAL_ATTACK_TIME = 5;

    public static final MapCodec<ComboAttackAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("required_attacks").forGetter(a -> a.requiredAttacks),
                    Codec.FLOAT.fieldOf("bonus_damage").forGetter(a -> a.bonusDamage),
                    PalladiumCodecs.TIME.optionalFieldOf("reset_time", 60).forGetter(a -> a.resetTime),
                    Action.LIST_CODEC.optionalFieldOf("on_final_attack", Collections.emptyList()).forGetter(a -> a.onFinalAttack),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, ComboAttackAbility::new));

    private final int requiredAttacks;
    private final float bonusDamage;
    private final int resetTime;
    private final List<Action> onFinalAttack;

    public ComboAttackAbility(int requiredAttacks, float bonusDamage, int resetTime, List<Action> onFinalAttack, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.requiredAttacks = Math.max(requiredAttacks, 1);
        this.bonusDamage = bonusDamage;
        this.resetTime = resetTime;
        this.onFinalAttack = onFinalAttack;
    }

    public void onSwing(LivingEntity entity, AbilityInstance<?> instance, LivingEntity target) {
        int count = instance.getOrDefault(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), 0) + 1;

        if (count >= this.requiredAttacks) {
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), 0);
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_RESET_TIMER.get(), 0);
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_PENDING_TARGET.get(), target.getId());
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_FINAL_TIMER.get(), FINAL_ATTACK_TIME);

            if (!this.onFinalAttack.isEmpty()) {
                var context = DataContext.forAbility(entity, instance).with(DataContextKeys.TARGET, target);
                Action.runList(this.onFinalAttack, context);
            }
        } else {
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), count);
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_RESET_TIMER.get(), this.resetTime);
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_PENDING_TARGET.get(), -1);
        }
    }

    public static float consumeFinalBonus(AbilityInstance<?> instance, int damagedEntityId) {
        if (!(instance.getAbility() instanceof ComboAttackAbility ability)) {
            return 0F;
        }

        int pendingTarget = instance.getOrDefault(PalladiumDataComponents.Abilities.COMBO_ATTACK_PENDING_TARGET.get(), -1);

        if (pendingTarget == -1) {
            return 0F;
        }

        instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_PENDING_TARGET.get(), -1);
        return pendingTarget == damagedEntityId ? ability.bonusDamage : 0F;
    }

    @Nullable
    public static AbilityInstance<ComboAttackAbility> getTopPriorityInstance(LivingEntity entity) {
        return AbilityUtil.getEnabledInstances(entity, AbilitySerializers.COMBO_ATTACK.get()).stream().findFirst().orElse(null);
    }

    public static void resetCombo(AbilityInstance<?> instance) {
        if (instance.getOrDefault(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), 0) != 0) {
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), 0);
            instance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_RESET_TIMER.get(), 0);
        }
    }

    public static boolean isFinalAttackReady(AbilityInstance<?> instance) {
        if (!(instance.getAbility() instanceof ComboAttackAbility ability)) {
            return false;
        }

        return instance.getOrDefault(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), 0) >= ability.requiredAttacks - 1;
    }

    public static boolean isFinalAttackHappening(AbilityInstance<?> instance) {
        return instance.getAbility() instanceof ComboAttackAbility
                && instance.getOrDefault(PalladiumDataComponents.Abilities.COMBO_ATTACK_FINAL_TIMER.get(), 0) > 0;
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder components) {
        super.registerDataComponents(components);
        components.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), 0);
        components.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_RESET_TIMER.get(), 0);
        components.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_PENDING_TARGET.get(), -1);
        components.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_FINAL_TIMER.get(), 0);
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (!entity.level().isClientSide()) {
            int finalTimer = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.COMBO_ATTACK_FINAL_TIMER.get(), 0);
            if (finalTimer > 0) {
                abilityInstance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_FINAL_TIMER.get(), finalTimer - 1);
            }

            int resetTimer = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.COMBO_ATTACK_RESET_TIMER.get(), 0);
            if (resetTimer > 0) {
                if (resetTimer == 1) {
                    abilityInstance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_RESET_TIMER.get(), 0);
                    abilityInstance.set(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), 0);
                } else {
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.COMBO_ATTACK_RESET_TIMER.get(), resetTimer - 1);
                }
            }
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (!entity.level().isClientSide()) {
            abilityInstance.setSilently(PalladiumDataComponents.Abilities.COMBO_ATTACK_COUNT.get(), 0);
            abilityInstance.setSilently(PalladiumDataComponents.Abilities.COMBO_ATTACK_RESET_TIMER.get(), 0);
            abilityInstance.setSilently(PalladiumDataComponents.Abilities.COMBO_ATTACK_FINAL_TIMER.get(), 0);
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.COMBO_ATTACK.get();
    }

    public static class Serializer extends AbilitySerializer<ComboAttackAbility> {

        @Override
        public MapCodec<ComboAttackAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ComboAttackAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Tracks consecutive melee attacks and deals bonus damage on the final hit of the combo. Missing a swing or hitting a block resets the combo.")
                    .add("required_attacks", TYPE_INT, "The number of consecutive attacks needed to trigger the final, bonus-damage attack.")
                    .add("bonus_damage", TYPE_FLOAT, "Flat damage added to the final attack of the combo.")
                    .addOptional("reset_time", TYPE_TIME, "The amount of time, in ticks, the entity can go without attacking before the combo counter resets.", 60)
                    .addOptional("on_final_attack", TYPE_ACTION_LIST, "Action(s) run when the final attack of the combo lands.")
                    .addExampleObject(new ComboAttackAbility(3, 5F, 60, Collections.emptyList(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
