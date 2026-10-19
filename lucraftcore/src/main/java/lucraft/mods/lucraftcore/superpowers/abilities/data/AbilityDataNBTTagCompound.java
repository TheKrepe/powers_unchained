package lucraft.mods.lucraftcore.superpowers.abilities.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

public class AbilityDataNBTTagCompound extends AbilityData<NBTTagCompound> {

    public AbilityDataNBTTagCompound(String key) {
        super(key);
    }

    @Override
    public NBTTagCompound parseValue(JsonObject jsonObject, NBTTagCompound defaultValue) {
        if (!JsonUtils.hasField(jsonObject, this.jsonKey))
            return defaultValue;
        try {
            return JsonToNBT.getTagFromJson(JsonUtils.getJsonObject(jsonObject, this.jsonKey).toString());
        } catch (NBTException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, NBTTagCompound value) {
        nbt.setTag(this.key, value);
    }

    @Override
    public NBTTagCompound readFromNBT(NBTTagCompound nbt, NBTTagCompound defaultValue) {
        if (!nbt.hasKey(this.key))
            return defaultValue;
        return nbt.getCompoundTag(this.key);
    }
}
