package net.threetag.palladium.compat.geckolib;

import com.geckolib.GeckoLibConstants;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayerSerializer;
import net.threetag.palladium.compat.geckolib.layer.GeoRenderLayer;

public class GeckoLibCompatClient {

    public static final PackRenderLayerSerializer<GeoRenderLayer> RENDER_LAYER = PackRenderLayerSerializer.register(GeckoLibConstants.id("render_layer"), new GeoRenderLayer.Serializer());

    public static void init() {
        GeckoLibCompat.ABILITY_HANDLER = new GeckoClientAbilityHandler();
    }

}
