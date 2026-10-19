package lucraft.mods.lucraftcore.superpowers.items;

import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.utilities.items.ItemInjection;
import lucraft.mods.lucraftcore.utilities.items.ItemInjection.Injection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class SuperpowerItems {

    public void loadSuperpowerInjection() {
        for (Superpower power : SuperpowerHandler.SUPERPOWER_REGISTRY.getValues()) {
            Injection in = new InjectionSuperpower(power);
            ItemInjection.registerInjection(in, power.getRegistryName());
        }
    }

    public static class InjectionSuperpower extends Injection {

        public Superpower superpower;

        public InjectionSuperpower(Superpower superpower) {
            super(superpower.getUnlocalizedName());
            this.superpower = superpower;
        }

        @Override
        public String getDisplayName() {
            return superpower.getDisplayName();
        }

        @Override
        public int chanceForInjection(EntityPlayer player, ItemStack stack) {
            return 100;
        }

        @Override
        public ItemStack inject(EntityPlayer player, ItemStack stack) {
            SuperpowerHandler.giveSuperpower(player, superpower);
            return stack;
        }

        @Override
        public int priorityForRemoving(EntityPlayer player, ItemStack stack) {
            if (SuperpowerHandler.getSuperpower(player) == superpower)
                return 100;
            return 0;
        }

        @Override
        public ItemStack removeFromPlayer(EntityPlayer player, ItemStack stack) {
            SuperpowerHandler.removeSuperpower(player);
            return stack;
        }

        @Override
        public int getColor() {
            return superpower.getCapsuleColor();
        }

    }

}
