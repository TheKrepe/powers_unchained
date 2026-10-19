package lucraft.mods.lucraftcore.superpowers.abilities.data;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;

/**
 * Created by Nictogen on 2019-02-15.
 */
public class AbilityDataItemStack extends AbilityData<ItemStack> {

    public AbilityDataItemStack(String key) {
        super(key);
    }

    @Override
    public ItemStack parseValue(JsonObject jsonObject, ItemStack defaultValue) {
        if (!JsonUtils.hasField(jsonObject, this.jsonKey))
            return defaultValue;
        return CraftingHelper.getItemStack(JsonUtils.getJsonObject(jsonObject, this.jsonKey), new JsonContext("null"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, ItemStack value) {
        nbt.setTag(this.key, value.serializeNBT());
    }

    @Override
    public ItemStack readFromNBT(NBTTagCompound nbt, ItemStack defaultValue) {
        if (!nbt.hasKey(this.key))
            return defaultValue;
        return new ItemStack(nbt.getCompoundTag(this.key));
    }

    @Override
    public String getDisplay(ItemStack value) {
        return "{ \"item\": \"" + Item.REGISTRY.getNameForObject(value.getItem()).toString() + "\", \"data\": " + value.getItemDamage() + ", \"count\": " + value.getCount() + " }";
    }
}
