package net.threetag.palladium.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.client.trail.ClientEntityTrailHandler;
import net.threetag.palladium.entity.CustomProjectile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomProjectileRenderState extends EntityRenderState {

    public float xRot;
    public float yRot;
    public List<CustomProjectile.Appearance> appearances = new ArrayList<>();
    public final List<ItemStackRenderState> items = new ArrayList<>();
    public Map<PackRenderLayer<PackRenderLayer.State>, PackRenderLayer.State> renderLayers = new HashMap<>();
    public final List<ClientEntityTrailHandler.TrailInstance> trails = new ArrayList<>();
    public final LivingEntityRenderState livingState = new LivingEntityRenderState();

}
