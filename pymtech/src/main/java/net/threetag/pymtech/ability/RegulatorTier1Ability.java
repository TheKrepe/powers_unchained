package net.threetag.pymtech.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.threetag.pymtech.container.RegulatorTier1Container;

import javax.annotation.Nullable;

public class RegulatorTier1Ability extends RegulatorAbility {

    public RegulatorTier1Ability() {
        super(PTAbilityTypes.REGULATOR_TIER1.get());
    }

    @Override
    public int getVialSlots() {
        return 4;
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
        return new RegulatorTier1Container(id, playerInventory, this);
    }
}
