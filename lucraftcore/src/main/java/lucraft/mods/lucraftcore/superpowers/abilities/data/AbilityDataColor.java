package lucraft.mods.lucraftcore.superpowers.abilities.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

import java.awt.*;

public class AbilityDataColor extends AbilityData<Color> {

    public AbilityDataColor(String key) {
        super(key);
    }

    @Override
    public Color parseValue(JsonObject jsonObject, Color defaultValue) {
        if (!JsonUtils.hasField(jsonObject, this.jsonKey))
            return defaultValue;
        JsonArray array = JsonUtils.getJsonArray(jsonObject, this.jsonKey);
        return new Color(array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt());
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, Color value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Red", value.getRed());
        tag.setInteger("Green", value.getGreen());
        tag.setInteger("Blue", value.getBlue());
        nbt.setTag(this.key, tag);
    }

    @Override
    public Color readFromNBT(NBTTagCompound nbt, Color defaultValue) {
        if (!nbt.hasKey(this.key))
            return defaultValue;
        return new Color(nbt.getCompoundTag(this.key).getInteger("Red"), nbt.getCompoundTag(this.key).getInteger("Green"), nbt.getCompoundTag(this.key).getInteger("Blue"));
    }

    @Override
    public String getDisplay(Color value) {
        return "[" + value.getRed() + ", " + value.getGreen() + ", " + value.getBlue() + "]";
    }
}
