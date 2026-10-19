package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.util.items.IColorableItem;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCape extends ItemBase implements IItemExtendedInventory, IColorableItem {

    int[] dyeInts = {1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 11250603, 4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844};

    public ItemCape(String name) {
        super(name);
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.setMaxStackSize(1);
    }

    @Override
    public int getDefaultColor(ItemStack stack) {
        return 10511680;
    }

    @Override
    public ExtendedInventoryItemType getEIItemType(ItemStack stack) {
        return ExtendedInventoryItemType.MANTLE;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        super.getSubItems(tab, items);
        if (this.isInCreativeTab(tab)) {
            for (int i : dyeInts) {
                ItemStack quiver = new ItemStack(this);
                this.setColor(quiver, i);
                items.add(quiver);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        return tintIndex == 0 ? ((IColorableItem) stack.getItem()).getColor(stack) : -1;
    }

}
