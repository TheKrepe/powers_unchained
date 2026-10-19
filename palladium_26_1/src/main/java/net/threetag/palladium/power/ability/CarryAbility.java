package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.attachment.PalladiumAttachments;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CarryAbility extends Ability {

    public static final MapCodec<CarryAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("range").forGetter(a -> a.range),
                    Codec.DOUBLE.optionalFieldOf("carry_distance", 2.0).forGetter(a -> a.carryDistance),
                    Codec.DOUBLE.optionalFieldOf("carry_height_offset", 0.0).forGetter(a -> a.carryHeightOffset),
                    Codec.BOOL.optionalFieldOf("throwable", false).forGetter(a -> a.throwable),
                    Codec.DOUBLE.optionalFieldOf("throw_strength", 1.5).forGetter(a -> a.throwStrength),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, CarryAbility::new));

    private final double range;
    private final double carryDistance;
    private final double carryHeightOffset;
    private final boolean throwable;
    private final double throwStrength;

    public CarryAbility(double range, double carryDistance, double carryHeightOffset, boolean throwable, double throwStrength,
                         AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.range = range;
        this.carryDistance = carryDistance;
        this.carryHeightOffset = carryHeightOffset;
        this.throwable = throwable;
        this.throwStrength = throwStrength;
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity.level().isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.getData(PalladiumAttachments.CARRIED_TARGET_ID) != -1) {
            AbilityEventHandler.releaseCarry(player);
            return;
        }

        LivingEntity target = raycastTarget(player);

        if (target == null) {
            return;
        }

        player.setData(PalladiumAttachments.CARRIED_TARGET_ID, target.getId());
        player.setData(PalladiumAttachments.CARRY_THROWABLE, this.throwable);
        player.setData(PalladiumAttachments.CARRY_THROW_STRENGTH, this.throwStrength);
        player.setData(PalladiumAttachments.CARRY_DISTANCE, this.carryDistance);
        player.setData(PalladiumAttachments.CARRY_HEIGHT_OFFSET, this.carryHeightOffset);

        if (target instanceof Mob mob) {
            mob.setNoAi(true);
        }

        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity.level().isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (player.getData(PalladiumAttachments.CARRIED_TARGET_ID) != -1) {
            AbilityEventHandler.releaseCarry(player);
        }
    }

    @Nullable
    private LivingEntity raycastTarget(ServerPlayer player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(this.range));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(this.range)).inflate(1.0);

        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, searchBox,
                e -> e instanceof LivingEntity living && living.isAlive() && living != player, this.range * this.range);

        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.CARRY.get();
    }

    public static class Serializer extends AbilitySerializer<CarryAbility> {

        @Override
        public MapCodec<CarryAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, CarryAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("While enabled, pressing the ability's key grabs the first living entity hit by a forward raycast, holding it until the key is pressed again (drop) or, if throwable, the player right-clicks (throw).")
                    .add("range", TYPE_DOUBLE, "How far forward the grab raycast reaches.")
                    .addOptional("carry_distance", TYPE_DOUBLE, "How many blocks in front of the player the carried target is held. Defaults to 2.0.", 2.0)
                    .addOptional("carry_height_offset", TYPE_DOUBLE, "Extra vertical offset applied to the carried target's held position. Defaults to 0.0.", 0.0)
                    .addOptional("throwable", TYPE_BOOLEAN, "If true, right-clicking while carrying throws the target instead of requiring the key to drop it. Defaults to false.", false)
                    .addOptional("throw_strength", TYPE_DOUBLE, "Velocity applied to the target when thrown, in the player's look direction. Only relevant if throwable is true. Defaults to 1.5.", 1.5)
                    .addExampleObject(new CarryAbility(4.0, 2.0, 0.0, true, 1.5, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
