package lucraft.mods.lucraftcore.extendedinventory.events;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory.ExtendedInventoryItemType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

public class ExtendedInventoryKeyEvent extends Event {

    public Side side;
    public ExtendedInventoryItemType type;
    public boolean pressed;

    private ExtendedInventoryKeyEvent(ExtendedInventoryItemType type, Side side, boolean pressed) {
        this.type = type;
        this.side = side;
        this.pressed = pressed;
    }

    @Cancelable
    public static class Client extends ExtendedInventoryKeyEvent {

        public Client(ExtendedInventoryItemType type, boolean pressed) {
            super(type, Side.CLIENT, pressed);
        }

    }

    @Cancelable
    public static class Server extends ExtendedInventoryKeyEvent {

        public EntityPlayer player;

        public Server(ExtendedInventoryItemType type, EntityPlayer player, boolean pressed) {
            super(type, Side.SERVER, pressed);
            this.player = player;
        }

    }

}
