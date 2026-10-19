package lucraft.mods.lucraftcore.superpowers.abilities.data;

import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.JsonUtils;

import java.util.UUID;

public class AbilityDataUUID extends AbilityData<UUID> {

    public AbilityDataUUID(String key) {
        super(key);
    }

    @Override
    public UUID parseValue(JsonObject jsonObject, UUID defaultValue) {
        if (!JsonUtils.hasField(jsonObject, this.jsonKey))
            return defaultValue;
        return UUID.fromString(JsonUtils.getString(jsonObject, this.jsonKey));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, UUID value) {
        nbt.setTag(this.key, NBTUtil.createUUIDTag(value));
    }

    @Override
    public UUID readFromNBT(NBTTagCompound nbt, UUID defaultValue) {
        if (!nbt.hasKey(this.key))
            return defaultValue;
        return NBTUtil.getUUIDFromTag(nbt.getCompoundTag(this.key));
    }

    @Override
    public String getDisplay(UUID value) {
        return value.toString();
    }

    @Override
    public boolean displayAsString(UUID value) {
        return true;
    }

}
