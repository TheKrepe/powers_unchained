package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.entity.effect.EnergyBlastEffectRenderer;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.EntityUtil;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlastAbility extends Ability {

    public static final MapCodec<BlastAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("beam_renderer").forGetter(ab -> ab.beamId),
                    Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("damage", 0F).forGetter(ab -> ab.damage),
                    Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("max_distance", 30F).forGetter(ab -> ab.maxDistance),
                    Codec.floatRange(0.01F, Float.MAX_VALUE).optionalFieldOf("speed", 2F).forGetter(ab -> ab.speed),
                    Codec.floatRange(0.01F, Float.MAX_VALUE).optionalFieldOf("length", 4F).forGetter(ab -> ab.length),
                    PalladiumCodecs.TIME.optionalFieldOf("set_on_fire_ticks", 0).forGetter(ab -> ab.setOnFireSeconds),
                    Codec.BOOL.optionalFieldOf("cause_fire", false).forGetter(ab -> ab.causeFire),
                    Codec.BOOL.optionalFieldOf("smelt_blocks", false).forGetter(ab -> ab.smeltBlocks),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, BlastAbility::new));

    public final Identifier beamId;
    public final float damage, maxDistance, speed, length;
    public final int setOnFireSeconds;
    public final boolean causeFire, smeltBlocks;

    public BlastAbility(Identifier beamId, float damage, float maxDistance, float speed, float length, int setOnFireSeconds, boolean causeFire, boolean smeltBlocks, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.beamId = beamId;
        this.damage = damage;
        this.maxDistance = maxDistance;
        this.speed = speed;
        this.length = length;
        this.setOnFireSeconds = setOnFireSeconds;
        this.causeFire = causeFire;
        this.smeltBlocks = smeltBlocks;
    }

    @Override
    public AbilitySerializer<BlastAbility> getSerializer() {
        return AbilitySerializers.BLAST.get();
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder components) {
        components.set(PalladiumDataComponents.Abilities.BLAST.get(), null);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> instance) {
        if (entity.level().isClientSide()) {
            if (entity instanceof Player player) {
                EnergyBlastEffectRenderer.start(player, instance.getReference());
            }

            return;
        }

        var origin = entity.getEyePosition();
        var end = origin.add(EntityUtil.getLookVector(entity).scale(this.maxDistance));
        var hit = EntityUtil.rayTraceAlongPath(entity, origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, en -> en != entity);
        instance.set(PalladiumDataComponents.Abilities.BLAST.get(), new ActiveBlast(origin, hit.getLocation(), entity.level().getGameTime()));
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> instance, boolean enabled) {
        var blast = this.getBlast(instance);

        if (blast != null && !entity.level().isClientSide() && entity.level().getGameTime() - blast.startTime() == this.travelTicks(blast)) {
            this.impact(entity, blast);
        }

        return super.tick(entity, instance, enabled);
    }

    private void impact(LivingEntity entity, ActiveBlast blast) {
        HitResult hit = EntityUtil.rayTraceAlongPath(entity, blast.origin(), blast.target(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, en -> en != entity);

        if (hit instanceof EntityHitResult entityHitResult) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                if (this.setOnFireSeconds > 0) {
                    entityHitResult.getEntity().setRemainingFireTicks(this.setOnFireSeconds);
                }

                if (this.damage > 0) {
                    var damageSrc = entity instanceof Player player ? entity.level().damageSources().playerAttack(player) : entity.damageSources().mobAttack(entity);
                    entityHitResult.getEntity().hurtServer(serverLevel, damageSrc, this.damage);
                }
            }

            Palladium.PROXY.spawnEnergyBeamParticles(entity.level(), hit.getLocation(), this.beamId);
        } else if (hit instanceof BlockHitResult blockHitResult) {
            BlockState blockState = entity.level().getBlockState(blockHitResult.getBlockPos());

            if (!blockState.isAir()) {
                if (entity.level() instanceof ServerLevel level) {
                    if (this.smeltBlocks) {
                        SingleRecipeInput simpleContainer = new SingleRecipeInput(new ItemStack(blockState.getBlock()));
                        level.recipeAccess().getRecipeFor(RecipeType.SMELTING, simpleContainer, entity.level()).ifPresent(recipe -> {
                            ItemStack result = recipe.value().assemble(simpleContainer);

                            if (!result.isEmpty() && Block.byItem(result.getItem()) != Blocks.AIR) {
                                entity.level().setBlockAndUpdate(blockHitResult.getBlockPos(), Block.byItem(result.getItem()).defaultBlockState());
                            }
                        });

                        blockState = entity.level().getBlockState(blockHitResult.getBlockPos());
                    }

                    if (this.causeFire && blockState.isFlammable(level, blockHitResult.getBlockPos(), blockHitResult.getDirection())) {
                        BlockPos pos = blockHitResult.getBlockPos().offset(blockHitResult.getDirection().getUnitVec3i());
                        if (entity.level().isEmptyBlock(pos)) {
                            entity.level().setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
                        }
                    }
                }

                Palladium.PROXY.spawnEnergyBeamParticles(entity.level(), hit.getLocation(), this.beamId);
            }
        }
    }

    @Nullable
    public ActiveBlast getBlast(AbilityInstance<?> instance) {
        return instance.get(PalladiumDataComponents.Abilities.BLAST.get());
    }

    public int travelTicks(ActiveBlast blast) {
        return Mth.ceil(blast.origin().distanceTo(blast.target()) / this.speed);
    }

    public float travelledDistance(ActiveBlast blast, Level level, float partialTick) {
        return Math.max(0F, (level.getGameTime() - blast.startTime() + partialTick) * this.speed);
    }

    public boolean hasPassedTarget(ActiveBlast blast, Level level, double distance, float partialTick) {
        return this.travelledDistance(blast, level, partialTick) - this.length >= distance;
    }

    public record ActiveBlast(Vec3 origin, Vec3 target, long startTime) {

        public static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = ByteBufCodecs.VECTOR3F.map(Vec3::new, Vec3::toVector3f);

        public static final StreamCodec<ByteBuf, ActiveBlast> STREAM_CODEC = StreamCodec.composite(
                VEC3_STREAM_CODEC, ActiveBlast::origin,
                VEC3_STREAM_CODEC, ActiveBlast::target,
                ByteBufCodecs.VAR_LONG, ActiveBlast::startTime,
                ActiveBlast::new
        );
    }

    public static class Serializer extends AbilitySerializer<BlastAbility> {

        @Override
        public MapCodec<BlastAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, BlastAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Shoots a piece of a beam into the direction the player is looking at. The blast keeps the direction it was fired in and only deals damage once it reached its target.")
                    .add("beam_renderer", TYPE_IDENTIFIER, "The id of the beam renderer to use")
                    .addOptional("damage", TYPE_FLOAT, "The damage the blast deals to entities", 0F)
                    .addOptional("max_distance", TYPE_FLOAT, "The maximum distance the blast can travel", 30F)
                    .addOptional("speed", TYPE_FLOAT, "The blocks the blast travels per tick", 2F)
                    .addOptional("length", TYPE_FLOAT, "The length of the blast in blocks", 4F)
                    .addOptional("set_on_fire_ticks", TYPE_INT, "The amount of ticks the hit entity is set on fire", 0)
                    .addOptional("cause_fire", TYPE_BOOLEAN, "If the blast should cause fire on blocks", false)
                    .addOptional("smelt_blocks", TYPE_BOOLEAN, "If the blast should smelt hit blocks", false)
                    .addExampleObject(new BlastAbility(
                            Identifier.fromNamespaceAndPath("example", "beam_renderer_id"),
                            5F,
                            25F,
                            2F,
                            4F,
                            20,
                            false,
                            false,
                            AbilityProperties.BASIC,
                            AbilityStateManager.EMPTY,
                            List.of()
                    ));
        }
    }
}
