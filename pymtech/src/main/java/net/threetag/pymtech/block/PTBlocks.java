package net.threetag.pymtech.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.tileentity.StructureShrinkerTileEntity;
import net.threetag.threecore.item.ItemGroupRegistry;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = PymTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PTBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PymTech.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, PymTech.MODID);

    public static final RegistryObject<Block> STRUCTURE_SHRINKER = BLOCKS.register("structure_shrinker", () -> new StructureShrinkerBlock(Block.Properties.create(Material.IRON)));

    public static final RegistryObject<TileEntityType<StructureShrinkerTileEntity>> STRUCTURE_SHRINKER_TILE_ENTITY = TILE_ENTITIES.register("structure_shrinker", () -> TileEntityType.Builder.create(StructureShrinkerTileEntity::new, STRUCTURE_SHRINKER.get()).build(null));

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(new BlockItem(Objects.requireNonNull(STRUCTURE_SHRINKER.get()), new Item.Properties().group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY))).setRegistryName(Objects.requireNonNull(STRUCTURE_SHRINKER.get().getRegistryName())));
    }

}
