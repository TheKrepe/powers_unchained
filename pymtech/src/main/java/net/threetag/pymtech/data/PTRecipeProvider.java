package net.threetag.pymtech.data;

import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.block.PTBlocks;
import net.threetag.pymtech.fluid.PTFluids;
import net.threetag.pymtech.item.PTItems;
import net.threetag.threecore.data.ConstructionTableRecipeBuilder;
import net.threetag.threecore.data.FluidComposingRecipeBuilder;
import net.threetag.threecore.data.MultiversalRecipeBuilder;
import net.threetag.threecore.fluid.FluidIngredient;
import net.threetag.threecore.item.TCItems;
import net.threetag.threecore.item.recipe.TCRecipeSerializers;
import net.threetag.threecore.item.recipe.ToolIngredient;
import net.threetag.threecore.tags.TCItemTags;

import java.util.function.Consumer;

public class PTRecipeProvider extends RecipeProvider {

    public PTRecipeProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {

        // Fluid Composer
        new FluidComposingRecipeBuilder(new FluidStack(PTFluids.SHRINK_PYM_PARTICLES, 500)).setInputFluid(new FluidIngredient(new FluidStack(Fluids.WATER, 1000))).addIngredient(TCItemTags.INTERTIUM_DUSTS).addIngredient(TCItemTags.PALLADIUM_DUSTS).addIngredient(Tags.Items.DUSTS_REDSTONE).setEnergy(2500).addCriterion("has_intertium", hasItem(TCItemTags.INTERTIUM_INGOTS)).addCriterion("has_palladium", hasItem(TCItemTags.PALLADIUM_INGOTS)).addCriterion("has_redstone", hasItem(Tags.Items.DUSTS_REDSTONE)).build(consumer);
        new FluidComposingRecipeBuilder(new FluidStack(PTFluids.ENLARGE_PYM_PARTICLES, 500)).setInputFluid(new FluidIngredient(new FluidStack(Fluids.WATER, 1000))).addIngredient(TCItemTags.LEAD_DUSTS).addIngredient(TCItemTags.PALLADIUM_DUSTS).addIngredient(Items.BONE_MEAL).setEnergy(2500).addCriterion("has_lead", hasItem(TCItemTags.LEAD_INGOTS)).addCriterion("has_palladium", hasItem(TCItemTags.PALLADIUM_INGOTS)).addCriterion("has_bones", hasItem(Items.BONE)).build(consumer);

        // Blocks
        ShapedRecipeBuilder.shapedRecipe(PTBlocks.STRUCTURE_SHRINKER.get()).patternLine("POP").patternLine("BCX").patternLine("PRP").key('P', TCItemTags.LEAD_PLATES).key('B', Items.BUCKET).key('R', PTItems.REGULATOR.get()).key('C', PTItems.SHRINKING_SIZE_COIL.get()).key('X', TCItems.CAPACITOR.get()).key('O', Tags.Items.STORAGE_BLOCKS_REDSTONE).addCriterion("has_lead", hasItem(TCItemTags.LEAD_INGOTS)).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);

        // Multiverse
        new MultiversalRecipeBuilder("earth-199999", "ant-man_tier3_helmet", PTItems.Suits.ANT_MAN_TIER3_HELMET).build(consumer, new ResourceLocation(PymTech.MODID, "earth-199999_ant-man_tier3_helmet"));
        new MultiversalRecipeBuilder("earth-199999", "ant-man_tier3_chestplate", PTItems.Suits.ANT_MAN_TIER3_CHESTPLATE).build(consumer, new ResourceLocation(PymTech.MODID, "earth-199999_ant-man_tier3_chestplate"));
        new MultiversalRecipeBuilder("earth-199999", "ant-man_tier3_leggings", PTItems.Suits.ANT_MAN_TIER3_LEGGINGS).build(consumer, new ResourceLocation(PymTech.MODID, "earth-199999_ant-man_tier3_leggings"));
        new MultiversalRecipeBuilder("earth-199999", "ant-man_tier3_boots", PTItems.Suits.ANT_MAN_TIER3_BOOTS).build(consumer, new ResourceLocation(PymTech.MODID, "earth-199999_ant-man_tier3_boots"));

        new MultiversalRecipeBuilder("earth-616", "ant-man_tier3_helmet", PTItems.Suits.GIANT_MAN_HELMET).build(consumer, new ResourceLocation(PymTech.MODID, "earth-616_ant-man_tier3_helmet"));
        new MultiversalRecipeBuilder("earth-616", "ant-man_tier3_chestplate", PTItems.Suits.GIANT_MAN_CHESTPLATE).build(consumer, new ResourceLocation(PymTech.MODID, "earth-616_ant-man_tier3_chestplate"));
        new MultiversalRecipeBuilder("earth-616", "ant-man_tier3_leggings", PTItems.Suits.GIANT_MAN_LEGGINGS).build(consumer, new ResourceLocation(PymTech.MODID, "earth-616_ant-man_tier3_leggings"));
        new MultiversalRecipeBuilder("earth-616", "ant-man_tier3_boots", PTItems.Suits.GIANT_MAN_BOOTS).build(consumer, new ResourceLocation(PymTech.MODID, "earth-616_ant-man_tier3_boots"));

        new MultiversalRecipeBuilder("earth-2149", "ant-man_tier3_helmet", PTItems.Suits.ZOMBIE_GIANT_MAN_HELMET).build(consumer, new ResourceLocation(PymTech.MODID, "earth-2149_ant-man_tier3_helmet"));
        new MultiversalRecipeBuilder("earth-2149", "ant-man_tier3_chestplate", PTItems.Suits.ZOMBIE_GIANT_MAN_CHESTPLATE).build(consumer, new ResourceLocation(PymTech.MODID, "earth-2149_ant-man_tier3_chestplate"));
        new MultiversalRecipeBuilder("earth-2149", "ant-man_tier3_leggings", PTItems.Suits.ZOMBIE_GIANT_MAN_LEGGINGS).build(consumer, new ResourceLocation(PymTech.MODID, "earth-2149_ant-man_tier3_leggings"));
        new MultiversalRecipeBuilder("earth-2149", "ant-man_tier3_boots", PTItems.Suits.ZOMBIE_GIANT_MAN_BOOTS).build(consumer, new ResourceLocation(PymTech.MODID, "earth-2149_ant-man_tier3_boots"));

        // Suits
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.HELMET_CRAFTING.get(), PTItems.Suits.ANT_MAN_HELMET).setToolIngredient(new ToolIngredient(ToolIngredient.ToolType.HAMMER)).patternLine("AXXA").patternLine("GPPG").patternLine("PPPP").patternLine("IFFI").key('X', TCItemTags.INTERTIUM_INGOTS).key('A', PTItems.ANT_ANTENNA.get()).key('P', TCItemTags.IRON_PLATES).key('G', Tags.Items.GLASS_PANES_RED).key('F', PTItems.AIR_FILTER.get()).key('I', Tags.Items.INGOTS_IRON).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.CHESTPLATE_CRAFTING.get(), PTItems.Suits.ANT_MAN_CHESTPLATE).setToolIngredient(new ToolIngredient(ToolIngredient.ToolType.HAMMER)).patternLine("RRIRR").patternLine("BIRIB").patternLine("BRRRB").patternLine("IXI").key('R', TCItemTags.RED_FABRIC).key('I', Tags.Items.INGOTS_IRON).key('B', TCItemTags.BLACK_FABRIC).key('X', PTItems.REGULATOR.get()).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.LEGGINGS_CRAFTING.get(), PTItems.Suits.ANT_MAN_LEGGINGS).setToolIngredient(new ToolIngredient(ToolIngredient.ToolType.HAMMER)).patternLine("RIR").patternLine("RR").patternLine("BB").patternLine("BB").key('I', Tags.Items.INGOTS_IRON).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.BOOTS_CRAFTING.get(), PTItems.Suits.ANT_MAN_BOOTS).setToolIngredient(new ToolIngredient(ToolIngredient.ToolType.HAMMER)).patternLine("RR").patternLine("II").patternLine("BBBB").key('I', Tags.Items.INGOTS_IRON).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);

        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.HELMET_CRAFTING.get(), PTItems.Suits.ANT_MAN_TIER2_HELMET).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_HELMET)).patternLine("APPA").patternLine("GPPG").patternLine("PPPP").patternLine("IFFI").key('A', PTItems.ANT_ANTENNA.get()).key('P', TCItemTags.SILVER_PLATES).key('G', Tags.Items.GLASS_PANES_RED).key('F', PTItems.AIR_FILTER.get()).key('I', TCItemTags.SILVER_INGOTS).addCriterion("has_silver", hasItem(TCItemTags.SILVER_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.CHESTPLATE_CRAFTING.get(), PTItems.Suits.ANT_MAN_TIER2_CHESTPLATE).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_CHESTPLATE)).patternLine("RRIRR").patternLine("BIRIB").patternLine("BRRRB").patternLine("IXI").key('R', TCItemTags.RED_FABRIC).key('I', TCItemTags.SILVER_INGOTS).key('B', TCItemTags.BLACK_FABRIC).key('X', PTItems.REGULATOR.get()).addCriterion("has_silver", hasItem(TCItemTags.SILVER_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.LEGGINGS_CRAFTING.get(), PTItems.Suits.ANT_MAN_TIER2_LEGGINGS).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_LEGGINGS)).patternLine("RIR").patternLine("RR").patternLine("BB").patternLine("BB").key('I', TCItemTags.SILVER_INGOTS).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_silver", hasItem(TCItemTags.SILVER_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.BOOTS_CRAFTING.get(), PTItems.Suits.ANT_MAN_TIER2_BOOTS).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_BOOTS)).patternLine("RR").patternLine("II").patternLine("BBBB").key('I', TCItemTags.SILVER_INGOTS).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_silver", hasItem(TCItemTags.SILVER_INGOTS)).build(consumer);

        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.HELMET_CRAFTING.get(), PTItems.Suits.ANT_MAN_TIER3_HELMET).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_TIER2_HELMET)).patternLine("AXXA").patternLine("GPPG").patternLine("PPPP").patternLine("IFFI").key('X', TCItemTags.INTERTIUM_INGOTS).key('A', PTItems.ANT_ANTENNA.get()).key('P', TCItemTags.STEEL_PLATES).key('G', Tags.Items.GLASS_PANES_RED).key('F', PTItems.AIR_FILTER.get()).key('I', TCItemTags.STEEL_INGOTS).addCriterion("has_steel", hasItem(TCItemTags.STEEL_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.CHESTPLATE_CRAFTING.get(), PTItems.Suits.ANT_MAN_TIER3_CHESTPLATE).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_TIER2_CHESTPLATE)).patternLine("RRIRR").patternLine("BIRIB").patternLine("BRRRB").patternLine("IXI").key('R', TCItemTags.RED_FABRIC).key('I', TCItemTags.STEEL_INGOTS).key('B', TCItemTags.BLACK_FABRIC).key('X', PTItems.REGULATOR.get()).addCriterion("has_steel", hasItem(TCItemTags.STEEL_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.LEGGINGS_CRAFTING.get(), PTItems.Suits.ANT_MAN_TIER3_LEGGINGS).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_TIER2_LEGGINGS)).patternLine("RIR").patternLine("RR").patternLine("BB").patternLine("BB").key('I', TCItemTags.STEEL_INGOTS).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_steel", hasItem(TCItemTags.STEEL_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.BOOTS_CRAFTING.get(), PTItems.Suits.ANT_MAN_TIER3_BOOTS).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_TIER2_BOOTS)).patternLine("RR").patternLine("II").patternLine("BBBB").key('I', TCItemTags.STEEL_INGOTS).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_steel", hasItem(TCItemTags.STEEL_INGOTS)).build(consumer);

        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.HELMET_CRAFTING.get(), PTItems.Suits.WASP_HELMET).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_HELMET)).patternLine("AXXA").patternLine("GGGG").patternLine("PPPP").patternLine("IFFI").key('X', TCItemTags.INTERTIUM_INGOTS).key('A', PTItems.ANT_ANTENNA.get()).key('P', TCItemTags.IRON_PLATES).key('G', Tags.Items.GLASS_PANES_RED).key('F', PTItems.AIR_FILTER.get()).key('I', Tags.Items.INGOTS_IRON).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.CHESTPLATE_CRAFTING.get(), PTItems.Suits.WASP_CHESTPLATE).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_CHESTPLATE)).patternLine("WRBRW").patternLine("BRBRB").patternLine("RWRWR").patternLine("IXI").key('W', PTItems.FIBER_COMPOSITE_WING.get()).key('R', TCItemTags.RED_FABRIC).key('I', Tags.Items.INGOTS_IRON).key('B', TCItemTags.BLACK_FABRIC).key('X', PTItems.REGULATOR.get()).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.LEGGINGS_CRAFTING.get(), PTItems.Suits.WASP_LEGGINGS).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_LEGGINGS)).patternLine("IBI").patternLine("RR").patternLine("BB").patternLine("RR").key('I', Tags.Items.INGOTS_IRON).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.BOOTS_CRAFTING.get(), PTItems.Suits.WASP_BOOTS).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.ANT_MAN_BOOTS)).patternLine("RR").patternLine("II").patternLine("RBBR").key('I', Tags.Items.INGOTS_IRON).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);

        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.HELMET_CRAFTING.get(), PTItems.Suits.WASP_TIER2_HELMET).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.WASP_HELMET)).patternLine("APPA").patternLine("GGGG").patternLine("PPPP").patternLine("IFFI").key('A', PTItems.ANT_ANTENNA.get()).key('P', TCItemTags.STEEL_PLATES).key('G', Tags.Items.GLASS_PANES_YELLOW).key('F', PTItems.AIR_FILTER.get()).key('I', TCItemTags.STEEL_INGOTS).addCriterion("has_steel", hasItem(TCItemTags.STEEL_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.CHESTPLATE_CRAFTING.get(), PTItems.Suits.WASP_TIER2_CHESTPLATE).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.WASP_CHESTPLATE)).patternLine("WRBRW").patternLine("IYYYI").patternLine("BWYWB").patternLine("IXI").key('Y', TCItemTags.YELLOW_FABRIC).key('W', PTItems.FIBER_COMPOSITE_WING.get()).key('R', TCItemTags.RED_FABRIC).key('I', TCItemTags.STEEL_INGOTS).key('B', TCItemTags.BLACK_FABRIC).key('X', PTItems.REGULATOR.get()).addCriterion("has_steel", hasItem(TCItemTags.STEEL_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.LEGGINGS_CRAFTING.get(), PTItems.Suits.WASP_TIER2_LEGGINGS).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.WASP_LEGGINGS)).patternLine("YIY").patternLine("YY").patternLine("BB").patternLine("BB").key('I', TCItemTags.STEEL_INGOTS).key('Y', TCItemTags.YELLOW_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_steel", hasItem(TCItemTags.STEEL_INGOTS)).build(consumer);
        ConstructionTableRecipeBuilder.recipe(TCRecipeSerializers.BOOTS_CRAFTING.get(), PTItems.Suits.WASP_TIER2_BOOTS).enableToolConsuming().setToolIngredient(Ingredient.fromItems(PTItems.Suits.WASP_BOOTS)).patternLine("BB").patternLine("II").patternLine("BRRB").key('I', TCItemTags.STEEL_INGOTS).key('R', TCItemTags.RED_FABRIC).key('B', TCItemTags.BLACK_FABRIC).addCriterion("has_steel", hasItem(TCItemTags.STEEL_INGOTS)).build(consumer);

        // Basic Items
        ShapedRecipeBuilder.shapedRecipe(PTItems.SHRINK_DISK.get(), 8).patternLine(" I ").patternLine("IBI").patternLine(" I ").key('I', Tags.Items.INGOTS_IRON).key('B', PTItems.SHRINK_PYM_PARTICLES_BUCKET.get()).addCriterion("has_shrink_pym_particles", hasItem(PTItems.SHRINK_PYM_PARTICLES_BUCKET.get())).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.ENLARGE_DISK.get(), 8).patternLine(" I ").patternLine("IBI").patternLine(" I ").key('I', Tags.Items.INGOTS_IRON).key('B', PTItems.ENLARGE_PYM_PARTICLES_BUCKET.get()).addCriterion("has_shrink_pym_particles", hasItem(PTItems.ENLARGE_PYM_PARTICLES_BUCKET.get())).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.ANT_ANTENNA.get()).patternLine("  I").patternLine(" I ").patternLine("P  ").key('P', PTItems.EMP_COMMUNICATION_DEVICE.get()).key('I', Tags.Items.INGOTS_IRON).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.EMP_COMMUNICATION_DEVICE.get()).patternLine("PP ").patternLine("PRG").patternLine(" II").key('P', TCItemTags.IRON_PLATES).key('R', Tags.Items.DUSTS_REDSTONE).key('G', Tags.Items.DUSTS_GLOWSTONE).key('I', Tags.Items.INGOTS_IRON).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).addCriterion("has_redstone", hasItem(Tags.Items.DUSTS_REDSTONE)).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.AIR_FILTER.get()).patternLine("IBI").patternLine("BRB").patternLine("IBI").key('I', TCItemTags.IRON_PLATES).key('R', TCItemTags.SILVER_PLATES).key('B', Blocks.IRON_BARS).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).addCriterion("has_silver", hasItem(TCItemTags.SILVER_INGOTS)).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.SHRINKING_SIZE_COIL.get()).patternLine("WIW").patternLine("PXP").patternLine("WRW").key('W', TCItemTags.COPPER_NUGGETS).key('I', TCItemTags.INTERTIUM_INGOTS).key('P', TCItemTags.IRON_PLATES).key('R', Tags.Items.DUSTS_REDSTONE).key('X', TCItemTags.PALLADIUM_INGOTS).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).addCriterion("has_redstone", hasItem(Tags.Items.DUSTS_REDSTONE)).addCriterion("has_copper", hasItem(TCItemTags.COPPER_INGOTS)).addCriterion("has_palladium", hasItem(TCItemTags.PALLADIUM_INGOTS)).addCriterion("has_intertium", hasItem(TCItemTags.INTERTIUM_INGOTS)).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.ENLARGEMENT_SIZE_COIL.get()).patternLine("WIW").patternLine("PXP").patternLine("WRW").key('W', TCItemTags.COPPER_NUGGETS).key('I', TCItemTags.LEAD_INGOTS).key('P', TCItemTags.IRON_PLATES).key('R', Items.BONE_MEAL).key('X', TCItemTags.PALLADIUM_INGOTS).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).addCriterion("has_bones", hasItem(Items.BONE)).addCriterion("has_copper", hasItem(TCItemTags.COPPER_INGOTS)).addCriterion("has_palladium", hasItem(TCItemTags.PALLADIUM_INGOTS)).addCriterion("has_lead", hasItem(TCItemTags.LEAD_INGOTS)).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.FIBER_COMPOSITE_WING.get()).patternLine("IDS").patternLine("ISD").patternLine("IDS").key('I', Tags.Items.NUGGETS_IRON).key('S', Items.STRING).key('D', TCItemTags.CHARCOAL_DUSTS).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.REGULATOR.get()).patternLine("NPN").patternLine("SCG").patternLine("NPN").key('P', TCItemTags.IRON_PLATES).key('S', PTItems.SHRINKING_SIZE_COIL.get()).key('C', TCItems.ADVANCED_CIRCUIT.get()).key('G', PTItems.ENLARGEMENT_SIZE_COIL.get()).key('N', Tags.Items.NUGGETS_IRON).addCriterion("has_iron", hasItem(Tags.Items.INGOTS_IRON)).build(consumer);
        ShapedRecipeBuilder.shapedRecipe(PTItems.STRUCTURE_SHRINKER_REMOTE.get()).patternLine("AI").patternLine("CI").patternLine("II").key('A', PTItems.ANT_ANTENNA.get()).key('C', TCItems.CIRCUIT.get()).key('I', TCItemTags.IRON_PLATES).addCriterion("has_iron", hasItem(TCItemTags.IRON_PLATES)).build(consumer);
    }

    @Override
    public String getName() {
        return "PymTech Recipes";
    }
}
