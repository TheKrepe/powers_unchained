package net.threetag.palladium.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.util.EntityScaleUtil;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CustomProjectile extends ThrowableProjectile implements IEntityWithComplexSpawn {

    public static final BiMap<String, MapCodec<? extends Appearance>> APPEARANCE_REGISTRY = HashBiMap.create();
    public static final Codec<Appearance> APPEARANCE_CODEC = Codec.STRING.dispatch(
            "type",
            Appearance::getId,
            APPEARANCE_REGISTRY::get);
    public ResourceKey<DamageType> damageType;
    public float damage = 3F;
    public float gravity = 0.03F;
    public boolean dieOnBlockHit = true;
    public boolean dieOnEntityHit = true;
    public boolean preventShooterInteraction = false;
    public int lifetime = -1;
    public int setEntityOnFireSeconds = 0;
    public float explosionRadius = 0F;
    public boolean explosionCausesFire = false;
    public Level.ExplosionInteraction explosionBlockInteraction = Level.ExplosionInteraction.NONE;
    public float knockbackStrength = 0F;
    public List<Action> entityHitAction = null;
    public List<Action> blockHitAction = null;
    public EntityDimensions dimensions = EntityDimensions.fixed(0.1F, 0.1F);
    public List<Appearance> appearances = new ArrayList<>();

    static {
        APPEARANCE_REGISTRY.put("item", ItemAppearance.CODEC);
        APPEARANCE_REGISTRY.put("particles", ParticleAppearance.CODEC);
        APPEARANCE_REGISTRY.put("laser", LaserAppearance.CODEC);
        APPEARANCE_REGISTRY.put("renderLayer", RenderLayerAppearance.CODEC);
        APPEARANCE_REGISTRY.put("trail", TrailAppearance.CODEC);
    }

    public CustomProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public CustomProjectile(double x, double y, double z, Level level) {
        super(PalladiumEntityTypes.CUSTOM_PROJECTILE.get(), x, y, z, level);
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.dimensions;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected double getDefaultGravity() {
        return this.gravity;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = result.getEntity();

            if (entity != this.getOwner() || !this.preventShooterInteraction) {
                if (this.entityHitAction != null && !this.entityHitAction.isEmpty()) {
                    var context = DataContext.forEntity(entity);
                    for (Action action : this.entityHitAction) {
                        action.run(context);
                    }
                }

                if (this.damage > 0F) {
                    DamageSource damageSource;

                    if (this.damageType != null) {
                        damageSource = new DamageSource(
                                serverLevel.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(this.damageType),
                                this, this.getOwner()
                        );
                    } else {
                        damageSource = this.damageSources().thrown(this, this.getOwner());
                    }

                    entity.hurtServer(serverLevel, damageSource, this.damage);
                }

                if (this.setEntityOnFireSeconds > 0) {
                    entity.setRemainingFireTicks(this.setEntityOnFireSeconds * 20);
                }

                if (this.explosionRadius > 0F) {
                    serverLevel.explode(this, this.damageSources().thrown(this, this.getOwner()),
                            null, this.getX(), this.getEyeY(), this.getZ(),
                            this.explosionRadius, this.explosionCausesFire, this.explosionBlockInteraction);
                }

                if (this.knockbackStrength > 0F && entity instanceof LivingEntity living) {
                    living.knockback(this.knockbackStrength, -this.getDeltaMovement().x, -this.getDeltaMovement().z);
                }

                if (this.dieOnEntityHit) {
                    this.spawnDeathParticles();
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);

        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.blockHitAction != null && !this.blockHitAction.isEmpty()) {
                var context = DataContext.forEntity(this).with(DataContextKeys.BLOCK_POS, blockHitResult.getBlockPos());
                for (Action action : this.blockHitAction) {
                    action.run(context);
                }
            }

            if (this.explosionRadius > 0F) {
                serverLevel.explode(this, this.damageSources().thrown(this, this.getOwner()),
                        null, this.getX(), this.getEyeY(), this.getZ(),
                        this.explosionRadius, this.explosionCausesFire, this.explosionBlockInteraction);
            }

            if (this.dieOnBlockHit) {
                this.spawnDeathParticles();
                this.discard();
            }
        }
    }

    private void spawnDeathParticles() {
        for (Appearance appearance : this.appearances) {
            appearance.spawnParticlesOnHit(this);
        }
    }

    @Override
    public void tick() {
        super.tick();

        for (Appearance appearance : this.appearances) {
            appearance.onTick(this);
        }

        if (this.lifetime > 0 && this.tickCount >= this.lifetime && !this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.storeNullable("damage_type", ResourceKey.codec(Registries.DAMAGE_TYPE), this.damageType);
        output.putFloat("damage", this.damage);
        output.putFloat("gravity", this.gravity);
        output.putBoolean("die_on_entity_hit", this.dieOnEntityHit);
        output.putBoolean("die_on_block_hit", this.dieOnBlockHit);
        output.putBoolean("prevent_shooter_interaction", this.preventShooterInteraction);
        output.putFloat("size", this.dimensions.width());
        output.putInt("lifetime", this.lifetime);
        output.putInt("set_entity_on_fire_seconds", this.setEntityOnFireSeconds);
        output.putFloat("explosion_radius", this.explosionRadius);
        output.putBoolean("explosion_causes_fire", this.explosionCausesFire);
        output.store("explosion_interaction", Level.ExplosionInteraction.CODEC, this.explosionBlockInteraction);
        output.putFloat("knockback_strength", this.knockbackStrength);
        output.storeNullable("entity_hit_actions", Action.LIST_CODEC, this.entityHitAction);
        output.storeNullable("block_hit_actions", Action.LIST_CODEC, this.blockHitAction);
        ValueOutput.TypedOutputList<Appearance> appearances = output.list("appearances", APPEARANCE_CODEC);
        for (Appearance appearance : this.appearances) {
            appearances.add(appearance);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        this.damageType = input.read("damage_type", ResourceKey.codec(Registries.DAMAGE_TYPE)).orElse(null);
        this.damage = input.getFloatOr("damage", this.damage);
        this.gravity = input.getFloatOr("gravity", this.gravity);
        this.lifetime = input.getIntOr("lifetime", this.lifetime);
        this.setEntityOnFireSeconds = input.getIntOr("set_entity_on_fire_seconds", this.setEntityOnFireSeconds);
        this.dieOnEntityHit = input.getBooleanOr("die_on_entity_hit", this.dieOnEntityHit);
        this.dieOnBlockHit = input.getBooleanOr("die_on_block_hit", this.dieOnBlockHit);
        this.preventShooterInteraction = input.getBooleanOr("prevent_shooter_interaction", this.preventShooterInteraction);
        float size = input.getFloatOr("size", this.dimensions.width());
        this.dimensions = EntityDimensions.fixed(size, size);
        this.explosionRadius = input.getFloatOr("explosion_radius", this.explosionRadius);
        this.explosionCausesFire = input.getBooleanOr("explosion_causes_fire", this.explosionCausesFire);
        this.explosionBlockInteraction = input.read("explosion_interaction", Level.ExplosionInteraction.CODEC).orElse(Level.ExplosionInteraction.NONE);
        this.knockbackStrength = input.getFloatOr("knockback_strength", this.knockbackStrength);
        this.entityHitAction = input.read("entity_hit_actions", Action.LIST_CODEC).orElse(null);
        this.blockHitAction = input.read("block_hit_actions", Action.LIST_CODEC).orElse(null);
        this.appearances.clear();
        for (Appearance appearance : input.listOrEmpty("appearances", APPEARANCE_CODEC)) {
            this.appearances.add(appearance);
        }

        this.refreshDimensions();
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buf) {
        buf.writeFloat(this.getXRot());
        buf.writeFloat(this.getYRot());
        buf.writeFloat(this.gravity);
        buf.writeFloat(this.dimensions.width());
        CompoundTag tag = new CompoundTag();
        var list = new ListTag();
        for (Appearance appearance : this.appearances) {
            list.add(APPEARANCE_CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), appearance).getOrThrow());
        }
        tag.put("appearances", list);
        buf.writeNbt(tag);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buf) {
        this.setXRot(buf.readFloat());
        this.setYRot(buf.readFloat());
        this.gravity = buf.readFloat();
        float size = buf.readFloat();
        this.dimensions = EntityDimensions.fixed(size, size);
        this.refreshDimensions();
        CompoundTag tag = Objects.requireNonNull(buf.readNbt());
        for (Tag t : tag.getListOrEmpty("appearances")) {
            this.appearances.add(APPEARANCE_CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), t).getOrThrow());
        }
    }

    public static abstract class Appearance {

        public abstract String getId();

        public void onTick(CustomProjectile projectile) {

        }

        public void spawnParticlesOnHit(CustomProjectile projectile) {

        }

    }

    public static class ParticleAppearance extends Appearance {

        public static final MapCodec<ParticleAppearance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.PARTICLE_TYPE.byNameCodec().fieldOf("particle_type").forGetter(a -> a.type),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("amount", 1).forGetter(a -> a.amount),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("spread", 1F).forGetter(a -> a.spread),
                CompoundTag.CODEC.optionalFieldOf("options", new CompoundTag()).forGetter(a -> a.options)
        ).apply(instance, ParticleAppearance::new));

        public final ParticleType<?> type;
        public final int amount;
        public final float spread;
        public final CompoundTag options;

        public ParticleAppearance(ParticleType<?> type, int amount, float spread, CompoundTag options) {
            this.type = type;
            this.amount = amount;
            this.spread = spread;
            this.options = options;
        }

        @Override
        public String getId() {
            return "particles";
        }

        @Nullable
        private ParticleOptions parseParticleOptions(CustomProjectile projectile) {
            if (this.type == null) {
                return null;
            }
            try {
                return this.type.codec().codec()
                        .parse(projectile.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.options)
                        .getOrThrow();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void onTick(CustomProjectile projectile) {
            ParticleOptions particleOptions = parseParticleOptions(projectile);
            if (particleOptions == null) {
                return;
            }

            Random random = new Random();
            for (int i = 0; i < this.amount; i++) {
                float widthScale = EntityScaleUtil.getInstance().getWidthScale(projectile);
                float heightScale = EntityScaleUtil.getInstance().getHeightScale(projectile);
                float sX = (random.nextFloat() - 0.5F) * this.spread * widthScale;
                float sY = (random.nextFloat() - 0.5F) * this.spread * heightScale;
                float sZ = (random.nextFloat() - 0.5F) * this.spread * widthScale;

                projectile.level().addParticle(particleOptions, projectile.getX(), projectile.getY(), projectile.getZ(), sX, sY, sZ);
            }
        }

        @Override
        public void spawnParticlesOnHit(CustomProjectile projectile) {
            ParticleOptions particleOptions = parseParticleOptions(projectile);
            if (particleOptions == null) {
                return;
            }

            for (int i = 0; i < this.amount; i++) {
                Random random = new Random();
                float sX = (random.nextFloat() - 0.5F) * this.spread * 2F;
                float sY = (random.nextFloat() - 0.5F) * this.spread * 2F;
                float sZ = (random.nextFloat() - 0.5F) * this.spread * 2F;

                projectile.level().addParticle(particleOptions, projectile.getX(), projectile.getY(), projectile.getZ(), sX, sY, sZ);
            }
        }
    }

    public static class ItemAppearance extends Appearance {

        public static final MapCodec<ItemAppearance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                PalladiumCodecs.SIMPLE_ITEM_STACK.fieldOf("item").forGetter(a -> a.item)
        ).apply(instance, ItemAppearance::new));

        public final ItemStack item;

        public ItemAppearance(ItemStack item) {
            this.item = item;
        }

        @Override
        public String getId() {
            return "item";
        }

        @Override
        public void spawnParticlesOnHit(CustomProjectile projectile) {
            var data = new ItemParticleOption(ParticleTypes.ITEM, this.item.getItem());

            for (int i = 0; i < 8; ++i) {
                projectile.level().addParticle(data, projectile.getX(), projectile.getY(), projectile.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static class LaserAppearance extends Appearance {

        public static final MapCodec<LaserAppearance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("thickness").forGetter(a -> a.thickness),
                PalladiumCodecs.COLOR_CODEC.fieldOf("color").forGetter(a -> a.color)
        ).apply(instance, LaserAppearance::new));

        public final float thickness;
        public final Color color;

        public LaserAppearance(float thickness, Color color) {
            this.thickness = thickness;
            this.color = color;
        }

        @Override
        public String getId() {
            return "laser";
        }
    }

    public static class RenderLayerAppearance extends Appearance {

        public static final MapCodec<RenderLayerAppearance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.compactListCodec(Identifier.CODEC).fieldOf("render_layers").forGetter(a -> a.renderLayers)
        ).apply(instance, RenderLayerAppearance::new));

        public final List<Identifier> renderLayers;

        public RenderLayerAppearance(List<Identifier> renderLayers) {
            this.renderLayers = renderLayers;
        }

        @Override
        public String getId() {
            return "renderLayer";
        }
    }

    public static class TrailAppearance extends Appearance {

        public static final MapCodec<TrailAppearance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.compactListCodec(Identifier.CODEC).fieldOf("trails").forGetter(a -> a.trails)
        ).apply(instance, TrailAppearance::new));

        public final List<Identifier> trails;

        public TrailAppearance(List<Identifier> trails) {
            this.trails = trails;
        }

        @Override
        public String getId() {
            return "trail";
        }
    }

}
