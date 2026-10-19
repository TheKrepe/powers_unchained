package lucraft.mods.lucraftcore;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public class SupporterHandler {

    private static final String SUPPORTERS = "https://squirrelcontrol.threetag.net/api/legacy/data";
    private static RegistryNamespaced<UUID, SupporterData> REGISTRY = new RegistryNamespaced<>();
    private static boolean CHECK = false;

    public static void load() {
        try {
            JsonObject json = readJsonFromUrl(SUPPORTERS);
            JsonArray array = JsonUtils.getJsonArray(JsonUtils.getJsonObject(json, "data"), "supporters");

            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                UUID uuid = UUID.fromString(JsonUtils.getString(obj, "uuid"));
                String name = JsonUtils.getString(obj, "name");
                NBTTagCompound nbt = JsonToNBT.getTagFromJson(obj.toString());
                SupporterData data = new SupporterData(uuid, name, JsonUtils.getBoolean(obj, "access", false), nbt);
                REGISTRY.register(i, uuid, data);
            }

            LucraftCore.LOGGER.info("Successfully read supporter information file!");
        } catch (Exception e) {
            LucraftCore.LOGGER.error("Was not able to read supporter information file!");
            e.printStackTrace();
            addDefaultData();
            LucraftCore.LOGGER.error("Loaded default supporter data!");
        }
    }

    private static void addDefaultData() {
        REGISTRY.register(0, UUID.fromString("0669d99d-b34d-40fc-a4d8-c7ee963cc842"), new SupporterData(UUID.fromString("0669d99d-b34d-40fc-a4d8-c7ee963cc842"), "TheLucraft", true, new NBTTagCompound()));
        REGISTRY.register(1, UUID.fromString("70e36bc3-f6d5-406b-924c-46d5c5f52101"), new SupporterData(UUID.fromString("70e36bc3-f6d5-406b-924c-46d5c5f52101"), "Neon", true, new NBTTagCompound()));
        REGISTRY.register(2, UUID.fromString("3fa3dc7d-3de2-4ba1-a0ca-adc57bf0827d"), new SupporterData(UUID.fromString("3fa3dc7d-3de2-4ba1-a0ca-adc57bf0827d"), "Sheriff", true, new NBTTagCompound()));
        REGISTRY.register(3, UUID.fromString("fa396f29-9e23-479b-93a5-43e0780f1453"), new SupporterData(UUID.fromString("fa396f29-9e23-479b-93a5-43e0780f1453"), "Nictogen", true, new NBTTagCompound()));
        REGISTRY.register(4, UUID.fromString("7400ab2f-0980-453a-a945-0bafe6cba8cc"), new SupporterData(UUID.fromString("7400ab2f-0980-453a-a945-0bafe6cba8cc"), "Spyeedy", true, new NBTTagCompound()));
        REGISTRY.register(5, UUID.fromString("13b07ab0-663e-456d-98fa-debdb8a3777b"), new SupporterData(UUID.fromString("13b07ab0-663e-456d-98fa-debdb8a3777b"), "HydroSimp", true, new NBTTagCompound()));
        REGISTRY.register(6, UUID.fromString("bc8b891e-5c25-4c9f-ae61-cdfb270f1cc1"), new SupporterData(UUID.fromString("bc8b891e-5c25-4c9f-ae61-cdfb270f1cc1"), "SubPai", true, new NBTTagCompound()));
        REGISTRY.register(7, UUID.fromString("ab572785-66d7-4f5f-b9d4-2a3a68fb9d1a"), new SupporterData(UUID.fromString("ab572785-66d7-4f5f-b9d4-2a3a68fb9d1a"), "Honeyluck", true, new NBTTagCompound()));
    }

    public static void enableSupporterCheck() {
        CHECK = true;
        LucraftCore.LOGGER.info("The supporter check has been enabled!");
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onWorldJoin(EntityJoinWorldEvent e) {
        if (CHECK && e.getEntity() == Minecraft.getMinecraft().player && (getSupporterData(Minecraft.getMinecraft().player) == null || !getSupporterData(Minecraft.getMinecraft().player).modAccess)) {
            LucraftCore.LOGGER.error("You are not allowed to play this version of the mod!");
            int i = 5 / 0;
        }
    }

    public static SupporterData getSupporterData(EntityPlayer player) {
        if (REGISTRY.containsKey(player.getGameProfile().getId())) {
            return REGISTRY.getObject(player.getGameProfile().getId());
        }
        return null;
    }

    public static SupporterData getSupporterData(UUID uuid) {
        return REGISTRY.getObject(uuid);
    }

    public static List<UUID> getUUIDs() {
        return ImmutableList.copyOf(REGISTRY.getKeys());
    }

    public static JsonObject readJsonFromUrl(String url) throws Exception {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            JsonObject json = (new JsonParser()).parse(rd).getAsJsonObject();

            if (JsonUtils.getInt(json, "error") != 200) {
                throw new Exception("Error while reading json: " + JsonUtils.getString(json, "message"));
            }

            return json;
        } finally {
            is.close();
        }
    }

    public static class SupporterData {

        protected UUID owner;
        protected String name;
        protected boolean modAccess;
        protected NBTTagCompound nbt;

        public SupporterData(UUID owner, String name, boolean modAccess, NBTTagCompound nbt) {
            this.owner = owner;
            this.name = name;
            this.modAccess = modAccess;
            this.nbt = nbt;
        }

        public UUID getOwner() {
            return owner;
        }

        public String getName() {
            return name;
        }

        public boolean hasModAccess() {
            return modAccess;
        }

        public NBTTagCompound getNbt() {
            return nbt;
        }
    }

}
