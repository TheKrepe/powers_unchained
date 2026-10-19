package net.threetag.pymtech.container;

import com.mojang.datafixers.util.Pair;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.tags.PTItemTags;

import javax.annotation.Nonnull;

public class RegulatorSlot extends SlotItemHandler {

    public static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(PymTech.MODID, "item/empty_regulator_slot");

    public RegulatorSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return canFitRegulatorSlot(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Pair<ResourceLocation, ResourceLocation> getBackground() {
        return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, OVERLAY_TEXTURE);
    }

    public static boolean canFitRegulatorSlot(ItemStack stack) {
        return stack.getItem().isIn(PTItemTags.REGULATORS);
    }
}
