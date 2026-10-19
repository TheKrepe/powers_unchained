package net.threetag.palladium.mixin.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.AABB;
import net.threetag.palladium.client.model.ModelPartExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public class ModelPartMixin implements ModelPartExtension {

    @Unique
    private AABB palladium5$boundingBox;

    @Shadow
    @Final
    public Map<String, ModelPart> children;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(List<ModelPart.Cube> cubes, Map<String, ModelPart> children, CallbackInfo ci) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE,
                maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE, maxZ = Float.MIN_VALUE;

        for (ModelPart.Cube cube : cubes) {
            minX = Math.min(minX, cube.minX);
            minY = Math.min(minY, cube.minY);
            minZ = Math.min(minZ, cube.minZ);
            maxX = Math.max(maxX, cube.maxX);
            maxY = Math.max(maxY, cube.maxY);
            maxZ = Math.max(maxZ, cube.maxZ);
        }

        this.palladium5$boundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Inject(method = "getChild", at = @At("HEAD"), cancellable = true)
    public void getChild(String name, CallbackInfoReturnable<ModelPart> cir) {
        ModelPart modelPart = this.children.get(name);
        if (modelPart == null) {
            cir.setReturnValue(new ModelPart(Collections.emptyList(), Collections.emptyMap()));
        }
    }

    @Override
    public AABB palladium$getBoundingBox() {
        return this.palladium5$boundingBox;
    }
}
