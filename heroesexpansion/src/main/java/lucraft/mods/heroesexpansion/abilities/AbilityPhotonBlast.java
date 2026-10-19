package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataColor;
import lucraft.mods.lucraftcore.superpowers.render.RenderSuperpowerLayerEvent;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class AbilityPhotonBlast extends AbilityHeld {

    public static final AbilityData<Color> COLOR = new AbilityDataColor("color").disableSaving().enableSetting("color", "The color of the beam");

    public AbilityPhotonBlast(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(COLOR, new Color(0.39F, 0.36F, 0f));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        Color color = this.dataManager.get(COLOR);
        GlStateManager.enableBlend();
        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 12);
        GlStateManager.color(1, 1, 1);
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 11);
    }

    @Override
    public void updateTick() {
        RayTraceResult rtr = getPosLookingAt();

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

                        if ((!(entity instanceof EntityPlayer) && !entity.world.getGameRules().getBoolean("mobGriefing")) || (entity instanceof EntityPlayer && MinecraftForge.EVENT_BUS.post(new BlockEvent.PlaceEvent(new BlockSnapshot(entity.world, p, Blocks.FIRE.getDefaultState()), entity.world.getBlockState(pos), (EntityPlayer) entity, EnumHand.MAIN_HAND))))
                            return;

                        entity.world.setBlockState(pos.add(dir.getDirectionVec()), Blocks.FIRE.getDefaultState());
                        return;
                    }
                }
            }
        }
    }

    public RayTraceResult getPosLookingAt() {
        double distance = 30D;
        Vec3d startPos = entity.getPositionVector().add(0, entity.getEyeHeight(), 0);
        Vec3d endPos = entity.getPositionVector().add(0, entity.getEyeHeight(), 0).add(entity.getLookVec().scale(distance));

        for (int i = 0; i < distance * 2; i++) {
            float scale = i / 2F;
            Vec3d v = endPos.subtract(startPos);
            Vec3d pos = startPos.add(v.scale(scale / 30F));

            if (entity.world.isBlockFullCube(new BlockPos(pos)) && !entity.world.isAirBlock(new BlockPos(pos))) {
                return new RayTraceResult(pos, null);
            } else {
                Vec3d min = pos.add(0.25F, 0.25F, 0.25F);
                Vec3d max = pos.add(-0.25F, -0.25F, -0.25F);
                for (Entity entity : entity.world.getEntitiesWithinAABBExcludingEntity(entity, new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z))) {
                    return new RayTraceResult(entity);
                }
            }
        }
        return new RayTraceResult(endPos, null);
    }


    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderWorld(RenderWorldLastEvent e) {

        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderHand(RenderHandEvent e) {
            if (Minecraft.getMinecraft().player == null)
                return;

            EntityPlayer player = Minecraft.getMinecraft().player;

            for (AbilityPhotonBlast ab : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityPhotonBlast.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled() && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                    LCRenderHelper.setupRenderLightning();

                    Vec3d start = new Vec3d(player.getPrimaryHand() == EnumHandSide.RIGHT ? 0.3F : -0.3F, -0.2F, -0.4F);
                    double distance = Minecraft.getMinecraft().objectMouseOver.hitVec.distanceTo(player.getPositionVector().add(0, player.eyeHeight, 0));
                    Vec3d end = new Vec3d(0, 0, -distance);
                    LCRenderHelper.drawGlowingLine(start, end, 0.5F, ab.dataManager.get(COLOR));

                    e.setCanceled(true);
                    LCRenderHelper.finishRenderLightning();
                    return;
                }
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent(receiveCanceled = true)
        public static void onSetupModel(RenderModelEvent.SetRotationAngels e) {
            if (!(e.getEntity() instanceof EntityPlayer))
                return;

            EntityPlayer player = (EntityPlayer) e.getEntity();
            for (AbilityPhotonBlast ab : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityPhotonBlast.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                    e.setCanceled(true);
                    ModelRenderer renderer = player.getPrimaryHand() == EnumHandSide.RIGHT ? e.model.bipedRightArm : e.model.bipedLeftArm;
                    renderer.rotateAngleX = (float) (e.model.bipedHead.rotateAngleX - Math.toRadians(90F));
                    renderer.rotateAngleY = e.model.bipedHead.rotateAngleY;
                    renderer.rotateAngleZ = e.model.bipedHead.rotateAngleZ;

                    if (e.model instanceof ModelPlayer) {
                        ModelPlayer model = (ModelPlayer) e.model;
                        ModelRenderer renderer2 = player.getPrimaryHand() == EnumHandSide.RIGHT ? model.bipedRightArmwear : model.bipedLeftArmwear;
                        renderer2.rotateAngleX = renderer.rotateAngleX;
                        renderer2.rotateAngleY = renderer.rotateAngleY;
                        renderer2.rotateAngleZ = renderer.rotateAngleZ;
                    }

                    return;
                }
            }
        }

        @SubscribeEvent
        public static void onRenderLayer(RenderSuperpowerLayerEvent e) {
            if (Minecraft.getMinecraft().player == null)
                return;

            EntityPlayer player = e.getPlayer();

            for (AbilityPhotonBlast ab : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityPhotonBlast.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                    double distance = player.getPositionVector().add(0, player.getEyeHeight(), 0).distanceTo(ab.getPosLookingAt().hitVec);

                    ModelRenderer renderer = player.getPrimaryHand() == EnumHandSide.RIGHT ? e.getRenderPlayer().getMainModel().bipedRightArm : e.getRenderPlayer().getMainModel().bipedLeftArm;
                    renderer.postRender(0.0625F);
                    LCRenderHelper.setupRenderLightning();
                    Vec3d start = new Vec3d(0, 0, 0);
                    Vec3d end = start.add(0, distance, 0);
                    LCRenderHelper.drawGlowingLine(start, end, 0.5F, ab.dataManager.get(COLOR));
                    LCRenderHelper.finishRenderLightning();
                    return;
                }
            }
        }

    }

}
