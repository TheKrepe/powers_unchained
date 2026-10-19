package lucraft.mods.heroesexpansion.blocks;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.render.item.ItemRendererPortalDevice;
import lucraft.mods.heroesexpansion.items.ItemKryptonite;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HEBlocks {

    public static Block KRYPTONITE_BLOCK = new BlockKryptonite("kryptonite_block");
    public static Block HEART_SHAPED_HERB = new BlockHeartShapedHerb("heart_shaped_herb");
    public static Block PORTAL_DEVICE = new BlockPortalDevice("portal_device");

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> e) {
        e.getRegistry().register(HEART_SHAPED_HERB);
        e.getRegistry().register(KRYPTONITE_BLOCK);
        e.getRegistry().register(PORTAL_DEVICE);

        GameRegistry.registerTileEntity(BlockKryptonite.TileEntityKryptonite.class, new ResourceLocation(HeroesExpansion.MODID, "kryptonite_te"));

    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(new ItemBlock(HEART_SHAPED_HERB).setRegistryName(HEART_SHAPED_HERB.getRegistryName()));
        e.getRegistry().register(new ItemKryptonite.ItemBlockKryptonite(KRYPTONITE_BLOCK).setRegistryName(KRYPTONITE_BLOCK.getRegistryName()));
        e.getRegistry().register(new ItemBlock(PORTAL_DEVICE).setRegistryName(PORTAL_DEVICE.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent e) {
        Item.getItemFromBlock(PORTAL_DEVICE).setTileEntityItemStackRenderer(new ItemRendererPortalDevice());

        ItemHelper.registerItemModel(Item.getItemFromBlock(HEART_SHAPED_HERB), HeroesExpansion.MODID, "heart_shaped_herb");
        ItemHelper.registerItemModel(Item.getItemFromBlock(KRYPTONITE_BLOCK), HeroesExpansion.MODID, "kryptonite_block");
        ItemHelper.registerItemModel(Item.getItemFromBlock(PORTAL_DEVICE), HeroesExpansion.MODID, "portal_device");

        ModelLoader.setCustomStateMapper(PORTAL_DEVICE, (b) -> new HashMap<>());
    }

}
