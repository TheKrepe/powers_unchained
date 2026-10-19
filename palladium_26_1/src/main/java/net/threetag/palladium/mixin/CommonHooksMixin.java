package net.threetag.palladium.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import net.threetag.palladium.component.PalladiumDataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {

    @Inject(method = "getDefaultCreatorModId", at = @At("HEAD"), cancellable = true)
    private static void getDefaultCreatorModId(HolderLookup.Provider registries, ItemStack itemStack, CallbackInfoReturnable<String> cir) {
        if (itemStack.has(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT)) {
            cir.setReturnValue(Objects.requireNonNull(itemStack.get(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT))
                    .variant().identifier().getNamespace());
        }
    }

}
