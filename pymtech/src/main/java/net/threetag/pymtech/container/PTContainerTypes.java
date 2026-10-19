package net.threetag.pymtech.container;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.ObjectHolder;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.ability.RegulatorTier1Ability;
import net.threetag.pymtech.ability.RegulatorTier2Ability;
import net.threetag.pymtech.ability.RegulatorTier3Ability;
import net.threetag.pymtech.tileentity.StructureShrinkerTileEntity;
import net.threetag.threecore.ability.Ability;
import net.threetag.threecore.ability.AbilityHelper;

@ObjectHolder(PymTech.MODID)
@Mod.EventBusSubscriber(modid = PymTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PTContainerTypes {

    @ObjectHolder("structure_shrinker")
    public static final ContainerType<StructureShrinkerContainer> STRUCTURE_SHRINKER = null;

    @ObjectHolder("regulator")
    public static final ContainerType<RegulatorTier1Container> REGULATOR_TIER1 = null;

    @ObjectHolder("regulator_tier2")
    public static final ContainerType<RegulatorTier2Container> REGULATOR_TIER2 = null;

    @ObjectHolder("regulator_tier3")
    public static final ContainerType<RegulatorTier3Container> REGULATOR_TIER3 = null;

    @SubscribeEvent
    public static void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> e) {
        // Structure Shrinker
        e.getRegistry().register(new ContainerType<>((IContainerFactory<Container>) (windowId, inv, data) -> {
            TileEntity tileEntity = inv.player.world.getTileEntity(data.readBlockPos());
            return tileEntity instanceof StructureShrinkerTileEntity ? new StructureShrinkerContainer(windowId, inv, (StructureShrinkerTileEntity) tileEntity) : null;
        }).setRegistryName("structure_shrinker"));

        // Regulator
        e.getRegistry().register(new ContainerType<>((IContainerFactory<Container>) (windowId, inv, data) -> {
            Ability ability = AbilityHelper.getAbilityById(inv.player, data.readString(), null);
            return ability instanceof RegulatorTier1Ability ? new RegulatorTier1Container(windowId, inv, (RegulatorTier1Ability) ability) : null;
        }).setRegistryName("regulator"));

        // Regulator Tier 2
        e.getRegistry().register(new ContainerType<>((IContainerFactory<Container>) (windowId, inv, data) -> {
            Ability ability = AbilityHelper.getAbilityById(inv.player, data.readString(), null);
            return ability instanceof RegulatorTier2Ability ? new RegulatorTier2Container(windowId, inv, (RegulatorTier2Ability) ability) : null;
        }).setRegistryName("regulator_tier2"));

        // Regulator Tier 3
        e.getRegistry().register(new ContainerType<>((IContainerFactory<Container>) (windowId, inv, data) -> {
            Ability ability = AbilityHelper.getAbilityById(inv.player, data.readString(), null);
            return ability instanceof RegulatorTier3Ability ? new RegulatorTier3Container(windowId, inv, (RegulatorTier3Ability) ability) : null;
        }).setRegistryName("regulator_tier3"));
    }

}
