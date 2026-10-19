package net.threetag.palladium.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.threetag.palladium.Palladium;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class ArmorStandInteractions {

    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract e) {
        if (e.getHand() != InteractionHand.MAIN_HAND || !(e.getTarget() instanceof ArmorStand stand)) {
            return;
        }

        Player player = e.getEntity();
        if (!player.isShiftKeyDown()) {
            return;
        }

        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.SUCCESS);

        if (player.level().isClientSide()) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!isSwappable(slot)) {
                continue;
            }

            ItemStack playerStack = player.getItemBySlot(slot);
            ItemStack standStack = stand.getItemBySlot(slot);
            player.setItemSlot(slot, standStack);
            stand.setItemSlot(slot, playerStack);
        }
    }

    public static boolean isSwappable(EquipmentSlot slot) {
        return slot.getType() == EquipmentSlot.Type.HAND || slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }
}
