package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityToggle;
import lucraft.mods.lucraftcore.superpowers.render.RenderSuperpowerLayerEvent;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityWebWings extends AbilityToggle {

    public AbilityWebWings(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 8);
    }

    @Override
    public void updateTick() {
        if (shouldGlide()) {
            float horizontalSpeed = 0.0625F;
            double x = Math.cos(Math.toRadians(entity.rotationYaw + 90)) * horizontalSpeed;
            double z = Math.sin(Math.toRadians(entity.rotationYaw + 90)) * horizontalSpeed;
            entity.motionX += x;

            entity.motionZ += z;
            entity.motionY = -0.1F;
            entity.fallDistance = 0;
        }
    }

    public boolean shouldGlide() {
        return entity.motionY < 0F && !entity.onGround && !entity.isInWater() && !entity.isElytraFlying();
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        public static final ResourceLocation WEB_WINGS_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/web_wings.png");

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderModel(RenderModelEvent e) {
            if (e.getEntity() instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) e.getEntity();

                for (AbilityWebWings webWings : Ability.getAbilitiesFromClass(Ability.getAbilities(entity), AbilityWebWings.class)) {
                    if (webWings != null && webWings.isUnlocked() && webWings.isEnabled() && webWings.shouldGlide()) {
                        double d = Math.min(Math.sqrt((entity.prevPosX - entity.posX) * (entity.prevPosX - entity.posX) + (entity.prevPosZ - entity.posZ) * (entity.prevPosZ - entity.posZ)), 1.0D) * (entity.moveForward == 0.0F ? 1.0F : entity.moveForward);
                        GlStateManager.rotate((float) (100F * d), 1, 0, 0);
                        GlStateManager.translate(0, 0, 30 * d * 4 / 100);
                        return;
                    }
                }
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent(receiveCanceled = true)
        public static void onSetupModel(RenderModelEvent.SetRotationAngels e) {
            if (e.getEntity() instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) e.getEntity();

                for (AbilityWebWings wallCrawling : Ability.getAbilitiesFromClass(Ability.getAbilities(entity), AbilityWebWings.class)) {
                    if (wallCrawling != null && wallCrawling.isUnlocked() && wallCrawling.isEnabled() && wallCrawling.shouldGlide()) {
                        double d = Math.min(Math.sqrt((entity.prevPosX - entity.posX) * (entity.prevPosX - entity.posX) + (entity.prevPosZ - entity.posZ) * (entity.prevPosZ - entity.posZ)), 1.0D) * (entity.moveForward == 0.0F ? 1.0F : entity.moveForward);
                        e.setCanceled(true);
                        e.limbSwingAmount = 0;

                        e.model.bipedRightLeg.rotateAngleX = 0;
                        e.model.bipedRightLeg.rotateAngleY = 0;
                        e.model.bipedRightLeg.rotateAngleZ = 0;
                        e.model.bipedLeftLeg.rotateAngleX = 0;
                        e.model.bipedLeftLeg.rotateAngleY = 0;
                        e.model.bipedLeftLeg.rotateAngleZ = 0;
                        e.model.bipedRightArm.rotateAngleX = 0;
                        e.model.bipedRightArm.rotateAngleZ = (float) (d * 2F);
                        e.model.bipedLeftArm.rotateAngleX = 0;
                        e.model.bipedLeftArm.rotateAngleZ = (float) (-d * 2F);
                        e.model.bipedHead.rotateAngleX = (float) (-1.5F * d);
                        e.model.bipedHeadwear.rotateAngleX = (float) (-1.5F * d);

                        if (e.model instanceof ModelPlayer) {
                            ModelPlayer model = (ModelPlayer) e.model;
                            model.bipedRightLegwear.rotateAngleX = 0;
                            model.bipedRightLegwear.rotateAngleY = 0;
                            model.bipedRightLegwear.rotateAngleZ = 0;
                            model.bipedLeftLegwear.rotateAngleX = 0;
                            model.bipedLeftLegwear.rotateAngleY = 0;
                            model.bipedLeftLegwear.rotateAngleZ = 0;
                            model.bipedRightArmwear.rotateAngleX = 0;
                            model.bipedRightArmwear.rotateAngleZ = (float) (d * 2F);
                            model.bipedLeftArmwear.rotateAngleX = 0;
                            model.bipedLeftArmwear.rotateAngleZ = (float) (-d * 2F);
                        }

                        return;
                    }
                }
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderLayer(RenderSuperpowerLayerEvent e) {
            EntityPlayer player = e.getPlayer();
            ModelPlayer model = e.getRenderPlayer().getMainModel();

            for (AbilityWebWings webWings : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityWebWings.class)) {
                if (webWings != null && webWings.isUnlocked() && webWings.isEnabled() && webWings.shouldGlide()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.glTexParameteri(3553, 10242, 10497);
                    GlStateManager.glTexParameteri(3553, 10243, 10497);
                    GlStateManager.disableCull();
                    GlStateManager.enableBlend();
                    GlStateManager.depthMask(true);
                    GlStateManager.color(1, 1, 1);

                    Minecraft.getMinecraft().renderEngine.bindTexture(WEB_WINGS_TEXTURE);
                    GlStateManager.translate(0, 0.05F, 0);
                    Tessellator tes = Tessellator.getInstance();
                    BufferBuilder bb = tes.getBuffer();


                    // Right
                    {
                        bb.begin(7, DefaultVertexFormats.POSITION_TEX);

                        Vec3d startVec = new Vec3d(-0.2F, 0F, 0);
                        float rightXAngle = (float) -Math.toRadians((model.bipedRightArm.rotateAngleX / 1.55F) * 90F);
                        float rightYAngle = (float) Math.toRadians((model.bipedRightArm.rotateAngleY / 1.55F) * 90F);
                        float rightZAngle = (float) Math.toRadians((model.bipedRightArm.rotateAngleZ / 1.55F) * 90F);
                        Vec3d vec1 = startVec.add(rotateZ(new Vec3d(0, 0.7F, 0).rotatePitch(rightXAngle).rotateYaw(rightYAngle), rightZAngle));

                        bb.pos(vec1.x, vec1.y, vec1.z).tex(0, 0).endVertex();
                        bb.pos(startVec.x, startVec.y, startVec.z).tex(1, 0).endVertex();
                        bb.pos(-0.25F, 0.8F, 0).tex(1, 1).endVertex();
                        bb.pos(-0.9F, 0.8F, 0).tex(0, 1).endVertex();

                        tes.draw();
                    }

                    // Left
                    {
                        bb.begin(7, DefaultVertexFormats.POSITION_TEX);

                        Vec3d startVec = new Vec3d(0.2F, 0F, 0);
                        float rightXAngle = (float) -Math.toRadians((model.bipedLeftArm.rotateAngleX / 1.55F) * 90F);
                        float rightYAngle = (float) Math.toRadians((model.bipedLeftArm.rotateAngleY / 1.55F) * 90F);
                        float rightZAngle = (float) Math.toRadians((model.bipedLeftArm.rotateAngleZ / 1.55F) * 90F);
                        Vec3d vec1 = startVec.add(rotateZ(new Vec3d(0, 0.7F, 0).rotatePitch(rightXAngle).rotateYaw(rightYAngle), rightZAngle));

                        bb.pos(vec1.x, vec1.y, vec1.z).tex(0, 0).endVertex();
                        bb.pos(startVec.x, startVec.y, startVec.z).tex(1, 0).endVertex();
                        bb.pos(0.25F, 0.8F, 0).tex(1, 1).endVertex();
                        bb.pos(0.9F, 0.8F, 0).tex(0, 1).endVertex();

                        tes.draw();
                    }

                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();

                    return;
                }
            }
        }

        public static Vec3d rotateZ(Vec3d vector, double angle) {
            float x1 = (float) (vector.x * Math.cos(angle) - vector.y * Math.sin(angle));
            float y1 = (float) (vector.x * Math.sin(angle) + vector.y * Math.cos(angle));
            return new Vec3d(x1, y1, vector.z);
        }

    }

}