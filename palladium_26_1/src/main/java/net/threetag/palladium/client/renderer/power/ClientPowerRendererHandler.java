package net.threetag.palladium.client.renderer.power;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.client.model.ModelLayerLocationCodec;
import net.threetag.palladium.client.model.ModelLayerManager;
import net.threetag.palladium.client.model.PowerRendererModel;
import net.threetag.palladium.entity.SkinTypedValue;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.EntityPowerHandler;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.util.PlayerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientPowerRendererHandler extends EntityPowerRendererHandler {

    private enum Phase { INACTIVE, TRANSITIONING_IN, ACTIVE, TRANSITIONING_OUT }

    private Phase phase = Phase.INACTIVE;
    private @Nullable PowerRenderer activeRenderer;
    private @Nullable Identifier activeRendererPowerId;
    private @Nullable PowerRenderer pendingRenderer;
    private @Nullable Identifier pendingRendererPowerId;

    private SkinTypedValue<ModelLayerLocationCodec> builtModelLayers;
    private SkinTypedValue<PowerRendererModel> model;
    private @Nullable List<String> cachedPartNames;

    private int inCounter = 0, prevInCounter = 0, inDuration = 0;
    private int outCounter = 0, prevOutCounter = 0, outDuration = 0;

    @Override
    public void tick() {
        LivingEntity entity = this.getEntity();
        var powerHandler = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.POWER_HANDLER.get());

        PowerRenderer targetRenderer = null;
        Identifier targetRendererPowerId = null;
        if (powerHandler instanceof EntityPowerHandler handler) {
            var context = DataContext.forEntity(entity);
            for (PowerInstance power : handler.getPowers()) {
                PowerRenderer candidate = PowerRendererManager.INSTANCE.get(power.getPowerId());
                if (candidate != null && candidate.entityRenderer() != null
                        && candidate.entityRenderer().conditions().test(context)
                        && ModelLayerManager.hasLayers(candidate.entityRenderer().modelLayer())) {
                    targetRenderer = candidate;
                    targetRendererPowerId = power.getPowerId();
                    break;
                }
            }
        }

        switch (this.phase) {
            case INACTIVE -> {
                if (targetRenderer != null) {
                    this.activateRenderer(entity, targetRenderer, targetRendererPowerId);
                }
            }
            case TRANSITIONING_IN -> {
                this.prevInCounter = this.inCounter;
                if (this.inCounter < this.inDuration) this.inCounter++;
                if (this.inCounter >= this.inDuration) {
                    this.phase = Phase.ACTIVE;
                }
                if (targetRenderer != this.activeRenderer) {
                    this.startTransitionOut(entity, targetRenderer, targetRendererPowerId);
                }
            }
            case ACTIVE -> {
                if (targetRenderer != this.activeRenderer) {
                    this.startTransitionOut(entity, targetRenderer, targetRendererPowerId);
                }
            }
            case TRANSITIONING_OUT -> {
                this.prevOutCounter = this.outCounter;
                if (this.outCounter < this.outDuration) this.outCounter++;
                if (this.outCounter >= this.outDuration) {
                    if (this.pendingRenderer != null) {
                        this.activateRenderer(entity, this.pendingRenderer, this.pendingRendererPowerId);
                        this.pendingRenderer = null;
                        this.pendingRendererPowerId = null;
                    } else {
                        this.deactivate();
                    }
                }
                if (targetRenderer != this.pendingRenderer) {
                    this.pendingRenderer = targetRenderer;
                    this.pendingRendererPowerId = targetRendererPowerId;
                }
            }
        }
    }

    private void activateRenderer(LivingEntity entity, PowerRenderer renderer, @Nullable Identifier powerId) {
        if (this.activeRenderer != renderer) {
            this.model = null;
            this.builtModelLayers = null;
        }
        this.activeRenderer = renderer;
        this.activeRendererPowerId = powerId;
        this.prevInCounter = 0;
        this.inCounter = 0;
        this.inDuration = renderer.entityRenderer() != null && renderer.entityRenderer().transition() != null
                ? renderer.entityRenderer().transition().inDuration() : 0;
        if (this.inDuration <= 0) {
            this.phase = Phase.ACTIVE;
        } else {
            this.phase = Phase.TRANSITIONING_IN;
        }
    }

    private void startTransitionOut(LivingEntity entity, @Nullable PowerRenderer pending, @Nullable Identifier pendingPowerId) {
        this.pendingRenderer = pending;
        this.pendingRendererPowerId = pendingPowerId;
        this.prevOutCounter = 0;
        this.outCounter = 0;
        this.outDuration = this.activeRenderer != null
                && this.activeRenderer.entityRenderer() != null
                && this.activeRenderer.entityRenderer().transition() != null
                ? this.activeRenderer.entityRenderer().transition().outDuration() : 0;
        if (this.outDuration <= 0) {
            if (pending != null) {
                this.activateRenderer(entity, pending, pendingPowerId);
                this.pendingRenderer = null;
                this.pendingRendererPowerId = null;
            } else {
                this.deactivate();
            }
        } else {
            this.phase = Phase.TRANSITIONING_OUT;
        }
    }

    private void deactivate() {
        this.phase = Phase.INACTIVE;
        this.activeRenderer = null;
        this.activeRendererPowerId = null;
        this.model = null;
        this.builtModelLayers = null;
    }

    public boolean isActive() {
        return this.phase != Phase.INACTIVE;
    }

    public boolean isModelVisible() {
        var transition = this.activeRenderer != null && this.activeRenderer.entityRenderer() != null
                ? this.activeRenderer.entityRenderer().transition() : null;
        return this.phase == Phase.ACTIVE || (transition != null && transition.showDuringTransition());
    }

    public @Nullable PowerRenderer getActiveRenderer() {
        return this.activeRenderer;
    }

    public @Nullable Identifier getVisibleModelRendererPowerId() {
        if (this.activeRenderer == null || this.activeRenderer.entityRenderer() == null || !this.isModelVisible()) {
            return null;
        }
        return this.activeRendererPowerId;
    }

    @Nullable
    public Identifier getTransitionAnimation() {
        if (this.activeRenderer == null || this.activeRenderer.entityRenderer() == null
                || this.activeRenderer.entityRenderer().transition() == null) {
            return null;
        }
        var transition = this.activeRenderer.entityRenderer().transition();
        return switch (this.phase) {
            case TRANSITIONING_IN  -> transition.inAnimation();
            case TRANSITIONING_OUT -> transition.outAnimation();
            default -> null;
        };
    }

    public int getTransitionAnimationOutBlend() {
        if (this.activeRenderer == null || this.activeRenderer.entityRenderer() == null
                || this.activeRenderer.entityRenderer().transition() == null) {
            return 0;
        }
        return this.activeRenderer.entityRenderer().transition().animationOutBlend();
    }

    public float getProgress(float partialTick) {
        return switch (this.phase) {
            case INACTIVE -> 0f;
            case TRANSITIONING_IN -> this.inDuration == 0 ? 1f
                    : Mth.lerp(partialTick, (float) this.prevInCounter / this.inDuration, (float) this.inCounter / this.inDuration);
            case ACTIVE -> 1f;
            case TRANSITIONING_OUT -> this.outDuration == 0 ? 0f
                    : 1f - Mth.lerp(partialTick, (float) this.prevOutCounter / this.outDuration, (float) this.outCounter / this.outDuration);
        };
    }

    public @Nullable PowerRendererRenderState createRenderState(LivingEntity entity, float partialTick) {
        if (this.phase == Phase.INACTIVE || this.activeRenderer == null) {
            return null;
        }

        var context = DataContext.forEntity(entity);
        boolean isSlim = entity instanceof Player player && PlayerUtil.hasSmallArms(player);
        PowerRendererModel resolvedModel = this.activeRenderer.entityRenderer() != null ? this.getOrBuildModel(isSlim) : null;
        return new PowerRendererRenderState(this.activeRenderer, this.getProgress(partialTick), isSlim, resolvedModel, this.activeRenderer.entityRenderer().texture().getTexture(context), this.isModelVisible());
    }

    @Nullable
    public PowerRendererModel getOrBuildModel(boolean isSlim) {
        if (this.activeRenderer == null || this.activeRenderer.entityRenderer() == null) {
            return null;
        }

        var entityRenderer = this.activeRenderer.entityRenderer();
        SkinTypedValue<ModelLayerLocationCodec> targetLayers = entityRenderer.modelLayer();
        // Also retry while the model is null, so a layer that appears after a resource reload gets baked
        if (this.model == null || !targetLayers.equals(this.builtModelLayers)) {
            boolean layersChanged = !targetLayers.equals(this.builtModelLayers);
            this.builtModelLayers = targetLayers;
            this.cachedPartNames = null;
            if (!ModelLayerManager.hasLayers(targetLayers)) {
                if (layersChanged) {
                    AddonPackLog.error("No model for layer {}. Skipping power entity renderer.", ModelLayerManager.missingLayerNames(targetLayers));
                }
                this.model = null;
                return null;
            }
            var entityModels = Minecraft.getInstance().getEntityModels();
            this.model = new SkinTypedValue<>(
                    new PowerRendererModel(entityModels.bakeLayer(targetLayers.getWide().get()), entityRenderer.boneMapping(), entityRenderer.itemBones()),
                    new PowerRendererModel(entityModels.bakeLayer(targetLayers.getSlim().get()), entityRenderer.boneMapping(), entityRenderer.itemBones())
            );
        }
        return this.model == null ? null : this.model.get(isSlim);
    }

    public List<String> getActivePartNames() {
        if (this.model != null && this.cachedPartNames != null) {
            return this.cachedPartNames;
        }
        PowerRendererModel model = this.getOrBuildModel(false);
        if (model == null) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        collectPartNames(model.root(), names);
        this.cachedPartNames = names;
        return names;
    }

    private void collectPartNames(net.minecraft.client.model.geom.ModelPart part, List<String> out) {
        for (String name : part.children.keySet()) {
            out.add(name);
            collectPartNames(part.children.get(name), out);
        }
    }

    @Nullable
    public static ClientPowerRendererHandler get(Entity entity) {
        var handler = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.POWER_RENDERER_HANDLER.get());
        return handler instanceof ClientPowerRendererHandler client ? client : null;
    }
}
