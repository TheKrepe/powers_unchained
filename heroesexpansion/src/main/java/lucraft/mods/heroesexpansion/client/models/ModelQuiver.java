package lucraft.mods.heroesexpansion.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelQuiver extends ModelBase {

    public ModelRenderer shape1;
    public ModelRenderer strip1;
    public ModelRenderer strip2;
    public ModelRenderer strip3;
    public ModelRenderer strip4;
    public ModelRenderer strip5;
    public ModelRenderer strip6;
    public ModelRenderer strip7;
    public ModelRenderer shape2;
    public ModelRenderer shape3;
    public ModelRenderer shape4;
    public ModelRenderer shape5;

    public ModelQuiver() {
        this.textureWidth = 64;
        this.textureHeight = 64;
        this.shape4 = new ModelRenderer(this, 18, 0);
        this.shape4.setRotationPoint(-1.0F, 0.0F, 2.1F);
        this.shape4.addBox(0.0F, 0.0F, 0.0F, 2, 10, 1, 0.0F);
        this.strip6 = new ModelRenderer(this, 38, 0);
        this.strip6.setRotationPoint(-4.4F, 8.0F, -2.3F);
        this.strip6.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.shape2 = new ModelRenderer(this, 6, 0);
        this.shape2.setRotationPoint(0.6F, 0.0F, 0.6F);
        this.shape2.addBox(0.0F, 0.0F, 0.0F, 1, 10, 2, 0.0F);
        this.strip3 = new ModelRenderer(this, 38, 0);
        this.strip3.setRotationPoint(3.3F, 1.0F, -2.3F);
        this.strip3.addBox(0.0F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
        this.shape3 = new ModelRenderer(this, 12, 0);
        this.shape3.setRotationPoint(-1.6F, 0.0F, 0.6F);
        this.shape3.addBox(0.0F, 0.0F, 0.0F, 1, 10, 2, 0.0F);
        this.strip5 = new ModelRenderer(this, 38, 0);
        this.strip5.setRotationPoint(-4.4F, 8.0F, -1.7F);
        this.strip5.addBox(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
        this.strip2 = new ModelRenderer(this, 39, 0);
        this.strip2.setRotationPoint(-1.3F, 2.7F, 1.3F);
        this.strip2.addBox(0.0F, 0.0F, 0.0F, 5, 1, 1, 0.0F);
        this.setRotateAngle(strip2, 0.0F, 0.0F, -0.3490658503988659F);
        this.shape1 = new ModelRenderer(this, 0, 0);
        this.shape1.setRotationPoint(-2.0F, 0.8F, 2.0F);
        this.shape1.addBox(-1.0F, 0.0F, 0.0F, 2, 10, 1, 0.0F);
        this.setRotateAngle(shape1, 0.0F, 0.0F, -0.3490658503988659F);
        this.strip1 = new ModelRenderer(this, 38, 0);
        this.strip1.setRotationPoint(3.3F, 1.0F, -1.7F);
        this.strip1.addBox(0.0F, 0.0F, 0.0F, 1, 1, 4, 0.0F);
        this.strip4 = new ModelRenderer(this, 39, 0);
        this.strip4.setRotationPoint(-0.3F, 6.7F, 1.3F);
        this.strip4.addBox(-4.0F, 0.0F, 0.0F, 4, 1, 1, 0.0F);
        this.setRotateAngle(strip4, 0.0F, 0.0F, -0.3490658503988659F);
        this.strip7 = new ModelRenderer(this, 39, 0);
        this.strip7.setRotationPoint(-4.0F, 8.1F, -2.3F);
        this.strip7.addBox(0.0F, 0.0F, 0.0F, 10, 1, 1, 0.0F);
        this.setRotateAngle(strip7, 0.0F, 0.0F, -0.7494443808063651F);
        this.shape5 = new ModelRenderer(this, 24, 0);
        this.shape5.setRotationPoint(-1.0F, 10.0F, 0.6F);
        this.shape5.addBox(0.0F, 0.0F, 0.0F, 2, 1, 2, 0.0F);
        this.shape1.addChild(this.shape4);
        this.shape1.addChild(this.shape2);
        this.shape1.addChild(this.shape3);
        this.shape1.addChild(this.shape5);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.strip6.render(f5);
        this.strip3.render(f5);
        this.strip5.render(f5);
        this.strip2.render(f5);
        this.shape1.render(f5);
        this.strip1.render(f5);
        this.strip4.render(f5);
        this.strip7.render(f5);
    }

    public void render(float f) {
        this.strip6.render(f);
        this.strip3.render(f);
        this.strip5.render(f);
        this.strip2.render(f);
        this.shape1.render(f);
        this.strip1.render(f);
        this.strip4.render(f);
        this.strip7.render(f);
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
