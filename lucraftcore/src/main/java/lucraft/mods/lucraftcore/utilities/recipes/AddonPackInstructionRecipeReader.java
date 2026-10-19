package lucraft.mods.lucraftcore.utilities.recipes;

import com.google.gson.*;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.addonpacks.AddonPackReadEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = LucraftCore.MODID)
public class AddonPackInstructionRecipeReader {

    public static Map<ResourceLocation, JsonObject> recipes = new HashMap<>();

    @SubscribeEvent
    public static void onRead(AddonPackReadEvent e) {
        if (e.getDirectory().equals("instructionrecipes") && FilenameUtils.getExtension(e.getFileName()).equalsIgnoreCase("json")) {
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(e.getInputStream(), StandardCharsets.UTF_8));
            JsonObject jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
            recipes.put(e.getResourceLocation(), jsonobject);
        }
    }

    public static void loadRecipes() {
        for (ResourceLocation loc : recipes.keySet()) {
            InstructionRecipe recipe = null;

            try {
                JsonObject jsonobject = recipes.get(loc);
                ItemStack output = deserializeItemStack(JsonUtils.getJsonObject(jsonobject, "output"));
                List<ItemStack> requirements = new ArrayList<>();
                JsonArray array = JsonUtils.getJsonArray(jsonobject, "requirements");
                for (int i = 0; i < array.size(); i++) {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    requirements.add(deserializeItemStack(obj));
                }
                recipe = new InstructionRecipe(output, requirements.toArray(new ItemStack[requirements.size()])).setRegistryName(loc);
            } catch (JsonParseException e) {
                LucraftCore.LOGGER.error("Error in instruction recipe '" + loc.toString() + "': " + e.getMessage());
            }
            if (recipe != null)
                InstructionRecipe.registerInstructionRecipe(recipe);
        }

        recipes.clear();
    }

    private static ItemStack deserializeItemStack(JsonObject object) throws JsonParseException {
        if (!object.has("item")) {
            throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
        } else {
            Item item = JsonUtils.getItem(object, "item");
            int i = JsonUtils.getInt(object, "data", 0);
            ItemStack ret = new ItemStack(item, JsonUtils.getInt(object, "amount", 1), i);
            ret.setTagCompound(net.minecraftforge.common.util.JsonUtils.readNBT(object, "nbt"));
            return ret;
        }
    }

}
