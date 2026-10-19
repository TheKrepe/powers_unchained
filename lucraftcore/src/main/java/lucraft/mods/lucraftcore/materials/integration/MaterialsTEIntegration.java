package lucraft.mods.lucraftcore.materials.integration;

import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.util.helper.mods.ThermalExpansionHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class MaterialsTEIntegration {

    public static void postInit() {
        addCrucibleRecipe(Material.IRON);
        addCrucibleRecipe(Material.GOLD);
        addCrucibleRecipe(Material.COPPER);
        addCrucibleRecipe(Material.TIN);
        addCrucibleRecipe(Material.NICKEL);
        addCrucibleRecipe(Material.LEAD);
        addCrucibleRecipe(Material.SILVER);
        addCrucibleRecipe(Material.PALLADIUM);
        addCrucibleRecipe(Material.TITANIUM);
        addCrucibleRecipe(Material.VIBRANIUM);
        addCrucibleRecipe(Material.BRONZE);
        addCrucibleRecipe(Material.OSMIUM);
        addCrucibleRecipe(Material.DWARF_STAR_ALLOY);
        addCrucibleRecipe(Material.GOLD_TITANIUM_ALLOY);
        addCrucibleRecipe(Material.INTERTIUM);
        addCrucibleRecipe(Material.URANIUM);
        addCrucibleRecipe(Material.STEEL);
        addCrucibleRecipe(Material.ADAMANTIUM);
        addCrucibleRecipe(Material.URU);
    }

    public static void addCrucibleRecipe(Material material) {
        if (FluidRegistry.isFluidRegistered(material.getIdentifier().toLowerCase())) {
            Fluid fluid = FluidRegistry.getFluid(material.getIdentifier().toLowerCase());
            ThermalExpansionHelper.addCrucibleRecipe(8000, material.getItemStack(Material.MaterialComponent.ORE), new FluidStack(fluid, 288));
            ThermalExpansionHelper.addCrucibleRecipe(500, material.getItemStack(Material.MaterialComponent.NUGGET), new FluidStack(fluid, 16));
            ThermalExpansionHelper.addCrucibleRecipe(32000, material.getItemStack(Material.MaterialComponent.BLOCK), new FluidStack(fluid, 1296));
            ThermalExpansionHelper.addCrucibleRecipe(4000, material.getItemStack(Material.MaterialComponent.PLATE), new FluidStack(fluid, 144));
            ThermalExpansionHelper.addCrucibleRecipe(2000, material.getItemStack(Material.MaterialComponent.DUST), new FluidStack(fluid, 144));
        }
    }

}
