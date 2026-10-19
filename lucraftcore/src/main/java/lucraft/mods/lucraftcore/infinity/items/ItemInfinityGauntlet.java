package lucraft.mods.lucraftcore.infinity.items;

import com.google.common.collect.Multimap;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.EntityItemIndestructible;
import lucraft.mods.lucraftcore.infinity.ModuleInfinity;
import lucraft.mods.lucraftcore.infinity.gui.GuiHandlerEntryInfinityGauntlet;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.IAbilityProvider;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.items.ExtendedTooltip;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemInfinityGauntlet extends ItemIndestructible implements ExtendedTooltip.IExtendedItemToolTip, IAbilityProvider {

    public float attackDamage = 5;

    public ItemInfinityGauntlet(String name) {
        this.setTranslationKey(name);
        this.setRegistryName(StringHelper.unlocalizedToResourceName(name));
        this.setMaxStackSize(1);
        this.setCreativeTab(ModuleInfinity.TAB);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            ItemStack stack = new ItemStack(this);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("Fist", true);
            stack.setTagCompound(nbt);
            items.add(stack);
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        if (!item.hasTagCompound())
            item.setTagCompound(new NBTTagCompound());
        item.getTagCompound().setBoolean("Fist", false);
        return super.onDroppedByPlayer(item, player);
    }

    @Override
    public boolean onItemPickUp(World world, EntityPlayer player, ItemStack stack, EntityItemIndestructible entity) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setBoolean("Fist", true);
        return super.onItemPickUp(world, player, stack, entity);
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setBoolean("Fist", true);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (handIn == EnumHand.MAIN_HAND) {
            if (!playerIn.getHeldItem(handIn).hasTagCompound())
                playerIn.getHeldItem(handIn).setTagCompound(new NBTTagCompound());
            playerIn.openGui(LucraftCore.INSTANCE, GuiHandlerEntryInfinityGauntlet.ID, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (stack.hasTagCompound()) {
            if (isSelected) {
                InventoryInfinityGauntlet inv = new InventoryInfinityGauntlet(stack);

                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    ItemStack s = inv.getStackInSlot(i);

                    if (!s.isEmpty() && s.getItem() instanceof ItemInfinityStone) {
                        ((ItemInfinityStone) s.getItem()).onGauntletTick(entityIn, worldIn, stack);
                    }
                }
            } else if (stack.getTagCompound().getInteger("GrabbedEntity") != 0) {
                stack.getTagCompound().setInteger("GrabbedEntity", 0);
            }
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double) this.attackDamage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }

        return multimap;
    }

    @Override
    public boolean shouldShiftTooltipAppear(ItemStack stack, EntityPlayer player) {
        return stack.hasTagCompound() && !new InventoryInfinityGauntlet(stack).isEmpty();
    }

    @Override
    public List<String> getShiftToolTip(ItemStack stack, EntityPlayer player) {
        List<String> list = new ArrayList<>();
        if (stack.hasTagCompound()) {
            InventoryInfinityGauntlet inv = new InventoryInfinityGauntlet(stack);

            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack s = inv.getStackInSlot(i);

                if (!s.isEmpty()) {
                    list.add(TextFormatting.GRAY + " - " + ((ItemInfinityStone) s.getItem()).getType().getTextColor() + s.getDisplayName());
                }
            }
        }
        return list;
    }

    @Override
    public boolean shouldCtrlTooltipAppear(ItemStack stack, EntityPlayer player) {
        return false;
    }

    @Override
    public List<String> getCtrlToolTip(ItemStack stack, EntityPlayer player) {
        return null;
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase player, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        ItemStack stack = context == Ability.EnumAbilityContext.MAIN_HAND ? player.getHeldItemMainhand() : context == Ability.EnumAbilityContext.OFF_HAND ? player.getHeldItemOffhand() : ItemStack.EMPTY;
        if (stack.hasTagCompound()) {
            InventoryInfinityGauntlet inv = new InventoryInfinityGauntlet(stack);

            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack s = inv.getStackInSlot(i);
                if (s.getItem() instanceof ItemInfinityStone) {
                    ((ItemInfinityStone) s.getItem()).addStoneAbilities(player, abilities, context);
                }
            }
        }
        return abilities;
    }
}
