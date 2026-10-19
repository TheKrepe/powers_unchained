package lucraft.mods.lucraftcore.utilities.jei;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.addonpacks.AddonPackReadEvent;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.utilities.network.MessageClearJEIInfo;
import lucraft.mods.lucraftcore.utilities.network.MessageSyncJEIInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public class JEIInfoReader {

    private static Map<ResourceLocation, JsonObject> data = new HashMap<>();
    private static Map<ResourceLocation, JEIInfo> INFO = new HashMap<>();
    private static Map<ResourceLocation, JEIInfo> OVERRIDEN = new HashMap<>();

    @SubscribeEvent
    public static void onReadInfo(AddonPackReadEvent e) {
        if (Loader.isModLoaded("jei") && e.getDirectory().equals("jei_info") && FilenameUtils.getExtension(e.getFileName()).equalsIgnoreCase("json")) {
            try {
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(e.getInputStream(), StandardCharsets.UTF_8));
                JsonObject jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
                data.put(e.getResourceLocation(), jsonobject);
            } catch (Exception e2) {
                LucraftCore.LOGGER.error("Wasn't able to read JEI info '" + e.getFileName() + "' in addon pack '" + e.getPackFile().getName() + "': " + e2.getMessage());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!e.player.world.isRemote && e.player instanceof EntityPlayerMP) {
            LCPacketDispatcher.sendTo(new MessageClearJEIInfo(), (EntityPlayerMP) e.player);
            for (ResourceLocation loc : INFO.keySet()) {
                JEIInfo info = INFO.get(loc);
                LCPacketDispatcher.sendTo(new MessageSyncJEIInfo(loc, info), (EntityPlayerMP) e.player);
            }
        }
    }

    public static Map<ResourceLocation, JEIInfo> getInfo() {
        return OVERRIDEN;
    }

    public static void register(ResourceLocation loc, JEIInfo info) {
        INFO.put(loc, info);
        OVERRIDEN.put(loc, info);
    }

    public static void loadRecipes() {
        if (!Loader.isModLoaded("jei"))
            return;
        for (ResourceLocation loc : data.keySet()) {
            JsonObject jsonobject = data.get(loc);
            INFO.put(loc, JEIInfo.deserialize(jsonobject));
            OVERRIDEN.put(loc, JEIInfo.deserialize(jsonobject));
        }
        data.clear();
    }

    public static class JEIInfo {

        protected ItemStack[] itemStacks;
        protected ITextComponent text;

        public JEIInfo(ITextComponent text, ItemStack... itemStacks) {
            this.itemStacks = itemStacks;
            this.text = text;
        }

        public ItemStack[] getItemStacks() {
            return itemStacks;
        }

        public ITextComponent getText() {
            return text;
        }

        public static JEIInfo deserialize(JsonObject json) {
            JsonArray array = JsonUtils.getJsonArray(json, "items");
            ItemStack[] stacks = new ItemStack[array.size()];
            for (int i = 0; i < array.size(); i++) {
                JsonObject object = array.get(i).getAsJsonObject();
                stacks[i] = deserializeItemStack(object);
            }
            ITextComponent text = ITextComponent.Serializer.jsonToComponent(JsonUtils.getJsonObject(json, "text").toString());
            return new JEIInfo(text, stacks);
        }

        private static ItemStack deserializeItemStack(JsonObject object) {
            if (!object.has("item")) {
                throw new JsonSyntaxException("Unsupported item, add 'item' key");
            } else {
                Item item = JsonUtils.getItem(object, "item");
                int i = JsonUtils.getInt(object, "data", 0);
                ItemStack ret = new ItemStack(item, JsonUtils.getInt(object, "amount", 1), i);
                ret.setTagCompound(net.minecraftforge.common.util.JsonUtils.readNBT(object, "nbt"));
                return ret;
            }
        }

    }

}
