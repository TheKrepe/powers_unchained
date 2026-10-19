package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.superpowers.SuperpowerGodOfThunder;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AbilityLightningStrike extends AbilityHeld {

    public AbilityLightningStrike(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        HEIconHelper.drawIcon(mc, gui, x, y, 0, 14);
    }

    @Override
    public void firstTick() {
        entity.world.addWeatherEffect(new EntityLightningBolt(entity.world, entity.posX, entity.posY, entity.posZ, true));
    }

    @Override
    public void updateTick() {
        for (Entity entity : effectedEntities()) {
            if (entity instanceof EntityLivingBase) {
                entity.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 3F);
            }
        }

        SuperpowerGodOfThunder.addXP(entity, SuperpowerGodOfThunder.XP_AMOUNT_LIGHTING_STRIKE);
    }

    public List<EntityLivingBase> effectedEntities() {
        List<EntityLivingBase> list = new ArrayList<>();
        Vec3d lookVec = entity.getLookVec();

        double x = lookVec.x / 10D;
        double y = lookVec.y / 10D;
        double z = lookVec.z / 10D;
        Random rand = new Random();

        for (int i = 0; i < 100; i++) {
            double xCoord = entity.posX + x * i;
            double yCoord = entity.posY + entity.getEyeHeight() + y * i;
            double zCoord = entity.posZ + z * i;

            for (Entity entity : this.entity.world.getEntitiesWithinAABBExcludingEntity(this.entity, new AxisAlignedBB(xCoord - 1, yCoord - 1, zCoord - 1, xCoord + 1, yCoord + 1, zCoord + 1))) {
                if (entity instanceof EntityLivingBase && !list.contains(entity) && this.entity.canEntityBeSeen(entity))
                    list.add((EntityLivingBase) entity);
            }
        }
        return list;
    }

    @EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class EventHandler {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderPlayer(RenderLivingEvent.Post<EntityLivingBase> e) {
            if (Minecraft.getMinecraft().player == null)
                return;
            GlStateManager.pushMatrix();
            translateRendering(Minecraft.getMinecraft().player, e.getEntity(), e.getPartialRenderTick());
            renderLightningStreak(e.getEntity());
            GlStateManager.popMatrix();
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderWorld(RenderWorldLastEvent e) {
            if (Minecraft.getMinecraft().player == null)
                return;
            GlStateManager.pushMatrix();
            renderLightningStreak(Minecraft.getMinecraft().player);
            GlStateManager.popMatrix();
        }

        @SideOnly(Side.CLIENT)
        public static void renderLightningStreak(EntityLivingBase entity) {
            for (AbilityLightningStrike ab : Ability.getAbilitiesFromClass(Ability.getAbilities(entity), AbilityLightningStrike.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                    float thickness = 0.005F;
                    Color color = new Color(0.28F, 1F, 1F);
                    List<EntityLivingBase> list = ab.effectedEntities();
                    if (list.size() <= 0) {
                        Vec3d start = Vec3d.ZERO.add(0, entity.height / 2D, 0);
                        Vec3d end = entity.rayTrace(10, 0).hitVec.subtract(entity.getPositionVector());
                        List<Vec3d[]> lightning = LCRenderHelper.getRandomLightningCoords(start, end, new Random(entity.ticksExisted / 2));
                        LCRenderHelper.drawLightnings(thickness, color, lightning);
                    } else {
                        for (Entity en : list) {
                            Vec3d start = Vec3d.ZERO.add(0, entity.height / 2D, 0);
                            Vec3d end = en.getPositionVector().add(0, entity.height / 2D, 0).subtract(entity.getPositionVector());
                            List<Vec3d[]> lightning = LCRenderHelper.getRandomLightningCoords(start, end, new Random(en.getEntityId() + (entity.ticksExisted / 2)));
                            LCRenderHelper.drawLightnings(thickness, color, lightning);
                        }
                    }
                }
            }
        }

        @SideOnly(Side.CLIENT)
        public static void translateRendering(EntityPlayer start, EntityLivingBase end, float partialTicks) {
            GlStateManager.translate(LCRenderHelper.interpolateValue(end.posX, end.prevPosX, partialTicks) - LCRenderHelper.interpolateValue(start.posX, start.prevPosX, partialTicks),
                    LCRenderHelper.interpolateValue(end.posY, end.prevPosY, partialTicks) - LCRenderHelper.interpolateValue(start.posY, start.prevPosY, partialTicks),
                    LCRenderHelper.interpolateValue(end.posZ, end.prevPosZ, partialTicks) - LCRenderHelper.interpolateValue(start.posZ, start.prevPosZ, partialTicks));
        }

    }

}
