package lucraft.mods.lucraftcore.infinity.container;

import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import lucraft.mods.lucraftcore.infinity.items.ItemInfinityStone;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.sounds.LCSoundEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

public class SlotInfinityStone extends Slot {

    public final EnumInfinityStone type;
    public final EntityPlayer player;

    public SlotInfinityStone(IInventory inventoryIn, EntityPlayer player, EnumInfinityStone type, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
        this.type = type;
        this.player = player;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof ItemInfinityStone && !((ItemInfinityStone) stack.getItem()).isContainer() && ((ItemInfinityStone) stack.getItem()).getType() == this.type;
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();

        if (this.getStack().getItem() instanceof ItemInfinityStone)
            PlayerHelper.playSoundToAll(this.player.world, player.posX, player.posY + player.height / 2D, player.posZ, 50, LCSoundEvents.INFINITY_STONE_EQUIP, SoundCategory.PLAYERS);
    }
}
