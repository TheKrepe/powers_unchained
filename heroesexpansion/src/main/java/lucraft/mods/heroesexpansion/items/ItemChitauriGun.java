package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.events.PlayerUseGadgetEvent;
import lucraft.mods.lucraftcore.superpowers.entities.EntityEnergyBlast;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import lucraft.mods.lucraftcore.util.sounds.LCSoundEvents;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class ItemChitauriGun extends ItemBase {

    public ItemChitauriGun(String name) {
        super(name);
        this.setMaxStackSize(1);
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.setMaxDamage(1024);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (MinecraftForge.EVENT_BUS.post(new PlayerUseGadgetEvent(playerIn, itemstack)))
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);

        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.damageItem(1, playerIn);
        }

        worldIn.playSound((EntityPlayer) null, playerIn.posX, playerIn.posY, playerIn.posZ, LCSoundEvents.ENERGY_BLAST, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        playerIn.getCooldownTracker().setCooldown(this, 5);

        if (!worldIn.isRemote) {
            EntityEnergyBlast entity = new EntityEnergyBlast(worldIn, playerIn, 4, new Color(0f, 0f, 1f));
            entity.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            worldIn.spawnEntity(entity);
        }

        playerIn.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == HEItems.CHITAURI_METAL;
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent(receiveCanceled = true)
        public static void onSetupModel(RenderModelEvent.SetRotationAngels e) {
            if (e.getEntity() instanceof EntityPlayer) {
                boolean b = false;

                if (((EntityPlayer) e.getEntity()).getHeldItemMainhand().getItem() instanceof ItemChitauriGun) {
                    e.model.bipedRightArm.rotateAngleY = -0.1F + e.model.bipedHead.rotateAngleY;
                    e.model.bipedLeftArm.rotateAngleY = 0.1F + e.model.bipedHead.rotateAngleY + 0.4F;
                    e.model.bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + e.model.bipedHead.rotateAngleX;
                    e.model.bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + e.model.bipedHead.rotateAngleX;
                    b = true;
                    e.setCanceled(true);
                } else if (((EntityPlayer) e.getEntity()).getHeldItemOffhand().getItem() instanceof ItemChitauriGun) {
                    e.model.bipedRightArm.rotateAngleY = -0.1F + e.model.bipedHead.rotateAngleY - 0.4F;
                    e.model.bipedLeftArm.rotateAngleY = 0.1F + e.model.bipedHead.rotateAngleY;
                    e.model.bipedRightArm.rotateAngleX = -((float) Math.PI / 2F) + e.model.bipedHead.rotateAngleX;
                    e.model.bipedLeftArm.rotateAngleX = -((float) Math.PI / 2F) + e.model.bipedHead.rotateAngleX;
                    b = true;
                    e.setCanceled(true);
                }

                if (b && e.model instanceof ModelPlayer) {
                    ModelPlayer model = (ModelPlayer) e.model;
                    copyModelAngles(e.model.bipedRightArm, model.bipedRightArmwear);
                    copyModelAngles(e.model.bipedLeftArm, model.bipedLeftArmwear);
                }
            }
        }

        @SideOnly(Side.CLIENT)
        public static void copyModelAngles(ModelRenderer source, ModelRenderer dest) {
            dest.rotateAngleX = source.rotateAngleX;
            dest.rotateAngleY = source.rotateAngleY;
            dest.rotateAngleZ = source.rotateAngleZ;
        }

    }

}
