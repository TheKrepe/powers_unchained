package lucraft.mods.lucraftcore.materials;

import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabMaterials extends CreativeTabs {

    public CreativeTabMaterials(String label) {
        super(label);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack createIcon() {
        return Material.IRIDIUM.getItemStack(MaterialComponent.PLATE);
    }

    @Override
    public boolean hasSearchBar() {
        return true;
    }

    @Override
    public int getSearchbarWidth() {
        return 40;
    }

    @Override
    public String getBackgroundImageName() {
        return "lucraftcoremetals.png";
    }

    @Override
    public void displayAllRelevantItems(NonNullList<ItemStack> list) {
        for (Material m : Material.getMaterials()) {
            NonNullList<ItemStack> l = NonNullList.create();
            for (MaterialComponent c : MaterialComponent.values()) {
                ItemStack stack = m.getItemStack(c);

                if (!stack.isEmpty())
                    l.add(stack);
            }
            if (FluidRegistry.isFluidRegistered(m.getIdentifier().toLowerCase()) && FluidRegistry.isUniversalBucketEnabled()) {
                ItemStack bucket = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("forge:bucketfilled")));
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("FluidName", m.getIdentifier().toLowerCase());
                nbt.setInteger("Amount", 1000);
                bucket.setTagCompound(nbt);
                l.add(bucket);
            }
            while (l.size() < 9)
                l.add(ItemStack.EMPTY);
            list.addAll(l);
        }
    }

}
