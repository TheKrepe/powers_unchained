package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityToggle;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityWallCrawling extends AbilityToggle {

    public AbilityWallCrawling(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 0, 12);
    }

    @Override
    public void updateTick() {
        if (!entity.world.isRemote && !entity.onGround && entity.motionY >= -1D) {
            entity.fallDistance = 0;
        }

        if (isWallCrawling()) {
            entity.motionY = entity.isSneaking() ? 0D : entity.getLookVec().y / 5D;
            entity.fallDistance = 0;
        }
    }

    public boolean isWallCrawling() {
        return entity.collidedHorizontally && !entity.collidedVertically;
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent(receiveCanceled = true)
        public static void onSetupModel(RenderModelEvent.SetRotationAngels e) {
            if (e.getEntity() instanceof EntityLivingBase) {
                EntityLivingBase entity = (EntityLivingBase) e.getEntity();

                for (AbilityWallCrawling wallCrawling : Ability.getAbilitiesFromClass(Ability.getAbilities(entity), AbilityWallCrawling.class)) {
                    if (wallCrawling != null && wallCrawling.isUnlocked() && wallCrawling.isEnabled() && wallCrawling.isWallCrawling()) {
                        e.setCanceled(true);
                        float rA = -2.3F + MathHelper.sin((float) entity.posY * 3F) / 7F;
                        float lA = -2.3F + MathHelper.cos((float) entity.posY * 3F) / 7F;
                        float rL = -0.7F + MathHelper.sin((float) entity.posY * 3F) / 7F;
                        float lL = -0.7F + MathHelper.cos((float) entity.posY * 3F) / 7F;

                        e.model.bipedRightArm.rotateAngleX = rA;
                        e.model.bipedLeftArm.rotateAngleX = lA;
                        e.model.bipedRightLeg.rotateAngleX = rL;
                        e.model.bipedLeftLeg.rotateAngleX = lL;

                        if (e.model instanceof ModelPlayer) {
                            ModelPlayer model = (ModelPlayer) e.model;
                            model.bipedRightArmwear.rotateAngleX = rA;
                            model.bipedLeftArmwear.rotateAngleX = lA;
                            model.bipedRightLegwear.rotateAngleX = rL;
                            model.bipedLeftLegwear.rotateAngleX = lL;
                        }

                        return;
                    }
                }
            }
        }

    }

}
