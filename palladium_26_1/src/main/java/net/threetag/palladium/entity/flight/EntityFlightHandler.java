package net.threetag.palladium.entity.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.network.SyncEntityFlightPacket;
import net.threetag.palladium.network.ToggleEntityFlightPacket;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.util.Easing;

public class EntityFlightHandler extends PalladiumEntityData<LivingEntity, EntityFlightHandler> {

    public static final Codec<EntityFlightHandler> CODEC = MapCodec.unit(EntityFlightHandler::new).codec();

    private FlightType flightType;
    private FlightController<?> controller;
    private FlightAnimationHandler<?> animationHandler;
    private final AnimationTimer animationTimer = new AnimationTimerSetting(0, 10, AnimationTimerSetting.Behaviour.FOLLOW, Easing.INOUTCUBIC).create();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void tick() {
        var entity = this.getEntity();
        this.animationTimer.tickAndUpdate(this.controller != null);

        if (this.controller != null) {
            FlightController controller = this.controller;
            controller.tick(entity, this.flightType);
        }

        if (this.animationHandler != null) {
            FlightAnimationHandler animationHandler = this.animationHandler;
            animationHandler.tick(entity, this.flightType == null);
        }
    }

    public FlightType getFlightType() {
        return this.flightType;
    }

    public FlightController<?> getController() {
        return this.controller;
    }

    public FlightAnimationHandler<?> getAnimationHandler() {
        return this.animationHandler;
    }

    public boolean startFlight() {
        var flightType = getAvailableFlightType(this.getEntity());

        if (flightType != null) {
            this.startFlight(flightType);
            return true;
        }

        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void startFlight(FlightType flightType) {
        this.flightType = flightType;
        this.controller = flightType.createController();

        if (this.getEntity().level().isClientSide()) {
            this.animationHandler = flightType.createAnimationHandler();
            Palladium.PROXY.startFlightSound(this.getEntity(), flightType);
        }

        FlightController c = this.controller;
        c.start(this.getEntity(), flightType);
        this.getEntity().refreshDimensions();
        this.broadcastFlightState();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void stopFlight() {
        if (this.flightType != null) {
            FlightController c = this.controller;

            if (c.interceptStopFlight(this.getEntity(), this.flightType)) {
                return;
            }

            this.forceStopFlight();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void forceStopFlight() {
        if (this.flightType != null) {
            FlightController c = this.controller;
            c.stop(this.getEntity(), this.flightType);
            this.flightType = null;
            this.controller = null;
            this.getEntity().refreshDimensions();
            this.broadcastFlightState();

            if (this.getEntity().level().isClientSide() && this.getEntity() == Palladium.PROXY.getClientPlayer()) {
                Palladium.PROXY.sendPacketToServer(new ToggleEntityFlightPacket(false));
            }
        }
    }

    public void setSyncedPhase(int phaseId) {
        if (this.controller == null) {
            return;
        }

        boolean smallHitbox = this.controller.hasSmallHitbox(this.getEntity());
        this.controller.setSyncedPhase(phaseId);

        if (smallHitbox != this.controller.hasSmallHitbox(this.getEntity())) {
            this.getEntity().refreshDimensions();
        }
    }

    public void broadcastFlightState() {
        if (!this.getEntity().level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(this.getEntity(), SyncEntityFlightPacket.create(this.getEntity()));
        }
    }

    public void reevaluateFlightType() {
        if (this.flightType != null) {
            var possible = getAvailableFlightType(this.getEntity());

            if (possible != this.flightType) {
                this.forceStopFlight();
            }
        }
    }

    public boolean isFlying() {
        return this.flightType != null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean isFlightSoundActive() {
        if (this.flightType != null && this.controller != null) {
            FlightController controller = this.controller;
            return controller.isFlightSoundActive(this.getEntity(), this.flightType);
        }

        return false;
    }

    public float getInFlightTimer(float partialTick) {
        return this.animationTimer.eased(partialTick);
    }

    @Override
    public Codec<EntityFlightHandler> codec() {
        return CODEC;
    }

    public static FlightType getAvailableFlightType(LivingEntity entity) {
        for (AbilityInstance<FlightAbility> instance : AbilityUtil.getEnabledInstances(entity, AbilitySerializers.FLIGHT.get())) {
            var flightType = instance.getAbility().getFlightType();

            if (flightType != null && flightType.canStart(entity)) {
                return flightType;
            }
        }

        return null;
    }

    public static EntityFlightHandler get(LivingEntity entity) {
        return PalladiumEntityData.get(entity, PalladiumEntityDataTypes.FLIGHT.get());
    }
}
