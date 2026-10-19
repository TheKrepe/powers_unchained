package net.threetag.pymtech.client.renderer.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.util.math.MathHelper;
import net.threetag.threecore.util.RenderUtil;

public class WaspWingsModel<T extends LivingEntity> extends BipedModel<T> {

    public ModelRenderer wingHolder;
    public ModelRenderer wingbigLeft;
    public ModelRenderer wingbigRight;
    public ModelRenderer wingsmallLeft;
    public ModelRenderer wingsmallRight;
    public ModelRenderer detail;
    public ModelRenderer texture;
    public ModelRenderer texture_1;
    public ModelRenderer texture_2;
    public ModelRenderer texture_3;

    public WaspWingsModel(float modelSize) {
        super(modelSize);

        this.textureWidth = 64;
        this.textureHeight = 32;
        this.texture_3 = new ModelRenderer(this, 0, 21);
        this.texture_3.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.texture_3.addBox(-12.0F, 0.0F, 0.0F, 12, 4, 0, 0.0F);
        this.wingHolder = new ModelRenderer(this, 0, 0);
        this.wingHolder.setRotationPoint(0.0F, 4.5F, 1.5F);
        this.wingHolder.addBox(-2.0F, -2.0F, 0.0F, 4, 4, 1, 0.0F);
        this.detail = new ModelRenderer(this, 12, 0);
        this.detail.setRotationPoint(0.0F, 0.0F, 0.7F);
        this.detail.addBox(-1.5F, -1.5F, 0.0F, 3, 3, 1, 0.0F);
        this.texture_1 = new ModelRenderer(this, 10, 13);
        this.texture_1.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.texture_1.addBox(-16.0F, 0.0F, 0.0F, 16, 6, 0, 0.0F);
        this.wingbigLeft = new ModelRenderer(this, 10, 10);
        this.wingbigLeft.setRotationPoint(1.0F, -1.0F, 1.0F);
        this.wingbigLeft.addBox(0.0F, -0.5F, -0.5F, 12, 1, 1, 0.0F);
        this.setRotateAngle(wingbigLeft, 0.0F, -0.0F, -0.7853981633974483F);
        this.texture = new ModelRenderer(this, 10, 13);
        this.texture.mirror = true;
        this.texture.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.texture.addBox(0.0F, 0.0F, 0.0F, 16, 6, 0, 0.0F);
        this.wingbigRight = new ModelRenderer(this, 10, 10);
        this.wingbigRight.mirror = true;
        this.wingbigRight.setRotationPoint(-1.0F, -1.0F, 1.0F);
        this.wingbigRight.addBox(-12.0F, -0.5F, -0.5F, 12, 1, 1, 0.0F);
        this.setRotateAngle(wingbigRight, 0.0F, 0.0F, 0.7853981633974483F);
        this.wingsmallRight = new ModelRenderer(this, 0, 6);
        this.wingsmallRight.mirror = true;
        this.wingsmallRight.setRotationPoint(-1.0F, 1.0F, 1.0F);
        this.wingsmallRight.addBox(-8.0F, -0.5F, -0.5F, 8, 1, 1, 0.0F);
        this.setRotateAngle(wingsmallRight, 0.0F, 0.0F, -0.7853981633974483F);
        this.wingsmallLeft = new ModelRenderer(this, 0, 6);
        this.wingsmallLeft.setRotationPoint(1.0F, 1.0F, 1.0F);
        this.wingsmallLeft.addBox(0.0F, -0.5F, -0.5F, 8, 1, 1, 0.0F);
        this.setRotateAngle(wingsmallLeft, 0.0F, 0.0F, 0.7853981633974483F);
        this.texture_2 = new ModelRenderer(this, 0, 21);
        this.texture_2.mirror = true;
        this.texture_2.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.texture_2.addBox(0.0F, 0.0F, 0.0F, 12, 4, 0, 0.0F);

        this.bipedBody.addChild(this.wingHolder);
        this.wingsmallRight.addChild(this.texture_3);
        this.wingHolder.addChild(this.detail);
        this.wingbigRight.addChild(this.texture_1);
        this.wingHolder.addChild(this.wingbigLeft);
        this.wingbigLeft.addChild(this.texture);
        this.wingHolder.addChild(this.wingbigRight);
        this.wingHolder.addChild(this.wingsmallRight);
        this.wingHolder.addChild(this.wingsmallLeft);
        this.wingsmallLeft.addChild(this.texture_2);

        this.bipedBody.cubeList.clear();
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        boolean still = entityIn instanceof ArmorStandEntity;

        float h = still ? -0.4F : (-0.4F + MathHelper.cos((Minecraft.getInstance().player.ticksExisted + RenderUtil.renderTickTime) * 3F) / 4);

        this.wingbigRight.rotateAngleY = -h;
        this.wingbigLeft.rotateAngleY = h;
        this.wingsmallRight.rotateAngleY = -h;
        this.wingsmallLeft.rotateAngleY = h;
    }

}
