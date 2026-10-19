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

public class PymParticleVialSlot extends SlotItemHandler {

    public static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(PymTech.MODID, "item/empty_vial_slot");

    public PymParticleVialSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return canFitVialSlot(stack);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Pair<ResourceLocation, ResourceLocation> getBackground() {
        return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, OVERLAY_TEXTURE);
    }

    public static boolean canFitVialSlot(ItemStack stack) {
        return stack.getItem().isIn(PTItemTags.REGULATOR_VIALS);
    }
}
