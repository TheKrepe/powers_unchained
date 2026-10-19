package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.heroesexpansion.trigger.HECriteriaTriggers;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.karma.KarmaHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityConstant;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class AbilitySpiderSense extends AbilityConstant {

    public static final AbilityData<Integer> TIMER = new AbilityDataInteger("timer").setSyncType(EnumSync.SELF);
    public int prevTimer;

    public AbilitySpiderSense(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.getDataManager().register(TIMER, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 0, 13);
    }

    @Override
    public void updateTick() {
        this.prevTimer = getDataManager().get(TIMER);
        List<Entity> entities = entity.world.getEntitiesWithinAABBExcludingEntity(entity, new AxisAlignedBB(entity.getPosition().add(-10, -10, -10), entity.getPosition().add(10, 10, 10)));

        boolean b = false;
        for (Entity entity : entities) {
            if (isThreat(entity))
                b = true;
        }

        if (getDataManager().get(TIMER) >= 1 || b)
            getDataManager().set(TIMER, getDataManager().get(TIMER) + 1);

        if (!b && getDataManager().get(TIMER) >= 40) {
            getDataManager().set(TIMER, 0);
            prevTimer = 0;
        }

        if (getDataManager().get(TIMER) == 1 && this.entity instanceof EntityPlayerMP) {
            PlayerHelper.playSound(entity.world, (EntityPlayer) entity, entity.posX, entity.posY, entity.posZ, HESoundEvents.SPIDER_SENSE, SoundCategory.PLAYERS);
            HECriteriaTriggers.SPIDER_SENSE.trigger((EntityPlayerMP) this.entity);
        }
    }

    public boolean isThreat(Entity entity) {
        return (entity instanceof EntityLivingBase && KarmaHandler.isEvilEntity((EntityLivingBase) entity)) || (entity instanceof EntityPlayer && KarmaHandler.isEvilPlayer((EntityPlayer) entity)) || (entity instanceof EntityThrowable && ((EntityThrowable) entity).getThrower() != this.entity) || (entity instanceof EntityArrow && ((EntityArrow) entity).shootingEntity != this.entity);
    }

    public float getProgress(float partialTick) {
        float f = MathHelper.clamp(prevTimer + (getDataManager().get(TIMER) - prevTimer) * partialTick, 0F, 40F) / 40F;
        return f > 40F ? 0 : f;
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        public static ResourceLocation OVERLAY = new ResourceLocation(HeroesExpansion.MODID, "textures/gui/spider_sense_overlay.png");

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderGameOverlay(RenderGameOverlayEvent.Post e) {
            if (e.getType() == RenderGameOverlayEvent.ElementType.HELMET) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                for (AbilitySpiderSense spiderSense : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilitySpiderSense.class)) {
                    if (spiderSense != null && spiderSense.isUnlocked() && spiderSense.getProgress(e.getPartialTicks()) > 0F) {
                        GlStateManager.pushMatrix();
                        GlStateManager.enableBlend();
                        GlStateManager.disableDepth();
                        GlStateManager.depthMask(false);
                        GlStateManager.translate(e.getResolution().getScaledWidth() / 2D, e.getResolution().getScaledHeight() / 2D, 0);
                        float progress = spiderSense.getProgress(e.getPartialTicks());
                        float scale = 1.02F + progress * 0.5F + MathHelper.sin((player.ticksExisted + e.getPartialTicks() / 20F)) / 50F;
                        GlStateManager.scale(scale, scale, 1);
                        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1F - progress);
                        GlStateManager.disableAlpha();
                        Minecraft.getMinecraft().getTextureManager().bindTexture(OVERLAY);
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferbuilder = tessellator.getBuffer();
                        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                        bufferbuilder.pos((double) e.getResolution().getScaledWidth() / -2D, (double) e.getResolution().getScaledHeight() / 2D, -90.0D).tex(0.0D, 1.0D).endVertex();
                        bufferbuilder.pos((double) e.getResolution().getScaledWidth() / 2D, (double) e.getResolution().getScaledHeight() / 2D, -90.0D).tex(1.0D, 1.0D).endVertex();
                        bufferbuilder.pos((double) e.getResolution().getScaledWidth() / 2D, (double) e.getResolution().getScaledHeight() / -2D, -90.0D).tex(1.0D, 0.0D).endVertex();
                        bufferbuilder.pos((double) e.getResolution().getScaledWidth() / -2D, (double) e.getResolution().getScaledHeight() / -2D, -90.0D).tex(0.0D, 0.0D).endVertex();
                        tessellator.draw();
                        GlStateManager.depthMask(true);
                        GlStateManager.enableDepth();
                        GlStateManager.enableAlpha();
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.popMatrix();
                        return;
                    }
                }
            }
        }

    }

}
