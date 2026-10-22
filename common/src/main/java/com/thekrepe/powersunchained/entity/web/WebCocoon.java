package com.thekrepe.powersunchained.entity.web;

import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.web.WebSlowness;
import java.util.List;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Web cocoon: when {@link #WEBS_NEEDED} sticky webs stick to the same entity at once, they merge into a cocoon that
 * covers it for {@link #DURATION} ticks; meanwhile the entity can't move or jump. Follows its target like a sticky web.
 */
public class WebCocoon extends Entity {

    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(WebCocoon.class, EntityDataSerializers.INT);

    /** Sticky webs on one entity that wrap it up. */
    public static final int WEBS_NEEDED = 5;
    /** Ticks the cocoon holds (5 s). */
    public static final int DURATION = 100;

    public WebCocoon(EntityType<? extends WebCocoon> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** Wraps the entity up (or restarts the cocoon it already has). */
    public static void wrap(LivingEntity target) {
        WebCocoon existing = find(target);
        if (existing != null) {
            existing.tickCount = 0;
            return;
        }
        WebCocoon cocoon = new WebCocoon(PUEntities.WEB_COCOON.get(), target.level());
        cocoon.entityData.set(TARGET, target.getId() + 1);
        cocoon.snapTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
        target.level().addFreshEntity(cocoon);
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WOOL_PLACE, SoundSource.NEUTRAL, 1.0F, 0.6F);
    }

    /** The cocoon on the entity, if any. */
    public static @Nullable WebCocoon find(LivingEntity target) {
        List<WebCocoon> cocoons = target.level().getEntitiesOfClass(WebCocoon.class, target.getBoundingBox().inflate(2.0),
                c -> c.getTarget() == target);
        return cocoons.isEmpty() ? null : cocoons.getFirst();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET, 0);
    }

    public @Nullable LivingEntity getTarget() {
        int id = this.entityData.get(TARGET) - 1;
        return id >= 0 && this.level().getEntity(id) instanceof LivingEntity living ? living : null;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity target = this.getTarget();
        boolean server = !this.level().isClientSide();
        if (target == null || !target.isAlive()) {
            if (server) {
                this.discard();
            }
            return;
        }
        this.setPos(target.getX(), target.getY(), target.getZ());
        if (server) {
            if (this.tickCount >= DURATION) {
                this.discard();
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WOOL_BREAK, SoundSource.NEUTRAL, 1.0F, 0.8F);
            } else {
                WebSlowness.hold(target);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64.0 * 64.0;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
