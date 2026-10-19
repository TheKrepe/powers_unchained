package lucraft.mods.lucraftcore.utilities.jei;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.ModuleInfinity;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.util.recipe.RecipeFactoryRecolor;
import lucraft.mods.lucraftcore.utilities.blocks.UtilitiesBlocks;
import lucraft.mods.lucraftcore.utilities.container.ContainerExtractor;
import lucraft.mods.lucraftcore.utilities.gui.GuiBoiler;
import lucraft.mods.lucraftcore.utilities.gui.GuiExtractor;
import lucraft.mods.lucraftcore.utilities.jei.boiler.BoilerRecipeCategory;
import lucraft.mods.lucraftcore.utilities.jei.boiler.BoilerRecipeWrapper;
import lucraft.mods.lucraftcore.utilities.jei.crafting.RecolorRecipeWrapper;
import lucraft.mods.lucraftcore.utilities.jei.extractor.ExtractorRecipeCategory;
import lucraft.mods.lucraftcore.utilities.jei.extractor.ExtractorRecipeWrapper;
import lucraft.mods.lucraftcore.utilities.jei.extractor.ExtractorTransferInfo;
import lucraft.mods.lucraftcore.utilities.jei.instruction.InstructionRecipeCategory;
import lucraft.mods.lucraftcore.utilities.jei.instruction.InstructionRecipeWrapper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@JEIPlugin
public class LCJEIPlugin implements IModPlugin {

    public static final ResourceLocation TEXTURE = new ResourceLocation(LucraftCore.MODID, "textures/gui/jei.png");
    public static final String EXTRACTOR = "lucraftcore.extractor";
    public static final String INSTRUCTION = "lucraftcore.instruction";
    public static final String BOILER = "lucraftcore.boiler";

    public static IGuiHelper guiHelper;

    @Override
    public void register(IModRegistry reg) {
        IRecipeTransferRegistry recipeTransferRegistry = reg.getRecipeTransferRegistry();

        // Crafting
        reg.handleRecipes(RecipeFactoryRecolor.RecipeRecolor.class, recipe -> new RecolorRecipeWrapper(reg.getJeiHelpers(), recipe), VanillaRecipeCategoryUid.CRAFTING);

        // Extractor
        reg.addRecipes(ExtractorRecipeWrapper.getRecipes(), EXTRACTOR);
        reg.addRecipeClickArea(GuiExtractor.class, 4, 4, 168, 11, EXTRACTOR);
        reg.addRecipeCatalyst(new ItemStack(UtilitiesBlocks.EXTRACTOR), EXTRACTOR);
        recipeTransferRegistry.addRecipeTransferHandler(ContainerExtractor.class, EXTRACTOR, 0, 1, 6, 36);
        recipeTransferRegistry.addRecipeTransferHandler(new ExtractorTransferInfo());

        // Instruction Recipes
        reg.addRecipes(InstructionRecipeWrapper.getRecipes(), INSTRUCTION);
        reg.addRecipeCatalyst(new ItemStack(UtilitiesBlocks.CONSTRUCTION_TABLE), INSTRUCTION);

        // Boiler
        reg.addRecipes(BoilerRecipeWrapper.getRecipes(reg.getJeiHelpers()), BOILER);
        reg.addRecipeClickArea(GuiBoiler.class, 106, 44, 28, 23, BOILER);
        reg.addRecipeCatalyst(new ItemStack(UtilitiesBlocks.BOILER), BOILER);

        // Information
        for (JEIInfoReader.JEIInfo i : JEIInfoReader.getInfo().values()) {
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack s : i.getItemStacks())
                items.add(s);
            String s = i.text instanceof TextComponentTranslation ? ((TextComponentTranslation) i.text).getKey() : i.text.getFormattedText();
            reg.addIngredientInfo(items, ItemStack.class, s);
        }

        // Anvil
        if (ModuleInfinity.INSTANCE.isEnabled()) {
            Predicate<LCAnvilRecipe> predicate = (recipe) -> {
                if ((Loader.isModLoaded("tconstruct") || Loader.isModLoaded("thermalexpansion") || Loader.isModLoaded("immersiveengineering")) && LCConfig.infinity.disableAnvilWithMods)
                    return false;
                if (!LCConfig.infinity.anvilCrafting)
                    return false;
                return true;
            };
            reg.addRecipes(Arrays.asList(new LCAnvilRecipe(new ItemStack(ModuleInfinity.INFINITY_GAUNTLET_CAST), ItemHelper.getOres("plateGoldTitaniumAlloy", 6), Arrays.asList(new ItemStack(ModuleInfinity.INFINITY_GAUNTLET))).setPredicate(predicate)), VanillaRecipeCategoryUid.ANVIL);
        }
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        LCJEIPlugin.guiHelper = reg.getJeiHelpers().getGuiHelper();
        reg.addRecipeCategories(new ExtractorRecipeCategory(guiHelper));
        reg.addRecipeCategories(new InstructionRecipeCategory(guiHelper));
        reg.addRecipeCategories(new BoilerRecipeCategory(guiHelper));
    }
}
