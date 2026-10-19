package lucraft.mods.lucraftcore.extendedinventory.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory.ExtendedInventoryItemType;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.events.ExtendedInventoryKeyEvent;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageExtendedInventoryKey implements IMessage {

    public boolean pressed;
    public ExtendedInventoryItemType type;

    public MessageExtendedInventoryKey() {
    }

    public MessageExtendedInventoryKey(boolean pressed, ExtendedInventoryItemType type) {
        this.pressed = pressed;
        this.type = type;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pressed = buf.readBoolean();
        this.type = ExtendedInventoryItemType.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.pressed);
        buf.writeInt(this.type.ordinal());
    }

    public static class Handler extends AbstractServerMessageHandler<MessageExtendedInventoryKey> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageExtendedInventoryKey message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                @Override
                public void run() {
                    if (message != null && ctx != null) {
                        if (MinecraftForge.EVENT_BUS.post(new ExtendedInventoryKeyEvent.Server(message.type, player, message.pressed)))
                            return;

                        InventoryExtendedInventory inv = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();
                        ItemStack s = null;

                        switch (message.type) {
                            case NECKLACE:
                                s = inv.getStackInSlot(InventoryExtendedInventory.SLOT_NECKLACE);
                                break;
                            case MANTLE:
                                s = inv.getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE);
                                break;
                            case WRIST:
                                s = inv.getStackInSlot(InventoryExtendedInventory.SLOT_WRIST);
                                break;
                            default:
                                break;
                        }

                        if (s != null && !s.isEmpty() && s.getItem() instanceof IItemExtendedInventory && ((IItemExtendedInventory) s.getItem()).useButton(s, player)) {
                            ((IItemExtendedInventory) s.getItem()).onPressedButton(s, player, message.pressed);
                        }
                    }
                }

            });

            return null;
        }

    }

}
