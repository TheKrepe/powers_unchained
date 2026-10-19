package lucraft.mods.lucraftcore.materials.potions;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MaterialsPotions {

    public static PotionRadiation RADIATION = new PotionRadiation();

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Potion> e) {
        e.getRegistry().register(RADIATION);
    }

}
