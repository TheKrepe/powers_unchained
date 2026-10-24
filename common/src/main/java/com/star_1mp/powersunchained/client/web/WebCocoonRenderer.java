package com.star_1mp.powersunchained.client.web;

import com.star_1mp.powersunchained.entity.web.WebCocoon;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** The cocoon entity draws nothing itself: the web is drawn over the wrapped entity's model by a render layer. */
public class WebCocoonRenderer extends EntityRenderer<WebCocoon, EntityRenderState> {

    public WebCocoonRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected boolean affectedByCulling(WebCocoon entity) {
        return false;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
