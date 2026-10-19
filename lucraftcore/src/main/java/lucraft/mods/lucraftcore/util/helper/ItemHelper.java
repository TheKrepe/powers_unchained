package lucraft.mods.lucraftcore.util.helper;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class ItemHelper {

    @SideOnly(Side.CLIENT)
    public static void registerItemModel(Item item, int meta, ModelResourceLocation loc) {
        ModelLoader.setCustomModelResourceLocation(item, meta, loc);
        ModelBakery.registerItemVariants(item, loc);
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModel(Item item, int meta, String modid, String loc) {
        registerItemModel(item, meta, new ModelResourceLocation(new ResourceLocation(modid, loc), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModel(Item item, String modid, String loc) {
        registerItemModel(item, 0, modid, loc);
    }

    public static List<String> getOreDictionaryEntries(ItemStack stack) {
        List<String> list = new ArrayList<>();
        int[] ia = OreDictionary.getOreIDs(stack);

        for (int i : ia)
            list.add(OreDictionary.getOreNames()[i]);

        return list;
    }

    public static NonNullList<ItemStack> getOres(String name) {
        return getOres(name, 1);
    }

    public static NonNullList<ItemStack> getOres(String name, int amount) {
        NonNullList<ItemStack> list = NonNullList.create();

        for (ItemStack stack : OreDictionary.getOres(name)) {
            ItemStack stack1 = stack.copy();
            stack1.setCount(amount);
            list.add(stack1);
        }

        return list;
    }

    public static List<ItemStack> getItems(IItemHandler itemHandler) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty())
                items.add(stack);
        }
        return items;
    }

}
