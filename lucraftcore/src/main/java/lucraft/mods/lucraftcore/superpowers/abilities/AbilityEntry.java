package lucraft.mods.lucraftcore.superpowers.abilities;

import net.minecraft.util.ResourceLocation;

public class AbilityEntry extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<AbilityEntry> {

    private Class<? extends Ability> clazz;

    public AbilityEntry(Class<? extends Ability> clazz, ResourceLocation registryName) {
        this.clazz = clazz;
        this.setRegistryName(registryName);
    }

    public Class<? extends Ability> getAbilityClass() {
        return clazz;
    }

}