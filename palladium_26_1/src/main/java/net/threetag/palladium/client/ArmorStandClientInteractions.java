package net.threetag.palladium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.entity.ArmorStandInteractions;
import net.threetag.palladium.item.multiverse.ItemMultiversalVariant;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.network.CycleArmorStandVariantPacket;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class ArmorStandClientInteractions {

    private static final ScrollWheelHandler SCROLL = new ScrollWheelHandler();

    @SubscribeEvent
    static void onScroll(InputEvent.MouseScrollingEvent e) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        if (!minecraft.player.isShiftKeyDown() || !minecraft.player.hasInfiniteMaterials()) {
            return;
        }
        if (!(minecraft.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof ArmorStand stand)) {
            return;
        }

        Vector2i wheelXY = SCROLL.onMouseScroll(e.getScrollDeltaX(), e.getScrollDeltaY());
        int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
        if (wheel == 0) {
            return;
        }

        List<CycleArmorStandVariantPacket.SlotVariant> updates = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!ArmorStandInteractions.isSwappable(slot)) {
                continue;
            }

            ItemStack stack = stand.getItemBySlot(slot);
            Identifier category = stack.get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get());
            if (category == null) {
                continue;
            }

            List<ResourceKey<MultiversalItemVariant>> variants = MultiversalItemVariant.variantsForCategory(minecraft.level.registryAccess(), category);
            if (variants.size() <= 1) {
                continue;
            }

            ItemMultiversalVariant current = stack.get(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get());
            int currentIndex = current == null ? -1 : variants.indexOf(current.variant());
            ResourceKey<MultiversalItemVariant> next = variants.get(ScrollWheelHandler.getNextScrollWheelSelection(wheel, currentIndex, variants.size()));

            if (current == null || !next.equals(current.variant())) {
                MultiversalItemVariant.apply(minecraft.level.registryAccess(), stack, next);
                updates.add(new CycleArmorStandVariantPacket.SlotVariant(slot, next));
            }
        }

        if (!updates.isEmpty()) {
            ClientPacketDistributor.sendToServer(new CycleArmorStandVariantPacket(stand.getId(), updates));
            e.setCanceled(true);
        }
    }
}
