package net.threetag.palladium.client.renderer.entity.layer.pack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.threetag.palladium.client.util.PerspectiveAwareConditions;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;

import java.util.*;

public class ClientEntityRenderLayers extends EntityRenderLayers {

    private final Map<PackRenderLayer<?>, PackRenderLayer.State> layers = new HashMap<>();
    private final Map<PlayerModelPart, Float> shrinkProgress = new EnumMap<>(PlayerModelPart.class);

    @Override
    public void tick() {
        List<PackRenderLayer<?>> layers = new ArrayList<>();
        PackRenderLayerProvider.forEach(this.getEntity(), (context, layer) -> {
            layers.add(layer);

            if (!this.layers.containsKey(layer)) {
                this.layers.put(layer, layer.createState(context));
            } else {
                this.layers.get(layer).updateContext(context);
            }

            if (layer instanceof CompoundPackRenderLayer com) {
                for (PackRenderLayer<?> comLayer : com.getLayers()) {
                    if (!this.layers.containsKey(comLayer)) {
                        this.layers.put(comLayer, comLayer.createState(context));
                    } else {
                        this.layers.get(comLayer).updateContext(context);
                    }
                }
            }
        });

        // Remove inactive layers
        for (Map.Entry<PackRenderLayer<?>, PackRenderLayer.State> e : this.layers.entrySet()) {
            var layer = e.getKey();
            boolean found = false;
            for (PackRenderLayer<?> checkedLayer : layers) {
                if (checkedLayer.isOrContains(layer)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                e.getValue().markForRemoval();
            }
        }

        List<PackRenderLayer<?>> toRemove = new ArrayList<>();
        for (Map.Entry<PackRenderLayer<?>, PackRenderLayer.State> e : this.layers.entrySet()) {
            if (e.getValue().isMarkedForRemoval()) {
                toRemove.add(e.getKey());
            }
        }

        for (PackRenderLayer<?> layer : toRemove) {
            this.layers.remove(layer);
        }

        for (PackRenderLayer.State state : this.layers.values()) {
            state.tick(this.getEntity());
        }

        this.updateShrinkProgress();
    }

    private void updateShrinkProgress() {
        this.shrinkProgress.clear();

        LivingEntity entity = this.getEntity();
        float ability = entity == null ? 0F
                : AbilityUtil.getHighestAnimationTimerProgress(entity, AbilitySerializers.SHRINK_PLAYER_OVERLAY.get(), 1.0F);

        Set<PlayerModelPart> instant = null;
        for (Map.Entry<PackRenderLayer<?>, PackRenderLayer.State> e : this.layers.entrySet()) {
            List<PlayerModelPart> parts = e.getKey().getProperties().shrinkPlayerParts();
            if (parts.isEmpty()) {
                continue;
            }
            if (e.getKey().shouldRender(e.getValue(), PerspectiveAwareConditions.Perspective.THIRD_PERSON)) {
                if (instant == null) {
                    instant = EnumSet.noneOf(PlayerModelPart.class);
                }
                instant.addAll(parts);
            }
        }

        if (ability <= 0F && instant == null) {
            return;
        }

        for (PlayerModelPart part : PlayerModelPart.values()) {
            float value = ability;
            if (instant != null && instant.contains(part)) {
                value = Math.max(value, 1.0F);
            }
            if (value > 0F) {
                this.shrinkProgress.put(part, value);
            }
        }
    }

    public Collection<PackRenderLayer<?>> getLayers() {
        return this.layers.keySet();
    }

    public Map<PlayerModelPart, Float> getShrinkProgress() {
        return this.shrinkProgress;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "UnnecessaryLocalVariable"})
    public <T extends PackRenderLayer.State> Map<PackRenderLayer<T>, T> getLayerStates() {
        Map map = this.layers;
        return map;
    }
}
