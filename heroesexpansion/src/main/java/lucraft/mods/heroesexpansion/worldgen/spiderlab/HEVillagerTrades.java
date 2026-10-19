package lucraft.mods.heroesexpansion.worldgen.spiderlab;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import javax.annotation.Nonnull;
import java.util.Random;

public class HEVillagerTrades {

    // Credit: BluSunrize - ImmersiveEnginering

    public static class EmeraldForItemstack implements EntityVillager.ITradeList {

        public ItemStack buyingItem;
        public EntityVillager.PriceInfo buyAmounts;

        public EmeraldForItemstack(@Nonnull ItemStack item, @Nonnull EntityVillager.PriceInfo buyAmounts) {
            this.buyingItem = item;
            this.buyAmounts = buyAmounts;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            recipeList.add(new MerchantRecipe(copyStackWithAmount(this.buyingItem, this.buyAmounts.getPrice(random)), Items.EMERALD));
        }
    }

    public static class ItemstackForEmerald implements EntityVillager.ITradeList {
        public ItemStack sellingItem;
        public EntityVillager.PriceInfo priceInfo;

        public ItemstackForEmerald(Item par1Item, EntityVillager.PriceInfo priceInfo) {
            this.sellingItem = new ItemStack(par1Item);
            this.priceInfo = priceInfo;
        }

        public ItemstackForEmerald(ItemStack stack, EntityVillager.PriceInfo priceInfo) {
            this.sellingItem = stack;
            this.priceInfo = priceInfo;
        }

        @Override
        public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
            int i = 1;
            if (this.priceInfo != null)
                i = this.priceInfo.getPrice(random);
            ItemStack itemstack;
            ItemStack itemstack1;
            if (i < 0) {
                itemstack = new ItemStack(Items.EMERALD);
                itemstack1 = copyStackWithAmount(sellingItem, -i);
            } else {
                itemstack = new ItemStack(Items.EMERALD, i, 0);
                itemstack1 = copyStackWithAmount(sellingItem, 1);
            }
            recipeList.add(new MerchantRecipe(itemstack, itemstack1));
        }
    }

    public static ItemStack copyStackWithAmount(ItemStack stack, int amount) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        ItemStack s2 = stack.copy();
        s2.setCount(amount);
        return s2;
    }

}
