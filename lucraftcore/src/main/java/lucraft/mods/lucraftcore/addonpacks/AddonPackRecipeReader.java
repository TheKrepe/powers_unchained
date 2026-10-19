package lucraft.mods.lucraftcore.addonpacks;

import com.google.common.collect.Maps;
import com.google.gson.*;
import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public class AddonPackRecipeReader {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static Map<String, List<RecipeCache>> recipes = new HashMap<>();
    private static Map<String, JsonContextExt> contexts = new HashMap<>();

    @SubscribeEvent
    public static void onRead(AddonPackReadEvent e) {
        if (e.getMod() == null && e.getDirectory().equals("recipes") && FilenameUtils.getExtension(e.getFileName()).equalsIgnoreCase("json")) {
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(e.getInputStream(), StandardCharsets.UTF_8));
            JsonContextExt ctx = contexts.containsKey(e.getResourceLocation().getNamespace()) ? contexts.get(e.getResourceLocation().getNamespace()) : new JsonContextExt(e.getResourceLocation().getNamespace());

            if (e.getFileName().endsWith("_constants.json")) {
                JsonObject[] json = JsonUtils.fromJson(GSON, bufferedreader, JsonObject[].class);
                ctx.loadConstants(json);
            } else {
                JsonObject jsonobject = (new JsonParser()).parse(bufferedreader).getAsJsonObject();
                List<RecipeCache> list = recipes.containsKey(e.getResourceLocation().getNamespace()) ? recipes.get(e.getResourceLocation().getNamespace()) : new ArrayList<>();
                list.add(new RecipeCache(jsonobject, e.getResourceLocation()));
                recipes.put(e.getResourceLocation().getNamespace(), list);
            }
            IOUtils.closeQuietly(bufferedreader);
            contexts.put(e.getResourceLocation().getNamespace(), ctx);
        }
    }

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> e) {
        for (String modid : recipes.keySet()) {
            List<RecipeCache> list = recipes.get(modid);

            for (RecipeCache recipes : list) {
                try {
                    JsonContextExt ctx = contexts.containsKey(recipes.resourceLocation.getNamespace()) ? contexts.get(recipes.resourceLocation.getNamespace()) : new JsonContextExt(recipes.resourceLocation.getNamespace());
                    JsonObject json = recipes.jsonObject;
                    if (!json.has("conditions") || CraftingHelper.processConditions(JsonUtils.getJsonArray(json, "conditions"), ctx)) {
                        IRecipe recipe = CraftingHelper.getRecipe(json, ctx).setRegistryName(recipes.resourceLocation);
                        e.getRegistry().register(recipe);
                    }
                    contexts.put(recipes.resourceLocation.getNamespace(), ctx);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        contexts.clear();
        recipes.clear();
    }

    public static class RecipeCache {

        public JsonObject jsonObject;
        public ResourceLocation resourceLocation;

        public RecipeCache(JsonObject jsonObject, ResourceLocation resourceLocation) {
            this.jsonObject = jsonObject;
            this.resourceLocation = resourceLocation;
        }
    }

    public static class JsonContextExt extends JsonContext {

        private Map<String, Ingredient> constants = Maps.newHashMap();

        public JsonContextExt(String modId) {
            super(modId);
        }

        @Override
        @Nullable
        public Ingredient getConstant(String name) {
            return constants.get(name);
        }

        public void loadConstants(JsonObject[] jsons) {
            for (JsonObject json : jsons) {
                if (json.has("conditions") && !CraftingHelper.processConditions(json.getAsJsonArray("conditions"), this))
                    continue;
                if (!json.has("ingredient"))
                    throw new JsonSyntaxException("Constant entry must contain 'ingredient' value");
                constants.put(JsonUtils.getString(json, "name"), CraftingHelper.getIngredient(json.get("ingredient"), this));
            }

        }
    }

}
