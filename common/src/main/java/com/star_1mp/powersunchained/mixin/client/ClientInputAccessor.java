package com.star_1mp.powersunchained.mixin.client;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Wall climbing / rolling take the movement keys over: the movement input event can't clear the (protected) move
 * vector, so vanilla would add its own walking push on top.
 */
@Mixin(ClientInput.class)
public interface ClientInputAccessor {

    @Accessor("moveVector")
    void powers_unchained$setMoveVector(Vec2 moveVector);
}
