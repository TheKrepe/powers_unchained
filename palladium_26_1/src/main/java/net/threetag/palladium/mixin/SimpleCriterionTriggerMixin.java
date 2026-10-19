package net.threetag.palladium.mixin;

import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.threetag.palladium.logic.automation.AutomationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(SimpleCriterionTrigger.class)
public class SimpleCriterionTriggerMixin {

    @Inject(method = "trigger", at = @At("RETURN"))
    protected void trigger(ServerPlayer player, Predicate<?> matcher, CallbackInfo ci) {
        if (AutomationManager.INSTANCE != null) {
            AutomationManager.INSTANCE.run(player, (SimpleCriterionTrigger<?>) (Object) this, matcher);
        }
    }

}
