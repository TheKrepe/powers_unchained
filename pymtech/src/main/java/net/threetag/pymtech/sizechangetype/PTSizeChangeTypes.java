package net.threetag.pymtech.sizechangetype;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;
import net.threetag.pymtech.PymTech;
import net.threetag.threecore.sizechanging.SizeChangeType;

@ObjectHolder(PymTech.MODID)
@Mod.EventBusSubscriber(modid = PymTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PTSizeChangeTypes {

    @ObjectHolder("pym_particles")
    public static final SizeChangeType PYM_PARTICLES = null;

    @SubscribeEvent
    public static void onRegisterSizeChangeTypes(RegistryEvent.Register<SizeChangeType> e) {
        e.getRegistry().register(new PymParticleSizeChangeType().setRegistryName(PymTech.MODID, "pym_particles"));
    }

}
