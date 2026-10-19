package lucraft.mods.heroesexpansion.items;

import akka.japi.Pair;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.events.PlayerUseGadgetEvent;
import lucraft.mods.heroesexpansion.util.items.ItemOwner;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCompoundBow extends ItemOwner {

    public ItemCompoundBow(String name) {
        super(name);
        this.setMaxStackSize(1);
        this.setMaxDamage(384);
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
    }

    public Pair<ItemStack, Boolean> findAmmo(EntityPlayer player) {
        if (this.isArrow(player.getHeldItem(EnumHand.OFF_HAND))) {
            return new Pair(player.getHeldItem(EnumHand.OFF_HAND), false);
        } else if (this.isArrow(player.getHeldItem(EnumHand.MAIN_HAND))) {
            return new Pair(player.getHeldItem(EnumHand.MAIN_HAND), false);
        } else if (player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory().getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE).getItem() instanceof ItemQuiver) {
            ItemStack quiver = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory().getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE);
            InventoryQuiver inv = new InventoryQuiver(quiver);
            ItemStack stack = inv.getStackInSlot(quiver.getTagCompound().getInteger("Selected"));

            if (this.isArrow(stack)) {
                return new Pair(stack, true);
            }
        } else {
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack itemstack = player.inventory.getStackInSlot(i);

                if (this.isArrow(itemstack)) {
                    return new Pair(itemstack, false);
                }
            }
        }
        return new Pair(ItemStack.EMPTY, false);
    }

    protected boolean isArrow(ItemStack stack) {
        return stack.getItem() instanceof ItemArrow;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {

    }

    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entityLiving;
            boolean flag = entityplayer.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
            Pair<ItemStack, Boolean> pair = this.findAmmo((EntityPlayer) entityLiving);
            ItemStack itemstack = pair.first();

            int i = this.getMaxItemUseDuration(stack) - timeLeft;
            i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, worldIn, entityplayer, i, !itemstack.isEmpty() || flag);
            if (i < 0)
                return;

            if (!itemstack.isEmpty() || flag) {
                if (itemstack.isEmpty()) {
                    itemstack = new ItemStack(Items.ARROW);
                }

                float f = getArrowVelocity(i);

                if ((double) f >= 0.1D) {
                    boolean flag1 = entityplayer.capabilities.isCreativeMode || (itemstack.getItem() instanceof ItemArrow && ((ItemArrow) itemstack.getItem()).isInfinite(itemstack, stack, entityplayer));

                    if (!worldIn.isRemote) {
                        ItemArrow itemarrow = (ItemArrow) (itemstack.getItem() instanceof ItemArrow ? itemstack.getItem() : Items.ARROW);
                        EntityArrow entityarrow = itemarrow.createArrow(worldIn, itemstack, entityplayer);
                        entityarrow.shoot(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F, f * 5.0F, 1.0F);

                        if (f == 1.0F) {
                            entityarrow.setIsCritical(true);
                        }

                        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

                        if (j > 0) {
                            entityarrow.setDamage(entityarrow.getDamage() + (double) j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);

                        if (k > 0) {
                            entityarrow.setKnockbackStrength(k);
                        }

                        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0) {
                            entityarrow.setFire(100);
                        }

                        stack.damageItem(1, entityplayer);

                        if (flag1 || entityplayer.capabilities.isCreativeMode && (itemstack.getItem() == Items.SPECTRAL_ARROW || itemstack.getItem() == Items.TIPPED_ARROW)) {
                            entityarrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                        }

                        worldIn.spawnEntity(entityarrow);
                    }

                    worldIn.playSound((EntityPlayer) null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

                    if (!flag1 && !entityplayer.capabilities.isCreativeMode) {
                        if (pair.second()) {
                            ItemStack quiver = entityLiving.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory().getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE);
                            InventoryQuiver inv = new InventoryQuiver(quiver);
                            itemstack.shrink(1);
                            if (itemstack.isEmpty() || itemstack.getCount() <= 0)
                                itemstack = ItemStack.EMPTY;
                            inv.setInventorySlotContents(quiver.getTagCompound().getInteger("Selected"), itemstack);
                            // entityLiving.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP,
                            // null).getInventory().setInventorySlotContents(InventoryExtendedInventory.SLOT_MANTLE,
                            // quiver);
                            entityLiving.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).syncToAll();
                        } else {
                            itemstack.shrink(1);
                            if (itemstack.isEmpty())
                                entityplayer.inventory.deleteStack(itemstack);
                        }
                    }

                    entityplayer.addStat(StatList.getObjectUseStats(this));
                }
            }
        }
    }

    public static float getArrowVelocity(int charge) {
        float f = (float) charge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;

        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        boolean flag = !this.findAmmo(playerIn).first().isEmpty();

        if (MinecraftForge.EVENT_BUS.post(new PlayerUseGadgetEvent(playerIn, itemstack)))
            return new ActionResult(EnumActionResult.PASS, itemstack);

        ActionResult<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, worldIn, playerIn, handIn, flag);
        if (ret != null)
            return ret;

        if (!playerIn.capabilities.isCreativeMode && !flag) {
            return flag ? new ActionResult(EnumActionResult.PASS, itemstack) : new ActionResult(EnumActionResult.FAIL, itemstack);
        } else {
            playerIn.setActiveHand(handIn);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        }
    }

    public int getItemEnchantability() {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onFOV(EntityViewRenderEvent.CameraSetup.FOVModifier e) {
            if (e.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) e.getEntity();
                SuitSet suitSet = SuitSet.getSuitSet(player);

                if (suitSet != null && !player.getActiveItemStack().isEmpty() && player.getActiveItemStack().getItem() == HEItems.COMPOUND_BOW && suitSet.getData() != null && suitSet.getData().hasKey("bow_fov")) {
                    float progress = (float) MathHelper.clamp((player.getActiveItemStack().getMaxItemUseDuration() - player.getItemInUseCount() + e.getRenderPartialTicks()) / 20.0F, 0, 1.0F);

                    e.setFOV(e.getFOV() - (progress * progress * suitSet.getData().getFloat("bow_fov")));
                }
            }
        }

    }

}
