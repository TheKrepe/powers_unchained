package lucraft.mods.lucraftcore.materials;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.ModuleInfinity;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import lucraft.mods.lucraftcore.util.helper.mods.ThermalExpansionHelper;
import lucraft.mods.lucraftcore.utilities.blocks.UtilitiesBlocks;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MaterialsRecipes {

    public static boolean generateRecipeJson = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRegisterRecipes(RegistryEvent.Register<IRecipe> e) {
        // Ore Dictionary
        MaterialsRecipes.init();

        if (generateRecipeJson) {

            for (Material m : Material.getMaterials()) {
                // Ingot -> Block
                if (m.autoGenerateComponent(MaterialComponent.BLOCK))
                    addShapedRecipe(m.getItemStack(MaterialComponent.BLOCK), new ResourceLocation(LucraftCore.MODID, "ingots_to_block"), "XXX", "XXX", "XXX", 'X', m.getOreDictionaryName(MaterialComponent.INGOT));

                // Block -> Ingot
                if (m.autoGenerateComponent(MaterialComponent.INGOT))
                    addShapedRecipe(m.getItemStack(MaterialComponent.INGOT, 9), new ResourceLocation(LucraftCore.MODID, "block_to_ingots"), "X", 'X', m.getOreDictionaryName(MaterialComponent.BLOCK));

                // Nugget -> Ingot
                if (m.autoGenerateComponent(MaterialComponent.INGOT))
                    addShapedRecipe(m.getItemStack(MaterialComponent.INGOT), new ResourceLocation(LucraftCore.MODID, "nuggets_to_ingot"), "XXX", "XXX", "XXX", 'X', m.getOreDictionaryName(MaterialComponent.NUGGET));

                // Ingot -> Nugget
                if (m.autoGenerateComponent(MaterialComponent.NUGGET))
                    addShapedRecipe(m.getItemStack(MaterialComponent.NUGGET, 9), new ResourceLocation(LucraftCore.MODID, "ingot_to_nuggets"), "X", 'X', m.getOreDictionaryName(MaterialComponent.INGOT));

                // Ingot -> Plate
                if (m.autoGenerateComponent(MaterialComponent.PLATE)) {
                    ItemStack stack = m.getItemStack(MaterialComponent.INGOT);
                    if (!stack.isEmpty())
                        addShapelessRecipe(m.getItemStack(MaterialComponent.PLATE), new ResourceLocation(LucraftCore.MODID, "ingots_to_plate"), new ItemStack(UtilitiesItems.HAMMER, 1, OreDictionary.WILDCARD_VALUE), m.getOreDictionaryName(MaterialComponent.INGOT), stack);
                }

                // Ingot -> Dust
                if (m.autoGenerateComponent(MaterialComponent.DUST)) {
                    Object stack = m == Material.COAL ? new ItemStack(Items.COAL) : (m == Material.CHARCOAL ? new ItemStack(Items.COAL, 1, 1) : m.getOreDictionaryName(MaterialComponent.INGOT));
                    if (stack != null)
                        addShapelessRecipe(m.getItemStack(MaterialComponent.DUST), new ResourceLocation(LucraftCore.MODID, "ingot_to_dust"), new ItemStack(UtilitiesItems.HAMMER, 1, OreDictionary.WILDCARD_VALUE), stack);
                }
            }

            for (EnumDyeColor color : EnumDyeColor.values()) {
                String dye = "dye" + color.getTranslationKey().substring(0, 1).toUpperCase() + color.getTranslationKey().substring(1, color.getTranslationKey().length());
                if (color == EnumDyeColor.SILVER)
                    dye = "dyeLightGray";
                addShapelessRecipe(new ItemStack(UtilitiesItems.TRI_POLYMER.get(color)), new ResourceLocation(LucraftCore.MODID, "tri_polymer"), dye, "plateIron", Items.LEATHER);
            }

            addShapedRecipe(new ItemStack(UtilitiesItems.HAMMER), " XX", "SSX", " XX", 'X', "ingotIron", 'S', "stickWood");
            addShapedRecipe(new ItemStack(UtilitiesItems.INJECTION), "  N", "IP ", "PI ", 'P', "plateTin", 'N', "nuggetSilver", 'I', "ingotIron");
            addShapedRecipe(new ItemStack(UtilitiesItems.LV_CAPACITOR), new ResourceLocation(LucraftCore.MODID, "capacitor"), "IRI", "PLP", "PCP", 'R', "dustRedstone", 'I', "ingotIron", 'P', "plateIron", 'L', "ingotLead", 'C', "plateCopper");
            addShapedRecipe(new ItemStack(UtilitiesItems.MV_CAPACITOR), new ResourceLocation(LucraftCore.MODID, "capacitor"), "IRI", "PLP", "PCP", 'R', "blockRedstone", 'I', "ingotIron", 'P', "plateIron", 'L', "ingotLead", 'C', "plateOsmium");
            addShapedRecipe(new ItemStack(UtilitiesItems.HV_CAPACITOR), new ResourceLocation(LucraftCore.MODID, "capacitor"), "IRI", "PLP", "PCP", 'R', "blockRedstone", 'I', "ingotSteel", 'P', "plateIron", 'L', "blockLead", 'C', "plateIntertium");
            addShapedRecipe(new ItemStack(UtilitiesItems.WIRE_CUTTER), " P ", "S S", 'P', "plateIron", 'S', "stickWood");
            addShapelessRecipe(Material.COPPER.getItemStack(MaterialComponent.WIRING, 4), "plateCopper", UtilitiesItems.WIRE_CUTTER);
            addShapedRecipe(new ItemStack(UtilitiesItems.SERVO_MOTOR), " P ", "WIW", " P ", 'P', "plateIron", 'I', "ingotIron", 'W', "wireCopper");
            addShapedRecipe(new ItemStack(UtilitiesItems.BASIC_CIRCUIT), new ResourceLocation(LucraftCore.MODID, "circuit"), "WWW", "RPR", "WWW", 'W', "wireCopper", 'R', "dustRedstone", 'P', "plateIron");
            addShapedRecipe(new ItemStack(UtilitiesItems.ADVANCED_CIRCUIT), new ResourceLocation(LucraftCore.MODID, "circuit"), "WCW", "RPR", "WCW", 'W', "wireCopper", 'R', "dustRedstone", 'P', "circuitBasic", 'C', UtilitiesItems.LV_CAPACITOR);
            addShapedRecipe(new ItemStack(UtilitiesBlocks.CONSTRUCTION_TABLE), "PPP", "ICI", "IPI", 'P', "plateIron", 'C', Blocks.CRAFTING_TABLE, 'I', "ingotIron");
            addShapedRecipe(new ItemStack(UtilitiesBlocks.SUIT_STAND), " I ", " A ", "PPP", 'P', "plateIron", 'I', "ingotIron", 'A', Items.ARMOR_STAND);
            addShapedRecipe(new ItemStack(UtilitiesBlocks.EXTRACTOR), "PIP", "BCB", "PXP", 'P', "plateIron", 'I', UtilitiesItems.INJECTION, 'B', Items.BUCKET, 'C', "circuitBasic", 'X', UtilitiesItems.LV_CAPACITOR);
            addShapedRecipe(new ItemStack(UtilitiesBlocks.BOILER), "PCP", "BXB", "PKP", 'P', "plateIron", 'K', Items.CAULDRON, 'B', Items.BUCKET, 'C', "circuitBasic", 'X', UtilitiesItems.LV_CAPACITOR);
            addShapedRecipe(new ItemStack(ModuleInfinity.INFINITY_GENERATOR), "PCP", "SNS", "PAP", 'P', "plateDwarfStarAlloy", 'C', "circuitAdvanced", 'S', "blockSteel", 'N', Items.NETHER_STAR, 'A', UtilitiesItems.HV_CAPACITOR);
            addShapedRecipe(new ItemStack(UtilitiesBlocks.FURNACE_GENERATOR), "CRC", "IBI", "IFI", 'C', "ingotCopper", 'R', "blockRedstone", 'I', "plateIron", 'B', UtilitiesItems.LV_CAPACITOR, 'F', Blocks.FURNACE);

            addShapelessRecipe(Material.STEEL.getItemStack(MaterialComponent.DUST, 1), new ResourceLocation(LucraftCore.MODID, "alloys"), "dustIron", "dustCoal", "dustCoal", "dustCoal", "dustCoal");
            addShapelessRecipe(Material.ADAMANTIUM.getItemStack(MaterialComponent.DUST, 3), new ResourceLocation(LucraftCore.MODID, "alloys"), "dustSteel", "dustSteel", "dustVibranium");
            addShapelessRecipe(Material.INTERTIUM.getItemStack(MaterialComponent.DUST, 3), new ResourceLocation(LucraftCore.MODID, "alloys"), "dustIron", "dustIron", "dustOsmium");
            addShapelessRecipe(Material.BRONZE.getItemStack(MaterialComponent.DUST, 2), new ResourceLocation(LucraftCore.MODID, "alloys"), "dustCopper", "dustCopper", "dustCopper", "dustTin");
            addShapelessRecipe(Material.GOLD_TITANIUM_ALLOY.getItemStack(MaterialComponent.DUST, 3), new ResourceLocation(LucraftCore.MODID, "alloys"), "dustGold", "dustTitanium", "dustTitanium");

            generateConstants();
        }
    }

    public static void init() {
        // OreDictionary Registry
        for (Material m : Material.getMaterials()) {
            for (MaterialComponent mc : MaterialComponent.values()) {
                if (mc != MaterialComponent.ALL && mc != MaterialComponent.FLUID && m.autoGenerateComponent(mc)) {
                    OreDictionary.registerOre(m.getOreDictionaryName(mc), m.getItemStack(mc));
                    if (mc == MaterialComponent.WIRING)
                        OreDictionary.registerOre(m.getOreDictionaryName(mc).replace("wire", "wiring"), m.getItemStack(mc));
                }
            }
        }
    }

    public static void postInit() {
        for (Material m : Material.getMaterials()) {
            if (!m.getItemStack(MaterialComponent.ORE).isEmpty() && !m.getItemStack(MaterialComponent.INGOT).isEmpty())
                GameRegistry.addSmelting(m.getItemStack(MaterialComponent.ORE), m.getItemStack(MaterialComponent.INGOT), 1F);
            if (!m.getItemStack(MaterialComponent.DUST).isEmpty() && !m.getItemStack(MaterialComponent.INGOT).isEmpty())
                GameRegistry.addSmelting(m.getItemStack(MaterialComponent.DUST), m.getItemStack(MaterialComponent.INGOT), 1F);
        }

        // Bronze
        addAlloyRecipe("ingotTin", 1, "ingotCopper", 3, "ingotBronze", 4);
        addAlloyRecipe("ingotTin", 1, "dustCopper", 3, "ingotBronze", 4);
        addAlloyRecipe("dustTin", 1, "ingotCopper", 3, "ingotBronze", 4);
        addAlloyRecipe("dustTin", 1, "dustCopper", 3, "ingotBronze", 4);

        // Adamantium
        addAlloyRecipe("ingotSteel", 2, "ingotVibranium", 1, "ingotAdamantium", 3);
        addAlloyRecipe("ingotSteel", 2, "dustVibranium", 1, "ingotAdamantium", 3);
        addAlloyRecipe("dustSteel", 2, "ingotVibranium", 1, "ingotAdamantium", 3);
        addAlloyRecipe("dustSteel", 2, "dustVibranium", 1, "ingotAdamantium", 3);

        // Intertium
        addAlloyRecipe("ingotIron", 2, "ingotOsmium", 1, "ingotIntertium", 3);
        addAlloyRecipe("ingotIron", 2, "dustOsmium", 1, "ingotIntertium", 3);
        addAlloyRecipe("dustIron", 2, "ingotOsmium", 1, "ingotIntertium", 3);
        addAlloyRecipe("dustIron", 2, "dustOsmium", 1, "ingotIntertium", 3);

        // Gold-Titanium-Alloy
        addAlloyRecipe("ingotGold", 1, "ingotTitanium", 2, "ingotGoldTitaniumAlloy", 3);
        addAlloyRecipe("ingotGold", 1, "dustGold", 2, "ingotGoldTitaniumAlloy", 3);
        addAlloyRecipe("dustGold", 1, "ingotTitanium", 2, "ingotGoldTitaniumAlloy", 3);
        addAlloyRecipe("dustGold", 1, "dustTitanium", 2, "ingotGoldTitaniumAlloy", 3);

        // Invar
        addAlloyRecipe("ingotIron", 2, "ingotNickel", 1, "ingotInvar", 3);
        addAlloyRecipe("ingotIron", 2, "dustIron", 1, "ingotInvar", 3);
        addAlloyRecipe("dustIron", 2, "ingotNickel", 1, "ingotInvar", 3);
        addAlloyRecipe("dustIron", 2, "dustNickel", 1, "ingotInvar", 3);

        // Electrum
        addAlloyRecipe("ingotGold", 1, "ingotSilver", 1, "ingotElectrum", 2);
        addAlloyRecipe("ingotGold", 1, "dustGold", 1, "ingotElectrum", 2);
        addAlloyRecipe("dustGold", 1, "ingotSilver", 1, "ingotElectrum", 2);
        addAlloyRecipe("dustGold", 2, "dustSilver", 1, "ingotElectrum", 3);

        // Steel
        addAlloyRecipe("dustIron", 1, "dustCoal", 2, "ingotSteel", 1);
        addAlloyRecipe("dustIron", 1, "dustCharcoal", 4, "ingotSteel", 1);
        addAlloyRecipe("ingotIron", 1, "dustCoal", 2, "ingotSteel", 1);
        addAlloyRecipe("ingotIron", 1, "dustCharcoal", 4, "ingotSteel", 1);
        addAlloyRecipe("dustSteel", 1, "dustCoal", 2, "ingotSteel", 1);
        addAlloyRecipe("dustSteel", 1, "dustCharcoal", 2, "ingotSteel", 1);
    }

    public static void addAlloyRecipe(String ore1, int amount1, String ore2, int amount2, String output, int amountOutput) {
        for (ItemStack i1 : OreDictionary.getOres(ore1)) {
            for (ItemStack i2 : OreDictionary.getOres(ore2)) {
                for (ItemStack i3 : OreDictionary.getOres(output)) {
                    ItemStack ingredient1 = i1.copy();
                    ingredient1.setCount(amount1);
                    ItemStack ingredient2 = i2.copy();
                    ingredient2.setCount(amount2);
                    ItemStack outputStack = i3.copy();
                    outputStack.setCount(amountOutput);

                    // addAlloySmelterRecipe(ingredient1, ingredient2,
                    // outputStack, 2F);
                    int amount = ingredient1.getCount() + ingredient2.getCount();
                    ThermalExpansionHelper.addSmelterRecipe(amount * 1200, ingredient1, ingredient2, outputStack);
                }
            }
        }
    }

    // Credit:
    // https://gist.github.com/williewillus/a1a899ce5b0f0ba099078d46ae3dae6e

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File RECIPE_DIR = null;
    private static final Set<String> USED_OD_NAMES = new TreeSet<>();

    public static void setupDir() {
        if (RECIPE_DIR == null) {
            RECIPE_DIR = new File("recipes");
        }

        if (!RECIPE_DIR.exists()) {
            RECIPE_DIR.mkdir();
        }
    }

    public static void addShapedRecipe(ItemStack result, Object... components) {
        addShapedRecipe(result, null, components);
    }

    public static void addShapedRecipe(ItemStack result, ResourceLocation group, Object... components) {
        setupDir();

        // GameRegistry.addShapedRecipe(result, components);

        Map<String, Object> json = new HashMap<>();

        {
            List<ResourceLocation> items = new ArrayList<>();
            items.add(result.getItem().getRegistryName());

            for (Object c : components) {
                ResourceLocation loc = null;
                if (c instanceof ItemStack)
                    loc = ((ItemStack) c).getItem().getRegistryName();
                else if (c instanceof Item)
                    loc = ((Item) c).getRegistryName();
                else if (c instanceof Block)
                    loc = ((Block) c).getRegistryName();

                if (!items.contains(loc) && loc != null)
                    items.add(loc);
            }

            List<Map<String, Object>> conditions = new ArrayList<>();

            for (ResourceLocation rl : items) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "minecraft:item_exists");
                map.put("item", rl.toString());
                conditions.add(map);
            }

            json.put("conditions", conditions.toArray(new Map[conditions.size()]));
        }

        List<String> pattern = new ArrayList<>();
        int i = 0;
        while (i < components.length && components[i] instanceof String) {
            pattern.add((String) components[i]);
            i++;
        }
        json.put("pattern", pattern);

        boolean isOreDict = false;
        Map<String, Map<String, Object>> key = new HashMap<>();
        Character curKey = null;
        for (; i < components.length; i++) {
            Object o = components[i];
            if (o instanceof Character) {
                if (curKey != null)
                    throw new IllegalArgumentException("Provided two char ac_keys in a row");
                curKey = (Character) o;
            } else {
                if (curKey == null)
                    throw new IllegalArgumentException("Providing object without a char key");
                if (o instanceof String)
                    isOreDict = true;
                key.put(Character.toString(curKey), serializeItem(o));
                curKey = null;
            }
        }
        json.put("key", key);
        json.put("type", isOreDict ? "forge:ore_shaped" : "minecraft:crafting_shaped");
        json.put("result", serializeItem(result));

        if (group != null) {
            json.put("group", group.toString());
        }

        // names the json the same name as the output's registry name
        // repeatedly adds _alt if a file already exists
        // janky I know but it works
        String suffix = result.getItem().getHasSubtypes() ? "_" + result.getItemDamage() : "";
        File f = new File(RECIPE_DIR, result.getItem().getRegistryName().getPath() + suffix + ".json");

        while (f.exists()) {
            suffix += "_alt";
            f = new File(RECIPE_DIR, result.getItem().getRegistryName().getPath() + suffix + ".json");
        }

        try (FileWriter w = new FileWriter(f)) {
            GSON.toJson(json, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addShapelessRecipe(ItemStack result, Object... components) {
        addShapelessRecipe(result, null, components);
    }

    public static void addShapelessRecipe(ItemStack result, ResourceLocation group, Object... components) {
        setupDir();

        // addShapelessRecipe(result, components);

        Map<String, Object> json = new HashMap<>();

        {
            List<ResourceLocation> items = new ArrayList<>();
            items.add(result.getItem().getRegistryName());

            for (Object c : components) {
                ResourceLocation loc = null;
                if (c instanceof ItemStack)
                    loc = ((ItemStack) c).getItem().getRegistryName();
                else if (c instanceof Item)
                    loc = ((Item) c).getRegistryName();
                else if (c instanceof Block)
                    loc = ((Block) c).getRegistryName();

                if (!items.contains(loc) && loc != null)
                    items.add(loc);
            }

            List<Map<String, Object>> conditions = new ArrayList<>();

            for (ResourceLocation rl : items) {
                Map<String, Object> map = new HashMap<>();
                map.put("type", "minecraft:item_exists");
                map.put("item", rl.toString());
                conditions.add(map);
            }

            json.put("conditions", conditions.toArray(new Map[conditions.size()]));
        }

        boolean isOreDict = false;
        List<Map<String, Object>> ingredients = new ArrayList<>();
        for (Object o : components) {
            if (o instanceof String)
                isOreDict = true;
            ingredients.add(serializeItem(o));
        }
        json.put("ingredients", ingredients);
        json.put("type", isOreDict ? "forge:ore_shapeless" : "minecraft:crafting_shapeless");
        json.put("result", serializeItem(result));

        if (group != null) {
            json.put("group", group.toString());
        }

        // names the json the same name as the output's registry name
        // repeatedly adds _alt if a file already exists
        // janky I know but it works
        String suffix = result.getItem().getHasSubtypes() ? "_" + result.getItemDamage() : "";
        File f = new File(RECIPE_DIR, result.getItem().getRegistryName().getPath() + suffix + ".json");

        while (f.exists()) {
            suffix += "_alt";
            f = new File(RECIPE_DIR, result.getItem().getRegistryName().getPath() + suffix + ".json");
        }

        try (FileWriter w = new FileWriter(f)) {
            GSON.toJson(json, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> serializeItem(Object thing) {
        if (thing instanceof Item) {
            return serializeItem(new ItemStack((Item) thing));
        }
        if (thing instanceof Block) {
            return serializeItem(new ItemStack((Block) thing));
        }
        if (thing instanceof ItemStack) {
            ItemStack stack = (ItemStack) thing;
            Map<String, Object> ret = new HashMap<>();
            ret.put("item", stack.getItem().getRegistryName().toString());
            if (stack.getItem().getHasSubtypes() || stack.getItemDamage() != 0) {
                ret.put("data", stack.getItemDamage());
            }
            if (stack.getCount() > 1) {
                ret.put("count", stack.getCount());
            }

            if (stack.hasTagCompound()) {
                ret.put("type", "minecraft:item_nbt");
                ret.put("nbt", stack.getTagCompound().toString());
            }

            return ret;
        }
        if (thing instanceof String) {
            Map<String, Object> ret = new HashMap<>();
            USED_OD_NAMES.add((String) thing);
            ret.put("item", "#" + ((String) thing).toUpperCase(Locale.ROOT));
            return ret;
        }

        throw new IllegalArgumentException("Not a block, item, stack, or od name");
    }

    // Call this after you are done generating
    public static void generateConstants() {
        List<Map<String, Object>> json = new ArrayList<>();
        for (String s : USED_OD_NAMES) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("name", s.toUpperCase(Locale.ROOT));
            entry.put("ingredient", ImmutableMap.of("type", "forge:ore_dict", "ore", s));
            json.add(entry);
        }

        try (FileWriter w = new FileWriter(new File(RECIPE_DIR, "_constants.json"))) {
            GSON.toJson(json, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
