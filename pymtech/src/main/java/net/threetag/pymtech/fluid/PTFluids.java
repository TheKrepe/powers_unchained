package net.threetag.pymtech.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.item.PTItems;

import java.util.function.Supplier;

@ObjectHolder(PymTech.MODID)
@Mod.EventBusSubscriber(modid = PymTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PTFluids {

    @ObjectHolder("shrink_pym_particles")
    public static final Fluid SHRINK_PYM_PARTICLES = null;

    @ObjectHolder("shrink_pym_particles_flowing")
    public static final Fluid SHRINK_PYM_PARTICLES_FLOWING = null;

    @ObjectHolder("enlarge_pym_particles")
    public static final Fluid ENLARGE_PYM_PARTICLES = null;

    @ObjectHolder("enlarge_pym_particles_flowing")
    public static final Fluid ENLARGE_PYM_PARTICLES_FLOWING = null;

    @ObjectHolder("shrink_pym_particles")
    public static final Block SHRINK_PYM_PARTICLES_BLOCK = null;

    @ObjectHolder("enlarge_pym_particles")
    public static final Block ENLARGE_PYM_PARTICLES_BLOCK = null;

    @SubscribeEvent
    public static void onRegisterFluids(RegistryEvent.Register<Fluid> e) {
        makeFluid(e.getRegistry(), "shrink_pym_particles", 0xffce100a, () -> SHRINK_PYM_PARTICLES, () -> SHRINK_PYM_PARTICLES_FLOWING, () -> (FlowingFluidBlock) SHRINK_PYM_PARTICLES_BLOCK, PTItems.SHRINK_PYM_PARTICLES_BUCKET);
        makeFluid(e.getRegistry(), "enlarge_pym_particles", 0xff0a5ccb, () -> ENLARGE_PYM_PARTICLES, () -> ENLARGE_PYM_PARTICLES_FLOWING, () -> (FlowingFluidBlock) ENLARGE_PYM_PARTICLES_BLOCK, PTItems.ENLARGE_PYM_PARTICLES_BUCKET);
    }

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> e) {
        e.getRegistry().register(new PymParticleFluidBlock(() -> (FlowingFluid) SHRINK_PYM_PARTICLES_FLOWING, Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops()).setRegistryName("shrink_pym_particles"));
        e.getRegistry().register(new PymParticleFluidBlock(() -> (FlowingFluid) ENLARGE_PYM_PARTICLES_FLOWING, Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops()).setRegistryName("enlarge_pym_particles"));
    }

    public static void makeFluid(IForgeRegistry<Fluid> registry, String name, int color, Supplier<Fluid> source, Supplier<Fluid> flowing, Supplier<FlowingFluidBlock> block, Supplier<Item> item) {
        ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(source, flowing,
                FluidAttributes.builder(new ResourceLocation(PymTech.MODID, "block/pym_particles"),
                        new ResourceLocation(PymTech.MODID, "block/pym_particles_flowing"))
                        .overlay(new net.minecraft.util.ResourceLocation("block/water_overlay"))
                        .color(color)
                        .viscosity(750))
                .block(block)
                .bucket(item)
                .levelDecreasePerBlock(4);
        registry.register(new ForgeFlowingFluid.Source(properties).setRegistryName(PymTech.MODID, name));
        registry.register(new ForgeFlowingFluid.Flowing(properties).setRegistryName(PymTech.MODID, name + "_flowing"));
    }

}
