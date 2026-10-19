package net.threetag.palladium.mixin.fabric;

import net.minecraft.client.gui.screens.Screen;
import net.threetag.palladiumcore.event.ScreenEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "rebuildWidgets", at = @At("TAIL"))
    private void palladium$fireInitPostOnRebuild(CallbackInfo ci) {
        ScreenEvents.INIT_POST.invoker().screenInitPost((Screen) (Object) this);
    }
}
