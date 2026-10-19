package lucraft.mods.lucraftcore.utilities.blocks;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UtilitiesBlocks {

    public static Block CONSTRUCTION_TABLE = new BlockConstructionTable();
    public static Block SUIT_STAND = new BlockSuitStand();
    public static Block EXTRACTOR = new BlockExtractor();
    public static Block BOILER = new BlockBoiler();
    public static Block FURNACE_GENERATOR = new BlockFurnaceGenerator();

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> e) {
        e.getRegistry().register(CONSTRUCTION_TABLE);
        e.getRegistry().register(SUIT_STAND);
        e.getRegistry().register(EXTRACTOR);
        e.getRegistry().register(BOILER);
        e.getRegistry().register(FURNACE_GENERATOR);
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(new ItemBlock(CONSTRUCTION_TABLE).setRegistryName(CONSTRUCTION_TABLE.getRegistryName()));
        e.getRegistry().register(new ItemBlock(SUIT_STAND).setRegistryName(SUIT_STAND.getRegistryName()));
        e.getRegistry().register(new ItemBlock(EXTRACTOR).setRegistryName(EXTRACTOR.getRegistryName()));
        e.getRegistry().register(new ItemBlock(BOILER).setRegistryName(BOILER.getRegistryName()));
        e.getRegistry().register(new ItemBlock(FURNACE_GENERATOR).setRegistryName(FURNACE_GENERATOR.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent e) {
        OBJLoader.INSTANCE.addDomain(LucraftCore.MODID);
        ItemHelper.registerItemModel(Item.getItemFromBlock(CONSTRUCTION_TABLE), LucraftCore.MODID, "construction_table");
        ItemHelper.registerItemModel(Item.getItemFromBlock(SUIT_STAND), LucraftCore.MODID, "suit_stand");
        ItemHelper.registerItemModel(Item.getItemFromBlock(EXTRACTOR), LucraftCore.MODID, "extractor");
        ItemHelper.registerItemModel(Item.getItemFromBlock(BOILER), LucraftCore.MODID, "boiler");
        ItemHelper.registerItemModel(Item.getItemFromBlock(FURNACE_GENERATOR), LucraftCore.MODID, "furnace_generator");
    }

}