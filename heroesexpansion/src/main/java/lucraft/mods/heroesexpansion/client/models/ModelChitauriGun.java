package lucraft.mods.heroesexpansion.client.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * Gun - by Sheriff Created using Tabula 7.0.0
 */
public class ModelChitauriGun extends ModelBase {

    public ModelRenderer shape1;
    public ModelRenderer shape1_1;
    public ModelRenderer shape1_2;
    public ModelRenderer shape1_3;
    public ModelRenderer shape1_4;
    public ModelRenderer shape1_5;
    public ModelRenderer shape1_6;
    public ModelRenderer shape1_7;
    public ModelRenderer shape1_8;
    public ModelRenderer shape1_9;
    public ModelRenderer shape1_10;
    public ModelRenderer shape1_11;
    public ModelRenderer shape1_12;
    public ModelRenderer shape1_13;
    public ModelRenderer shape1_14;
    public ModelRenderer shape1_15;
    public ModelRenderer shape1_16;
    public ModelRenderer shape1_17;
    public ModelRenderer shape1_18;
    public ModelRenderer shape1_19;

    public ModelChitauriGun() {
        this.textureWidth = 48;
        this.textureHeight = 48;
        this.shape1_12 = new ModelRenderer(this, 23, 16);
        this.shape1_12.setRotationPoint(-8.0F, 1.4F, -1.0F);
        this.shape1_12.addBox(0.0F, 0.0F, 0.0F, 8, 1, 3, 0.0F);
        this.shape1_4 = new ModelRenderer(this, 21, 4);
        this.shape1_4.setRotationPoint(-9.0F, 1.9F, -1.0F);
        this.shape1_4.addBox(-2.5F, -2.0F, 0.0F, 3, 2, 3, 0.0F);
        this.setRotateAngle(shape1_4, 0.0F, 0.0F, 0.08726646259971645F);
        this.shape1_17 = new ModelRenderer(this, 5, 7);
        this.shape1_17.setRotationPoint(0.0F, 3.0F, -0.5F);
        this.shape1_17.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2, 0.0F);
        this.shape1_2 = new ModelRenderer(this, 24, 0);
        this.shape1_2.setRotationPoint(-11.0F, -1.6F, -0.5F);
        this.shape1_2.addBox(0.0F, 0.0F, 0.0F, 3, 2, 2, 0.0F);
        this.shape1_11 = new ModelRenderer(this, 23, 12);
        this.shape1_11.setRotationPoint(1.0F, -1.0F, -1.0F);
        this.shape1_11.addBox(0.0F, 0.0F, 0.0F, 5, 1, 3, 0.0F);
        this.shape1_14 = new ModelRenderer(this, 30, 4);
        this.shape1_14.setRotationPoint(3.0F, 3.0F, -0.5F);
        this.shape1_14.addBox(0.0F, -0.4F, 0.0F, 1, 1, 2, 0.0F);
        this.shape1_10 = new ModelRenderer(this, 39, 0);
        this.shape1_10.setRotationPoint(-3.5F, 3.9999999999999996F, -0.5F);
        this.shape1_10.addBox(-2.0F, -1.0F, 0.0F, 2, 1, 2, 0.0F);
        this.setRotateAngle(shape1_10, 0.0F, 0.0F, 0.9599310885968813F);
        this.shape1 = new ModelRenderer(this, 0, 0);
        this.shape1.setRotationPoint(-2.0F, 0.6F, 0.5F);
        this.shape1.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.0F);
        this.shape1_8 = new ModelRenderer(this, 30, 8);
        this.shape1_8.setRotationPoint(-8.0F, -1.1F, -1.0F);
        this.shape1_8.addBox(0.0F, 0.0F, 0.0F, 2, 1, 3, 0.0F);
        this.shape1_19 = new ModelRenderer(this, 28, 20);
        this.shape1_19.setRotationPoint(1.0F, -0.6F, -0.5F);
        this.shape1_19.addBox(0.0F, 0.0F, 0.0F, 5, 3, 2, 0.0F);
        this.shape1_3 = new ModelRenderer(this, 34, 0);
        this.shape1_3.setRotationPoint(6.0F, -1.0F, -1.0F);
        this.shape1_3.addBox(0.0F, 0.0F, 0.0F, 1, 1, 3, 0.0F);
        this.setRotateAngle(shape1_3, 0.0F, 0.0F, 0.7853981633974483F);
        this.shape1_13 = new ModelRenderer(this, 0, 17);
        this.shape1_13.setRotationPoint(-1.5F, 2.4F, -1.0F);
        this.shape1_13.addBox(0.0F, 0.0F, 0.0F, 7, 1, 3, 0.0F);
        this.shape1_6 = new ModelRenderer(this, 0, 7);
        this.shape1_6.setRotationPoint(-9.0F, -1.1F, -1.0F);
        this.shape1_6.addBox(0.0F, 0.0F, 0.0F, 1, 3, 3, 0.0F);
        this.shape1_7 = new ModelRenderer(this, 8, 9);
        this.shape1_7.setRotationPoint(-5.0F, -1.1F, -1.0F);
        this.shape1_7.addBox(0.0F, 0.0F, 0.0F, 6, 1, 3, 0.0F);
        this.shape1_16 = new ModelRenderer(this, 18, 20);
        this.shape1_16.setRotationPoint(-3.5F, 3.0F, -0.5F);
        this.shape1_16.addBox(0.0F, 0.0F, 0.0F, 3, 1, 2, 0.0F);
        this.shape1_9 = new ModelRenderer(this, 0, 13);
        this.shape1_9.setRotationPoint(-10.0F, -2.1F, -1.0F);
        this.shape1_9.addBox(0.0F, 0.0F, 0.0F, 10, 1, 3, 0.0F);
        this.shape1_15 = new ModelRenderer(this, 37, 9);
        this.shape1_15.setRotationPoint(-6.0F, -1.1F, -1.0F);
        this.shape1_15.addBox(0.0F, 0.0F, 0.0F, 1, 3, 3, 0.0F);
        this.shape1_18 = new ModelRenderer(this, 23, 9);
        this.shape1_18.setRotationPoint(1.5F, 3.0F, -0.5F);
        this.shape1_18.addBox(0.0F, -0.2F, 0.0F, 1, 1, 2, 0.0F);
        this.shape1_1 = new ModelRenderer(this, 4, 0);
        this.shape1_1.setRotationPoint(-8.0F, -2.6F, -0.5F);
        this.shape1_1.addBox(0.0F, 0.0F, 0.0F, 8, 5, 2, 0.0F);
        this.shape1_5 = new ModelRenderer(this, 33, 4);
        this.shape1_5.setRotationPoint(0.0F, -2.1F, -1.0F);
        this.shape1_5.addBox(0.0F, 0.0F, 0.0F, 3, 1, 3, 0.0F);
        this.setRotateAngle(shape1_5, 0.0F, 0.0F, 0.3665191429188092F);
        this.shape1.addChild(this.shape1_12);
        this.shape1.addChild(this.shape1_4);
        this.shape1.addChild(this.shape1_17);
        this.shape1.addChild(this.shape1_2);
        this.shape1.addChild(this.shape1_11);
        this.shape1.addChild(this.shape1_14);
        this.shape1.addChild(this.shape1_10);
        this.shape1.addChild(this.shape1_8);
        this.shape1.addChild(this.shape1_19);
        this.shape1.addChild(this.shape1_3);
        this.shape1.addChild(this.shape1_13);
        this.shape1.addChild(this.shape1_6);
        this.shape1.addChild(this.shape1_7);
        this.shape1.addChild(this.shape1_16);
        this.shape1.addChild(this.shape1_9);
        this.shape1.addChild(this.shape1_15);
        this.shape1.addChild(this.shape1_18);
        this.shape1.addChild(this.shape1_1);
        this.shape1.addChild(this.shape1_5);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.shape1.offsetX, this.shape1.offsetY, this.shape1.offsetZ);
        GlStateManager.translate(this.shape1.rotationPointX * f5, this.shape1.rotationPointY * f5, this.shape1.rotationPointZ * f5);
        GlStateManager.scale(1.0D, 1.0D, 0.7D);
        GlStateManager.translate(-this.shape1.offsetX, -this.shape1.offsetY, -this.shape1.offsetZ);
        GlStateManager.translate(-this.shape1.rotationPointX * f5, -this.shape1.rotationPointY * f5, -this.shape1.rotationPointZ * f5);
        this.shape1.render(f5);
        GlStateManager.popMatrix();
    }

    public void renderModel(float f) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.shape1.offsetX, this.shape1.offsetY, this.shape1.offsetZ);
        GlStateManager.translate(this.shape1.rotationPointX * f, this.shape1.rotationPointY * f, this.shape1.rotationPointZ * f);
        GlStateManager.scale(1.0D, 1.0D, 0.7D);
        GlStateManager.translate(-this.shape1.offsetX, -this.shape1.offsetY, -this.shape1.offsetZ);
        GlStateManager.translate(-this.shape1.rotationPointX * f, -this.shape1.rotationPointY * f, -this.shape1.rotationPointZ * f);
        this.shape1.render(f);
        GlStateManager.popMatrix();
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
