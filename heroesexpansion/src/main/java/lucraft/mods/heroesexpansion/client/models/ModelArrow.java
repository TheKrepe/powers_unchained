package lucraft.mods.heroesexpansion.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelArrow extends ModelBase {

    public ModelRenderer shape4;
    public ModelRenderer shape12;
    public ModelRenderer shape13;
    public ModelRenderer shape16;
    public ModelRenderer shape17;
    public ModelRenderer shape18;
    public ModelRenderer shape20;
    public ModelRenderer shape5;
    public ModelRenderer shape6;
    public ModelRenderer shape7;
    public ModelRenderer shape8;
    public ModelRenderer shape9;
    public ModelRenderer shape10;
    public ModelRenderer shape11;
    public ModelRenderer shape15;
    public ModelRenderer shape21;
    public ModelRenderer shape19;
    public ModelRenderer shape14;
    public ModelRenderer shape44;
    public ModelRenderer shape44_1;

    public ModelArrow() {
        this.textureWidth = 100;
        this.textureHeight = 100;
        this.shape20 = new ModelRenderer(this, 24, 0);
        this.shape20.setRotationPoint(0.0F, 7.0F, 0.3F);
        this.shape20.addBox(2.5F, 0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.shape21 = new ModelRenderer(this, 88, 0);
        this.shape21.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.shape21.addBox(2.5F, 0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.shape14 = new ModelRenderer(this, 0, 2);
        this.shape14.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.shape14.addBox(1.5F, 0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.shape17 = new ModelRenderer(this, 16, 0);
        this.shape17.setRotationPoint(0.0F, 6.0F, 0.3F);
        this.shape17.addBox(0.5F, -0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.setRotateAngle(shape17, 0.0F, -2.1816615649929116F, 0.0F);
        this.shape10 = new ModelRenderer(this, 74, 0);
        this.shape10.setRotationPoint(1.84F, 2.4F, 0.0F);
        this.shape10.addBox(-0.5F, -1.5F, 0.0F, 2, 1, 1, 0.0F);
        this.setRotateAngle(shape10, 0.0F, 0.0F, -0.7853981633974483F);
        this.shape15 = new ModelRenderer(this, 84, 0);
        this.shape15.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.shape15.addBox(1.5F, 0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.shape12 = new ModelRenderer(this, 4, 0);
        this.shape12.setRotationPoint(0.0F, -44.0F, 0.3F);
        this.shape12.addBox(-0.5F, -0.5F, -0.5F, 1, 64, 1, 0.0F);
        this.shape9 = new ModelRenderer(this, 66, 0);
        this.shape9.setRotationPoint(1.41F, 2.83F, 0.0F);
        this.shape9.addBox(-0.5F, -0.5F, 0.0F, 3, 3, 1, 0.0F);
        this.setRotateAngle(shape9, 0.0F, 0.0F, -0.7853981633974483F);
        this.shape4 = new ModelRenderer(this, 0, 0);
        this.shape4.setRotationPoint(0.0F, -50.0F, 0.0F);
        this.shape4.addBox(-0.5F, -0.5F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(shape4, 0.0F, 0.0F, 0.7853981633974483F);
        this.shape18 = new ModelRenderer(this, 20, 0);
        this.shape18.setRotationPoint(0.0F, 6.0F, 0.3F);
        this.shape18.addBox(0.5F, -0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.setRotateAngle(shape18, 0.0F, 2.1816615649929116F, 0.0F);
        this.shape11 = new ModelRenderer(this, 80, 0);
        this.shape11.setRotationPoint(2.12F, 2.12F, 0.0F);
        this.shape11.addBox(-0.5F, -2.5F, 0.0F, 1, 1, 1, 0.0F);
        this.setRotateAngle(shape11, 0.0F, 0.0F, -0.7853981633974483F);
        this.shape5 = new ModelRenderer(this, 28, 0);
        this.shape5.setRotationPoint(-0.5F, 0.5F, 0.5F);
        this.shape5.addBox(0.0F, 0.0F, -0.5F, 1, 4, 1, 0.0F);
        this.setRotateAngle(shape5, 0.0F, 0.0F, -0.3490658503988659F);
        this.shape13 = new ModelRenderer(this, 8, 0);
        this.shape13.setRotationPoint(0.0F, 6.0F, 0.3F);
        this.shape13.addBox(1.5F, 0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.shape16 = new ModelRenderer(this, 12, 0);
        this.shape16.setRotationPoint(0.0F, 6.0F, 0.3F);
        this.shape16.addBox(0.5F, -0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.shape19 = new ModelRenderer(this, 92, 0);
        this.shape19.setRotationPoint(0.0F, 1.0F, 0.0F);
        this.shape19.addBox(2.5F, 0.5F, -0.5F, 1, 12, 1, 0.0F);
        this.shape6 = new ModelRenderer(this, 32, 0);
        this.shape6.setRotationPoint(0.5F, -0.5F, 0.5F);
        this.shape6.addBox(0.0F, 0.0F, -0.5F, 4, 1, 1, 0.0F);
        this.setRotateAngle(shape6, 0.0F, 0.0F, 0.3490658503988659F);
        this.shape8 = new ModelRenderer(this, 54, 0);
        this.shape8.setRotationPoint(0.5F, -0.5F, 0.5F);
        this.shape8.addBox(4.0F, 0.0F, -0.5F, 5, 5, 1, 0.0F);
        this.setRotateAngle(shape8, 0.0F, 0.0F, 0.3490658503988659F);
        this.shape7 = new ModelRenderer(this, 42, 0);
        this.shape7.setRotationPoint(-0.5F, 0.5F, 0.5F);
        this.shape7.addBox(0.0F, 4.0F, -0.5F, 5, 5, 1, 0.0F);
        this.setRotateAngle(shape7, 0.0F, 0.0F, -0.3490658503988659F);
        this.shape44 = new ModelRenderer(this, 10, 14);
        this.shape44.setRotationPoint(0.0F, -33.0F, 0.3F);
        this.shape44.addBox(-1.5F, -0.5F, -1.5F, 3, 16, 3, 0.0F);
        this.shape44_1 = new ModelRenderer(this, 24, 14);
        this.shape44_1.setRotationPoint(0.0F, -32.0F, 0.3F);
        this.shape44_1.addBox(-2.0F, -0.5F, -2.0F, 4, 14, 4, 0.0F);
        this.shape17.addChild(this.shape21);
        this.shape18.addChild(this.shape14);
        this.shape4.addChild(this.shape10);
        this.shape17.addChild(this.shape15);
        this.shape4.addChild(this.shape9);
        this.shape4.addChild(this.shape11);
        this.shape4.addChild(this.shape5);
        this.shape18.addChild(this.shape19);
        this.shape4.addChild(this.shape6);
        this.shape4.addChild(this.shape8);
        this.shape4.addChild(this.shape7);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.shape20.render(f5);
        this.shape17.render(f5);
        this.shape12.render(f5);
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.shape4.offsetX, this.shape4.offsetY, this.shape4.offsetZ);
        GlStateManager.translate(this.shape4.rotationPointX * f5, this.shape4.rotationPointY * f5, this.shape4.rotationPointZ * f5);
        GlStateManager.scale(1.0D, 1.0D, 0.6D);
        GlStateManager.translate(-this.shape4.offsetX, -this.shape4.offsetY, -this.shape4.offsetZ);
        GlStateManager.translate(-this.shape4.rotationPointX * f5, -this.shape4.rotationPointY * f5, -this.shape4.rotationPointZ * f5);
        this.shape4.render(f5);
        GlStateManager.popMatrix();
        this.shape18.render(f5);
        this.shape13.render(f5);
        this.shape16.render(f5);
        this.shape44.render(f5);
        this.shape44_1.render(f5);
    }

    public void renderModel(float f) {
        this.shape20.render(f);
        this.shape17.render(f);
        this.shape12.render(f);
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.shape4.offsetX, this.shape4.offsetY, this.shape4.offsetZ);
        GlStateManager.translate(this.shape4.rotationPointX * f, this.shape4.rotationPointY * f, this.shape4.rotationPointZ * f);
        GlStateManager.scale(1.0D, 1.0D, 0.6D);
        GlStateManager.translate(-this.shape4.offsetX, -this.shape4.offsetY, -this.shape4.offsetZ);
        GlStateManager.translate(-this.shape4.rotationPointX * f, -this.shape4.rotationPointY * f, -this.shape4.rotationPointZ * f);
        this.shape4.render(f);
        GlStateManager.popMatrix();
        this.shape18.render(f);
        this.shape13.render(f);
        this.shape16.render(f);
        this.shape44.render(f);
        this.shape44_1.render(f);
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
