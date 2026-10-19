package lucraft.mods.lucraftcore.materials.fluids;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MaterialsFluids {

    public static List<BlockMoltenMetal> FLUIDS = new ArrayList<>();

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> e) {
        for (Material m : Material.getMaterials()) {
            if (m.autoGenerateComponent(Material.MaterialComponent.FLUID) && !FluidRegistry.isFluidRegistered(m.getIdentifier().toLowerCase())) {
                FluidMoltenMetal fluid = new FluidMoltenMetal(m);
                FluidRegistry.registerFluid(fluid);
                FluidRegistry.addBucketForFluid(fluid);
                BlockMoltenMetal block = (BlockMoltenMetal) new BlockMoltenMetal(fluid).setRegistryName("molten_" + StringHelper.unlocalizedToResourceName(m.getIdentifier()));
                e.getRegistry().register(block);
                FLUIDS.add(block);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent e) {
        for (BlockMoltenMetal blocks : FLUIDS) {
            FluidStateMapper mapper = new FluidStateMapper(blocks.getFluid());
            ModelLoader.setCustomStateMapper(blocks, mapper);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRegisterTexture(TextureStitchEvent.Pre e) {
        e.getMap().registerSprite(FluidMoltenMetal.ICON_METAL_STIL);
        e.getMap().registerSprite(FluidMoltenMetal.ICON_METAL_FLOW);
    }

    @SideOnly(Side.CLIENT)
    public static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition {

        public final ModelResourceLocation location;

        public FluidStateMapper(ModelResourceLocation location) {
            this.location = location;
        }

        public FluidStateMapper(Fluid fluid) {
            this(new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "molten_metal"), fluid.getName()));
        }

        @Nonnull
        @Override
        protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
            return location;
        }

        @Nonnull
        @Override
        public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack) {
            return location;
        }
    }

}
