package net.threetag.palladium.mixin.client;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.threetag.palladium.client.util.SplashTextUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashManager.class)
public class SplashManagerMixin {

    @Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
    private void getSplash(CallbackInfoReturnable<SplashRenderer> cir) {
        var splash = SplashTextUtil.getPossibleOverrideSplash();

        if (splash != null) {
            cir.setReturnValue(splash);
        }
    }

}
