package net.threetag.pymtech.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.threetag.threecore.ability.Ability;

import javax.annotation.Nullable;

public abstract class AbstractAdvancedRegulatorContainer extends Container {

    public final Ability ability;

    protected AbstractAdvancedRegulatorContainer(@Nullable ContainerType<?> type, Ability ability, int id) {
        super(type, id);
        this.ability = ability;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
}
