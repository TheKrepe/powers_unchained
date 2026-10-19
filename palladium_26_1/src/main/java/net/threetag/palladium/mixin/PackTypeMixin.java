package net.threetag.palladium.mixin;

import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PackType.class)
@Unique
public enum PackTypeMixin {

    PALLADIUM_ADDON("addon");

    @Shadow
    PackTypeMixin(String directory) {
    }

}
