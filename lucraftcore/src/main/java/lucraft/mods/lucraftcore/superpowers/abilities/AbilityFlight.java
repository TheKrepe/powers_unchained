package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import lucraft.mods.lucraftcore.superpowers.ModuleSuperpowers;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataBoolean;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityFlight extends AbilityToggle {

    public static final AbilityData<Float> SPEED = new AbilityDataFloat("speed").disableSaving().setSyncType(EnumSync.SELF).enableSetting("speed", "Sets the speed multiplier for flying when you are NOT sprinting");
    public static final AbilityData<Float> SPRINT_SPEED = new AbilityDataFloat("sprint_speed").disableSaving().setSyncType(EnumSync.SELF).enableSetting("sprint_speed", "Sets the speed multiplier for flying when you are sprinting");
    public static final AbilityData<Boolean> ROTATE_ARMS = new AbilityDataBoolean("rotate_arms").disableSaving().enableSetting("rotate_arms", "If enabled the players arms will face in your direction (like Superman)");

    public AbilityFlight(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(SPEED, 0.1F);
        this.dataManager.register(SPRINT_SPEED, 0.2F);
        this.dataManager.register(ROTATE_ARMS, true);
    }

    @Override
    public void firstTick() {
        super.firstTick();
    }

    @Override
    public void updateTick() {
        if ((entity.onGround && ticks > 20) || !(entity instanceof EntityPlayer))
            this.setEnabled(false);

        if (entity.moveForward > 0F && !entity.onGround) {
            Vec3d vec = entity.getLookVec();
            double speed = entity.isSprinting() ? this.dataManager.get(SPRINT_SPEED) : this.dataManager.get(SPEED);
            if (LCConfig.modules.size_changing)
                speed *= entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).getSize();
            entity.motionX = vec.x * speed;
            entity.motionY = vec.y * speed;
            entity.motionZ = vec.z * speed;

            if (!entity.world.isRemote) {
                entity.fallDistance = 0.0F;
            }
        } else {
            float motionY = 0F;
            if (ticks < 20) {
                int lowestY = entity.getPosition().getY();

                while (lowestY > 0 && !entity.world.isBlockFullCube(new BlockPos(entity.posX, lowestY, entity.posZ))) {
                    lowestY--;
                }

                if (entity.getPosition().getY() - lowestY < 5) {
                    motionY += 1;
                }
            }

            motionY += Math.sin(entity.ticksExisted / 10F) / 100F;
            entity.fallDistance = 0F;
            entity.motionY = motionY;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 1, 0);
    }

    @EventBusSubscriber(modid = LucraftCore.MODID, value = Side.CLIENT)
    public static class Renderer {

        @SubscribeEvent
        public static void onRenderModel(RenderModelEvent e) {
            if (!ModuleSuperpowers.INSTANCE.isEnabled())
                return;

            if (e.getEntityLiving() instanceof EntityPlayer) {
                for (AbilityFlight ab : Ability
                        .getAbilitiesFromClass(Ability.getAbilities(e.getEntityLiving()), AbilityFlight.class)) {
                    if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                        EntityPlayer player = (EntityPlayer) e.getEntityLiving();
                        float speed = (float) MathHelper.clamp(Math
                                .sqrt((player.prevPosX - player.posX) * (player.prevPosX - player.posX) + (player.prevPosZ - player.posZ) * (player.prevPosZ
                                        - player.posZ) + (player.prevPosY - player.posY) * (player.prevPosY - player.posY)), 0F, 1F);
                        GlStateManager.rotate(speed * (90F + player.rotationPitch), 1, 0, 0);
                        break;
                    }
                }
            }
        }

        @SubscribeEvent(receiveCanceled = true)
        public static void onSetUpModel(RenderModelEvent.SetRotationAngels e) {
            if (!ModuleSuperpowers.INSTANCE.isEnabled())
                return;

            if (e.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) e.getEntity();
                for (AbilityFlight ab : Ability.getAbilitiesFromClass(Ability.getAbilities((EntityPlayer) e.getEntity()), AbilityFlight.class)) {
                    if (ab != null && ab.isUnlocked() && ab.isEnabled() && player.moveForward > 0F) {
                        float speed = (float) MathHelper.clamp(Math
                                .sqrt((player.prevPosX - player.posX) * (player.prevPosX - player.posX) + (player.prevPosZ - player.posZ) * (player.prevPosZ
                                        - player.posZ) + (player.prevPosY - player.posY) * (player.prevPosY - player.posY)), 0F, 1F);
                        e.setCanceled(true);
                        float rotation = speed * (90F + player.rotationPitch);
                        e.model.bipedHead.rotateAngleX -= Math.toRadians(rotation);
                        e.model.bipedHeadwear.rotateAngleX = e.model.bipedHead.rotateAngleX;

                        e.model.bipedRightArm.rotateAngleX = (float) Math.toRadians(ab.dataManager.get(ROTATE_ARMS) ? 180F : 0F);
                        e.model.bipedLeftArm.rotateAngleX = e.model.bipedRightArm.rotateAngleX;
                        e.model.bipedRightLeg.rotateAngleX = 0;
                        e.model.bipedLeftLeg.rotateAngleX = 0;

                        if (e.model instanceof ModelPlayer) {
                            ModelPlayer model = (ModelPlayer) e.model;
                            model.bipedRightArmwear.rotateAngleX = model.bipedRightArm.rotateAngleX;
                            model.bipedLeftArmwear.rotateAngleX = model.bipedLeftArm.rotateAngleX;
                            model.bipedRightLegwear.rotateAngleX = model.bipedRightLeg.rotateAngleX;
                            model.bipedLeftLegwear.rotateAngleX = model.bipedLeftLeg.rotateAngleX;
                        }
                    }
                }
            }
        }

    }

}
