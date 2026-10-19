package lucraft.mods.lucraftcore.superpowers.suitsets;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RegisterSuitSetEvent extends Event {

    private RegistryNamespaced registry;
    private int i = 0;

    public RegisterSuitSetEvent(RegistryNamespaced<ResourceLocation, SuitSet> registry) {
        this.registry = registry;
    }

    public void register(SuitSet suitSet) {
        registry.register(i++, suitSet.getRegistryName(), suitSet);
    }

}
