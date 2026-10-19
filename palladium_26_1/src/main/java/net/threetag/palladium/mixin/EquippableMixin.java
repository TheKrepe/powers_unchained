package net.threetag.palladium.mixin;

import net.minecraft.world.item.equipment.Equippable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(Equippable.Builder.class)
public class EquippableMixin {

    @Redirect(method = "setAsset", at = @At(value = "INVOKE", target = "Ljava/util/Optional;of(Ljava/lang/Object;)Ljava/util/Optional;"))
    private Optional<?> injected(Object value) {
        return Optional.ofNullable(value);
    }

}
