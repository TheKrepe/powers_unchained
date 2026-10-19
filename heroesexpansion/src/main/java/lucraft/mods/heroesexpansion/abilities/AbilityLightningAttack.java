package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.superpowers.SuperpowerGodOfThunder;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityAction;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityLightningAttack extends AbilityAction {

    public static final AbilityData<Float> DISTANCE = new AbilityDataFloat("distance").disableSaving().setSyncType(EnumSync.EVERYONE).enableSetting("damage", "The distance that you can strike the lightning from.");

    public AbilityLightningAttack(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(DISTANCE, 10F);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 3);
    }

    @Override
    public boolean action() {
        RayTraceResult rtr = getPosLookingAt();
        BlockPos pos = null;

        if (rtr != null && !entity.world.isRemote) {
            if (rtr.entityHit != null && rtr.entityHit != entity) {
                pos = rtr.entityHit.getPosition();
            } else if (rtr.hitVec != null) {
                pos = new BlockPos(rtr.hitVec);
            }
        }

        if (pos != null) {
            if (entity instanceof EntityPlayer)
                PlayerHelper.swingPlayerArm((EntityPlayer) entity, EnumHand.MAIN_HAND);
            this.entity.world.addWeatherEffect(new EntityLightningBolt(this.entity.world, pos.getX(), pos.getY(), pos.getZ(), (!(entity instanceof EntityPlayer) && !entity.world.getGameRules().getBoolean("mobGriefing")) || (entity instanceof EntityPlayer && MinecraftForge.EVENT_BUS.post(new BlockEvent.PlaceEvent(new BlockSnapshot(entity.world, pos, Blocks.FIRE.getDefaultState()), entity.world.getBlockState(pos), (EntityPlayer) entity, EnumHand.MAIN_HAND)))));
            SuperpowerGodOfThunder.addXP(entity, SuperpowerGodOfThunder.XP_AMOUNT_LIGHTNING_ATTACK);
            return true;
        }

        return false;
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
                for (Entity entity : entity.world.getEntitiesWithinAABBExcludingEntity(entity, new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z))) {
                    if (!(entity instanceof EffectTrail.EntityTrail))
                        return new RayTraceResult(entity);
                }
            }
        }
        return new RayTraceResult(entity.getPositionVector().add(lookVec.scale(distance)), null);
    }

}
