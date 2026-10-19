package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelChitauri;
import lucraft.mods.heroesexpansion.entities.EntityChitauri;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.util.ResourceLocation;

public class RenderChitauri extends RenderBiped<EntityChitauri> {

    private static final ResourceLocation CHITAURI_TEXTURES = new ResourceLocation(HeroesExpansion.MODID, "textures/models/chitauri.png");

    public RenderChitauri(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelChitauri(0F), 0.5F);
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
            protected void initArmor() {
                this.modelLeggings = new ModelChitauri(0.5F);
                this.modelArmor = new ModelChitauri(1.0F);
            }
        };
        this.addLayer(layerbipedarmor);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityChitauri entity) {
        return CHITAURI_TEXTURES;
    }

}
