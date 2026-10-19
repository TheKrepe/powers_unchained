package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class AbilitySetPunch extends AbilityAction {

    public static final AbilityData<Integer> ARM = new AbilityDataInteger("arm").setSyncType(EnumSync.NONE).enableSetting("arm", "The arm that will be used for this. 0 - main hand. 1 - off hand. 2 - both arms. 3 - random arm");
    public static final AbilityData<Float> REACH_DISTANCE = new AbilityDataFloat("reach_distance").setSyncType(EnumSync.NONE).enableSetting("reach_distance", "The maximum distance you can hit entities at");
    public static final AbilityData<Float> DAMAGE = new AbilityDataFloat("damage").setSyncType(EnumSync.NONE).enableSetting("damage", "The amount of damage dealt with each hit. If you set it to -1 it will use the current attack stat of the player");

    public AbilitySetPunch(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(ARM, 0);
        this.dataManager.register(REACH_DISTANCE, 5F);
        this.dataManager.register(DAMAGE, -1F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 0, 2);
    }

    @Override
    public boolean action() {
        if (this.entity instanceof EntityPlayer) {
            int armMode = this.dataManager.get(ARM);
            if (armMode == 0 || armMode == 2)
                PlayerHelper.swingPlayerArm((EntityPlayer) this.entity, EnumHand.MAIN_HAND);
            if (armMode == 1 || armMode == 2)
                PlayerHelper.swingPlayerArm((EntityPlayer) this.entity, EnumHand.OFF_HAND);
            if (armMode > 2)
                PlayerHelper.swingPlayerArm((EntityPlayer) this.entity, EnumHand.values()[new Random().nextInt(2)]);
        }

        float dmg = this.dataManager.get(DAMAGE);

        if (dmg == -1F)
            dmg = (float) this.entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();

        if (dmg > 0F) {
            RayTraceResult rtr = getPosLookingAt(this.dataManager.get(REACH_DISTANCE));

            if (rtr != null && rtr.entityHit != null) {
                if (this.entity instanceof EntityPlayer)
                    rtr.entityHit.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.entity), dmg);
                else
                    rtr.entityHit.attackEntityFrom(DamageSource.causeMobDamage(this.entity), dmg);
            }
        }

        return true;
    }

    public RayTraceResult getPosLookingAt(double distance) {
        Vec3d lookVec = entity.getLookVec();
        for (int i = 0; i < distance * 2; i++) {
            float scale = i / 2F;
            Vec3d pos = entity.getPositionVector().add(0, entity.getEyeHeight(), 0).add(lookVec.scale(scale));

            if (!entity.world.isAirBlock(new BlockPos(pos))) {
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

}
