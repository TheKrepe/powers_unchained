package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityKree;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;

public class RenderKree extends RenderBiped<EntityKree> {

    public RenderKree(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelPlayer(0, false), 0.5F);
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
            protected void initArmor() {
                this.modelLeggings = new ModelPlayer(0.5F, true);
                this.modelArmor = new ModelPlayer(1.0F, true);
            }
        };
        this.addLayer(layerbipedarmor);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityKree entity) {
        return new ResourceLocation(HeroesExpansion.MODID, "textures/models/kree_" + entity.getDataManager().get(EntityKree.KREE_TYPE) + ".png");
    }
}
