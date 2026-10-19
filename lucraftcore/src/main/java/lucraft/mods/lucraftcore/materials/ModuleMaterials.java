package lucraft.mods.lucraftcore.materials;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.materials.blocks.MaterialsBlocks;
import lucraft.mods.lucraftcore.materials.fluids.MaterialsFluids;
import lucraft.mods.lucraftcore.materials.integration.MaterialsCaBIntegration;
import lucraft.mods.lucraftcore.materials.integration.MaterialsIEIntegration;
import lucraft.mods.lucraftcore.materials.integration.MaterialsTEIntegration;
import lucraft.mods.lucraftcore.materials.integration.MaterialsTiConIntegration;
import lucraft.mods.lucraftcore.materials.items.MaterialsItems;
import lucraft.mods.lucraftcore.materials.potions.MaterialsPotions;
import lucraft.mods.lucraftcore.materials.worldgen.MaterialsWorldGenerator;
import lucraft.mods.lucraftcore.module.Module;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public class ModuleMaterials extends Module {

    public static final ModuleMaterials INSTANCE = new ModuleMaterials();

    public static CreativeTabs TAB_MATERIALS = new CreativeTabMaterials("tabMaterials");
    public MaterialsItems ITEMS = new MaterialsItems();
    public MaterialsBlocks BLOCKS = new MaterialsBlocks();
    public MaterialsPotions POTIONS = new MaterialsPotions();
    public MaterialsFluids FLUIDS = new MaterialsFluids();

    public static List<IBlockState> chiselsAndBitsBlocks = new ArrayList<>();

    @SubscribeEvent
    public static void onRegistry(RegistryEvent.NewRegistry e) {
        // Mod Integration
        if (Loader.isModLoaded("tconstruct"))
            MaterialsTiConIntegration.preInit();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(ITEMS);
        MinecraftForge.EVENT_BUS.register(BLOCKS);
        MinecraftForge.EVENT_BUS.register(POTIONS);
        MinecraftForge.EVENT_BUS.register(FLUIDS);
        MinecraftForge.EVENT_BUS.register(new MaterialsRecipes());

        GameRegistry.registerWorldGenerator(new MaterialsWorldGenerator(), 0);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Mod Integration
        if (Loader.isModLoaded("tconstruct"))
            MaterialsTiConIntegration.postInit();
        if (Loader.isModLoaded("thermalexpansion"))
            MaterialsTEIntegration.postInit();
        if (Loader.isModLoaded("immersiveengineering"))
            MaterialsIEIntegration.postInit();
        if (Loader.isModLoaded("chiselsandbits"))
            MaterialsCaBIntegration.postInit();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        MaterialsRecipes.postInit();
    }

    @Override
    public String getName() {
        return "Materials";
    }

    @Override
    public boolean isEnabled() {
        return LCConfig.modules.materials;
    }

}
