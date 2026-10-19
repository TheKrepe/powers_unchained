package lucraft.mods.heroesexpansion.items;

import com.google.common.collect.Multimap;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityCaptainAmericaShield;
import lucraft.mods.heroesexpansion.events.PlayerUseGadgetEvent;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageKineticEnergyBlast;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.heroesexpansion.trigger.HECriteriaTriggers;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.infinity.items.ItemIndestructible;
import lucraft.mods.lucraftcore.util.events.PlayerEmptyClickEvent;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

public class ItemCaptainAmericaShield extends ItemIndestructible implements IItemExtendedInventory {

    private final Item.ToolMaterial material;
    private final float attackDamage;

    public ItemCaptainAmericaShield(String name) {
        this.setTranslationKey(name);
        this.setRegistryName(StringHelper.unlocalizedToResourceName(name));
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.material = EnumHelper.addToolMaterial("captainAmericaShield", 4, 2048, 10, 6, 8);
        this.attackDamage = 3.0F + material.getAttackDamage();
        this.maxStackSize = 1;
        this.setMaxDamage(material.getMaxUses());
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, ItemArmor.DISPENSER_BEHAVIOR);
    }

    @Override
    public ExtendedInventoryItemType getEIItemType(ItemStack stack) {
        return ExtendedInventoryItemType.MANTLE;
    }

    @Override
    public boolean useButton(ItemStack stack, EntityPlayer player) {
        return player.getHeldItemMainhand().isEmpty();
    }

    @Override
    public String getAbilityBarDescription(ItemStack stack, EntityPlayer player) {
        return StringHelper.translateToLocal("heroesexpansion.info.equip_shield");
    }

    @Override
    public void onPressedButton(ItemStack itemstack, EntityPlayer player, boolean pressed) {
        if (pressed && player.getHeldItemMainhand().isEmpty()) {
            player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, itemstack);
            player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory().setInventorySlotContents(InventoryExtendedInventory.SLOT_MANTLE, ItemStack.EMPTY);
        }
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public int getItemEnchantability() {
        return this.material.getEnchantability();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        Block block = state.getBlock();

        if (block == Blocks.WEB) {
            return 15.0F;
        } else {
            Material material = state.getMaterial();
            return material != Material.PLANTS && material != Material.VINE && material != Material.CORAL && material != Material.LEAVES && material != Material.GOURD ? 1.0F : 1.5F;
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
            stack.damageItem(2, entityLiving);
        }

        return true;
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockIn) {
        return blockIn.getBlock() == Blocks.WEB;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        for (ItemStack stack : OreDictionary.getOres("plateVibranium")) {
            if (repair.getItem() == stack.getItem() && repair.getItemDamage() == stack.getItemDamage()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        @SuppressWarnings("deprecation")
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double) this.attackDamage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }

        return multimap;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        playerIn.setActiveHand(handIn);
        return new ActionResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void leftClick(PlayerEmptyClickEvent.LeftClick e) {
            if (!e.getEntityPlayer().getEntityWorld().isRemote && !e.getEntityPlayer().getHeldItemMainhand().isEmpty() &&
                    (e.getEntityPlayer().getHeldItemMainhand().getItem() instanceof ItemCaptainAmericaShield || e.getEntityPlayer().getHeldItemMainhand().getItem() == Items.SHIELD) &&
                    e.getEntityPlayer().isSneaking() && !e.getEntityPlayer().getCooldownTracker().hasCooldown(e.getEntityPlayer().getHeldItemMainhand().getItem()) &&
                    !MinecraftForge.EVENT_BUS.post(new PlayerUseGadgetEvent(e.getEntityPlayer(), e.getEntityPlayer().getHeldItemMainhand()))) {
                EntityCaptainAmericaShield shield = new EntityCaptainAmericaShield(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer(), e.getEntityPlayer().getHeldItemMainhand());
                shield.shoot(e.getEntityPlayer(), e.getEntityPlayer().rotationPitch, e.getEntityPlayer().rotationYaw, 0.0F, 2F, 1.0F);
                e.getEntityPlayer().getEntityWorld().spawnEntity(shield);
                e.getEntityPlayer().getCooldownTracker().setCooldown(e.getEntityPlayer().getHeldItemMainhand().getItem(), 30);
                e.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                PlayerHelper.playSoundToAll(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer().posX, e.getEntityPlayer().posY, e.getEntityPlayer().posZ, 30, HESoundEvents.SHIELD_THROW, SoundCategory.PLAYERS);
            }
        }

        @SubscribeEvent
        public static void onFall(LivingFallEvent e) {
            if (!e.getEntityLiving().getActiveItemStack().isEmpty() && e.getEntityLiving().getActiveItemStack().getItem() instanceof ItemCaptainAmericaShield && e.getEntityLiving().rotationPitch >= 75F) {
                e.setDamageMultiplier(0.1F);
            }
        }

        @SubscribeEvent
        public static void onBoom(LivingHurtEvent e) {
            if (!e.getEntityLiving().getActiveItemStack().isEmpty() && e.getEntityLiving().getActiveItemStack().getItem() instanceof ItemCaptainAmericaShield && e.getSource().getImmediateSource() != null && e.getSource().getImmediateSource() instanceof EntityLivingBase) {
                EntityLivingBase damager = (EntityLivingBase) e.getSource().getImmediateSource();

                if (!damager.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty() && damager.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemThorWeapon) {
                    Vec3d color = new Vec3d(0.62F, 0.93F, 1F);
                    float size = 10;
                    HEPacketDispatcher.sendToAll(new MessageKineticEnergyBlast(e.getEntityLiving().posX, e.getEntityLiving().posY, e.getEntityLiving().posZ, (int) size, color.x, color.y, color.z));
                    Vec3d pos = new Vec3d(e.getEntityLiving().posX, e.getEntityLiving().posY + e.getEntityLiving().height / 2F, e.getEntityLiving().posZ);
                    e.setAmount(0F);
                    PlayerHelper.playSoundToAll(e.getEntity().world, e.getEntity().posX, e.getEntity().posY, e.getEntity().posZ, 50, HESoundEvents.BOING, SoundCategory.PLAYERS);

                    if (damager instanceof EntityPlayerMP)
                        HECriteriaTriggers.TEAM_UP.trigger((EntityPlayerMP) damager);
                    if (e.getEntityLiving() instanceof EntityPlayerMP)
                        HECriteriaTriggers.TEAM_UP.trigger((EntityPlayerMP) e.getEntityLiving());

                    for (EntityLivingBase entity : e.getEntityLiving().world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.x - size, pos.y - size, pos.z - size, pos.x + size, pos.y + size, pos.z + size))) {
                        if (entity != e.getEntityLiving() && entity != damager && entity.getDistance(e.getEntityLiving()) <= size) {
                            Vec3d push = (entity.getPositionVector().subtract(pos)).scale(1F - (entity.getDistance(e.getEntityLiving()) / size));
                            entity.move(MoverType.PLAYER, push.x, push.y, push.z);
                            float damage = 1F - (entity.getDistance(e.getEntityLiving()) / size);
                            entity.attackEntityFrom(DamageSource.causeExplosionDamage(e.getEntityLiving()), damage * 15F);
                        }
                    }
                }
            }
        }

    }
}
