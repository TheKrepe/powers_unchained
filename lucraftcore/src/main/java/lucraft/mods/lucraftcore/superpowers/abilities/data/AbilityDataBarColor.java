package lucraft.mods.lucraftcore.superpowers.abilities.data;

import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.util.abilitybar.EnumAbilityBarColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

public class AbilityDataBarColor extends AbilityData<EnumAbilityBarColor> {

    public AbilityDataBarColor(String key) {
        super(key);
    }

    @Override
    public EnumAbilityBarColor parseValue(JsonObject jsonObject, EnumAbilityBarColor defaultValue) {
        return EnumAbilityBarColor.fromName(JsonUtils.getString(jsonObject, this.jsonKey, defaultValue.toString()));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, EnumAbilityBarColor value) {
        nbt.setString(this.key, value.toString().toLowerCase());
    }

    @Override
    public EnumAbilityBarColor readFromNBT(NBTTagCompound nbt, EnumAbilityBarColor defaultValue) {
        if (!nbt.hasKey(this.key))
            return defaultValue;
        return EnumAbilityBarColor.fromName(nbt.getString(this.key));
    }

    @Override
    public String getDisplay(EnumAbilityBarColor value) {
        return value.toString().toLowerCase();
    }

    @Override
    public boolean displayAsString(EnumAbilityBarColor value) {
        return true;
    }

    public static String values() {
        StringBuilder s = new StringBuilder();

        for (EnumAbilityBarColor color : EnumAbilityBarColor.values()) {
            s.append(", ").append(color.toString().toLowerCase());
        }

        return s.toString().substring(2);
    }
}
