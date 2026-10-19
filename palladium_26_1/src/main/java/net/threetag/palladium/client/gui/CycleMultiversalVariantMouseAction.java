package net.threetag.palladium.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.multiverse.ItemMultiversalVariant;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.network.CycleMultiversalVariantPacket;
import org.joml.Vector2i;

import java.util.List;

public class CycleMultiversalVariantMouseAction implements ItemSlotMouseAction {

    private final ScrollWheelHandler scrollWheelHandler = new ScrollWheelHandler();

    @Override
    public boolean matches(Slot slot) {
        if (!slot.getItem().has(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CreativeModeInventoryScreen) {
            return minecraft.player != null && slot.container == minecraft.player.getInventory();
        }
        return true;
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, ItemStack itemStack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !minecraft.player.hasInfiniteMaterials()) {
            return false;
        }

        Identifier category = itemStack.get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get());
        if (category == null) {
            return false;
        }

        List<ResourceKey<MultiversalItemVariant>> variants = MultiversalItemVariant.variantsForCategory(minecraft.level.registryAccess(), category);
        if (variants.size() <= 1) {
            return false;
        }

        Vector2i wheelXY = this.scrollWheelHandler.onMouseScroll(scrollX, scrollY);
        int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
        if (wheel != 0) {
            ItemMultiversalVariant current = itemStack.get(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get());
            int currentIndex = current == null ? -1 : variants.indexOf(current.variant());
            ResourceKey<MultiversalItemVariant> next = variants.get(ScrollWheelHandler.getNextScrollWheelSelection(wheel, currentIndex, variants.size()));

            if (current == null || !next.equals(current.variant())) {
                int inventorySlot = inventorySlot(minecraft, itemStack);
                boolean playerInventory = inventorySlot >= 0;
                int slotId = playerInventory ? inventorySlot : containerMenuSlotId(minecraft, slotIndex, itemStack);

                if (slotId < 0) {
                    return true;
                }

                MultiversalItemVariant.apply(minecraft.level.registryAccess(), itemStack, next);
                ClientPacketListener connection = minecraft.getConnection();
                if (connection != null) {
                    connection.send(new CycleMultiversalVariantPacket(slotId, playerInventory, next));
                }
            }
        }

        return true;
    }

    private static int inventorySlot(Minecraft minecraft, ItemStack itemStack) {
        Inventory inventory = minecraft.player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i) == itemStack) {
                return i;
            }
        }

        return -1;
    }

    private static int containerMenuSlotId(Minecraft minecraft, int slotIndex, ItemStack itemStack) {
        if (minecraft.screen instanceof CreativeModeInventoryScreen) {
            return -1;
        }

        AbstractContainerMenu menu = minecraft.player.containerMenu;

        if (slotIndex < 0 || slotIndex >= menu.slots.size() || menu.getSlot(slotIndex).getItem() != itemStack) {
            return -1;
        }

        return slotIndex;
    }

    @Override
    public void onStopHovering(Slot hoveredSlot) {
    }

    @Override
    public void onSlotClicked(Slot slot, ContainerInput containerInput) {
    }
}
