package lucraft.mods.heroesexpansion.fluids;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.blocks.BlockFluidAcid;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HEFluids {

    public static Fluid WEB_FLUID;
    public static Fluid SUPER_SOLDIER_SERUM;
    public static Fluid RADIOACTIVE_ACID;

    public static ResourceLocation WEB_FLUID_STILL = new ResourceLocation(HeroesExpansion.MODID, "fluids/web_fluid");
    public static ResourceLocation WEB_FLUID_FLOW = new ResourceLocation(HeroesExpansion.MODID, "fluids/web_fluid_flow");

    // Note to future-me: the color for fluids are specified using ARGB, not just RGB

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> e) {
        FluidRegistry.registerFluid(WEB_FLUID = new Fluid("web_fluid", WEB_FLUID_STILL, WEB_FLUID_FLOW, 0xffffffff));
        FluidRegistry.registerFluid(SUPER_SOLDIER_SERUM = new Fluid("super_soldier_serum", WEB_FLUID_STILL, WEB_FLUID_FLOW, 0xff4bd3ff));
        FluidRegistry.registerFluid(RADIOACTIVE_ACID = new Fluid("radioactive_acid", WEB_FLUID_STILL, WEB_FLUID_FLOW, 0xffb0ffa9));

        e.getRegistry().register(new BlockFluidClassic(WEB_FLUID, Material.WATER).setRegistryName("web_fluid"));
        e.getRegistry().register(new BlockFluidClassic(SUPER_SOLDIER_SERUM, Material.WATER).setRegistryName("super_soldier_serum"));
        e.getRegistry().register(new BlockFluidAcid(RADIOACTIVE_ACID, Material.WATER).setRegistryName("radioactive_acid"));

        FluidRegistry.addBucketForFluid(WEB_FLUID);
        FluidRegistry.addBucketForFluid(SUPER_SOLDIER_SERUM);
        FluidRegistry.addBucketForFluid(RADIOACTIVE_ACID);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent e) {
        ModelLoader.setCustomStateMapper(WEB_FLUID.getBlock(), new FluidStateMapper(WEB_FLUID));
        ModelLoader.setCustomStateMapper(SUPER_SOLDIER_SERUM.getBlock(), new FluidStateMapper(SUPER_SOLDIER_SERUM));
        ModelLoader.setCustomStateMapper(RADIOACTIVE_ACID.getBlock(), new FluidStateMapper(RADIOACTIVE_ACID));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRegisterTexture(TextureStitchEvent.Pre e) {
        e.getMap().registerSprite(WEB_FLUID_STILL);
        e.getMap().registerSprite(WEB_FLUID_FLOW);
    }

    @SideOnly(Side.CLIENT)
    public static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition {

        public final ModelResourceLocation location;

        public FluidStateMapper(Fluid fluid) {
            this.location = new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "fluids"), fluid.getName());
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