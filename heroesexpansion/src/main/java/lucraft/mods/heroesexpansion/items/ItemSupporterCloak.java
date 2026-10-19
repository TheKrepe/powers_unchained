package lucraft.mods.heroesexpansion.items;

import lucraft.mods.lucraftcore.SupporterHandler;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemSupporterCloak extends ItemBase implements IItemExtendedInventory {

    public ItemSupporterCloak(String name) {
        super(name);
        this.setCreativeTab(CREATIVE_TAB);
        this.setMaxStackSize(1);
    }

    @Override
    public ExtendedInventoryItemType getEIItemType(ItemStack stack) {
        return ExtendedInventoryItemType.MANTLE;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        boolean assigned = false;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Owner")) {
            UUID uuid = UUID.fromString(stack.getTagCompound().getString("Owner"));
            SupporterHandler.SupporterData data = SupporterHandler.getSupporterData(uuid);
            if (data != null) {
                tooltip.add(data.getName());
                assigned = true;
            }
        }

        if (!assigned)
            tooltip.add(StringHelper.translateToLocal("heroesexpansion.info.assign_cloak"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        boolean assigned = false;
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Owner")) {
            UUID uuid = UUID.fromString(stack.getTagCompound().getString("Owner"));
            SupporterHandler.SupporterData data = SupporterHandler.getSupporterData(uuid);
            if (data != null) {
                assigned = true;
            }
        }
        if (!assigned && SupporterHandler.getSupporterData(playerIn) != null && SupporterHandler.getSupporterData(playerIn).getNbt().hasKey("cloak")) {
            NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
            nbt.setString("Owner", playerIn.getGameProfile().getId().toString());
            stack.setTagCompound(nbt);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        } else if (!assigned) {
            playerIn.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.no_cloak"), true);
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            for (UUID uuid : SupporterHandler.getUUIDs()) {
                if (SupporterHandler.getSupporterData(uuid).getNbt().hasKey("cloak")) {
                    ItemStack stack = new ItemStack(this);
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setString("Owner", uuid.toString());
                    stack.setTagCompound(nbt);
                    items.add(stack);
                }
            }
        }
    }

    public static CreativeTabs CREATIVE_TAB = new CreativeTabs("tabSupporterCloaks") {

        @Override
        public ItemStack createIcon() {
            return new ItemStack(HEItems.SUPPORTER_CLOAK);
        }

        @Override
        public boolean hasSearchBar() {
            return true;
        }
    }.setBackgroundImageName("item_search.png");

}
