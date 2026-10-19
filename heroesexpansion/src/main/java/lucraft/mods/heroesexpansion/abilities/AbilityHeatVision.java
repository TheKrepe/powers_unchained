package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataColor;
import lucraft.mods.lucraftcore.superpowers.render.RenderSuperpowerLayerEvent;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class AbilityHeatVision extends AbilityHeld {

    public static final AbilityData<Color> COLOR = new AbilityDataColor("color").disableSaving().enableSetting("color", "The color of the beams");

    public AbilityHeatVision(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(COLOR, new Color(1F, 0F, 0F));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        Color color = this.dataManager.get(COLOR);
        GlStateManager.enableBlend();
        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 13);
        GlStateManager.color(1, 1, 1);
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 2);
    }

    @Override
    public void updateTick() {
        RayTraceResult rtr = getPosLookingAt();

//        if (this.entity.ticksExisted % 20 == 0) {
//            PlayerHelper.playSoundToAll(this.entity.world, this.entity.posX, this.entity.posY + this.entity.getEyeHeight(), this.entity.posZ, 50, HESoundEvents.HEAT_VISION, SoundCategory.PLAYERS);
//        }

        if (rtr != null && !entity.world.isRemote) {
            if (rtr.entityHit != null && rtr.entityHit != entity) {
                rtr.entityHit.setFire(5);
                if (entity instanceof EntityPlayer)
                    rtr.entityHit.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) entity), 3);
                else rtr.entityHit.attackEntityFrom(DamageSource.causeMobDamage(entity), 3);
            } else if (rtr.hitVec != null) {
                BlockPos pos = new BlockPos(rtr.hitVec);

                for (EnumFacing dir : EnumFacing.values()) {
                    if (entity.world.isAirBlock(pos.add(dir.getDirectionVec()))) {
                        BlockPos p = pos.add(dir.getDirectionVec());


                        if (entity instanceof EntityPlayer && MinecraftForge.EVENT_BUS.post(new BlockEvent.PlaceEvent(new BlockSnapshot(entity.world, p, Blocks.FIRE.getDefaultState()), entity.world.getBlockState(pos),
                                (EntityPlayer) entity, EnumHand.MAIN_HAND)))
                            return;

                        entity.world.setBlockState(pos.add(dir.getDirectionVec()), Blocks.FIRE.getDefaultState());
                        return;
                    }
                }
            }
        }
    }

    public RayTraceResult getPosLookingAt() {
        Vec3d lookVec = entity.getLookVec();
        double distance = 30D;
        for (int i = 0; i < distance * 2; i++) {
            float scale = i / 2F;
            Vec3d pos = entity.getPositionVector().add(0, entity.getEyeHeight(), 0).add(lookVec.scale(scale));

            if (entity.world.isBlockFullCube(new BlockPos(pos)) && !entity.world.isAirBlock(new BlockPos(pos))) {
                return new RayTraceResult(pos, null);
            } else {
                Vec3d min = pos.add(0.25F, 0.25F, 0.25F);
                Vec3d max = pos.add(-0.25F, -0.25F, -0.25F);
                for (Entity entity : this.entity.world.getEntitiesWithinAABBExcludingEntity(this.entity, new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z))) {
                    return new RayTraceResult(entity);
                }
            }
        }
        return new RayTraceResult(entity.getPositionVector().add(0, entity.getEyeHeight(), 0).add(lookVec.scale(distance)), null);
    }

    @EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderWorld(RenderWorldLastEvent e) {
            if (Minecraft.getMinecraft().player == null)
                return;

            EntityPlayer player = Minecraft.getMinecraft().player;

            for (AbilityHeatVision ab : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityHeatVision.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                    double distance = player.getPositionVector().add(0, player.getEyeHeight(), 0).distanceTo(Minecraft.getMinecraft().objectMouseOver.hitVec);
                    LCRenderHelper.setupRenderLightning();
                    GlStateManager.translate(0, player.getEyeHeight(), 0);
                    GlStateManager.rotate(-player.rotationYaw, 0, 1, 0);
                    GlStateManager.rotate(player.rotationPitch, 1, 0, 0);
                    {
                        Vec3d start = new Vec3d(0.1F, 0, 0);
                        Vec3d end = start.add(0, 0, distance);
                        LCRenderHelper.drawGlowingLine(start, end, 0.5F, ab.getDataManager().get(COLOR));
                    }
                    {
                        Vec3d start = new Vec3d(-0.1F, 0, 0);
                        Vec3d end = start.add(0, 0, distance);
                        LCRenderHelper.drawGlowingLine(start, end, 0.5F, ab.getDataManager().get(COLOR));
                    }
                    LCRenderHelper.finishRenderLightning();
                    return;
                }
            }
        }

        @SubscribeEvent
        public static void onRenderLayer(RenderSuperpowerLayerEvent e) {
            if (Minecraft.getMinecraft().player == null)
                return;

            EntityPlayer player = e.getPlayer();

            for (AbilityHeatVision ab : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityHeatVision.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                    double distance = player.getPositionVector().add(0, player.getEyeHeight(), 0).distanceTo(Minecraft.getMinecraft().objectMouseOver.hitVec);

                    LCRenderHelper.setupRenderLightning();
                    e.getRenderPlayer().getMainModel().bipedHead.postRender(e.getScale());
                    {
                        Vec3d start = new Vec3d(0.1F, -4F * e.getScale(), 0);
                        Vec3d end = start.add(0, -4F * e.getScale(), -distance);
                        LCRenderHelper.drawGlowingLine(start, end, 0.5F, ab.getDataManager().get(COLOR));
                    }
                    {
                        Vec3d start = new Vec3d(-0.1F, -4F * e.getScale(), 0);
                        Vec3d end = start.add(0, -4F * e.getScale(), -distance);
                        LCRenderHelper.drawGlowingLine(start, end, 0.5F, ab.getDataManager().get(COLOR));
                    }
                    LCRenderHelper.finishRenderLightning();
                    return;
                }
            }
        }

    }

}
