package lucraft.mods.heroesexpansion.client.models;

import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Leviathan - Neon Created using Tabula 4.1.1
 */
public class ModelLeviathan extends ModelBase {

    public ModelRenderer segment1;
    public ModelRenderer detail1;
    public ModelRenderer segment2;
    public ModelRenderer plate1;
    public ModelRenderer headMain;
    public ModelRenderer detail2;
    public ModelRenderer segment3;
    public ModelRenderer plate2;
    public ModelRenderer detail3;
    public ModelRenderer segment4;
    public ModelRenderer plate3;
    public ModelRenderer wing1L;
    public ModelRenderer wing1R;
    public ModelRenderer detail4;
    public ModelRenderer segment5;
    public ModelRenderer plate4;
    public ModelRenderer detail5;
    public ModelRenderer tailStart;
    public ModelRenderer plate5;
    public ModelRenderer detailTail;
    public ModelRenderer tail2;
    public ModelRenderer plate6;
    public ModelRenderer wing2L;
    public ModelRenderer wing2R;
    public ModelRenderer detailT2;
    public ModelRenderer tail3;
    public ModelRenderer plate7;
    public ModelRenderer tailEnd;
    public ModelRenderer spin5;
    public ModelRenderer spin4;
    public ModelRenderer spin2;
    public ModelRenderer shape49;
    public ModelRenderer spin1;
    public ModelRenderer jawDown;
    public ModelRenderer plateHead;

    public ModelLeviathan() {
        this.textureWidth = 256;
        this.textureHeight = 128;
        this.spin1 = new ModelRenderer(this, 246, 48);
        this.spin1.setRotationPoint(5.5F, -3.0F, -3.0F);
        this.spin1.addBox(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
        this.plate1 = new ModelRenderer(this, 140, 0);
        this.plate1.setRotationPoint(-6.5F, -3.5F, -0.3F);
        this.plate1.addBox(0.0F, -1.0F, -5.0F, 13, 2, 5, 0.0F);
        this.setRotateAngle(plate1, 0.136659280431156F, 0.0F, 0.0F);
        this.wing2R = new ModelRenderer(this, 100, 100);
        this.wing2R.mirror = true;
        this.wing2R.setRotationPoint(-5.0F, 0.0F, -5.0F);
        this.wing2R.addBox(-7.0F, -0.5F, -1.5F, 7, 1, 3, 0.0F);
        this.setRotateAngle(wing2R, 0.0F, 0.136659280431156F, -0.136659280431156F);
        this.detail3 = new ModelRenderer(this, 87, 22);
        this.detail3.setRotationPoint(-5.5F, 5.5F, -5.5F);
        this.detail3.addBox(0.0F, 0.0F, 0.0F, 11, 4, 5, 0.0F);
        this.detail5 = new ModelRenderer(this, 47, 65);
        this.detail5.setRotationPoint(-4.0F, 3.5F, -8.5F);
        this.detail5.addBox(0.0F, 0.0F, 0.0F, 8, 3, 8, 0.0F);
        this.spin4 = new ModelRenderer(this, 246, 3);
        this.spin4.setRotationPoint(4.5F, -6.0F, 1.6F);
        this.spin4.addBox(0.0F, 0.0F, 0.0F, 2, 5, 2, 0.0F);
        this.wing1R = new ModelRenderer(this, 128, 70);
        this.wing1R.mirror = true;
        this.wing1R.setRotationPoint(-6.5F, 0.0F, -3.0F);
        this.wing1R.addBox(-13.0F, -1.0F, -3.0F, 13, 2, 6, 0.0F);
        this.setRotateAngle(wing1R, 0.0F, 0.091106186954104F, -0.136659280431156F);
        this.jawDown = new ModelRenderer(this, 200, 83);
        this.jawDown.setRotationPoint(0.0F, 2.0F, 0.0F);
        this.jawDown.addBox(-4.0F, 0.0F, -8.0F, 8, 2, 8, 0.0F);
        this.setRotateAngle(jawDown, 0.136659280431156F, 0.0F, 0.0F);
        this.shape49 = new ModelRenderer(this, 246, 25);
        this.shape49.setRotationPoint(5.0F, -7.0F, 6.4F);
        this.shape49.addBox(0.0F, 0.0F, 0.0F, 2, 6, 2, 0.0F);
        this.plate3 = new ModelRenderer(this, 136, 17);
        this.plate3.setRotationPoint(-7.5F, -3.5F, 0.0F);
        this.plate3.addBox(0.0F, -1.0F, -6.0F, 15, 2, 6, 0.0F);
        this.setRotateAngle(plate3, 0.136659280431156F, 0.0F, 0.0F);
        this.detail4 = new ModelRenderer(this, 0, 65);
        this.detail4.setRotationPoint(-5.0F, 4.5F, -7.5F);
        this.detail4.addBox(0.0F, 0.0F, 0.0F, 10, 4, 7, 0.0F);
        this.segment3 = new ModelRenderer(this, 87, 0);
        this.segment3.setRotationPoint(0.0F, -0.2F, 5.5F);
        this.segment3.addBox(-7.0F, -3.5F, -6.0F, 14, 9, 6, 0.0F);
        this.tailStart = new ModelRenderer(this, 0, 87);
        this.tailStart.setRotationPoint(0.0F, -0.6F, 9.5F);
        this.tailStart.addBox(-5.5F, -3.0F, -10.0F, 11, 6, 10, 0.0F);
        this.segment4 = new ModelRenderer(this, 0, 47);
        this.segment4.setRotationPoint(0.0F, 0.2F, 7.5F);
        this.segment4.addBox(-6.5F, -3.5F, -8.0F, 13, 8, 8, 0.0F);
        this.plate4 = new ModelRenderer(this, 133, 35);
        this.plate4.setRotationPoint(-7.0F, -3.5F, 0.5F);
        this.plate4.addBox(0.0F, -1.0F, -8.0F, 14, 2, 8, 0.0F);
        this.setRotateAngle(plate4, 0.091106186954104F, 0.0F, 0.0F);
        this.spin5 = new ModelRenderer(this, 246, 13);
        this.spin5.setRotationPoint(-1.0F, -6.0F, -5.4F);
        this.spin5.addBox(0.0F, 0.0F, 0.0F, 2, 4, 2, 0.0F);
        this.detail1 = new ModelRenderer(this, 0, 22);
        this.detail1.setRotationPoint(-5.0F, 3.5F, -4.5F);
        this.detail1.addBox(0.0F, 0.0F, 0.0F, 10, 4, 4, 0.0F);
        this.tail3 = new ModelRenderer(this, 93, 40);
        this.tail3.setRotationPoint(0.0F, -0.6F, 6.5F);
        this.tail3.addBox(-4.5F, -2.0F, -7.0F, 9, 5, 7, 0.0F);
        this.detail2 = new ModelRenderer(this, 47, 22);
        this.detail2.setRotationPoint(-5.5F, 4.5F, -5.5F);
        this.detail2.addBox(0.0F, 0.0F, 0.0F, 11, 4, 5, 0.0F);
        this.wing1L = new ModelRenderer(this, 128, 70);
        this.wing1L.setRotationPoint(6.5F, 0.0F, -3.0F);
        this.wing1L.addBox(0.0F, -1.0F, -3.0F, 13, 2, 6, 0.0F);
        this.setRotateAngle(wing1L, 0.0F, -0.091106186954104F, 0.136659280431156F);
        this.spin2 = new ModelRenderer(this, 246, 39);
        this.spin2.setRotationPoint(5.0F, -5.0F, 2.0F);
        this.spin2.addBox(0.0F, 0.0F, 0.0F, 2, 4, 2, 0.0F);
        this.plateHead = new ModelRenderer(this, 159, 100);
        this.plateHead.setRotationPoint(0.0F, -4.3F, -0.3F);
        this.plateHead.addBox(-4.5F, -1.0F, -8.0F, 9, 2, 8, 0.0F);
        this.setRotateAngle(plateHead, 0.091106186954104F, 0.0F, 0.0F);
        this.plate5 = new ModelRenderer(this, 185, 13);
        this.plate5.setRotationPoint(-6.5F, -3.5F, 0.6F);
        this.plate5.addBox(0.0F, -1.0F, -9.0F, 13, 2, 9, 0.0F);
        this.setRotateAngle(plate5, 0.045553093477052F, 0.0F, 0.0F);
        this.plate2 = new ModelRenderer(this, 185, 0);
        this.plate2.setRotationPoint(-7.0F, -3.5F, -0.3F);
        this.plate2.addBox(0.0F, -1.0F, -6.0F, 14, 2, 6, 0.0F);
        this.setRotateAngle(plate2, 0.136659280431156F, 0.0F, 0.0F);
        this.wing2L = new ModelRenderer(this, 100, 100);
        this.wing2L.setRotationPoint(5.0F, 0.0F, -5.0F);
        this.wing2L.addBox(0.0F, -0.5F, -1.5F, 7, 1, 3, 0.0F);
        this.setRotateAngle(wing2L, 0.0F, -0.136659280431156F, 0.136659280431156F);
        this.segment1 = new ModelRenderer(this, 0, 0);
        this.segment1.setRotationPoint(0.0F, 0.0F, -14.0F);
        this.segment1.addBox(-6.0F, -3.5F, -5.0F, 12, 7, 5, 0.0F);
        this.tail2 = new ModelRenderer(this, 47, 87);
        this.tail2.setRotationPoint(0.0F, -0.2F, 7.5F);
        this.tail2.addBox(-5.0F, -2.5F, -8.0F, 10, 5, 8, 0.0F);
        this.plate6 = new ModelRenderer(this, 178, 35);
        this.plate6.setRotationPoint(-6.0F, -2.5F, -9.3F);
        this.plate6.addBox(0.0F, -1.0F, 0.0F, 12, 2, 10, 0.0F);
        this.detailTail = new ModelRenderer(this, 0, 105);
        this.detailTail.setRotationPoint(-3.5F, 3.0F, -9.5F);
        this.detailTail.addBox(0.0F, 0.0F, 0.0F, 7, 2, 9, 0.0F);
        this.tailEnd = new ModelRenderer(this, 87, 67);
        this.tailEnd.setRotationPoint(0.0F, 0.5F, -0.5F);
        this.tailEnd.addBox(-4.0F, -2.0F, 0.0F, 8, 4, 4, 0.0F);
        this.plate7 = new ModelRenderer(this, 158, 53);
        this.plate7.setRotationPoint(-5.5F, -2.1F, -7.4F);
        this.plate7.addBox(0.0F, -1.0F, 0.0F, 11, 2, 8, 0.0F);
        this.setRotateAngle(plate7, -0.045553093477052F, 0.0F, 0.0F);
        this.segment2 = new ModelRenderer(this, 44, 0);
        this.segment2.setRotationPoint(0.0F, -0.2F, 5.5F);
        this.segment2.addBox(-6.5F, -3.5F, -6.0F, 13, 8, 6, 0.0F);
        this.segment5 = new ModelRenderer(this, 47, 47);
        this.segment5.setRotationPoint(0.0F, 0.2F, 8.5F);
        this.segment5.addBox(-6.0F, -3.5F, -9.0F, 12, 7, 9, 0.0F);
        this.detailT2 = new ModelRenderer(this, 47, 105);
        this.detailT2.setRotationPoint(-3.0F, 2.5F, -7.5F);
        this.detailT2.addBox(0.0F, 0.0F, 0.0F, 6, 2, 7, 0.0F);
        this.headMain = new ModelRenderer(this, 200, 100);
        this.headMain.setRotationPoint(0.0F, 1.5F, -2.6F);
        this.headMain.addBox(-4.0F, -4.0F, -8.0F, 8, 6, 8, 0.0F);
        this.setRotateAngle(headMain, 0.136659280431156F, 0.0F, 0.0F);
        this.plate5.addChild(this.spin1);
        this.segment1.addChild(this.plate1);
        this.tailStart.addChild(this.wing2R);
        this.segment3.addChild(this.detail3);
        this.segment5.addChild(this.detail5);
        this.plate7.addChild(this.spin4);
        this.segment3.addChild(this.wing1R);
        this.headMain.addChild(this.jawDown);
        this.plate6.addChild(this.shape49);
        this.segment3.addChild(this.plate3);
        this.segment4.addChild(this.detail4);
        this.segment2.addChild(this.segment3);
        this.segment5.addChild(this.tailStart);
        this.segment3.addChild(this.segment4);
        this.segment4.addChild(this.plate4);
        this.tail3.addChild(this.spin5);
        this.segment1.addChild(this.detail1);
        this.tail2.addChild(this.tail3);
        this.segment2.addChild(this.detail2);
        this.segment3.addChild(this.wing1L);
        this.plate6.addChild(this.spin2);
        this.headMain.addChild(this.plateHead);
        this.segment5.addChild(this.plate5);
        this.segment2.addChild(this.plate2);
        this.tailStart.addChild(this.wing2L);
        this.tailStart.addChild(this.tail2);
        this.tailStart.addChild(this.plate6);
        this.tailStart.addChild(this.detailTail);
        this.tail3.addChild(this.tailEnd);
        this.tail2.addChild(this.plate7);
        this.segment1.addChild(this.segment2);
        this.segment4.addChild(this.segment5);
        this.tail2.addChild(this.detailT2);
        this.segment1.addChild(this.headMain);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.segment1.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        this.wing1R.rotateAngleZ = MathHelper.sin((entityIn.ticksExisted + LCRenderHelper.renderTick) / 20F) / 2F;
        this.wing1L.rotateAngleZ = -this.wing1R.rotateAngleZ;

        this.wing2R.rotateAngleZ = MathHelper.cos((entityIn.ticksExisted + LCRenderHelper.renderTick) / 20F) / 2F;
        this.wing2L.rotateAngleZ = -this.wing2R.rotateAngleZ;

        double pi = Math.PI;
        float f = (entityIn.ticksExisted + LCRenderHelper.renderTick) / 25F;

        this.segment1.rotateAngleX = MathHelper.cos(f) / 10F;
        this.headMain.rotateAngleX = -this.segment1.rotateAngleX;

        this.segment2.rotateAngleX = MathHelper.cos((float) (f + (pi / 4F))) / 10F;

        this.segment3.rotateAngleX = MathHelper.cos((float) (f + (pi / 2F))) / 10F;

        this.segment4.rotateAngleX = MathHelper.cos((float) (f + (pi * 0.75F))) / 10F;

        this.segment5.rotateAngleX = MathHelper.cos((float) (f + pi)) / 10F;

        this.tailStart.rotateAngleX = MathHelper.cos((float) (f + (pi * 2F))) / 10F;

        this.tail2.rotateAngleX = MathHelper.cos((float) (f + (pi * 1.5F))) / 10F;

        this.tail3.rotateAngleX = MathHelper.cos((float) (f + (pi * 1.75F))) / 10F;

        this.tailEnd.rotateAngleX = MathHelper.cos((float) (f + (pi * 2F))) / 10F;
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