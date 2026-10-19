package lucraft.mods.heroesexpansion.abilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityAttributeModifier;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityConstant;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AbilitySolarEnergy extends AbilityConstant {

    public static final AbilityData<Integer> SOLAR_ENERGY = new AbilityDataInteger("solar_energy").setSyncType(EnumSync.SELF);
    public static final AbilityData<Integer> MAX_SOLAR_ENERGY = new AbilityDataInteger("max_solar_energy").disableSaving().setSyncType(EnumSync.SELF).enableSetting("max_solar_energy", "The maximum amount of solar energy you can charge to");
    public static final AbilityData<List<SolarAbilityEntry>> ABILITY_VALUES = new AbilityDataSolarEntries("ability_values").disableSaving().setSyncType(EnumSync.SELF).enableSetting("ability_values", "An array of settings for every ability");

    public AbilitySolarEnergy(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(SOLAR_ENERGY, 0);
        this.dataManager.register(MAX_SOLAR_ENERGY, 100);
        LinkedList<SolarAbilityEntry> list = new LinkedList<>();
        list.add(new SolarAbilityEntry("ability_key_1", 3, 1));
        list.add(new SolarAbilityEntry("ability_key_2", 1, 2));
        this.dataManager.register(ABILITY_VALUES, list);
    }

    @Override
    public void init(Map<String, Ability> abilities) {
        super.init(abilities);
        for (SolarAbilityEntry entries : this.dataManager.get(ABILITY_VALUES)) {
            entries.ability = abilities.get(entries.abilityKey);
        }
    }

    @Override
    public void updateTick() {
        if (!entity.world.isRemote && entity.world.isDaytime() && entity.world.canSeeSky(entity.getPosition()) && entity.ticksExisted % (6 * 20) == 0 && getDataManager().get(SOLAR_ENERGY) < getDataManager().get(MAX_SOLAR_ENERGY)) {
            int i = 3;
            SuitSet suitSet = SuitSet.getSuitSet(entity);
            if (suitSet != null && suitSet.getData() != null && suitSet.getData().hasKey("solar_energy_multiplier")) {
                i *= suitSet.getData().getFloat("solar_energy_multiplier");
            }
            this.getDataManager().set(SOLAR_ENERGY, this.getDataManager().get(SOLAR_ENERGY) + i);
            SuperpowerHandler.syncToPlayer(entity);
        }

        for (SolarAbilityEntry entries : this.dataManager.get(ABILITY_VALUES)) {
            Ability ab = entries.ability;
            if (ab != null) {
                if (ab instanceof AbilityAttributeModifier) {
                    ab.getDataManager().set(AbilityAttributeModifier.MULTIPLIER, (float) getDataManager().get(SOLAR_ENERGY) / (float) getDataManager().get(MAX_SOLAR_ENERGY));
                } else {
                    if ((ab.getAbilityType() == AbilityType.CONSTANT || ab.getAbilityType() == AbilityType.HELD || ab.getAbilityType() == AbilityType.TOGGLE) && entity.ticksExisted % (entries.frequency * 20) == 0 && ab.isEnabled() && ab.isUnlocked()) {
                        this.extractEnergy(entries.amount, false);
                    }
                }
            }
        }
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(getDataManager().get(MAX_SOLAR_ENERGY) - getDataManager().get(SOLAR_ENERGY), maxReceive);
        if (!simulate)
            getDataManager().set(SOLAR_ENERGY, getDataManager().get(SOLAR_ENERGY) + energyReceived);
        return energyReceived;
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(getDataManager().get(SOLAR_ENERGY), maxExtract);
        if (!simulate)
            getDataManager().set(SOLAR_ENERGY, getDataManager().get(SOLAR_ENERGY) - energyExtracted);
        return energyExtracted;
    }

    @Override
    public boolean showInAbilityBar() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        GlStateManager.color(1, 1, 1, 1);
        HEIconHelper.drawIcon(mc, gui, x, y, 0, 15);
        float progress = (float) getDataManager().get(SOLAR_ENERGY) / (float) getDataManager().get(MAX_SOLAR_ENERGY);
        if (getDataManager().get(SOLAR_ENERGY).equals(getDataManager().get(MAX_SOLAR_ENERGY)))
            progress = 1F;
        int height = (int) (progress * 10);
        gui.drawTexturedModalRect(x + 3, y + 3 + 10 - height, 3, 19 + 10 - height, 10, height + 1);
    }

    public static class SolarAbilityEntry {

        public String abilityKey;
        public Ability ability;
        public int amount;
        public int frequency;

        public SolarAbilityEntry(String abilityKey, int amount, int frequency) {
            this.abilityKey = abilityKey;
            this.amount = amount;
            this.frequency = frequency;
        }

    }

    public static class AbilityDataSolarEntries extends AbilityData<List<SolarAbilityEntry>> {

        public AbilityDataSolarEntries(String key) {
            super(key);
        }

        @Override
        public List<SolarAbilityEntry> parseValue(JsonObject jsonObject, List<SolarAbilityEntry> defaultValue) {
            if (!JsonUtils.hasField(jsonObject, this.jsonKey))
                return defaultValue;
            JsonArray jsonArray = JsonUtils.getJsonArray(jsonObject, this.jsonKey);
            List<SolarAbilityEntry> list = new LinkedList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject json = jsonArray.get(i).getAsJsonObject();
                String ability = JsonUtils.getString(json, "ability");
                int amount = JsonUtils.getInt(json, "amount");
                int frequency = JsonUtils.getInt(json, "frequency");
                list.add(new SolarAbilityEntry(ability, amount, frequency));
            }
            return list;
        }

        @Override
        public void writeToNBT(NBTTagCompound nbt, List<SolarAbilityEntry> value) {
            NBTTagList list = new NBTTagList();
            for (SolarAbilityEntry entries : value) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("Ability", entries.abilityKey);
                tag.setInteger("Amount", entries.amount);
                tag.setInteger("Frequency", entries.frequency);
                list.appendTag(tag);
            }
            nbt.setTag(this.key, list);
        }

        @Override
        public List<SolarAbilityEntry> readFromNBT(NBTTagCompound nbt, List<SolarAbilityEntry> defaultValue) {
            if (!nbt.hasKey(this.key))
                return defaultValue;
            List<SolarAbilityEntry> list = new LinkedList<>();
            NBTTagCompound tag = nbt.getCompoundTag(this.key);
            NBTTagList nbttaglist = tag.getTagList(this.key, 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                String ability = nbttagcompound.getString("Ability");
                int amount = nbttagcompound.getInteger("Amount");
                int frequency = nbttagcompound.getInteger("Frequency");
                list.add(new SolarAbilityEntry(ability, amount, frequency));
            }
            return list;
        }

        @Override
        public String getDisplay(List<SolarAbilityEntry> value) {
            String s = "[";
            int i = 0;
            for (SolarAbilityEntry entry : value) {
                s += "<br> { \"ability\": \"" + entry.abilityKey + "\", \"amount\": " + entry.amount + ", \"frequency\": " + entry.frequency + " }";
                if (i < value.size() - 1)
                    s += ",";
                i++;
            }
            s += "<br>]";
            return s;
        }
    }

}
