package lucraft.mods.heroes.speedsterheroes.recipes;

import java.util.Iterator;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonCharge;
import lucraft.mods.heroes.speedsterheroes.util.SHNBTTags;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedTachyonChargeOreRecipe extends ShapedOreRecipe {

	public ShapedTachyonChargeOreRecipe(Block result, Object... recipe) {
		this(new ItemStack(result), recipe);
	}

	public ShapedTachyonChargeOreRecipe(Item result, Object... recipe) {
		this(new ItemStack(result), recipe);
	}

	public ShapedTachyonChargeOreRecipe(ItemStack result, Object... recipe) {
		super(result, recipe);
	}

	@SuppressWarnings("unchecked")
	protected boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror) {
		for (int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++) {
			for (int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++) {
				int subX = x - startX;
				int subY = y - startY;
				Object target = null;

				if (subX >= 0 && subY >= 0 && subX < width && subY < height) {
					if (mirror) {
						target = input[width - subX - 1 + subY * width];
					} else {
						target = input[subX + subY * width];
					}
				}

				ItemStack slot = inv.getStackInRowAndColumn(x, y);
				if (target instanceof ItemStack) {
					ItemStack targetStack = (ItemStack) target;
					if (!targetStack.isEmpty() && !slot.isEmpty() && ((ItemStack) target).getItem() != null && slot.getItem() != null) {
						if (((ItemStack) target).getItem() instanceof ItemTachyonCharge && slot.getItem() instanceof ItemTachyonCharge) {

							if(!((ItemStack) target).hasTagCompound() || !slot.hasTagCompound())
								return false;
							
							if(((ItemStack) target).getTagCompound().getInteger(SHNBTTags.progress) != slot.getTagCompound().getInteger(SHNBTTags.progress))
								return false;
						}
					}

					if(slot.isEmpty())
						return false;
					
					if (!targetStack.isEmpty() && !(((ItemStack)target).getItem() instanceof ItemTachyonCharge)&& !OreDictionary.itemMatches((ItemStack) target, slot, false)) {
						return false;
					}
				} else if (target instanceof List) {
					boolean matched = false;

					Iterator<ItemStack> itr = ((List<ItemStack>) target).iterator();
					while (itr.hasNext() && !matched) {
						matched = OreDictionary.itemMatches(itr.next(), slot, false);
					}

					if (!matched) {
						return false;
					}
				} else if (target == null && !slot.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

}
