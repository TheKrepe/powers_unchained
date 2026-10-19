package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.LivingEntity;

public class PreRenderCallbackBridge {

    public static void preRenderCallback(LivingRenderer renderer, LivingEntity entity, MatrixStack matrixStack, float partialTicks) {
        renderer.preRenderCallback(entity, matrixStack, partialTicks);
    }

}
