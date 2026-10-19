package net.threetag.palladium.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public class GrappleHookRenderState extends EntityRenderState {

    public LivingEntity owner;
    public Identifier beamRendererId;
    public float partialTick;

}
