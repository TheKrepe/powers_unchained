package lucraft.mods.lucraftcore.superpowers.abilities.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lucraft.mods.lucraftcore.sizechanging.sizechanger.SizeChanger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class AbilityDataSizeChanger extends AbilityData<SizeChanger> {

    public AbilityDataSizeChanger(String key) {
        super(key);
    }

    @Override
    public SizeChanger parseValue(JsonObject jsonObject, SizeChanger defaultValue) {
        if (!JsonUtils.hasField(jsonObject, this.jsonKey))
            return defaultValue;
        String sizeChangerKey = JsonUtils.getString(jsonObject, this.jsonKey);
        SizeChanger sizeChanger = SizeChanger.SIZE_CHANGER_REGISTRY.getValue(new ResourceLocation(sizeChangerKey));
        if (sizeChanger == null)
            throw new JsonSyntaxException("Size Changer " + sizeChangerKey + " does not exist!");
        return sizeChanger;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, SizeChanger value) {
        nbt.setString(this.key, SizeChanger.SIZE_CHANGER_REGISTRY.getKey(value).toString());
    }

    @Override
    public SizeChanger readFromNBT(NBTTagCompound nbt, SizeChanger defaultValue) {
        if (!nbt.hasKey(this.key))
            return defaultValue;
        SizeChanger sizeChanger = SizeChanger.SIZE_CHANGER_REGISTRY.getValue(new ResourceLocation(nbt.getString(this.key)));
        if (sizeChanger == null)
            return defaultValue;
        return sizeChanger;
    }

    @Override
    public String getDisplay(SizeChanger value) {
        return SizeChanger.SIZE_CHANGER_REGISTRY.getKey(value).toString();
    }

    @Override
    public boolean displayAsString(SizeChanger value) {
        return true;
    }
}
