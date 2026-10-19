package net.threetag.palladium.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.attachment.PalladiumAttachments;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.entity.flight.SwingingFlightType;
import net.threetag.palladium.entity.sizechange.EntitySizeChangeHandler;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.ability.RestrictSlotsAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    protected abstract boolean canGlide();

    @Shadow
    protected int fallFlyTicks;

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void preventLandingMovement(Vec3 input, CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        var controller = EntityFlightHandler.get(living).getController();
        if (controller instanceof SwingingFlightType.Controller c && c.isLanding() && living.onGround()) {
            living.setDeltaMovement(Vec3.ZERO);
            ci.cancel();
        }
    }

    @Inject(method = "canUseSlot", at = @At("HEAD"), cancellable = true)
    private void canUseSlot(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (RestrictSlotsAbility.isRestricted((LivingEntity) (Object) this, slot)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canGlide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void canGlideHook(CallbackInfoReturnable<Boolean> cir) {
        if (AbilityUtil.isTypeEnabled((LivingEntity) (Object) this, AbilitySerializers.GLIDING.get())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateFallFlying", at = @At("HEAD"), cancellable = true)
    private void updateFallFlying(CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;

        if (AbilityUtil.isTypeEnabled(entity, AbilitySerializers.PREVENT_GLIDING.get())) {
            if (entity.isFallFlying()) this.setSharedFlag(7, false);
            return;
        }

        if (AbilityUtil.isTypeEnabled(entity, AbilitySerializers.GLIDING.get())) {
            ci.cancel();

            entity.checkFallDistanceAccumulation();
            if (!entity.level().isClientSide()) {
                if (!this.canGlide()) {
                    this.setSharedFlag(7, false);
                    return;
                }

                int i = this.fallFlyTicks + 1;
                if (i % 10 == 0) {
                    this.gameEvent(GameEvent.ELYTRA_GLIDE);
                }
            }
        }
    }

    @Inject(method = "onClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;blockPosition()Lnet/minecraft/core/BlockPos;"), cancellable = true)
    private void onClimbable(CallbackInfoReturnable<Boolean> cir) {
        if (this.getData(PalladiumAttachments.IS_CLIMBING.get())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getScale", at = @At("RETURN"), cancellable = true)
    private void getScale(CallbackInfoReturnable<Float> cir) {
        var sizeChangeData = PalladiumEntityData.opt((LivingEntity) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        if (sizeChangeData.isPresent()) {
            float partialTick = this.level().isClientSide() ? EntitySizeChangeHandler.RENDER_PARTIAL_TICK : 1.0F;
            float originalScale = cir.getReturnValue();
            float sizeChangeScale = sizeChangeData.get().getSize(partialTick);
            cir.setReturnValue(originalScale * sizeChangeScale);
        }
    }

    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void maxUpStep(CallbackInfoReturnable<Float> cir) {
        var sizeChangeData = PalladiumEntityData.opt((LivingEntity) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        sizeChangeData.ifPresent(changeData -> cir.setReturnValue(cir.getReturnValue() * changeData.getCurrentSize()));
    }

    @Inject(method = "getSpeed", at = @At("RETURN"), cancellable = true)
    private void getSpeed(CallbackInfoReturnable<Float> cir) {
        var sizeChangeData = PalladiumEntityData.opt((LivingEntity) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        sizeChangeData.ifPresent(changeData -> cir.setReturnValue(cir.getReturnValue() * changeData.getSpeedMultiplier()));
    }

    @Inject(method = "getJumpPower(F)F", at = @At("RETURN"), cancellable = true)
    private void getJumpPower(float multiplier, CallbackInfoReturnable<Float> cir) {
        var sizeChangeData = PalladiumEntityData.opt((LivingEntity) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        sizeChangeData.ifPresent(changeData -> cir.setReturnValue(cir.getReturnValue() * changeData.getJumpMultiplier()));
    }

    @Inject(method = "getEffectiveGravity", at = @At("RETURN"), cancellable = true)
    private void getEffectiveGravity(CallbackInfoReturnable<Double> cir) {
        var sizeChangeData = PalladiumEntityData.opt((LivingEntity) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        sizeChangeData.ifPresent(changeData -> cir.setReturnValue(cir.getReturnValue() * changeData.getGravityMultiplier()));
    }

    @Inject(method = "getAttributeValue(Lnet/minecraft/core/Holder;)D", at = @At("RETURN"), cancellable = true)
    private void scaleAttackDamage(Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
        if (attribute.value() != Attributes.ATTACK_DAMAGE.value()) {
            return;
        }
        var sizeChangeData = PalladiumEntityData.opt((LivingEntity) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        sizeChangeData.ifPresent(changeData -> cir.setReturnValue(cir.getReturnValue() * changeData.getDamageMultiplier()));
    }

    @ModifyConstant(method = "jumpFromGround", constant = @Constant(doubleValue = 0.2))
    private double scaleSprintJumpBoost(double original) {
        var sizeChangeData = PalladiumEntityData.opt((LivingEntity) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        return sizeChangeData.map(entitySizeChangeHandler -> original * entitySizeChangeHandler.getSpeedMultiplier()).orElse(original);
    }

    @ModifyVariable(method = "updateWalkAnimation", at = @At("HEAD"), argsOnly = true, name = "distance")
    private float scaleWalkAnimationDistance(float distance) {
        var sizeChangeData = PalladiumEntityData.opt((LivingEntity) (Object) this, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        if (sizeChangeData.isPresent()) {
            float scale = sizeChangeData.get().getCurrentSize();
            if (scale > 0F) {
                return distance / scale;
            }
        }
        return distance;
    }

}
