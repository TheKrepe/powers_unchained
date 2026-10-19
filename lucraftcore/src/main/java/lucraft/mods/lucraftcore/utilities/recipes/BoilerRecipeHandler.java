package lucraft.mods.lucraftcore.utilities.recipes;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.addonpacks.AddonPackReadEvent;
import lucraft.mods.lucraftcore.addonpacks.AddonPackRecipeReader;
import lucraft.mods.lucraftcore.util.fluids.LCFluidUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public class BoilerRecipeHandler {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static int id = 0;
    private static RegistryNamespaced<ResourceLocation, IBoilerRecipe> RECIPES = new RegistryNamespaced();
    public static Map<ResourceLocation, JsonObject> cachedRecipes = new HashMap<>();
    public static Map<String, AddonPackRecipeReader.JsonContextExt> cachedContexts = new HashMap<>();
    public static Map<String, JsonObject[]> cachedJsonContexts = new HashMap<>();

    public static void registerRecipe(IBoilerRecipe recipe) {
        if (recipe == null) {
            LucraftCore.LOGGER.error("Tried to register null boiler recipe!");
            return;
        }
        if (recipe.getRegistryName() == null) {
            LucraftCore.LOGGER.error("Tried to register boiler recipe without registry name!");
            return;
        }
        if (RECIPES.containsKey(recipe.getRegistryName())) {
            LucraftCore.LOGGER.error("A boiler recipe with the registry name '" + recipe.getRegistryName().toString() + "' already exists!");
            return;
        }

        RECIPES.register(id++, recipe.getRegistryName(), recipe);
    }

    public static IBoilerRecipe getRecipe(ResourceLocation resourceLocation) {
        return RECIPES.getObject(resourceLocation);
    }

    public static List<IBoilerRecipe> getRecipes() {
        return ImmutableList.copyOf(RECIPES);
    }

    public static IBoilerRecipe findBoilerRecipe(List<ItemStack> items, FluidStack fluidStack, int energy) {
        for (IBoilerRecipe recipe : getRecipes()) {
            if (matches(recipe, items, fluidStack, energy)) {
                return recipe;
            }
        }

        return null;
    }

    public static boolean matches(IBoilerRecipe recipe, List<ItemStack> items, FluidStack fluidStack, int energy) {
        if (energy < recipe.getRequiredEnergy())
            return false;

        if (recipe.getInputFluid() != null) {
            if (fluidStack == null)
                return false;
            else if (recipe.getInputFluid().getFluid() != fluidStack.getFluid())
                return false;
            else if (fluidStack.amount < recipe.getInputFluid().amount)
                return false;
        }

        if (items == null || items.size() <= 0)
            return false;

        List<ItemStack> list = new ArrayList<>();
        list.addAll(items);
        List<Ingredient> ingredientList = new ArrayList<>();

        for (Ingredient ingredient : recipe.getIngredients()) {
            for (ItemStack stack : items) {
                if (!ingredientList.contains(ingredient) && list.contains(stack) && ingredient.apply(stack)) {
                    list.remove(stack);
                    ingredientList.add(ingredient);
                }
            }
        }

        return ingredientList.size() == recipe.getIngredients().length && list.size() == 0;
    }

    public static IBoilerRecipe parseFromJson(JsonObject json, JsonContext context, ResourceLocation loc) {
        FluidStack input = JsonUtils.hasField(json, "input_fluid") ? LCFluidUtil.parseFromJson(JsonUtils.getJsonObject(json, "input_fluid")) : null;
        FluidStack output = LCFluidUtil.parseFromJson(JsonUtils.getJsonObject(json, "result"));
        int energy = JsonUtils.getInt(json, "energy", 0);

        if (output == null)
            throw new RuntimeException("The result fluid can not be null!");

        JsonArray array = JsonUtils.getJsonArray(json, "ingredients");
        Ingredient[] ingredients = new Ingredient[array.size()];
        for (int i = 0; i < array.size(); i++) {
            ingredients[i] = CraftingHelper.getIngredient(array.get(i), context);
            if (ingredients[i] == null || ingredients[i].getMatchingStacks().length <= 0)
                return null;
        }

        if (JsonUtils.hasField(json, "conditions")) {
            JsonArray jsonArray = JsonUtils.getJsonArray(json, "conditions");

            if (!CraftingHelper.processConditions(jsonArray, context)) {
                return null;
            }
        }

        return new BoilerRecipe(output, energy, input, ingredients).setRegistryName(loc);
    }

    @SubscribeEvent
    public static void onRead(AddonPackReadEvent e) {
        if (e.getDirectory().equals("boiler_recipes") && FilenameUtils.getExtension(e.getFileName()).equalsIgnoreCase("json")) {
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(e.getInputStream(), StandardCharsets.UTF_8));
            if (e.getFileName().endsWith("_constants.json")) {
                JsonObject[] json = JsonUtils.fromJson(GSON, bufferedreader, JsonObject[].class);
                cachedJsonContexts.put(e.getResourceLocation().getNamespace(), json);
            } else {
                JsonObject json = JsonUtils.fromJson(GSON, bufferedreader, JsonObject.class);
                cachedRecipes.put(e.getResourceLocation(), json);
            }
            IOUtils.closeQuietly(bufferedreader);
        }
    }

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> e) {
        for (String mods : cachedJsonContexts.keySet()) {
            AddonPackRecipeReader.JsonContextExt context = new AddonPackRecipeReader.JsonContextExt(mods);
            context.loadConstants(cachedJsonContexts.get(mods));
            cachedContexts.put(mods, context);
        }
        for (ResourceLocation loc : cachedRecipes.keySet()) {
            try {
                AddonPackRecipeReader.JsonContextExt context = cachedContexts.containsKey(loc.getNamespace()) ? cachedContexts.get(loc.getNamespace()) : new AddonPackRecipeReader.JsonContextExt(loc.getNamespace());
                IBoilerRecipe recipe = parseFromJson(cachedRecipes.get(loc), context, loc);
                if (recipe != null)
                    registerRecipe(recipe);
                else
                    LucraftCore.LOGGER.error("Wasn't able to register boiler recipe '" + loc.toString() + "'!");
                cachedContexts.put(loc.getNamespace(), context);
            } catch (Exception exception) {
                LucraftCore.LOGGER.error("Wasn't able to register boiler recipe '" + loc.toString() + "': " + exception.getLocalizedMessage(), exception);
            }
        }
        cachedJsonContexts.clear();
        cachedContexts.clear();
        cachedRecipes.clear();
    }

}
