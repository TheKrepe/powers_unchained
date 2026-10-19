package net.threetag.palladium.entity.sizechange;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.network.SyncEntitySizeChangePacket;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.PlayerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class EntitySizeChangeHandler extends PalladiumEntityData<LivingEntity, EntitySizeChangeHandler> {

    public static float RENDER_PARTIAL_TICK = 1.0F;

    public static final Codec<EntitySizeChangeHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("base_size", 1F).forGetter(d -> d.baseSize),
            Codec.unboundedMap(Identifier.CODEC, ModifierEntry.CODEC)
                    .xmap(EntitySizeChangeHandler::decodeModifiers, EntitySizeChangeHandler::encodeModifiers)
                    .optionalFieldOf("modifiers", Map.of())
                    .forGetter(d -> d.modifiers)
    ).apply(instance, EntitySizeChangeHandler::new));

    private float baseSize;
    private final Map<ResourceKey<SizeChangeType>, ModifierEntry> modifiers;

    private EntitySizeChangeHandler(float baseSize, Map<ResourceKey<SizeChangeType>, ModifierEntry> modifiers) {
        this.baseSize = baseSize;
        this.modifiers = new LinkedHashMap<>(modifiers);
    }

    @Override
    protected void init() {
        this.modifiers.forEach((key, entry) -> entry.resolveType(this.registryAccess(), key));
        this.getEntity().refreshDimensions();
    }

    @Override
    public void tick() {
        if (this.modifiers.isEmpty()) {
            return;
        }

        float sizeBefore = this.getSize(1.0F);
        boolean dimensionsChanged = false;
        boolean destructive = false;
        var iterator = this.modifiers.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next().getValue();
            boolean wasTransitioning = entry.isTransitioning();
            float prevValue = entry.getCurrentValue();
            entry.tick();

            if (wasTransitioning && entry.type != null && entry.type.value().destructive()
                    && entry.getTargetValue() > 1F
                    && entry.getTargetValue() > entry.getStartValue()
                    && entry.getCurrentValue() > prevValue) {
                destructive = true;
            }

            if (entry.isDone() || (!entry.isTransitioning() && entry.getCurrentValue() == 1F && entry.getTargetValue() == 1F)) {
                iterator.remove();
                dimensionsChanged = true;
            } else if (wasTransitioning) {
                dimensionsChanged = true;
            }
        }

        AABB boxBefore = this.getEntity().getBoundingBox();

        if (dimensionsChanged) {
            this.getEntity().refreshDimensions();
        }

        if (destructive && this.getSize(1.0F) > sizeBefore) {
            this.destroyIntersectingBlocks(boxBefore);
            this.flingIntersectingEntities();
        }
    }

    private void flingIntersectingEntities() {
        LivingEntity entity = this.getEntity();

        if (this.getCurrentSize() <= 1F || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        var random = entity.getRandom();
        float strength = Math.min(0.3F + this.getCurrentSize() * 0.05F, 1F);

        for (Entity other : level.getEntities(entity, entity.getBoundingBox().inflate(0.2))) {
            if (other.isSpectator() || other.noPhysics || other.isPassengerOfSameVehicle(entity)) {
                continue;
            }

            Vec3 away = other.position().subtract(entity.getX(), other.getY(), entity.getZ());
            Vec3 direction = new Vec3(away.x, 0, away.z);
            direction = direction.lengthSqr() < 1.0E-4
                    ? new Vec3(random.nextFloat() - 0.5F, 0, random.nextFloat() - 0.5F).normalize()
                    : direction.normalize();
            other.push(direction.x * strength, 0.25F + strength * 0.25F, direction.z * strength);
            other.hurtMarked = true;
        }
    }

    private void destroyIntersectingBlocks(AABB previousBox) {
        LivingEntity entity = this.getEntity();

        if (this.getCurrentSize() <= 1F || !(entity.level() instanceof ServerLevel level)) {
            return;
        }

        var random = entity.getRandom();
        var box = entity.getBoundingBox();
        var scanBox = new AABB(box.minX - 0.05, box.minY + 0.01, box.minZ - 0.05, box.maxX + 0.05, box.maxY + 0.05, box.maxZ + 0.05);
        int fallingBlocks = 0;

        for (BlockPos pos : BlockPos.betweenClosed(scanBox)) {
            var blockBox = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

            if (blockBox.intersects(previousBox)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);

            if (state.isAir() || state.liquid() || state.getDestroySpeed(level, pos) < 0F
                    || state.getCollisionShape(level, pos).isEmpty()
                    || !CommonHooks.canEntityDestroy(level, pos, entity)) {
                continue;
            }

            if (state.hasBlockEntity() || fallingBlocks >= 64) {
                level.destroyBlock(pos, true);
                continue;
            }

            BlockPos blockPos = pos.immutable();
            level.levelEvent(2001, blockPos, Block.getId(state));
            FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, blockPos, state);
            Vec3 away = Vec3.atCenterOf(blockPos).subtract(entity.getX(), 0, entity.getZ());
            Vec3 direction = new Vec3(away.x, 0, away.z);
            direction = direction.lengthSqr() < 1.0E-4
                    ? new Vec3(random.nextFloat() - 0.5F, 0, random.nextFloat() - 0.5F).normalize()
                    : direction.normalize();
            double speed = 0.3 + random.nextFloat() * 0.3;
            fallingBlock.setDeltaMovement(direction.x * speed, 0.25 + random.nextFloat() * 0.25, direction.z * speed);
            fallingBlocks++;
        }
    }

    public void applyModifier(Holder<SizeChangeType> type, float size) {
        this.applyModifier(type, size, -1);
    }

    public void applyModifier(Holder<SizeChangeType> type, float size, int duration) {
        var typeKey = type.unwrapKey().orElseThrow();
        float clamped = type.value().clamp(size);
        var entry = this.modifiers.get(typeKey);

        if (entry == null) {
            entry = new ModifierEntry(1F, 1F, 1F, 0, 0, false);
            entry.type = type;
            entry.startTransitionTo(this.getEntity(), clamped, type, duration);
            this.modifiers.put(typeKey, entry);
        } else {
            if (!entry.removing && Float.compare(entry.targetValue, clamped) == 0) {
                return;
            }
            entry.removing = false;
            entry.startTransitionTo(this.getEntity(), clamped, type, duration);
        }

        this.syncToClients();
    }

    public void removeModifier(Holder<SizeChangeType> type) {
        var typeKey = type.unwrapKey().orElseThrow();
        var entry = this.modifiers.get(typeKey);

        if (entry != null && !entry.removing) {
            entry.removing = true;
            entry.startTransitionTo(this.getEntity(), 1F, type);
            this.syncToClients();
        }
    }

    public void setSize(float size) {
        this.modifiers.clear();
        this.baseSize = size;
        this.getEntity().refreshDimensions();
        this.syncToClients();
    }

    public float getSize(float partialTick) {
        float result = this.baseSize;
        for (var entry : this.modifiers.values()) {
            result *= entry.getContribution(partialTick);
        }
        return result;
    }

    public float getCurrentSize() {
        return this.getSize(1.0F);
    }

    public float getSpeedMultiplier() {
        float result = this.baseSize;
        for (var entry : this.modifiers.values()) {
            float contribution = entry.getCurrentValue();
            result *= entry.type != null
                    ? entry.type.value().speedContribution(contribution)
                    : contribution;
        }
        return result;
    }

    public float getJumpMultiplier() {
        float result = this.baseSize;
        for (var entry : this.modifiers.values()) {
            float contribution = entry.getCurrentValue();
            result *= entry.type != null
                    ? entry.type.value().jumpContribution(contribution)
                    : contribution;
        }
        return result;
    }

    public float getGravityMultiplier() {
        float result = this.baseSize;
        for (var entry : this.modifiers.values()) {
            float contribution = entry.getCurrentValue();
            result *= entry.type != null
                    ? entry.type.value().gravityContribution(contribution)
                    : contribution;
        }
        return result;
    }

    public float getDamageMultiplier() {
        float result = this.baseSize;
        for (var entry : this.modifiers.values()) {
            float contribution = entry.getCurrentValue();
            result *= entry.type != null
                    ? entry.type.value().damageContribution(contribution)
                    : contribution;
        }
        return result;
    }

    public float getTargetSize() {
        float result = this.baseSize;
        for (var entry : this.modifiers.values()) {
            result *= entry.targetValue;
        }
        return result;
    }

    public boolean isTransitioning() {
        return this.modifiers.values().stream().anyMatch(ModifierEntry::isTransitioning);
    }

    public float getBaseSize() {
        return this.baseSize;
    }

    public Map<ResourceKey<SizeChangeType>, ModifierEntry> getModifiers() {
        return Collections.unmodifiableMap(this.modifiers);
    }

    public List<Identifier> getActiveTrailIds() {
        List<Identifier> ids = new ArrayList<>();
        for (var entry : this.modifiers.values()) {
            if (entry.isTransitioning() && entry.type != null) {
                ids.addAll(entry.type.value().trails());
            }
        }
        return ids;
    }

    public void setFromPacket(float baseSize, Map<ResourceKey<SizeChangeType>, ModifierEntry> modifiers) {
        this.baseSize = baseSize;
        this.modifiers.clear();
        modifiers.forEach((key, entry) -> {
            entry.resolveType(this.registryAccess(), key);
            this.modifiers.put(key, entry);
        });
        this.getEntity().refreshDimensions();
    }

    private void syncToClients() {
        if (!this.getEntity().level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    this.getEntity(), SyncEntitySizeChangePacket.create(this.getEntity()));
        }
    }

    @Override
    public Codec<EntitySizeChangeHandler> codec() {
        return CODEC;
    }

    @Override
    public void copyFrom(PalladiumEntityData<LivingEntity, EntitySizeChangeHandler> source) {
        if (source instanceof EntitySizeChangeHandler data) {
            this.baseSize = data.baseSize;
            this.modifiers.clear();
            data.modifiers.forEach((key, entry) -> this.modifiers.put(key, entry.copy()));
        }
    }

    private static Map<ResourceKey<SizeChangeType>, ModifierEntry> decodeModifiers(Map<Identifier, ModifierEntry> map) {
        return map.entrySet().stream().collect(Collectors.toMap(
                e -> ResourceKey.create(PalladiumRegistryKeys.SIZE_CHANGE_TYPE, e.getKey()),
                Map.Entry::getValue,
                (a, b) -> b,
                LinkedHashMap::new
        ));
    }

    private static Map<Identifier, ModifierEntry> encodeModifiers(Map<ResourceKey<SizeChangeType>, ModifierEntry> map) {
        return map.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey().identifier(),
                Map.Entry::getValue,
                (a, b) -> b,
                LinkedHashMap::new
        ));
    }

    // -------------------------------------------------------------------------

    public static class ModifierEntry {

        public static final Codec<ModifierEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("current_value", 1F).forGetter(e -> e.currentValue),
                Codec.FLOAT.optionalFieldOf("start_value", 1F).forGetter(e -> e.startValue),
                Codec.FLOAT.optionalFieldOf("target_value", 1F).forGetter(e -> e.targetValue),
                Codec.INT.optionalFieldOf("transition_ticks", 0).forGetter(e -> e.transitionTicks),
                Codec.INT.optionalFieldOf("max_transition_ticks", 0).forGetter(e -> e.maxTransitionTicks),
                Codec.BOOL.optionalFieldOf("removing", false).forGetter(e -> e.removing)
        ).apply(instance, ModifierEntry::new));

        private float currentValue;
        private float previousValue;
        private float startValue;
        private float targetValue;
        private int transitionTicks;
        private int maxTransitionTicks;
        private boolean removing;

        @Nullable
        private Holder<SizeChangeType> type;

        public ModifierEntry(float currentValue, float startValue, float targetValue,
                             int transitionTicks, int maxTransitionTicks, boolean removing) {
            this.currentValue = currentValue;
            this.previousValue = currentValue;
            this.startValue = startValue;
            this.targetValue = targetValue;
            this.transitionTicks = transitionTicks;
            this.maxTransitionTicks = maxTransitionTicks;
            this.removing = removing;
        }

        void tick() {
            this.previousValue = this.currentValue;

            if (this.transitionTicks > 0) {
                this.transitionTicks--;

                float progress = this.maxTransitionTicks > 0
                        ? 1F - (float) this.transitionTicks / (float) this.maxTransitionTicks
                        : 1F;
                float easedProgress = this.type != null
                        ? this.type.value().easing().apply(progress)
                        : progress;

                this.currentValue = this.transitionTicks == 0
                        ? this.targetValue
                        : Mth.lerp(easedProgress, this.startValue, this.targetValue);

                if (this.type != null) {
                    float lower = Math.min(this.type.value().minSize(), Math.min(this.startValue, this.targetValue));
                    float upper = Math.max(this.type.value().maxSize(), Math.max(this.startValue, this.targetValue));
                    this.currentValue = Math.clamp(this.currentValue, lower, upper);
                }
            }
        }

        void startTransitionTo(Entity entity, float target, Holder<SizeChangeType> type) {
            this.startTransitionTo(entity, target, type, -1);
        }

        void startTransitionTo(Entity entity, float target, Holder<SizeChangeType> type, int durationOverride) {
            this.type = type;
            int duration = durationOverride >= 0 ? durationOverride : type.value().duration();

            if (!entity.level().isClientSide()) {
                if (target > this.currentValue && type.value().enlargeSound().isPresent()) {
                    var sound = type.value().enlargeSound().get().getAsIdentifier(DataContext.forEntity(entity));

                    if (sound != null) {
                        PlayerUtil.playSoundToAll(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), 50, sound, entity.getSoundSource());
                    }
                } else if (target < this.currentValue && type.value().shrinkSound().isPresent()) {
                    var sound = type.value().shrinkSound().get().getAsIdentifier(DataContext.forEntity(entity));

                    if (sound != null) {
                        PlayerUtil.playSoundToAll(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), 50, sound, entity.getSoundSource());
                    }
                }
            }

            if (duration <= 0) {
                this.currentValue = target;
                this.previousValue = target;
                this.startValue = target;
                this.targetValue = target;
                this.transitionTicks = 0;
                this.maxTransitionTicks = 0;
            } else {
                this.startValue = this.currentValue;
                this.targetValue = target;
                this.transitionTicks = duration;
                this.maxTransitionTicks = duration;
            }
        }

        void resolveType(RegistryAccess registryAccess, ResourceKey<SizeChangeType> typeKey) {
            if (this.type == null) {
                this.type = registryAccess.lookupOrThrow(PalladiumRegistryKeys.SIZE_CHANGE_TYPE)
                        .get(typeKey)
                        .orElse(null);
            }
        }

        float getContribution(float partialTick) {
            if (partialTick == 1.0F) {
                return this.currentValue;
            }
            return Mth.lerp(partialTick, this.previousValue, this.currentValue);
        }

        boolean isDone() {
            return this.removing && this.transitionTicks == 0;
        }

        boolean isTransitioning() {
            return this.transitionTicks > 0;
        }

        ModifierEntry copy() {
            var copy = new ModifierEntry(this.currentValue, this.startValue, this.targetValue,
                    this.transitionTicks, this.maxTransitionTicks, this.removing);
            copy.previousValue = this.previousValue;
            copy.type = this.type;
            return copy;
        }

        public float getCurrentValue() {
            return this.currentValue;
        }

        public float getStartValue() {
            return this.startValue;
        }

        public float getTargetValue() {
            return this.targetValue;
        }

        public int getTransitionTicks() {
            return this.transitionTicks;
        }

        public int getMaxTransitionTicks() {
            return this.maxTransitionTicks;
        }

        public boolean isRemoving() {
            return this.removing;
        }
    }
}
