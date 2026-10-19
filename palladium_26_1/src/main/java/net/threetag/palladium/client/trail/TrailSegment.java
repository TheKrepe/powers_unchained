package net.threetag.palladium.client.trail;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.util.ModelUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class TrailSegment<T> {

    private final Vec3 pos;
    private final Vec3 entityPosition;
    private final float yRot;
    private final float xRot;
    private final EntityDimensions dimensions;
    private final AABB boundingBox;
    private final Trail trail;
    private final T trailData;
    private int tickCount = 0;
    protected TrailSegment<T> previousSegment;
    protected TrailSegment<T> nextSegment;

    public TrailSegment(Entity entity, Trail trail, TrailRenderer<T> trailRenderer) {
        this.trail = trail;
        this.yRot = entity.getPreciseBodyRotation(0F);
        this.xRot = entity.getXRot(0F);

        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        Vec3 pos = entity.getPosition(0F).add(trail.offset().x(), trail.offset().y(), trail.offset().z());

        if (trail.anchor().isPresent() && entity instanceof AbstractClientPlayer player) {
            Vec3 anchorPos = ModelUtil.getInWorldPosition(trail.anchor().get(), new Vector3f(trail.offset()), player, 0F);
            dimensions = EntityDimensions.fixed(0.5F, 0.5F);
            pos = anchorPos.subtract(0, dimensions.height() / 2F, 0);
        }

        this.pos = pos;
        this.entityPosition = entity.getPosition(0F);
        this.dimensions = dimensions;
        this.boundingBox = this.dimensions.makeBoundingBox(this.pos);
        this.trailData = trailRenderer.createData(entity);
    }

    public void tick() {
        this.tickCount++;
    }

    public Trail getTrail() {
        return this.trail;
    }

    public Vec3 getEntityPosition() {
        return this.entityPosition;
    }

    public Vec3 getPosition() {
        return this.pos;
    }

    public float getXRot() {
        return xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public T getTrailData() {
        return this.trailData;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public boolean isObsolete() {
        return this.tickCount >= this.trail.lifetime();
    }

    public float getOpacity() {
        return 1F - (this.tickCount / (float) this.trail.lifetime());
    }

    public EntityDimensions getDimensions() {
        return this.dimensions;
    }

    public AABB getBoundingBox() {
        return this.boundingBox;
    }

    @Nullable
    public TrailSegment<T> getPreviousSegment() {
        return this.previousSegment;
    }

    @Nullable
    public TrailSegment<T> getNextSegment() {
        return this.nextSegment;
    }
}
