package lucraft.mods.lucraftcore.utilities.items;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.items.ExtendedTooltip.IExtendedItemToolTip;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import lucraft.mods.lucraftcore.utilities.recipes.InstructionRecipe;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ItemInstruction extends ItemBase implements IExtendedItemToolTip {

    public ItemInstruction(String name) {
        super(name);
        this.setCreativeTab(LucraftCore.CREATIVE_TAB);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab))
            return;

        for (InstructionRecipe recipes : InstructionRecipe.getInstructionRecipes()) {
            ItemStack stack = new ItemStack(this);
            setInstructionRecipes(stack, new InstructionRecipe[]{recipes});
            items.add(stack);
        }

        ItemStack stack = new ItemStack(this);
        stack.setStackDisplayName("All recipes");
        setInstructionRecipes(stack, InstructionRecipe.getInstructionRecipes().toArray(new InstructionRecipe[InstructionRecipe.getInstructionRecipes().size()]));
        items.add(stack);
    }

    public static void setInstructionRecipes(ItemStack stack, InstructionRecipe... instruction) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemInstruction))
            return;

        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < instruction.length; i++) {
            nbttaglist.appendTag(new NBTTagString(instruction[i].getRegistryName().toString()));
        }
        nbt.setTag("Recipes", nbttaglist);
        stack.setTagCompound(nbt);
    }

    public static InstructionRecipe[] getInstructionRecipes(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemInstruction) || !stack.hasTagCompound())
            return null;

        NBTTagList nbttaglist = stack.getTagCompound().getTagList("Recipes", Constants.NBT.TAG_STRING);
        List<InstructionRecipe> list = new ArrayList<>();
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            if (InstructionRecipe.getInstructionRecipesMap().containsKey(new ResourceLocation(nbttaglist.getStringTagAt(i))))
                list.add(InstructionRecipe.getInstructionRecipesMap().get(new ResourceLocation(nbttaglist.getStringTagAt(i))));
        }

        InstructionRecipe[] instructions = new InstructionRecipe[list.size()];

        for (int i = 0; i < list.size(); i++)
            instructions[i] = list.get(i);
        return instructions;
    }

    @Override
    public boolean shouldShiftTooltipAppear(ItemStack stack, EntityPlayer player) {
        return true;
    }

    @Override
    public List<String> getShiftToolTip(ItemStack stack, EntityPlayer player) {
        List<String> list = new ArrayList<>();
        if (stack.hasTagCompound()) {
            for (InstructionRecipe recipe : getInstructionRecipes(stack)) {
                if (recipe != null) {
                    list.add(TextFormatting.BOLD + recipe.getOutput().getDisplayName() + ":");
                    for (ItemStack s : recipe.getRequirements())
                        list.add(" - " + s.getDisplayName());
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

}
