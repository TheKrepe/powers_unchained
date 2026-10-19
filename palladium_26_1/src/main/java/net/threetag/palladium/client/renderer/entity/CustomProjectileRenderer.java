package net.threetag.palladium.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec2;
import net.threetag.palladium.client.renderer.LaserRenderer;
import net.threetag.palladium.client.renderer.entity.layer.pack.CompoundPackRenderLayer;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayerManager;
import net.threetag.palladium.client.renderer.entity.state.CustomProjectileRenderState;
import net.threetag.palladium.client.trail.ClientEntityTrailHandler;
import net.threetag.palladium.client.trail.Trail;
import net.threetag.palladium.client.trail.TrailConfiguration;
import net.threetag.palladium.client.trail.TrailManager;
import net.threetag.palladium.client.trail.TrailRenderer;
import net.threetag.palladium.client.trail.TrailSegment;
import net.threetag.palladium.client.util.PerspectiveAwareConditions;
import net.threetag.palladium.entity.CustomProjectile;
import net.threetag.palladium.logic.context.DataContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class CustomProjectileRenderer extends EntityRenderer<CustomProjectile, CustomProjectileRenderState> {

    private final ItemModelResolver itemModelResolver;
    private final Map<CustomProjectile, TrailData> trailData = new WeakHashMap<>();

    public CustomProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    private static class TrailData {
        final List<ClientEntityTrailHandler.TrailInstance> instances = new ArrayList<>();
        int lastTick = -1;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void submit(CustomProjectileRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        for (ItemStackRenderState item : state.items) {
            poseStack.pushPose();
            poseStack.mulPose(camera.orientation);
            item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }

        if (!state.renderLayers.isEmpty()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
            poseStack.translate(-0.5F, state.boundingBoxHeight / 2F, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(270.0F));
            EntityModel baseModel = new EntityModel<>(new ModelPart(Collections.emptyList(), Collections.emptyMap())) {
            };
            state.renderLayers.forEach((layer, layerState) -> {
                if (layer.shouldRender(layerState, PerspectiveAwareConditions.Perspective.THIRD_PERSON)) {
                    layer.submit(
                            poseStack,
                            submitNodeCollector,
                            baseModel,
                            state.livingState,
                            layerState,
                            state.lightCoords,
                            state.partialTick,
                            state.xRot,
                            state.yRot
                    );
                }
            });
            poseStack.popPose();
        }

        for (CustomProjectile.Appearance appearance : state.appearances) {
            if (appearance instanceof CustomProjectile.LaserAppearance laser) {
                var laserPart = new LaserRenderer.LaserPart(laser.color, 1F, 0F, null, LaserRenderer.BlendingMode.ADDITIVE);
                var laserRenderer = new LaserRenderer(laserPart, laserPart, 2, new Vector2f(laser.thickness, laser.thickness), 0F, 0F);
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
                poseStack.translate(-0.5F, state.boundingBoxHeight / 2F, 0);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                laserRenderer.render(poseStack, submitNodeCollector, (int) state.ageInTicks, 0.5, state.partialTick, 1F, Vec2.ONE);
                poseStack.popPose();
            }
        }

        for (ClientEntityTrailHandler.TrailInstance instance : state.trails) {
            TrailRenderer trailRenderer = instance.getTrail().trailRenderer();
            for (TrailSegment segment : instance.getSegments()) {
                poseStack.pushPose();
                Vec3 offset = segment.getPosition().subtract(state.x, state.y, state.z);
                poseStack.translate(offset.x, offset.y, offset.z);
                trailRenderer.submit(poseStack, submitNodeCollector, state.livingState, segment, state.lightCoords, state.partialTick);
                poseStack.popPose();
            }
        }

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public CustomProjectileRenderState createRenderState() {
        return new CustomProjectileRenderState();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void extractRenderState(CustomProjectile entity, CustomProjectileRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.livingState.partialTick = state.partialTick;
        state.livingState.ageInTicks = state.ageInTicks;
        state.livingState.lightCoords = state.lightCoords;
        state.livingState.outlineColor = state.outlineColor;
        state.livingState.boundingBoxHeight = state.boundingBoxHeight;
        state.livingState.boundingBoxWidth = state.boundingBoxWidth;
        state.livingState.xRot = state.xRot;
        state.livingState.yRot = state.yRot;
        state.livingState.bodyRot = state.yRot;
        state.livingState.x = state.x;
        state.livingState.y = state.y;
        state.livingState.z = state.z;
        state.appearances.clear();
        state.renderLayers.clear();
        state.trails.clear();

        // Trails: resolve trail configs once per entity, tick once per game tick
        TrailData trails = this.trailData.computeIfAbsent(entity, this::resolveTrails);
        if (entity.tickCount != trails.lastTick) {
            trails.lastTick = entity.tickCount;
            for (ClientEntityTrailHandler.TrailInstance instance : trails.instances) {
                instance.tick(entity);
            }
        }
        state.trails.addAll(trails.instances);

        // Appearances
        for (CustomProjectile.Appearance appearance : entity.appearances) {
            if (appearance instanceof CustomProjectile.ItemAppearance item) {
                var itemState = new ItemStackRenderState();
                this.itemModelResolver.updateForNonLiving(itemState, item.item, ItemDisplayContext.GROUND, entity);
                state.items.add(itemState);
            } else if (appearance instanceof CustomProjectile.RenderLayerAppearance renderLayers) {
                var context = DataContext.forEntity(entity);
                for (Identifier id : renderLayers.renderLayers) {
                    PackRenderLayer layer = PackRenderLayerManager.INSTANCE.get(id);

                    if (layer != null) {
                        if (layer instanceof CompoundPackRenderLayer compound) {
                            for (PackRenderLayer l : compound.getLayers()) {
                                PackRenderLayer.State layerState = l.createExtractedState(context);
                                state.renderLayers.put(l, layerState);
                            }
                        } else {
                            PackRenderLayer.State layerState = layer.createExtractedState(context);
                            state.renderLayers.put(layer, layerState);
                        }
                    }
                }
            } else if (!(appearance instanceof CustomProjectile.TrailAppearance)) {
                state.appearances.add(appearance);
            }
        }
    }

    private TrailData resolveTrails(CustomProjectile entity) {
        TrailData data = new TrailData();
        for (CustomProjectile.Appearance appearance : entity.appearances) {
            if (appearance instanceof CustomProjectile.TrailAppearance trailAppearance) {
                for (Identifier id : trailAppearance.trails) {
                    TrailConfiguration config = TrailManager.INSTANCE.get(id);
                    if (config != null) {
                        for (Trail trail : config.trails()) {
                            data.instances.add(new ClientEntityTrailHandler.TrailInstance(trail));
                        }
                    }
                }
            }
        }
        return data;
    }
}
