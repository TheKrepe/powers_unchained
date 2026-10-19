package lucraft.mods.lucraftcore.materials.integration;

import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.tconstruct.library.MaterialIntegration;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.ITrait;

import java.util.ArrayList;
import java.util.List;

public class MaterialsTiConIntegration {

    public static final ITrait TRAIT_SUPER_HEAVY = new TraitSuperHeavy("super_heavy", TextFormatting.GRAY);
    public static final List<LCMaterial> MATERIALS = new ArrayList<>();

    public static void preInit() {
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.NICKEL);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.PALLADIUM);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.TITANIUM);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.VIBRANIUM);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.OSMIUM);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.IRIDIUM);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.DWARF_STAR_ALLOY);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.GOLD_TITANIUM_ALLOY);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.INTERTIUM);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.ADAMANTIUM);
        registerMaterial(lucraft.mods.lucraftcore.materials.Material.URU);

        TinkerRegistry.addTrait(TRAIT_SUPER_HEAVY);
    }

    public static void postInit() {
        for (LCMaterial m : MATERIALS) {
            for (ITrait traits : m.material.getTiConTraits())
                if (!m.getAllTraits().contains(traits))
                    m.addTrait(traits);
        }

        registerAlloy("adamantium*432", "steel*288", "vibranium*144");
        registerAlloy("intertium*432", "iron*288", "osmium*144");
        registerAlloy("goldtitaniumalloy*432", "titanium*288", "gold*144");
    }

    public static void registerMaterial(lucraft.mods.lucraftcore.materials.Material lcMaterial) {
        LCMaterial m = new LCMaterial(lcMaterial);
        m.addItemIngot(lcMaterial.getOreDictionaryName(lucraft.mods.lucraftcore.materials.Material.MaterialComponent.INGOT));
        m.setRepresentativeItem(lcMaterial.getItemStack(lucraft.mods.lucraftcore.materials.Material.MaterialComponent.INGOT));
        m.setCastable(true);
        MATERIALS.add(m);
        LCMaterialIntegration integration = new LCMaterialIntegration(m, FluidRegistry.getFluid(m.getIdentifier().toLowerCase()), lcMaterial.getOreDictionaryName(lucraft.mods.lucraftcore.materials.Material.MaterialComponent.INGOT).replace("ingot", ""));
        TinkerRegistry.integrate(integration).toolforge();

        for (IMaterialStats stats : lcMaterial.getTiConMaterialStats())
            TinkerRegistry.addMaterialStats(m, stats);
    }

    public static void registerAlloy(String out, String... in) {
        FluidStack sOut = getFluidLoaded(out);
        List<FluidStack> sIns = new ArrayList<FluidStack>();
        for (String s : in) {
            sIns.add(getFluidLoaded(s));
        }
        if (sOut != null && !sIns.contains(null))
            TinkerRegistry.registerAlloy(sOut, sIns.toArray(new FluidStack[sIns.size()]));
    }

    public static FluidStack getFluidLoaded(String fluid) {
        String[] result = fluid.split("\\*");
        if (result.length == 2 && FluidRegistry.getFluid(result[0]) != null) {
            return new FluidStack(FluidRegistry.getFluid(result[0]), Integer.parseInt(result[1]));
        }
        return null;
    }

    public static class LCMaterial extends Material {

        public lucraft.mods.lucraftcore.materials.Material material;

        public LCMaterial(lucraft.mods.lucraftcore.materials.Material material) {
            super(material.getIdentifier().toLowerCase(), material.getColor());
            this.material = material;

            if (FMLCommonHandler.instance().getSide().isClient())
                this.setRenderInfo(material.getTiConMaterialRenderInfo());
        }

        @Override
        public String getLocalizedName() {
            return StringHelper.translateToLocal(material.getUnlocalizedName());
        }

        @Override
        public String getTextColor() {
            return super.getTextColor();
        }

    }

    public static class LCMaterialIntegration extends MaterialIntegration {

        public boolean preInit = false;

        public LCMaterialIntegration(Material material, Fluid fluid, String oreSuffix) {
            super(material, fluid, oreSuffix);
        }

        @Override
        public void registerFluidModel() {

        }

        @Override
        public void registerFluidBlock(IForgeRegistry<Block> registry) {

        }

        @Override
        public void integrate() {
            if (fluid == null && this.material instanceof LCMaterial) {
                this.fluid = FluidRegistry.getFluid(((LCMaterial) this.material).material.getIdentifier().toLowerCase());
                this.material.setFluid(this.fluid);
            }
            super.integrate();
        }
    }

}
