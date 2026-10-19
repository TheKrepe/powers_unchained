package lucraft.mods.lucraftcore.materials.blocks;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import lucraft.mods.lucraftcore.materials.ModuleMaterials;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class MaterialsBlocks {

    public static Map<Material, BlockLCMaterial> ORES = new HashMap<>();
    public static Map<Material, BlockLCMaterial> BLOCKS = new HashMap<>();

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> e) {
        for (Material m : Material.getMaterials()) {
            if (m.autoGenerateComponent(MaterialComponent.ORE)) {
                BlockLCMaterial oreBlock = new BlockLCMaterial(m, MaterialComponent.ORE);
                ORES.put(m, oreBlock);
                e.getRegistry().register(oreBlock);
                ModuleMaterials.chiselsAndBitsBlocks.add(oreBlock.getDefaultState());
            }
            if (m.autoGenerateComponent(MaterialComponent.BLOCK)) {
                BlockLCMaterial blockBlock = new BlockLCMaterial(m, MaterialComponent.BLOCK);
                BLOCKS.put(m, blockBlock);
                e.getRegistry().register(blockBlock);
                ModuleMaterials.chiselsAndBitsBlocks.add(blockBlock.getDefaultState());
            }
        }
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> e) {
        for (BlockLCMaterial blocks : ORES.values()) {
            e.getRegistry().register(new ItemBlock(blocks).setRegistryName(blocks.getRegistryName()));
        }

        for (BlockLCMaterial blocks : BLOCKS.values()) {
            e.getRegistry().register(new ItemBlock(blocks).setRegistryName(blocks.getRegistryName()));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent e) {
        for (BlockLCMaterial blocks : ORES.values()) {
            ModelLoader.setCustomStateMapper(blocks, new LCBlockMapper(blocks));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blocks), 0, new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "ore"), "type=" + blocks.material.getResourceName()));
        }

        for (BlockLCMaterial blocks : BLOCKS.values()) {
            ModelLoader.setCustomStateMapper(blocks, new LCBlockMapper(blocks));
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blocks), 0, new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "block"), "type=" + blocks.material.getResourceName()));
        }
    }

    @SideOnly(Side.CLIENT)
    public static class LCBlockMapper extends StateMapperBase {

        public BlockLCMaterial block;

        public LCBlockMapper(BlockLCMaterial block) {
            this.block = block;
        }

        @Override
        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, block.component.getName()), "type=" + block.material.getResourceName());
        }

    }

}
