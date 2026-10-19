package net.threetag.palladium.entity.flight;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class FlightController<T extends FlightType> {

    public void tick(LivingEntity entity, T flightType) {
        if (entity.onGround() || entity.getVehicle() != null) {
            this.stopFlight(entity);
        }
    }

    public void start(LivingEntity entity, T flightType) {

    }

    public void stop(LivingEntity entity, T flightType) {

    }

    public boolean interceptStopFlight(LivingEntity entity, T flightType) {
        return false;
    }

    public int getPhaseId() {
        return 0;
    }

    public void setSyncedPhase(int phaseId) {

    }

    public final void stopFlight(LivingEntity entity) {
        EntityFlightHandler.get(entity).stopFlight();
    }

    protected static boolean wantsToMove(@Nullable Input input) {
        return input != null && (input.forward() || input.backward() || input.left() || input.right() || input.jump());
    }

    protected static boolean isApproachingGround(LivingEntity entity) {
        double lookahead = Math.max(2, -entity.getDeltaMovement().y * 5);
        var hit = entity.level().clip(new ClipContext(entity.position(),
                entity.position().add(0, -lookahead, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, entity));
        return hit.getType() == HitResult.Type.BLOCK;
    }

    public void clampRotation(LivingEntity entity, T flightType) {

    }

    public boolean hasSmallHitbox(LivingEntity entity) {
        return false;
    }

    public boolean isFlightSoundActive(LivingEntity entity, T flightType) {
        return false;
    }

}
