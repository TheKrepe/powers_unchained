package net.threetag.palladium.mixin.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.client.AbilityClientEventHandler;
import net.threetag.palladium.client.renderer.PalladiumRenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTypes.class)
public class RenderTypesMixin {

    @Inject(method = "entityTranslucent(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void entityTranslucent(Identifier texture, boolean affectsOutline, CallbackInfoReturnable<RenderType> cir) {
        if (AbilityClientEventHandler.AFTER_IMAGE) {
            cir.setReturnValue(afterImageEntityType(texture));
        }
    }

    @Inject(method = "entitySolid", at = @At("HEAD"), cancellable = true)
    private static void entitySolid(Identifier texture, CallbackInfoReturnable<RenderType> cir) {
        if (AbilityClientEventHandler.OVERRIDDEN_OPACITY < 1F) {
            cir.setReturnValue(AbilityClientEventHandler.AFTER_IMAGE
                    ? afterImageEntityType(texture)
                    : RenderTypes.entityTranslucent(texture));
        }
    }

    @Inject(method = "entityCutout(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void entityCutout(Identifier texture, boolean affectsOutline, CallbackInfoReturnable<RenderType> cir) {
        if (AbilityClientEventHandler.OVERRIDDEN_OPACITY < 1F) {
            cir.setReturnValue(AbilityClientEventHandler.AFTER_IMAGE
                    ? afterImageEntityType(texture)
                    : RenderTypes.entityTranslucent(texture));
        }
    }

    @Inject(method = "armorCutoutNoCull", at = @At("HEAD"), cancellable = true)
    private static void armorCutoutNoCull(Identifier texture, CallbackInfoReturnable<RenderType> cir) {
        if (AbilityClientEventHandler.OVERRIDDEN_OPACITY < 1F) {
            cir.setReturnValue(AbilityClientEventHandler.AFTER_IMAGE
                    ? afterImageArmorType(texture)
                    : RenderTypes.armorTranslucent(texture));
        }
    }

    private static RenderType afterImageEntityType(Identifier texture) {
        if (AbilityClientEventHandler.AFTER_IMAGE_GHOST_EDGES) {
            return PalladiumRenderTypes.entityGhostEdges(texture);
        }

        return AbilityClientEventHandler.AFTER_IMAGE_GHOST
                ? PalladiumRenderTypes.entityGhost(texture)
                : PalladiumRenderTypes.entityTranslucentNoDepthWrite(texture);
    }

    private static RenderType afterImageArmorType(Identifier texture) {
        if (AbilityClientEventHandler.AFTER_IMAGE_GHOST_EDGES) {
            return PalladiumRenderTypes.armorGhostEdges(texture);
        }

        return AbilityClientEventHandler.AFTER_IMAGE_GHOST
                ? PalladiumRenderTypes.armorGhost(texture)
                : PalladiumRenderTypes.armorTranslucentNoDepthWrite(texture);
    }

}
