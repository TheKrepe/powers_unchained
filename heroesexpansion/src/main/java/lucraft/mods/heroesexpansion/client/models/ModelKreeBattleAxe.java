package lucraft.mods.heroesexpansion.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * KreeAxe - Neon Created using Tabula 4.1.1
 */
public class ModelKreeBattleAxe extends ModelBase {

    public ModelRenderer handleBase;
    public ModelRenderer handleTip;
    public ModelRenderer bladeholderBase;
    public ModelRenderer blade1;
    public ModelRenderer blade2;
    public ModelRenderer bladeholder2;
    public ModelRenderer bladeholder3;
    public ModelRenderer blade4;
    public ModelRenderer blade5;
    public ModelRenderer blade6;

    public ModelKreeBattleAxe() {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.blade1 = new ModelRenderer(this, 32, 0);
        this.blade1.setRotationPoint(0.0F, -2.0F, -2.0F);
        this.blade1.addBox(0.0F, 0.0F, 0.0F, 1, 6, 1, 0.0F);
        this.setRotateAngle(blade1, 0.136659280431156F, 0.0F, 0.0F);
        this.blade6 = new ModelRenderer(this, 0, 0);
        this.blade6.setRotationPoint(0.0F, -3.4F, -3.0F);
        this.blade6.addBox(0.0F, -3.0F, 0.0F, 1, 3, 2, -0.1F);
        this.setRotateAngle(blade6, -0.6373942428283291F, 0.0F, 0.0F);
        this.handleBase = new ModelRenderer(this, 43, 0);
        this.handleBase.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.handleBase.addBox(0.0F, 0.0F, 0.0F, 1, 12, 1, 0.0F);
        this.bladeholder3 = new ModelRenderer(this, 11, 6);
        this.bladeholder3.setRotationPoint(-0.5F, 0.0F, -1.5F);
        this.bladeholder3.addBox(0.0F, 0.0F, 0.0F, 2, 1, 2, -0.2F);
        this.bladeholder2 = new ModelRenderer(this, 11, 6);
        this.bladeholder2.setRotationPoint(-0.5F, -2.3F, -1.5F);
        this.bladeholder2.addBox(0.0F, 0.0F, 0.0F, 2, 1, 2, -0.2F);
        this.blade5 = new ModelRenderer(this, 0, 0);
        this.blade5.setRotationPoint(0.0F, 3.45F, -2.65F);
        this.blade5.addBox(0.0F, 0.0F, 0.0F, 1, 3, 2, -0.1F);
        this.setRotateAngle(blade5, 1.1383037381507017F, 0.0F, 0.0F);
        this.bladeholderBase = new ModelRenderer(this, 0, 18);
        this.bladeholderBase.setRotationPoint(0.5F, -3.2F, 0.5F);
        this.bladeholderBase.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, -0.2F);
        this.setRotateAngle(bladeholderBase, 0.0F, 0.7853981633974483F, 0.0F);
        this.blade4 = new ModelRenderer(this, 0, 0);
        this.blade4.setRotationPoint(0.0F, -3.5F, -3.0F);
        this.blade4.addBox(0.0F, 0.0F, 0.0F, 1, 7, 2, -0.1F);
        this.setRotateAngle(blade4, 0.045553093477052F, 0.0F, 0.0F);
        this.handleTip = new ModelRenderer(this, 13, 0);
        this.handleTip.setRotationPoint(0.0F, 10.0F, 0.0F);
        this.handleTip.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.25F);
        this.blade2 = new ModelRenderer(this, 32, 0);
        this.blade2.setRotationPoint(0.0F, -2.0F, -2.0F);
        this.blade2.addBox(0.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(blade2, -0.40980330836826856F, 0.0F, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.blade1.render(f5);
        this.blade6.render(f5);
        this.handleBase.render(f5);
        this.bladeholder3.render(f5);
        this.bladeholder2.render(f5);
        this.blade5.render(f5);
        this.bladeholderBase.render(f5);
        this.blade4.render(f5);
        this.handleTip.render(f5);
        this.blade2.render(f5);
    }

    public void renderModel(float f) {
        this.blade1.render(f);
        this.blade6.render(f);
        this.handleBase.render(f);
        this.bladeholder3.render(f);
        this.bladeholder2.render(f);
        this.blade5.render(f);
        this.bladeholderBase.render(f);
        this.blade4.render(f);
        this.handleTip.render(f);
        this.blade2.render(f);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
