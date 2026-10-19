package lucraft.mods.heroesexpansion.client.models;

import lucraft.mods.lucraftcore.superpowers.models.ModelBipedSuitSet;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ModelFalconWings extends ModelBipedSuitSet {

    public ResourceLocation WINGS_TEXTURE;
    public ModelRenderer rightWing1;
    public ModelRenderer leftWing1;
    public ModelRenderer rightWing2;
    public ModelRenderer rightWing3;
    public ModelRenderer rightWing4;
    public ModelRenderer rightWing5;
    public ModelRenderer rightWing6;
    public ModelRenderer leftWing2;
    public ModelRenderer leftWing3;
    public ModelRenderer leftWing4;
    public ModelRenderer leftWing5;
    public ModelRenderer leftWing6;

    public ModelFalconWings(float f, String normalTex, String lightTex, SuitSet hero, EntityEquipmentSlot slot, boolean hasSmallArms, boolean vibrating, ResourceLocation wingTexture) {
        this(f, normalTex, lightTex, hero, slot, hasSmallArms, vibrating, 64, 64, wingTexture);
    }

    public ModelFalconWings(float f, String normalTex, String lightTex, SuitSet hero, EntityEquipmentSlot slot, boolean hasSmallArms, boolean vibrating, int width, int height, ResourceLocation wingTexture) {
        super(f, normalTex, lightTex, hero, slot, hasSmallArms, vibrating, width, height);

        this.WINGS_TEXTURE = wingTexture;
        leftWing1 = new ModelRenderer(this, 0, 0);
        leftWing1.setRotationPoint(0.95F, 2.0F, 1.6F);
        leftWing1.addBox(-2.0F, -2.0F, -0.5F, 5, 7, 1, 0.0F);
        setRotateAngle(leftWing1, 0.0F, 0.0F, 0.6981317007977318F);
        rightWing5 = new ModelRenderer(this, 0, 16);
        rightWing5.mirror = true;
        rightWing5.setRotationPoint(-4.0F, 0.0F, 0.0F);
        rightWing5.addBox(-4.0F, 0.0F, -0.5F, 4, 7, 1, 0.0F);
        setRotateAngle(rightWing5, 0.0F, 0.017453292519943295F, -0.5009094953223726F);
        rightWing2 = new ModelRenderer(this, 12, 0);
        rightWing2.mirror = true;
        rightWing2.setRotationPoint(-3.0F, -2.0F, 0.0F);
        rightWing2.addBox(-5.0F, 0.0F, -0.5F, 5, 7, 1, 0.0F);
        setRotateAngle(rightWing2, 0.0F, 0.017453292519943295F, -0.8726646259971648F);
        rightWing6 = new ModelRenderer(this, 10, 16);
        rightWing6.mirror = true;
        rightWing6.setRotationPoint(-4.0F, 0.0F, 0.0F);
        rightWing6.addBox(-4.0F, 0.0F, -0.5F, 4, 7, 1, 0.0F);
        setRotateAngle(rightWing6, 0.0F, 0.017453292519943295F, -1.3203415791337103F);
        leftWing4 = new ModelRenderer(this, 10, 8);
        leftWing4.setRotationPoint(4.0F, 0.0F, 0.0F);
        leftWing4.addBox(0.0F, 0.0F, -0.5F, 4, 7, 1, 0.0F);
        setRotateAngle(leftWing4, 0.0F, -0.017453292519943295F, 1.0821041362364843F);
        rightWing3 = new ModelRenderer(this, 0, 8);
        rightWing3.mirror = true;
        rightWing3.setRotationPoint(-5.0F, 0.0F, 0.0F);
        rightWing3.addBox(-4.0F, 0.0F, -0.5F, 4, 7, 1, 0.0F);
        setRotateAngle(rightWing3, 0.0F, 0.017453292519943295F, -0.9948376736367678F);
        leftWing2 = new ModelRenderer(this, 12, 0);
        leftWing2.setRotationPoint(3.0F, -2.0F, 0.0F);
        leftWing2.addBox(0.0F, 0.0F, -0.5F, 5, 7, 1, 0.0F);
        setRotateAngle(leftWing2, 0.0F, -0.017453292519943295F, 0.8726646259971648F);
        leftWing6 = new ModelRenderer(this, 10, 16);
        leftWing6.setRotationPoint(4.0F, 0.0F, 0.0F);
        leftWing6.addBox(0.0F, 0.0F, -0.5F, 4, 7, 1, 0.0F);
        setRotateAngle(leftWing6, 0.0F, -0.017453292519943295F, 1.3203415791337103F);
        rightWing1 = new ModelRenderer(this, 0, 0);
        rightWing1.mirror = true;
        rightWing1.setRotationPoint(-0.95F, 2.0F, 1.6F);
        rightWing1.addBox(-3.0F, -2.0F, -0.5F, 5, 7, 1, 0.0F);
        setRotateAngle(rightWing1, 0.0F, 0.0F, -0.6981317007977318F);
        leftWing5 = new ModelRenderer(this, 0, 16);
        leftWing5.setRotationPoint(4.0F, 0.0F, 0.0F);
        leftWing5.addBox(0.0F, 0.0F, -0.5F, 4, 7, 1, 0.0F);
        setRotateAngle(leftWing5, 0.0F, -0.017453292519943295F, 0.5009094953223726F);
        leftWing3 = new ModelRenderer(this, 0, 8);
        leftWing3.setRotationPoint(5.0F, 0.0F, 0.0F);
        leftWing3.addBox(0.0F, 0.0F, -0.5F, 4, 7, 1, 0.0F);
        setRotateAngle(leftWing3, 0.0F, -0.017453292519943295F, 0.9948376736367678F);
        rightWing4 = new ModelRenderer(this, 10, 8);
        rightWing4.mirror = true;
        rightWing4.setRotationPoint(-4.0F, 0.0F, 0.0F);
        rightWing4.addBox(-4.0F, 0.0F, -0.5F, 4, 7, 1, 0.0F);
        setRotateAngle(rightWing4, 0.0F, 0.017453292519943295F, -1.0821041362364843F);
        rightWing4.addChild(rightWing5);
        rightWing1.addChild(rightWing2);
        rightWing5.addChild(rightWing6);
        leftWing3.addChild(leftWing4);
        rightWing2.addChild(rightWing3);
        leftWing1.addChild(leftWing2);
        leftWing5.addChild(leftWing6);
        leftWing4.addChild(leftWing5);
        leftWing2.addChild(leftWing3);
        rightWing3.addChild(rightWing4);
    }

    @Override
    public void renderModel(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.renderModel(entity, f, f1, f2, f3, f4, f5);

        if (entity instanceof EntityLivingBase && this.WINGS_TEXTURE != null) {
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;

            float f6 = MathHelper.clamp(entityLivingBase.getTicksElytraFlying() / 6F, 0, 1);
            float scale = 1 + f6 * 0.5F;

            GlStateManager.pushMatrix();
            if (entity.isSneaking())
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            GlStateManager.scale(scale, scale, scale);
            bipedBody.postRender(0.0625F);

            Minecraft.getMinecraft().renderEngine.bindTexture(this.WINGS_TEXTURE);

            GlStateManager.pushMatrix();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bb = tessellator.getBuffer();
            ModelRenderer[] rightWings = {rightWing1, rightWing2, rightWing3, rightWing4, rightWing5, rightWing6};
            ModelRenderer[] leftWings = {leftWing1, leftWing2, leftWing3, leftWing4, leftWing5, leftWing6};
            float prog = 26;
            float w = 1F / 32;
            float h = 1F / 16;

            for (int i = 0; i < rightWings.length; ++i) {
                ModelRenderer model = rightWings[i];
                ModelBox box = (ModelBox) model.cubeList.get(0);
                model.postRender(0.0625F);
                float width = box.posX2 - box.posX1;
                bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                bb.pos(box.posX1 * 0.0625F, box.posY2 * 0.0625F, box.posZ2 * 0.0625F).tex((prog - width) * w, 9 * h).endVertex();
                bb.pos(box.posX2 * 0.0625F, box.posY2 * 0.0625F, box.posZ2 * 0.0625F).tex(prog * w, 9 * h).endVertex();
                bb.pos(box.posX2 * 0.0625F, box.posY1 * 0.0625F, box.posZ2 * 0.0625F).tex(prog * w, 0).endVertex();
                bb.pos(box.posX1 * 0.0625F, box.posY1 * 0.0625F, box.posZ2 * 0.0625F).tex((prog - width) * w, 0).endVertex();
                tessellator.draw();
                prog -= width;
            }

            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            prog = 26;

            for (int i = 0; i < leftWings.length; ++i) {
                ModelRenderer model = leftWings[i];
                ModelBox box = (ModelBox) model.cubeList.get(0);
                model.postRender(0.0625F);

                float width = box.posX2 - box.posX1;
                bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                bb.pos(box.posX1 * 0.0625F, box.posY2 * 0.0625F, box.posZ2 * 0.0625F).tex(prog * w, 9 * h).endVertex();
                bb.pos(box.posX2 * 0.0625F, box.posY2 * 0.0625F, box.posZ2 * 0.0625F).tex((prog - width) * w, 9 * h).endVertex();
                bb.pos(box.posX2 * 0.0625F, box.posY1 * 0.0625F, box.posZ2 * 0.0625F).tex((prog - width) * w, 0).endVertex();
                bb.pos(box.posX1 * 0.0625F, box.posY1 * 0.0625F, box.posZ2 * 0.0625F).tex(prog * w, 0).endVertex();
                tessellator.draw();
                prog -= width;
            }

            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entityIn) {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entityIn);

        leftWing1.setRotationPoint(0.95F, 2.0F, 1.6F);
        setRotateAngle(leftWing1, 0.0F, 0.0F, 0.6981317007977318F);
        rightWing5.setRotationPoint(-4.0F, 0.0F, 0.0F);
        setRotateAngle(rightWing5, 0.0F, 0.017453292519943295F, -0.5009094953223726F);
        rightWing2.setRotationPoint(-3.0F, -2.0F, 0.0F);
        setRotateAngle(rightWing2, 0.0F, 0.017453292519943295F, -0.8726646259971648F);
        rightWing6.setRotationPoint(-4.0F, 0.0F, 0.0F);
        setRotateAngle(rightWing6, 0.0F, 0.017453292519943295F, -1.3203415791337103F);
        leftWing4.setRotationPoint(4.0F, 0.0F, 0.0F);
        setRotateAngle(leftWing4, 0.0F, -0.017453292519943295F, 1.0821041362364843F);
        rightWing3.setRotationPoint(-5.0F, 0.0F, 0.0F);
        setRotateAngle(rightWing3, 0.0F, 0.017453292519943295F, -0.9948376736367678F);
        leftWing2.setRotationPoint(3.0F, -2.0F, 0.0F);
        setRotateAngle(leftWing2, 0.0F, -0.017453292519943295F, 0.8726646259971648F);
        leftWing6.setRotationPoint(4.0F, 0.0F, 0.0F);
        setRotateAngle(leftWing6, 0.0F, -0.017453292519943295F, 1.3203415791337103F);
        rightWing1.setRotationPoint(-0.95F, 2.0F, 1.6F);
        setRotateAngle(rightWing1, 0.0F, 0.0F, -0.6981317007977318F);
        leftWing5.setRotationPoint(4.0F, 0.0F, 0.0F);
        setRotateAngle(leftWing5, 0.0F, -0.017453292519943295F, 0.5009094953223726F);
        leftWing3.setRotationPoint(5.0F, 0.0F, 0.0F);
        setRotateAngle(leftWing3, 0.0F, -0.017453292519943295F, 0.9948376736367678F);
        rightWing4.setRotationPoint(-4.0F, 0.0F, 0.0F);
        setRotateAngle(rightWing4, 0.0F, 0.017453292519943295F, -1.0821041362364843F);

        if (entityIn instanceof EntityLivingBase) {
            EntityLivingBase player = (EntityLivingBase) entityIn;
            float f6 = MathHelper.clamp(player.getTicksElytraFlying() / 6F, 0, 1);
            float f7 = 1 - f6 * f6 * 0.8F;
            float f8 = -0.261799F;
            float f9 = 1.0F;

            Vec3d motion = player == Minecraft.getMinecraft().player ? new Vec3d(player.motionX, player.motionY, player.motionZ) : new Vec3d(player.posX - player.prevPosX, player.posY - player.prevPosY, player.posZ - player.prevPosZ);

            if (motion.y < 0.0D) {
                motion = motion.normalize();
                f9 = 1.0F - (float) Math.pow(-motion.y, 1.5D);
            }

            f8 = f9 * -1.570796F + (1.0F - f9) * f8;

            ModelRenderer[] rightWings = {rightWing1, rightWing2, rightWing3, rightWing4, rightWing5, rightWing6};
            ModelRenderer[] leftWings = {leftWing1, leftWing2, leftWing3, leftWing4, leftWing5, leftWing6};

            for (int i = 0; i < rightWings.length; ++i) {
                ModelRenderer right = rightWings[i];
                ModelRenderer left = leftWings[i];

                right.rotateAngleX *= f7;
                right.rotateAngleY *= f7;
                right.rotateAngleZ *= f7;
                left.rotateAngleX *= f7;
                left.rotateAngleY *= f7;
                left.rotateAngleZ *= f7;

                if (i == 0) {
                    right.rotateAngleX -= 0.2F * f6;
                    right.rotateAngleY += 0.2F * f6;
                    right.rotateAngleZ = -((float) Math.PI / 2 - 0.4F * f6 + f8 * f6);
                    left.rotateAngleX -= 0.2F * f6;
                    left.rotateAngleY -= 0.2F * f6;
                    left.rotateAngleZ = (float) Math.PI / 2 - 0.4F * f6 + f8 * f6;
                }
            }
        }
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
