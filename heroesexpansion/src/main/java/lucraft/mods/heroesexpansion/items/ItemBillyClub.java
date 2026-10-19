package lucraft.mods.heroesexpansion.items;

import com.google.common.collect.Multimap;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityBillyClubHook;
import lucraft.mods.heroesexpansion.entities.EntityThrownBillyClub;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.heroesexpansion.util.items.IColorableItem;
import lucraft.mods.lucraftcore.util.events.PlayerEmptyClickEvent;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class ItemBillyClub extends ItemBase implements IColorableItem {

    int[] dyeInts = {1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 11250603, 4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844};
    public final float attackDamage;
    public boolean separate;

    public ItemBillyClub(String name, boolean separate) {
        super(name);
        this.separate = separate;
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.attackDamage = separate ? 5 : 10;
        this.setMaxStackSize(1);
        this.setMaxDamage(separate ? 512 : 1024);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab) && !this.separate) {
            {
                ItemStack stack = new ItemStack(this);
                ItemStack separate = new ItemStack(HEItems.BILLY_CLUB_SEPARATE);
                setClubItems(stack, separate, separate.copy());
                items.add(stack);
            }

            for (int i : dyeInts) {
                ItemStack stack = new ItemStack(this);
                ItemStack separate = new ItemStack(HEItems.BILLY_CLUB_SEPARATE);
                setColor(separate, i);
                setClubItems(stack, separate, separate.copy());
                items.add(stack);
            }
        }
    }

    public ItemStack damageItem(ItemStack stack, int damage, EntityLivingBase damageSource) {
        if (this.separate)
            stack.damageItem(damage, damageSource);
        else {
            ItemStack[] items = getClubItems(stack);
            items[new Random().nextInt(2)].damageItem(1, damageSource);

            if (items[0].isEmpty() && items[1].isEmpty()) {
                return ItemStack.EMPTY;
            } else if (items[0].isEmpty() && !items[1].isEmpty()) {
                return items[1];
            } else if (!items[0].isEmpty() && items[1].isEmpty()) {
                return items[0];
            } else if (!items[0].isEmpty() && !items[1].isEmpty()) {
                this.setClubItems(stack, items[0], items[1]);
                return stack;
            }
        }
        return stack;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (this.separate)
            return super.getDurabilityForDisplay(stack);
        ItemStack[] items = getClubItems(stack);
        double dmg1 = 1D - (double) items[0].getItemDamage() / (double) items[0].getMaxDamage();
        double dmg2 = 1D - (double) items[1].getItemDamage() / (double) items[1].getMaxDamage();
        return items == null ? 1D : (1D - dmg1 / 2D - dmg2 / 2D);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        if (this.separate)
            return super.isDamaged(stack);
        ItemStack[] items = getClubItems(stack);
        return items == null ? false : (items[0].isItemDamaged() || items[1].isItemDamaged());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        EnumHand otherHand = handIn == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;

        if (!this.separate && playerIn.getHeldItem(otherHand).isEmpty()) {
            ItemStack[] items = getClubItems(stack);
            playerIn.setHeldItem(handIn, items[0]);
            playerIn.setHeldItem(otherHand, items[1]);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }

        if (this.separate && playerIn.getHeldItem(otherHand).getItem() == this) {
            ItemStack stack1 = new ItemStack(HEItems.BILLY_CLUB);
            this.setClubItems(stack1, playerIn.getHeldItem(handIn), playerIn.getHeldItem(otherHand));
            playerIn.setHeldItem(handIn, stack1);
            playerIn.setHeldItem(otherHand, ItemStack.EMPTY);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
    }

    public ItemStack[] getClubItems(ItemStack stack) {
        if (!separate) {
            ItemStack[] items = new ItemStack[2];
            if (stack.hasTagCompound()) {
                NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("ClubItems");
                items[0] = new ItemStack(nbt.getCompoundTag("Item1"));
                items[1] = new ItemStack(nbt.getCompoundTag("Item2"));
            } else {
                ItemStack stack1 = new ItemStack(HEItems.BILLY_CLUB_SEPARATE);
                if (this.getColor(stack) != this.getDefaultColor(stack))
                    this.setColor(stack1, this.getColor(stack));
                items[0] = stack1;
                items[1] = stack1.copy();
            }
            return items;
        }

        return null;
    }

    public void setClubItems(ItemStack joined, ItemStack separate1, ItemStack separate2) {
        NBTTagCompound nbt = joined.hasTagCompound() ? joined.getTagCompound() : new NBTTagCompound();
        NBTTagCompound clubItemsTag = nbt.getCompoundTag("ClubItems");
        clubItemsTag.setTag("Item1", separate1.serializeNBT());
        clubItemsTag.setTag("Item2", separate2.serializeNBT());
        nbt.setTag("ClubItems", clubItemsTag);
        joined.setTagCompound(nbt);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        Block block = state.getBlock();
        float speed;
        if (block == Blocks.WEB) {
            speed = 15.0F;
        } else {
            Material material = state.getMaterial();
            speed = material != Material.PLANTS && material != Material.VINE && material != Material.CORAL && material != Material.LEAVES && material != Material.GOURD ? 1.0F : 1.5F;
        }

        return separate ? speed / 2F : speed;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        attacker.setHeldItem(EnumHand.MAIN_HAND, damageItem(stack, 1, attacker));
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
            entityLiving.setHeldItem(EnumHand.MAIN_HAND, damageItem(stack, 1, entityLiving));
        }

        return true;
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockIn) {
        return blockIn.getBlock() == Blocks.WEB;
    }

    @Override
    public int getItemEnchantability() {
        return 10;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        ItemStack mat = new ItemStack(Items.IRON_INGOT);
        if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false))
            return true;
        return super.getIsRepairable(toRepair, repair);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);

        if (slot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double) this.attackDamage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D / (this.separate ? 2F : 1F), 0));
        }

        return multimap;
    }

    @Override
    public int getDefaultColor(ItemStack stack) {
        return 10633547;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (this.separate)
            return tintIndex > 0 ? ((IColorableItem) stack.getItem()).getColor(stack) : -1;
        else {
            ItemStack[] items = getClubItems(stack);
            if (items == null || items.length != 2 || items[0].isEmpty() || items[1].isEmpty())
                return tintIndex > 0 ? ((IColorableItem) stack.getItem()).getColor(stack) : -1;
            else
                return tintIndex == 1 ? ((IColorableItem) items[0].getItem()).getColor(items[0]) : (tintIndex == 2 ? ((IColorableItem) items[1].getItem()).getColor(items[1]) : -1);
        }
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void leftClick(PlayerEmptyClickEvent.LeftClick e) {
            if (!e.getEntityPlayer().getEntityWorld().isRemote && !e.getEntityPlayer().getHeldItemMainhand().isEmpty() && e.getEntityPlayer().getHeldItemMainhand().getItem() instanceof ItemBillyClub && e.getEntityPlayer().isSneaking()) {
                if (((ItemBillyClub) e.getEntityPlayer().getHeldItemMainhand().getItem()).separate) {
                    EntityThrownBillyClub billyClub = new EntityThrownBillyClub(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer(), e.getEntityPlayer().getHeldItemMainhand());
                    billyClub.shoot(e.getEntityPlayer(), e.getEntityPlayer().rotationPitch, e.getEntityPlayer().rotationYaw, 0.0F, 2F, 1.0F);
                    e.getEntityPlayer().getEntityWorld().spawnEntity(billyClub);
                    e.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    PlayerHelper.playSoundToAll(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer().posX, e.getEntityPlayer().posY, e.getEntityPlayer().posZ, 30, HESoundEvents.SHIELD_THROW, SoundCategory.PLAYERS);
                } else {
                    ItemStack held = e.getEntityPlayer().getHeldItemMainhand();
                    ItemStack[] items = ((ItemBillyClub) held.getItem()).getClubItems(held);
                    EntityBillyClubHook billyClub = new EntityBillyClubHook(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer(), items[0]);
                    billyClub.shoot(e.getEntityPlayer(), e.getEntityPlayer().rotationPitch, e.getEntityPlayer().rotationYaw, 0.0F, 2F, 1.0F);
                    e.getEntityPlayer().getEntityWorld().spawnEntity(billyClub);
                    e.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, items[1]);
                    PlayerHelper.playSoundToAll(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer().posX, e.getEntityPlayer().posY, e.getEntityPlayer().posZ, 30, HESoundEvents.SHIELD_THROW, SoundCategory.PLAYERS);
                }
            }
        }

    }
}
