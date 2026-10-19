package net.threetag.palladium.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererInvoker {

    @Invoker("setupRotations")
    void invokeSetupRotations(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float scale);

    @Invoker("scale")
    void invokeScale(LivingEntityRenderState state, PoseStack poseStack);

    @Invoker("getWhiteOverlayProgress")
    float invokeGetWhiteOverlayProgress(LivingEntityRenderState state);

    @Invoker("getModelTint")
    int invokeGetModelTint(LivingEntityRenderState state);

    @Invoker("isBodyVisible")
    boolean invokeIsBodyVisible(LivingEntityRenderState state);

    @Invoker("shouldRenderLayers")
    boolean invokeShouldRenderLayers(LivingEntityRenderState state);
}
