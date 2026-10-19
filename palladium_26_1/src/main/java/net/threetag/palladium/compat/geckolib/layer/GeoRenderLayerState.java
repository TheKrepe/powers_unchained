package net.threetag.palladium.compat.geckolib.layer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.logic.context.DataContext;
import org.jetbrains.annotations.Nullable;

public class GeoRenderLayerState extends PackRenderLayer.State implements GeoAnimatable {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, false);

    public @Nullable Identifier texture;
    public @Nullable Identifier model;
    public @Nullable Identifier animations;
    HumanoidRenderState capturedGeoState;

    public GeoRenderLayerState(GeoRenderLayer renderLayer, DataContext context) {
        super(renderLayer, context);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "UnnecessaryLocalVariable"})
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        for (AnimationControllerFactory<?> factory : ((GeoRenderLayer) this.renderLayer).animationController) {
            AnimationControllerFactory f = factory;
            controllers.add(f.create(this));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void extractRenderState(PackRenderLayer<?> layer, DataContext context) {
        super.extractRenderState(layer, context);
        if (!(layer instanceof GeoRenderLayer geoLayer)) return;

        this.texture = geoLayer.texture.get(this.isSlim).getTexture(context);
        this.model = geoLayer.model.get(this.isSlim).getTexture(context);
        this.animations = geoLayer.animations != null ? geoLayer.animations.get(this.isSlim) : null;

        if (this.capturedGeoState == null)
            this.capturedGeoState = geoLayer.renderer.createRenderState(this, context);

        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        this.capturedGeoState.ageInTicks = context.getEntity().tickCount + partialTick;

        geoLayer.renderer.fillRenderState(this, context, this.capturedGeoState, partialTick);
    }
}
