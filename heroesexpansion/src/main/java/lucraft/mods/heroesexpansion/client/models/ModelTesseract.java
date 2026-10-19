package lucraft.mods.heroesexpansion.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTesseract extends ModelBase {

    public ModelRenderer shape1;
    public ModelRenderer shape2;

    public ModelTesseract() {
        this.textureWidth = 32;
        this.textureHeight = 16;
        this.shape1 = new ModelRenderer(this, 0, 0);
        this.shape1.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.shape1.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, 0.0F);
        this.shape2 = new ModelRenderer(this, 0, 12);
        this.shape2.setRotationPoint(0.0F, 21.0F, 0.0F);
        this.shape2.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.shape1.render(f5);
        this.shape2.render(f5);
    }

    public void renderModel(float f) {
        //this.shape2.render(f);
        this.shape1.render(f);
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
