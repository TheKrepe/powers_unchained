package net.threetag.pymtech.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.MathHelper;
import net.threetag.pymtech.container.RegulatorTier3Container;

import javax.annotation.Nullable;

public class RegulatorTier3Ability extends RegulatorAbility implements IAdvancedRegulatorAbility {

    public RegulatorTier3Ability() {
        super(PTAbilityTypes.REGULATOR_TIER3.get());
    }

    @Override
    public int getVialSlots() {
        return 12;
    }

    @Override
    public void registerData() {
        super.registerData();
        this.register(REGULATED_SIZE, 1F);
        this.register(RegulatorTier2Ability.MIN, RegulatorTier2Ability.MIN_SIZE);
        this.register(RegulatorTier2Ability.MAX, RegulatorTier2Ability.MAX_SIZE);
    }

    @Override
    public void setRegulatedSize(float size) {
        this.set(REGULATED_SIZE, MathHelper.clamp(size, this.get(RegulatorTier2Ability.MIN), this.get(RegulatorTier2Ability.MAX)));
    }

    @Override
    public float getRegulatedSize() {
        return this.get(REGULATED_SIZE);
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
        return new RegulatorTier3Container(id, playerInventory, this);
    }
}
