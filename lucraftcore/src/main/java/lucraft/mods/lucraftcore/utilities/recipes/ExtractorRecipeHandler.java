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
import net.minecraft.util.math.MathHelper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public class ExtractorRecipeHandler {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static int id = 0;
    private static RegistryNamespaced<ResourceLocation, IExtractorRecipe> RECIPES = new RegistryNamespaced();
    public static Map<ResourceLocation, JsonObject> cachedRecipes = new HashMap<>();
    public static Map<String, AddonPackRecipeReader.JsonContextExt> cachedContexts = new HashMap<>();
    public static Map<String, JsonObject[]> cachedJsonContexts = new HashMap<>();

    public static void registerRecipe(IExtractorRecipe recipe) {
        if (recipe == null) {
            LucraftCore.LOGGER.error("Tried to register null extractor recipe!");
            return;
        }
        if (recipe.getRegistryName() == null) {
            LucraftCore.LOGGER.error("Tried to register extractor recipe without registry name!");
            return;
        }
        if (RECIPES.containsKey(recipe.getRegistryName())) {
            LucraftCore.LOGGER.error("An extractor recipe with the registry name '" + recipe.getRegistryName().toString() + "' already exists!");
            return;
        }

        RECIPES.register(id++, recipe.getRegistryName(), recipe);
    }

    public static IExtractorRecipe getRecipe(ResourceLocation resourceLocation) {
        return RECIPES.getObject(resourceLocation);
    }

    public static List<IExtractorRecipe> getRecipes() {
        return ImmutableList.copyOf(RECIPES);
    }

    public static IExtractorRecipe findExtractorRecipe(ItemStack input, ItemStack inputContainer, FluidStack inputFluid, int energy) {
        for (IExtractorRecipe recipe : getRecipes()) {
            if (matches(recipe, input, inputContainer, inputFluid, energy)) {
                return recipe;
            }
        }

        return null;
    }

    public static boolean matches(IExtractorRecipe recipe, ItemStack input, ItemStack inputContainer, FluidStack inputFluid, int energy) {
        if (energy < recipe.getRequiredEnergy())
            return false;

        if (recipe.getInputFluid() != null) {
            if (inputFluid == null)
                return false;
            else if (recipe.getInputFluid().getFluid() != inputFluid.getFluid())
                return false;
            else if (inputFluid.amount < recipe.getInputFluid().amount)
                return false;
        }

        if (recipe.getInputContainer() != null && !recipe.getInputContainer().apply(inputContainer))
            return false;

        return recipe.getInput().apply(input) && input.getCount() >= recipe.getInputAmount();
    }

    public static IExtractorRecipe parseFromJson(JsonObject json, JsonContext context, ResourceLocation loc) {
        FluidStack inputFluid = JsonUtils.hasField(json, "input_fluid") ? LCFluidUtil.parseFromJson(JsonUtils.getJsonObject(json, "input_fluid")) : null;
        int energy = JsonUtils.getInt(json, "energy", 0);
        Ingredient input = CraftingHelper.getIngredient(JsonUtils.getJsonObject(json, "input"), context);
        int inputAmount = MathHelper.clamp(JsonUtils.getInt(json, "input_amount", 1), 0, 64);
        Ingredient inputContainer = JsonUtils.hasField(json, "input_container") ? CraftingHelper.getIngredient(JsonUtils.getJsonObject(json, "input_container"), context) : null;

        JsonObject outputJson = JsonUtils.getJsonObject(json, "outputs");
        JsonObject primaryOutput = JsonUtils.getJsonObject(outputJson, "primary");
        ItemStack primaryResult = CraftingHelper.getItemStack(primaryOutput, context);
        float primaryChance = MathHelper.clamp(JsonUtils.getFloat(primaryOutput, "chance", 1F), 0F, 1F);

        ItemStack secondaryResult = ItemStack.EMPTY;
        float secondaryChance = 0F;

        if (JsonUtils.hasField(outputJson, "secondary")) {
            JsonObject secondaryOutput = JsonUtils.getJsonObject(outputJson, "secondary");
            secondaryResult = CraftingHelper.getItemStack(secondaryOutput, context);
            secondaryChance = MathHelper.clamp(JsonUtils.getFloat(secondaryOutput, "chance", 1F), 0F, 1F);
        }

        if (input == null || input.getMatchingStacks().length <= 0)
            return null;

        if (JsonUtils.hasField(json, "conditions")) {
            JsonArray jsonArray = JsonUtils.getJsonArray(json, "conditions");

            if (!CraftingHelper.processConditions(jsonArray, context)) {
                return null;
            }
        }

        return new ExtractorRecipe(input, inputAmount, inputContainer, inputFluid, primaryResult, primaryChance, secondaryResult, secondaryChance, energy).setRegistryName(loc);
    }

    @SubscribeEvent
    public static void onRead(AddonPackReadEvent e) {
        if (e.getDirectory().equals("extractor_recipes") && FilenameUtils.getExtension(e.getFileName()).equalsIgnoreCase("json")) {
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
                IExtractorRecipe recipe = parseFromJson(cachedRecipes.get(loc), context, loc);
                if (recipe != null)
                    registerRecipe(recipe);
                else
                    LucraftCore.LOGGER.error("Wasn't able to register extractor recipe '" + loc.toString() + "'!");
                cachedContexts.put(loc.getNamespace(), context);
            } catch (Exception exception) {
                LucraftCore.LOGGER.error("Wasn't able to register extractor recipe '" + loc.toString() + "': " + exception.getLocalizedMessage(), exception);
            }
        }
        cachedJsonContexts.clear();
        cachedContexts.clear();
        cachedRecipes.clear();
    }

}
