package net.threetag.pymtech.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.math.MathHelper;
import net.threetag.pymtech.container.RegulatorTier2Container;
import net.threetag.threecore.util.threedata.FloatThreeData;
import net.threetag.threecore.util.threedata.ThreeData;

import javax.annotation.Nullable;

public class RegulatorTier2Ability extends RegulatorAbility implements IAdvancedRegulatorAbility {

    public static final float MIN_SIZE = 0.1F;
    public static final float MAX_SIZE = 8F;

    public static final ThreeData<Float> MIN = new FloatThreeData("min_size").enableSetting("Determines the minimum size the player can choose");
    public static final ThreeData<Float> MAX = new FloatThreeData("max_size").enableSetting("Determines the maximum size the player can choose");

    public RegulatorTier2Ability() {
        super(PTAbilityTypes.REGULATOR_TIER2.get());
    }

    @Override
    public int getVialSlots() {
        return 8;
    }

    @Override
    public void registerData() {
        super.registerData();
        this.register(REGULATED_SIZE, 1F);
        this.register(MIN, MIN_SIZE);
        this.register(MAX, MAX_SIZE);
    }

    @Override
    public void setRegulatedSize(float size) {
        this.set(REGULATED_SIZE, MathHelper.clamp(size, this.get(MIN), this.get(MAX)));
    }

    @Override
    public float getRegulatedSize() {
        return this.get(REGULATED_SIZE);
    }

    @Nullable
    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
        return new RegulatorTier2Container(id, playerInventory, this);
    }
}
