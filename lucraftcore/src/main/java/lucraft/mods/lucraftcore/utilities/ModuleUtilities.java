package lucraft.mods.lucraftcore.utilities;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.gui.LCGuiHandler;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityConstructionTable;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntitySuitStand;
import lucraft.mods.lucraftcore.utilities.blocks.UtilitiesBlocks;
import lucraft.mods.lucraftcore.utilities.gui.*;
import lucraft.mods.lucraftcore.utilities.items.ItemInjection;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import lucraft.mods.lucraftcore.utilities.jei.JEIInfoReader;
import lucraft.mods.lucraftcore.utilities.network.*;
import lucraft.mods.lucraftcore.utilities.recipes.AddonPackInstructionRecipeReader;
import lucraft.mods.lucraftcore.utilities.render.TESRConstructionTable;
import lucraft.mods.lucraftcore.utilities.render.TESRSuitStand;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModuleUtilities extends Module {

    public static final ModuleUtilities INSTANCE = new ModuleUtilities();

    public UtilitiesItems ITEMS = new UtilitiesItems();
    public UtilitiesBlocks BLOCKS = new UtilitiesBlocks();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // EventHandler Registering
        MinecraftForge.EVENT_BUS.register(ITEMS);
        MinecraftForge.EVENT_BUS.register(BLOCKS);

        // Gui Handler
        LCGuiHandler.registerGuiHandlerEntry(GuiHandlerEntryConstructionTable.ID, new GuiHandlerEntryConstructionTable());
        LCGuiHandler.registerGuiHandlerEntry(GuiHandlerEntrySuitStand.ID, new GuiHandlerEntrySuitStand());
        LCGuiHandler.registerGuiHandlerEntry(GuiHandlerEntryExtractor.ID, new GuiHandlerEntryExtractor());
        LCGuiHandler.registerGuiHandlerEntry(GuiHandlerEntryFurnaceGenerator.ID, new GuiHandlerEntryFurnaceGenerator());
        LCGuiHandler.registerGuiHandlerEntry(GuiHandlerEntryBoiler.ID, new GuiHandlerEntryBoiler());

        // Increase Armor Limit
        if (LCConfig.increaseArmorLimit)
            increaseArmorAttributeLimit();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Packet Registering
        LCPacketDispatcher.registerMessage(MessageSetSelectedRecipe.Handler.class, MessageSetSelectedRecipe.class, Side.SERVER, 20);
        LCPacketDispatcher.registerMessage(MessageClearInstructionRecipes.Handler.class, MessageClearInstructionRecipes.class, Side.CLIENT, 21);
        LCPacketDispatcher.registerMessage(MessageSyncInstructionRecipe.Handler.class, MessageSyncInstructionRecipe.class, Side.CLIENT, 22);
        LCPacketDispatcher.registerMessage(MessageClearJEIInfo.Handler.class, MessageClearJEIInfo.class, Side.CLIENT, 23);
        LCPacketDispatcher.registerMessage(MessageSyncJEIInfo.Handler.class, MessageSyncJEIInfo.class, Side.CLIENT, 24);

        // Recipes from Addon Packs
        AddonPackInstructionRecipeReader.loadRecipes();
        JEIInfoReader.loadRecipes();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        LucraftCore.CREATIVE_TAB_ICON = new ItemStack(ITEMS.HAMMER);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initClient(FMLInitializationEvent event) {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new ItemInjection.InjectionItemColor(), UtilitiesItems.INJECTION);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConstructionTable.class, new TESRConstructionTable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySuitStand.class, new TESRSuitStand());
    }

    @Override
    public String getName() {
        return "Utilities";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static void increaseArmorAttributeLimit() {
        RangedAttribute attribute = (RangedAttribute) SharedMonsterAttributes.ARMOR;
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, attribute, 1024, "maximumValue", "field_111118_b");
    }

}
