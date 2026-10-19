package lucraft.mods.heroesexpansion.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerUseGadgetEvent extends PlayerEvent {

    public final ItemStack stack;

    public PlayerUseGadgetEvent(EntityPlayer player, ItemStack stack) {
        super(player);
        this.stack = stack;
    }

    public ItemStack getItem() {
        return stack;
    }
}
