package net.threetag.palladium.datagen.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.RenderTypeRegistry;
import net.threetag.palladium.client.renderer.entity.layer.pack.DefaultPackRenderLayer;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayerProperties;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayerTexture;
import net.threetag.palladium.customization.BuiltinCustomization;
import net.threetag.palladium.datagen.RenderLayerProvider;
import net.threetag.palladium.entity.SkinTypedValue;
import net.threetag.palladium.logic.value.StaticValue;

import java.util.concurrent.CompletableFuture;

public class PalladiumRenderLayerProvider extends RenderLayerProvider {

    public PalladiumRenderLayerProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Palladium.MOD_ID);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        for (BuiltinCustomization.Type type : BuiltinCustomization.Type.values()) {
            this.unconditional(type.getRenderLayerId(), this.makeRenderLayer(type));
        }
    }

    public PackRenderLayer<?> makeRenderLayer(BuiltinCustomization.Type type) {
        boolean glowing = type == BuiltinCustomization.Type.LUCRAFT_ARC_REACTOR;

        return new DefaultPackRenderLayer(
                type.model,
                this.getTexture(type),
                glowing ? RenderTypeRegistry.GLOW : RenderTypeRegistry.ENTITY_TRANSLUCENT,
                new StaticValue(glowing ? 15 : 0),
                null,
                PackRenderLayerProperties.DEFAULT
        );
    }

    public SkinTypedValue<PackRenderLayerTexture> getTexture(BuiltinCustomization.Type type) {
        var texture = Palladium.id("textures/customization/" + type.key + ".png");
        return new SkinTypedValue<>(new PackRenderLayerTexture(texture));
    }
}
