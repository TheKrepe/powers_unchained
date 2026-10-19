package net.threetag.palladium.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.threetag.palladium.client.AbilityClientEventHandler;
import net.threetag.palladium.client.util.ModelUtil;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    private ItemStack offHandItem;

    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Shadow
    private float offHandHeight;

    @Shadow
    private float oOffHandHeight;

    @Shadow
    @Final
    private ItemModelResolver itemModelResolver;

    @Shadow
    protected abstract void renderPlayerArm(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, float equippedProgress, float swingProgress, HumanoidArm arm);

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0))
    private void renderArmWithItem(
            AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equippedProgress, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, CallbackInfo ci
    ) {
        if (hand == InteractionHand.MAIN_HAND && AbilityUtil.isTypeEnabled(player, AbilitySerializers.SHOW_BOTH_ARMS.get()) && this.offHandItem.isEmpty() && !player.isInvisible() && !this.mainHandItem.is(Items.FILLED_MAP)) {
            HumanoidArm humanoidArm = player.getMainArm().getOpposite();
            poseStack.pushPose();
            this.renderPlayerArm(poseStack, nodeCollector, packedLight, 0, 0, humanoidArm);
            poseStack.popPose();
        }
    }

    @Inject(method = "renderHandsWithItems", at = @At("RETURN"))
    public void renderHandsWithItems(float partialTick, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int packedLight, CallbackInfo ci) {
        AbilityClientEventHandler.resetColorOverrides();
    }

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void palladium$captureArmAnchors(float partialTick, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int packedLight, CallbackInfo ci) {
        float attackValue = player.getAttackAnim(partialTick);
        InteractionHand attackHand = player.swingingArm != null ? player.swingingArm : InteractionHand.MAIN_HAND;
        float xBob = Mth.lerp(partialTick, player.xBobO, player.xBob);
        float yBob = Mth.lerp(partialTick, player.yBobO, player.yBob);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(partialTick) - xBob) * 0.1F));
        poseStack.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(partialTick) - yBob) * 0.1F));

        for (InteractionHand hand : InteractionHand.values()) {
            boolean isMainHand = hand == InteractionHand.MAIN_HAND;
            HumanoidArm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
            ItemStack item = isMainHand ? this.mainHandItem : this.offHandItem;
            float attack = attackHand == hand ? attackValue : 0.0F;
            float inverseArmHeight = this.itemModelResolver.swapAnimationScale(item)
                    * (1.0F - Mth.lerp(partialTick, isMainHand ? this.oMainHandHeight : this.oOffHandHeight, isMainHand ? this.mainHandHeight : this.offHandHeight));

            poseStack.pushPose();
            palladium$applyPlayerArmTransforms(poseStack, arm, inverseArmHeight, attack);
            ModelUtil.captureFirstPersonArmAnchor(arm, poseStack.last().pose());
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Unique
    private static void palladium$applyPlayerArmTransforms(PoseStack poseStack, HumanoidArm arm, float inverseArmHeight, float attackValue) {
        float invert = arm != HumanoidArm.LEFT ? 1.0F : -1.0F;
        float sqrtAttackValue = Mth.sqrt(attackValue);
        float xSwingPosition = -0.3F * Mth.sin(sqrtAttackValue * (float) Math.PI);
        float ySwingPosition = 0.4F * Mth.sin(sqrtAttackValue * (float) (Math.PI * 2));
        float zSwingPosition = -0.4F * Mth.sin(attackValue * (float) Math.PI);
        poseStack.translate(invert * (xSwingPosition + 0.64000005F), ySwingPosition + -0.6F + inverseArmHeight * -0.6F, zSwingPosition + -0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * 45.0F));
        float zSwingRotation = Mth.sin(attackValue * attackValue * (float) Math.PI);
        float ySwingRotation = Mth.sin(sqrtAttackValue * (float) Math.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * ySwingRotation * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * zSwingRotation * -20.0F));
        poseStack.translate(invert * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * -135.0F));
        poseStack.translate(invert * 5.6F, 0.0F, 0.0F);
    }

}
