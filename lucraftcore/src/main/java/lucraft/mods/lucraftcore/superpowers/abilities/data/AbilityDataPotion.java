package lucraft.mods.lucraftcore.superpowers.abilities.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class AbilityDataPotion extends AbilityData<Potion> {

    public AbilityDataPotion(String key) {
        super(key);
    }

    @Override
    public Potion parseValue(JsonObject jsonObject, Potion defaultValue) {
        if (!JsonUtils.hasField(jsonObject, this.jsonKey))
            return defaultValue;
        String potionKey = JsonUtils.getString(jsonObject, this.jsonKey);
        Potion potion = Potion.REGISTRY.getObject(new ResourceLocation(potionKey));
        if (potion == null)
            throw new JsonSyntaxException("Potion " + potionKey + " does not exist!");
        return potion;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt, Potion value) {
        nbt.setString(this.key, Potion.REGISTRY.getNameForObject(value).toString());
    }

    @Override
    public Potion readFromNBT(NBTTagCompound nbt, Potion defaultValue) {
        if (!nbt.hasKey(this.key))
            return defaultValue;
        Potion potion = Potion.REGISTRY.getObject(new ResourceLocation(nbt.getString(this.key)));
        if (potion == null)
            return defaultValue;
        return potion;
    }

    @Override
    public String getDisplay(Potion value) {
        return Potion.REGISTRY.getNameForObject(value).toString();
    }

    @Override
    public boolean displayAsString(Potion value) {
        return true;
    }
}
