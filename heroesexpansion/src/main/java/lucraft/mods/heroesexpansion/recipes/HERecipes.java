package lucraft.mods.heroesexpansion.recipes;

import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.blocks.HEBlocks;
import lucraft.mods.heroesexpansion.fluids.HEFluids;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.suitsets.HESuitSet;
import lucraft.mods.heroesexpansion.util.items.IColorableItem;
import lucraft.mods.lucraftcore.materials.MaterialsRecipes;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.util.helper.mods.ThermalExpansionHelper;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HERecipes {

    @SubscribeEvent
    public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> e) {
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.CAPE).setRegistryName(HeroesExpansion.MODID, "cape_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.QUIVER).setRegistryName(HeroesExpansion.MODID, "quiver_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.SHARPENED_ARROW).setRegistryName(HeroesExpansion.MODID, "sharpened_arrow_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.EXPLOSIVE_ARROW).setRegistryName(HeroesExpansion.MODID, "explosive_arrow_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.SMOKE_ARROW).setRegistryName(HeroesExpansion.MODID, "smoke_arrow_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.GAS_ARROW).setRegistryName(HeroesExpansion.MODID, "gas_arrow_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.GRAPPLING_HOOK_ARROW).setRegistryName(HeroesExpansion.MODID, "grappling_hook_arrow_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.KRYPTONITE_ARROW).setRegistryName(HeroesExpansion.MODID, "kryptonite_arrow_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.VIBRANIUM_ARROW).setRegistryName(HeroesExpansion.MODID, "vibranium_arrow_dyes"));
        e.getRegistry().register(new RecipesColorableItem((IColorableItem) HEItems.BILLY_CLUB_SEPARATE).setRegistryName(HeroesExpansion.MODID, "billy_club_dyes"));
        e.getRegistry().register(new RecipeCaptainAmericaShield().setRegistryName(HeroesExpansion.MODID, "captain_america_shield"));
        e.getRegistry().register(new RecipeColorCaptainAmericaShield().setRegistryName(HeroesExpansion.MODID, "color_captain_america_shield"));
        e.getRegistry().register(new RecipeRemoveShieldColor().setRegistryName(HeroesExpansion.MODID, "remove_shield_color"));

        generateJsonRecipes();

        ThermalExpansionHelper.addCrucibleRecipe(2000, new ItemStack(HEItems.MJOLNIR_HEAD), new FluidStack(FluidRegistry.getFluid("uru"), 144 * 6));
        ThermalExpansionHelper.addCrucibleRecipe(2500, new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD), new FluidStack(FluidRegistry.getFluid("uru"), 144 * 9));
        ThermalExpansionHelper.addCrucibleRecipe(2500, new ItemStack(HEItems.STORMBREAKER_HEAD), new FluidStack(FluidRegistry.getFluid("uru"), 144 * 9));

        if (HEConfig.MOD_CRAFTING && Loader.isModLoaded("thermalexpansion")) {
            ThermalExpansionHelper.addTransposerFill(2000, new ItemStack(HEItems.MJOLNIR_HEAD_CAST), new ItemStack(HEItems.MJOLNIR_HEAD), new FluidStack(FluidRegistry.getFluid("uru"), 144 * 6), false);
            ThermalExpansionHelper.addTransposerFill(2000, new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD_CAST), new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD), new FluidStack(FluidRegistry.getFluid("uru"), 144 * 9), false);
            ThermalExpansionHelper.addTransposerFill(2000, new ItemStack(HEItems.STORMBREAKER_HEAD_CAST), new ItemStack(HEItems.STORMBREAKER_HEAD), new FluidStack(FluidRegistry.getFluid("uru"), 144 * 9), false);
        }
    }

    @SubscribeEvent
    public static void anvilUpdateEvent(AnvilUpdateEvent e) {
        if ((Loader.isModLoaded("tconstruct") || Loader.isModLoaded("thermalexpansion")) && HEConfig.DISABLE_ANVIL_WITH_MODS)
            return;

        if (HEConfig.ANVIL_CRAFTING && ItemHelper.getOreDictionaryEntries(e.getRight()).contains("ingotUru")) {
            if (e.getLeft().getItem() == HEItems.MJOLNIR_HEAD_CAST && e.getRight().getCount() == 6) {
                e.setOutput(new ItemStack(HEItems.MJOLNIR_HEAD));
                e.setCost(20);
            } else if (e.getLeft().getItem() == HEItems.ULTIMATE_MJOLNIR_HEAD_CAST && e.getRight().getCount() == 9) {
                e.setOutput(new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD));
                e.setCost(20);
            } else if (e.getLeft().getItem() == HEItems.STORMBREAKER_HEAD_CAST && e.getRight().getCount() == 9) {
                e.setOutput(new ItemStack(HEItems.STORMBREAKER_HEAD));
                e.setCost(20);
            }
        }
    }

    public static void generateJsonRecipes() {
        boolean generateRecipes = false;

        if (generateRecipes) {
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.CAPE), "XSX", "XXX", "XXX", 'X', Items.LEATHER, 'S', Items.STRING);
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.SUPPORTER_CLOAK), "XX", "XX", "XX", 'X', new ItemStack(Blocks.WOOL, 1, 1));
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.QUIVER), "XSX", "XAX", " X ", 'X', Items.LEATHER, 'S', Items.STRING, 'A', Items.ARROW);
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.COMPOUND_BOW), "NIS", "IBF", "SF ", 'N', "nuggetSteel", 'I', "ingotIron", 'S', "ingotSteel", 'B', Items.BOW, 'F', Items.STRING);
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.SHARPENED_ARROW, 2), Items.ARROW, Items.ARROW, "dyeGreen", "nuggetIron");
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.EXPLOSIVE_ARROW), HEItems.SHARPENED_ARROW, Blocks.TNT);
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.SMOKE_ARROW), HEItems.SHARPENED_ARROW, Items.COAL);
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.GAS_ARROW), HEItems.SHARPENED_ARROW, Items.COAL, Items.GUNPOWDER);
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.GAS_ARROW), HEItems.SMOKE_ARROW, Items.GUNPOWDER);
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.GRAPPLING_HOOK_ARROW), HEItems.SHARPENED_ARROW, Blocks.SLIME_BLOCK, Items.LEAD);
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.KRYPTONITE_ARROW), HEItems.SHARPENED_ARROW, HEItems.KRYPTONITE);
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.VIBRANIUM_ARROW), HEItems.SHARPENED_ARROW, "nuggetVibranium", "nuggetVibranium");
            MaterialsRecipes.addShapelessRecipe(new ItemStack(HEItems.HEART_SHAPED_HERB_POTION), HEBlocks.HEART_SHAPED_HERB, Items.BOWL);
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEBlocks.KRYPTONITE_BLOCK), "XX", "XX", 'X', HEItems.KRYPTONITE);
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.VIBRANIUM_SHIELD), "XXX", "XIX", "XXX", 'X', "plateVibranium", 'I', "ingotVibranium");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEBlocks.PORTAL_DEVICE), "CIC", "IBI", "WIE", 'C', "circuitAdvanced", 'I', "plateIridium", 'B', UtilitiesItems.HV_CAPACITOR, 'W', "wireCopper", 'E', "ingotIron");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.MJOLNIR), "H", "S", 'H', HEItems.MJOLNIR_HEAD, 'S', "stickWood");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.ULTIMATE_MJOLNIR), "H", "S", 'H', HEItems.ULTIMATE_MJOLNIR_HEAD, 'S', "stickWood");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.STORMBREAKER), "H", "S", 'H', HEItems.STORMBREAKER_HEAD, 'S', "stickWood");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.CHITAURI_GUN), "RM ", "MCM", " MM", 'R', "dustRedstone", 'M', HEItems.CHITAURI_METAL, 'C', HEItems.CHITAURI_ENERGY_CORE);
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.WEB_SHOOTER_1), "PDP", "BWB", "PCP", 'P', "plateIron", 'D', Blocks.DISPENSER, 'B', Items.BUCKET, 'W', "wireCopper", 'C', "circuitBasic");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.WEB_SHOOTER_2), "PDP", "BWB", "PCP", 'P', "plateSteel", 'D', Blocks.DISPENSER, 'B', Items.BUCKET, 'W', "wireCopper", 'C', "circuitAdvanced");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.BILLY_CLUB_SEPARATE), " P ", "RSR", " P ", 'P', "plateIron", 'S', "stickWood", 'R', "dyeRed");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HEItems.WAKANDAN_SHIELD), " I ", "VPV", "VVV", 'I', "ingotVibranium", 'V', "plateVibranium", 'P', Blocks.PISTON);

            // Super Soldier Serum
            ItemStack stack = new ItemStack(UtilitiesItems.INJECTION);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Injection", "heroesexpansion:super_soldier");
            stack.setTagCompound(nbt);
            MaterialsRecipes.addShapelessRecipe(stack, FluidUtil.getFilledBucket(new FluidStack(HEFluids.SUPER_SOLDIER_SERUM, 1000)), UtilitiesItems.INJECTION);

            // Falcon
            MaterialsRecipes.addShapedRecipe(new ItemStack(HESuitSet.FALCON.getHelmet()), new ResourceLocation(HeroesExpansion.MODID, "falcon_suit"), "NIN", "GNG", 'N', "nuggetIron", 'I', "ingotIron", 'G', "paneGlassRed");
            MaterialsRecipes.addShapedRecipe(new ItemStack(HESuitSet.FALCON.getChestplate()), new ResourceLocation(HeroesExpansion.MODID, "falcon_suit"), "PCP", "MEM", "IBI", 'P', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.RED), 'C', "circuitAdvanced", 'M', UtilitiesItems.SERVO_MOTOR, 'E', Items.ELYTRA, 'I', "plateIron", 'B', UtilitiesItems.HV_CAPACITOR);
            MaterialsRecipes.addShapedRecipe(new ItemStack(HESuitSet.FALCON.getLegs()), new ResourceLocation(HeroesExpansion.MODID, "falcon_suit"), "PRP", "B B", "N N", 'N', "nuggetIron", 'P', "plateIron", 'R', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.RED), 'B', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.BLACK));
            MaterialsRecipes.addShapedRecipe(new ItemStack(HESuitSet.FALCON.getBoots()), new ResourceLocation(HeroesExpansion.MODID, "falcon_suit"), "B B", "R R", 'B', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.BLACK), 'R', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.RED));

            // Daredevil 1
            MaterialsRecipes.addShapedRecipe(new ItemStack(HESuitSet.DAREDEVIL_HOMEMADE.getHelmet()), new ResourceLocation(HeroesExpansion.MODID, "daredevil_1_suit"), "BLB", "L L", 'L', Items.LEATHER, 'B', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.BLACK));
            MaterialsRecipes.addShapedRecipe(new ItemStack(HESuitSet.DAREDEVIL_HOMEMADE.getChestplate()), new ResourceLocation(HeroesExpansion.MODID, "daredevil_1_suit"), "B B", "LBL", "BLB", 'L', Items.LEATHER, 'B', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.BLACK));
            MaterialsRecipes.addShapedRecipe(new ItemStack(HESuitSet.DAREDEVIL_HOMEMADE.getLegs()), new ResourceLocation(HeroesExpansion.MODID, "daredevil_1_suit"), "BLB", "L L", "B B", 'L', Items.LEATHER, 'B', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.BLACK));
            MaterialsRecipes.addShapedRecipe(new ItemStack(HESuitSet.DAREDEVIL_HOMEMADE.getBoots()), new ResourceLocation(HeroesExpansion.MODID, "daredevil_1_suit"), "L L", "B B", 'L', Items.LEATHER, 'B', UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.BLACK));

            MaterialsRecipes.generateConstants();
        }
    }

}
