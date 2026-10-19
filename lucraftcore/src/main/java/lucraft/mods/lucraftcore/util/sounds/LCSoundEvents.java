package lucraft.mods.lucraftcore.util.sounds;

import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = LucraftCore.MODID)
public class LCSoundEvents {

    public static SoundEvent ENERGY_BLAST;
    public static SoundEvent INFINITY_STONE_EQUIP;
    public static SoundEvent USE_INFINITY_GAUNTLET;

    @SubscribeEvent
    public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> e) {
        e.getRegistry().register(ENERGY_BLAST = new SoundEvent(new ResourceLocation(LucraftCore.MODID, "energy_blast")).setRegistryName(new ResourceLocation(LucraftCore.MODID, "energy_blast")));
        e.getRegistry().register(INFINITY_STONE_EQUIP = new SoundEvent(new ResourceLocation(LucraftCore.MODID, "infinity_stone_equip")).setRegistryName(new ResourceLocation(LucraftCore.MODID, "infinity_stone_equip")));
        e.getRegistry().register(USE_INFINITY_GAUNTLET = new SoundEvent(new ResourceLocation(LucraftCore.MODID, "use_infinity_gauntlet")).setRegistryName(new ResourceLocation(LucraftCore.MODID, "use_infinity_gauntlet")));
    }

}
