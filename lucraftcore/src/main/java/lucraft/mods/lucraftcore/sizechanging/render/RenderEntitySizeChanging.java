package lucraft.mods.lucraftcore.sizechanging.render;

import lucraft.mods.lucraftcore.sizechanging.entities.EntitySizeChanging;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderEntitySizeChanging extends Render<EntitySizeChanging> {

    public RenderEntitySizeChanging(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySizeChanging entity, double x, double y, double z, float entityYaw, float partialTicks) {
        entity.sizeChanger.renderEntity(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntitySizeChanging entity) {
        return null;
    }
}
