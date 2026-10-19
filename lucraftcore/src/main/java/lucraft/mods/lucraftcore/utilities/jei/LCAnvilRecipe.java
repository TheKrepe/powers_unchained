package lucraft.mods.lucraftcore.utilities.jei;

import lucraft.mods.lucraftcore.util.helper.StringHelper;
import mezz.jei.plugins.vanilla.anvil.AnvilRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class LCAnvilRecipe extends AnvilRecipeWrapper {

    public Predicate<LCAnvilRecipe> predicate;

    public LCAnvilRecipe(List<ItemStack> leftInputs, List<ItemStack> rightInputs, List<ItemStack> outputs) {
        super(leftInputs, rightInputs, outputs);
    }

    public LCAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs) {
        super(Arrays.asList(leftInput), rightInputs, outputs);
    }

    public LCAnvilRecipe setPredicate(Predicate<LCAnvilRecipe> predicate) {
        this.predicate = predicate;
        return this;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (predicate != null && !predicate.test(this)) {
            String text = StringHelper.translateToLocal("lucraftcore.info.disabled");
            minecraft.fontRenderer.drawString(text, 123 - minecraft.fontRenderer.getStringWidth(text) + 1, 28, 0x3a0000);
            minecraft.fontRenderer.drawString(text, 123 - minecraft.fontRenderer.getStringWidth(text) + 1, 27, 0x3a0000);
            minecraft.fontRenderer.drawString(text, 123 - minecraft.fontRenderer.getStringWidth(text), 28, 0x3a0000);
            minecraft.fontRenderer.drawString(text, 123 - minecraft.fontRenderer.getStringWidth(text), 27, 0xc00000);
        } else {
            super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
        }
    }
}
