package lucraft.mods.lucraftcore.infinity;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.blocks.BlockInfinityGenerator;
import lucraft.mods.lucraftcore.infinity.blocks.TileEntityInfinityGenerator;
import lucraft.mods.lucraftcore.infinity.gui.GuiHandlerEntryInfinityGauntlet;
import lucraft.mods.lucraftcore.infinity.gui.GuiHandlerEntryInfinityGenerator;
import lucraft.mods.lucraftcore.infinity.items.ItemInfinityGauntlet;
import lucraft.mods.lucraftcore.infinity.items.ItemLCCast;
import lucraft.mods.lucraftcore.infinity.render.ItemModelInfinityGauntlet;
import lucraft.mods.lucraftcore.infinity.render.ItemRendererInfinityGauntlet;
import lucraft.mods.lucraftcore.infinity.render.RenderEntityInfinityStone;
import lucraft.mods.lucraftcore.infinity.render.TESRInfinityGenerator;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.util.gui.LCGuiHandler;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.util.helper.mods.ThermalExpansionHelper;
import lucraft.mods.lucraftcore.utilities.jei.JEIInfoReader;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import slimeknights.tconstruct.library.TinkerRegistry;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public class ModuleInfinity extends Module {

    public static final ModuleInfinity INSTANCE = new ModuleInfinity();

    public static CreativeTabs TAB = new CreativeTabs("tabInfinity") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(INFINITY_GAUNTLET);
        }
    };

    public static Item INFINITY_GAUNTLET;
    public static Item INFINITY_GAUNTLET_CAST;
    public static Block INFINITY_GENERATOR;

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> e) {
        if (!INSTANCE.isEnabled())
            return;

        e.getRegistry().register(INFINITY_GENERATOR = new BlockInfinityGenerator("infinity_generator"));
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e) {
        if (!INSTANCE.isEnabled())
            return;

        e.getRegistry().register(INFINITY_GAUNTLET = new ItemInfinityGauntlet("infinity_gauntlet"));
        e.getRegistry().register(INFINITY_GAUNTLET_CAST = new ItemLCCast("infinity_gauntlet_cast").setCreativeTab(ModuleInfinity.TAB));
        e.getRegistry().register(new ItemBlock(INFINITY_GENERATOR).setRegistryName(INFINITY_GENERATOR.getRegistryName()));
    }

    @SubscribeEvent
    public static void onRegisterEntities(RegistryEvent.Register<EntityEntry> e) {
        EntityRegistry.registerModEntity(new ResourceLocation(LucraftCore.MODID, "item_indestructible"), EntityItemIndestructible.class, "item_indestructible", 1, LucraftCore.INSTANCE, 64, 1, true);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent e) {
        RenderingRegistry.registerEntityRenderingHandler(EntityItemIndestructible.class, RenderEntityInfinityStone::new);

        if (!INSTANCE.isEnabled())
            return;

        ModelLoader.setCustomMeshDefinition(INFINITY_GAUNTLET, stack -> ItemModelInfinityGauntlet.LOCATION);
        ModelBakery.registerItemVariants(INFINITY_GAUNTLET, ItemModelInfinityGauntlet.LOCATION, ItemModelInfinityGauntlet.LOCATION_3D);
        INFINITY_GAUNTLET.setTileEntityItemStackRenderer(new ItemRendererInfinityGauntlet());

        ItemHelper.registerItemModel(INFINITY_GAUNTLET_CAST, LucraftCore.MODID, "infinity_gauntlet_cast");
        ItemHelper.registerItemModel(Item.getItemFromBlock(INFINITY_GENERATOR), LucraftCore.MODID, "infinity_generator");

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityInfinityGenerator.class, new TESRInfinityGenerator());
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e) {
        IBakedModel model3d = e.getModelRegistry().getObject(ItemModelInfinityGauntlet.LOCATION_3D);

        if (model3d != null)
            ItemModelInfinityGauntlet.BakedInfinityGauntletModel.model3d = model3d;
    }

    @SubscribeEvent
    public static void anvilUpdateEvent(AnvilUpdateEvent e) {
        if ((Loader.isModLoaded("tconstruct") || Loader.isModLoaded("thermalexpansion") || Loader.isModLoaded("immersiveengineering")) && LCConfig.infinity.disableAnvilWithMods)
            return;

        if (LCConfig.infinity.anvilCrafting && e.getLeft().getItem() == INFINITY_GAUNTLET_CAST && ItemHelper.getOreDictionaryEntries(e.getRight()).contains("plateGoldTitaniumAlloy") && e.getRight().getCount() == 6) {
            ItemStack stack = new ItemStack(INFINITY_GAUNTLET);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("Fist", true);
            stack.setTagCompound(nbt);
            e.setOutput(stack);
            e.setCost(20);
        }
    }

    @SubscribeEvent
    public static void loot(LootTableLoadEvent e) {
        if (LCConfig.infinity.gauntletCastGeneration && e.getName().toString().equalsIgnoreCase(LootTableList.CHESTS_VILLAGE_BLACKSMITH.toString())) {
            LootCondition[] chance = {new RandomChance(1F)};
            LootFunction[] count = {new SetCount(chance, new RandomValueRange(1.0F, 1.0F))};
            LootEntryItem item = new LootEntryItem(INFINITY_GAUNTLET_CAST, 2, 1, count, chance, "infinity_gauntlet");
            LootPool pool = new LootPool(new LootEntry[]{item}, chance, new RandomValueRange(0, 1), new RandomValueRange(0, 0), "infinity_gauntlet");
            e.getTable().addPool(pool);
        }
    }

    @SubscribeEvent
    public static void onMissingRegistries(RegistryEvent.MissingMappings<Item> e) {
        for (RegistryEvent.MissingMappings.Mapping<Item> missing : e.getMappings()) {
            if (missing.key.getNamespace().equals(LucraftCore.MODID) && missing.key.getPath().equals("failing_tent_unity")) {
                missing.remap(INFINITY_GAUNTLET);
            }
        }
    }

    public static void initTiConCrafting() {
        if (FluidRegistry.isFluidRegistered(Material.GOLD_TITANIUM_ALLOY.getIdentifier().toLowerCase())) {
            ItemStack stack = new ItemStack(INFINITY_GAUNTLET);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("Fist", true);
            stack.setTagCompound(nbt);
            TinkerRegistry.registerTableCasting(stack, new ItemStack(INFINITY_GAUNTLET_CAST), FluidRegistry.getFluid(Material.GOLD_TITANIUM_ALLOY.getIdentifier().toLowerCase()), Fluid.BUCKET_VOLUME * 6);
        }
    }

    public static void initIECrafting() {
        ItemStack stack = new ItemStack(INFINITY_GAUNTLET);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("Fist", true);
        stack.setTagCompound(nbt);
        for (ItemStack ingots : OreDictionary.getOres("ingotGoldTitaniumAlloy")) {
            ItemStack input = ingots.copy();
            input.setCount(6);
            MetalPressRecipe.addRecipe(stack, input, new ItemStack(INFINITY_GAUNTLET_CAST), 2000);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        LCGuiHandler.registerGuiHandlerEntry(GuiHandlerEntryInfinityGauntlet.ID, new GuiHandlerEntryInfinityGauntlet());
        LCGuiHandler.registerGuiHandlerEntry(GuiHandlerEntryInfinityGenerator.ID, new GuiHandlerEntryInfinityGenerator());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient(FMLPreInitializationEvent event) {
        ModelLoaderRegistry.registerLoader(ItemModelInfinityGauntlet.LoaderInfinityGauntlet.INSTANCE);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        if (LCConfig.infinity.modCrafting) {
            if (Loader.isModLoaded("tconstruct"))
                initTiConCrafting();
            if (Loader.isModLoaded("thermalexpansion")) {
                ItemStack stack = new ItemStack(INFINITY_GAUNTLET);
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setBoolean("Fist", true);
                stack.setTagCompound(nbt);
                ThermalExpansionHelper.addTransposerFill(2000, new ItemStack(INFINITY_GAUNTLET_CAST), stack, new FluidStack(FluidRegistry.getFluid(Material.GOLD_TITANIUM_ALLOY.getIdentifier().toLowerCase()), 6000), false);
            }
            if (Loader.isModLoaded("immersiveengineering")) {
                initIECrafting();
            }
        }

        if (Loader.isModLoaded("jei"))
            JEIInfoReader.register(new ResourceLocation(LucraftCore.MODID, "infinity_gauntlet_cast"), new JEIInfoReader.JEIInfo(new TextComponentTranslation("lucraftcore.info.infinity_gauntlet_cast_info"), new ItemStack(INFINITY_GAUNTLET_CAST)));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Override
    public String getName() {
        return "Infinity";
    }

    @Override
    public boolean isEnabled() {
        return LCConfig.modules.infinity;
    }
}
