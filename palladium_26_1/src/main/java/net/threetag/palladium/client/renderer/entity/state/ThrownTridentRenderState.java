package net.threetag.palladium.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ThrownTridentRenderState extends EntityRenderState {

    public float xRot;
    public float yRot;
    public final ItemStackRenderState item = new ItemStackRenderState();

}
