package net.threetag.palladium.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.entity.layer.pack.VibrationPackRenderLayer;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.client.trail.ClientEntityTrailHandler;
import net.threetag.palladium.client.trail.Trail;
import net.threetag.palladium.client.util.PerspectiveAwareConditions.Perspective;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class VibrationRenderer {

    public static final int MAX_SAMPLES = 32;

    private static final int CONFIG_SIZE = new Std140SizeCalculator().putVec2().putFloat().putFloat().putInt().putFloat().get();
    private static final int UNUSED_FRAMES_UNTIL_RELEASE = 600;
    private static final float SCREEN_MARGIN = 2.0F;
    private static final float WORLD_MARGIN = 1.0F;
    private static final float DEPTH_TOLERANCE = 0.05F;

    private static @Nullable TextureTarget ghostTarget;
    private static @Nullable MappableRingBuffer configBuffer;
    private static @Nullable SubmitNodeStorage nodeStorage;
    private static @Nullable FeatureRenderDispatcher features;
    private static int unusedFrames;
    private static int frame;

    @SubscribeEvent
    static void render(RenderLevelStageEvent.AfterTranslucentFeatures e) {
        var mc = Minecraft.getInstance();
        var levelState = e.getLevelRenderState();
        var camera = levelState.cameraRenderState;
        var mainTarget = mc.getMainRenderTarget();
        boolean rendered = false;
        frame++;

        for (EntityRenderState entityState : levelState.entityRenderStates) {
            Vibration vibration = getVibration(entityState);

            if (vibration == null) {
                continue;
            }

            if (!rendered) {
                prepare(mainTarget);
                rendered = true;
            }

            renderEntity(entityState, camera, mainTarget, vibration);
        }

        if (rendered) {
            unusedFrames = 0;
        } else if (ghostTarget != null && ++unusedFrames > UNUSED_FRAMES_UNTIL_RELEASE) {
            release();
        }
    }

    private static void renderEntity(EntityRenderState entityState, CameraRenderState camera, RenderTarget mainTarget, Vibration vibration) {
        var ghostTarget = VibrationRenderer.ghostTarget;
        var nodeStorage = VibrationRenderer.nodeStorage;
        var features = VibrationRenderer.features;

        if (ghostTarget == null || nodeStorage == null || features == null
                || ghostTarget.getColorTexture() == null || ghostTarget.getDepthTexture() == null
                || mainTarget.getColorTextureView() == null || mainTarget.getDepthTextureView() == null) {
            return;
        }

        float distance = (float) Math.sqrt(entityState.distanceToCameraSq);

        if (distance <= 0.0F) {
            return;
        }

        float pixelsPerBlock = camera.projectionMatrix.m11() * mainTarget.height * 0.5F / distance;
        float pixelRadius = vibration.radius() * pixelsPerBlock;

        if (pixelRadius < 0.05F) {
            return;
        }

        int[] scissor = new int[4];

        if (!computeScissor(entityState, camera, mainTarget, vibration.radius(), pixelRadius, scissor)) {
            return;
        }

        var encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.clearColorAndDepthTextures(ghostTarget.getColorTexture(), 0, ghostTarget.getDepthTexture(), 1.0, scissor[0], scissor[1], scissor[2], scissor[3]);

        submitGhost(entityState, camera, ghostTarget, nodeStorage, features, scissor);
        composite(encoder, ghostTarget, mainTarget, pixelRadius, vibration.samples(), depthBias(camera, distance), scissor);
    }

    private static void submitGhost(EntityRenderState entityState, CameraRenderState camera, TextureTarget ghostTarget, SubmitNodeStorage nodeStorage, FeatureRenderDispatcher features, int[] scissor) {
        var mc = Minecraft.getInstance();
        var bufferSource = mc.renderBuffers().bufferSource();

        List<EntityRenderState.ShadowPiece> shadowPieces = List.copyOf(entityState.shadowPieces);
        Map<Trail, ClientEntityTrailHandler.TrailInstance> trails = entityState.getRenderDataOrDefault(PalladiumRenderStateKeys.TRAILS, Collections.emptyMap());
        Component nameTag = entityState.nameTag;
        boolean fireAnimation = entityState.displayFireAnimation;
        int outlineColor = entityState.outlineColor;

        entityState.shadowPieces.clear();
        entityState.setRenderData(PalladiumRenderStateKeys.TRAILS, Collections.emptyMap());
        entityState.nameTag = null;
        entityState.displayFireAnimation = false;
        entityState.outlineColor = 0;

        ScissorState previousScissor = RenderSystem.getScissorStateForRenderTypeDraws();
        boolean hadScissor = previousScissor.enabled();
        int previousX = previousScissor.x();
        int previousY = previousScissor.y();
        int previousWidth = previousScissor.width();
        int previousHeight = previousScissor.height();

        RenderSystem.outputColorTextureOverride = ghostTarget.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = ghostTarget.getDepthTextureView();
        RenderSystem.enableScissorForRenderTypeDraws(scissor[0], scissor[1], scissor[2], scissor[3]);

        try {
            var cameraPos = camera.pos;
            mc.getEntityRenderDispatcher().submit(
                    entityState, camera,
                    entityState.x - cameraPos.x(), entityState.y - cameraPos.y(), entityState.z - cameraPos.z(),
                    new PoseStack(), nodeStorage
            );

            features.renderSolidFeatures();
            bufferSource.endBatch();
            features.renderTranslucentFeatures();
            bufferSource.endBatch();
        } finally {
            features.clearSubmitNodes();

            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;

            if (hadScissor) {
                RenderSystem.enableScissorForRenderTypeDraws(previousX, previousY, previousWidth, previousHeight);
            } else {
                RenderSystem.disableScissorForRenderTypeDraws();
            }

            entityState.shadowPieces.addAll(shadowPieces);
            entityState.setRenderData(PalladiumRenderStateKeys.TRAILS, trails);
            entityState.nameTag = nameTag;
            entityState.displayFireAnimation = fireAnimation;
            entityState.outlineColor = outlineColor;
        }
    }

    private static void composite(CommandEncoder encoder, TextureTarget ghostTarget, RenderTarget mainTarget, float pixelRadius, int samples, float depthBias, int[] scissor) {
        var configBuffer = VibrationRenderer.configBuffer;

        if (configBuffer == null) {
            return;
        }

        float alpha = 1.0F - (float) Math.pow(0.85F, samples);

        try (GpuBuffer.MappedView view = encoder.mapBuffer(configBuffer.currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putVec2(pixelRadius, pixelRadius)
                    .putFloat(alpha)
                    .putFloat(frame % 1024)
                    .putInt(samples)
                    .putFloat(depthBias);
        }

        var sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);

        try (RenderPass pass = encoder.createRenderPass(
                () -> "Palladium vibration composite",
                mainTarget.getColorTextureView(), OptionalInt.empty(),
                null, OptionalDouble.empty()
        )) {
            pass.setPipeline(PalladiumRenderTypes.Pipelines.VIBRATION);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("VibrationConfig", configBuffer.currentBuffer());
            pass.bindTexture("GhostSampler", ghostTarget.getColorTextureView(), sampler);
            pass.bindTexture("GhostDepthSampler", ghostTarget.getDepthTextureView(), sampler);
            pass.bindTexture("SceneDepthSampler", mainTarget.getDepthTextureView(), sampler);
            pass.enableScissor(scissor[0], scissor[1], scissor[2], scissor[3]);
            pass.draw(0, 3);
        }

        configBuffer.rotate();
    }

    private static float depthBias(CameraRenderState camera, float distance) {
        return Math.abs(0.5F * camera.projectionMatrix.m32() / (distance * distance)) * DEPTH_TOLERANCE;
    }

    private static boolean computeScissor(EntityRenderState entityState, CameraRenderState camera, RenderTarget mainTarget, float radius, float pixelRadius, int[] out) {
        double halfWidth = entityState.boundingBoxWidth * 0.5F + radius + WORLD_MARGIN;
        double height = entityState.boundingBoxHeight + radius + WORLD_MARGIN;
        var cameraPos = camera.pos;
        double x = entityState.x - cameraPos.x();
        double y = entityState.y - cameraPos.y();
        double z = entityState.z - cameraPos.z();

        Matrix4f viewProjection = new Matrix4f(camera.projectionMatrix).mul(camera.viewRotationMatrix);
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int corner = 0; corner < 8; corner++) {
            float cornerX = (float) (x + ((corner & 1) == 0 ? -halfWidth : halfWidth));
            float cornerY = (float) (y + ((corner & 2) == 0 ? -(radius + WORLD_MARGIN) : height));
            float cornerZ = (float) (z + ((corner & 4) == 0 ? -halfWidth : halfWidth));
            Vector4f projected = viewProjection.transform(new Vector4f(cornerX, cornerY, cornerZ, 1.0F));

            if (projected.w() <= 0.0F) {
                out[0] = 0;
                out[1] = 0;
                out[2] = mainTarget.width;
                out[3] = mainTarget.height;
                return true;
            }

            float screenX = (projected.x() / projected.w() * 0.5F + 0.5F) * mainTarget.width;
            float screenY = (projected.y() / projected.w() * 0.5F + 0.5F) * mainTarget.height;
            minX = Math.min(minX, screenX);
            minY = Math.min(minY, screenY);
            maxX = Math.max(maxX, screenX);
            maxY = Math.max(maxY, screenY);
        }

        float margin = pixelRadius + SCREEN_MARGIN;
        int left = Mth.clamp(Mth.floor(minX - margin), 0, mainTarget.width);
        int bottom = Mth.clamp(Mth.floor(minY - margin), 0, mainTarget.height);
        int right = Mth.clamp(Mth.ceil(maxX + margin), 0, mainTarget.width);
        int top = Mth.clamp(Mth.ceil(maxY + margin), 0, mainTarget.height);

        out[0] = left;
        out[1] = bottom;
        out[2] = right - left;
        out[3] = top - bottom;
        return out[2] > 0 && out[3] > 0;
    }

    private static void prepare(RenderTarget mainTarget) {
        if (ghostTarget == null) {
            ghostTarget = new TextureTarget("Palladium Vibration", mainTarget.width, mainTarget.height, true);
        } else if (ghostTarget.width != mainTarget.width || ghostTarget.height != mainTarget.height) {
            ghostTarget.resize(mainTarget.width, mainTarget.height);
        }

        if (configBuffer == null) {
            configBuffer = new MappableRingBuffer(() -> "Palladium Vibration Config", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, CONFIG_SIZE);
        }

        if (nodeStorage == null || features == null) {
            var mc = Minecraft.getInstance();
            nodeStorage = new SubmitNodeStorage();
            features = new FeatureRenderDispatcher(
                    nodeStorage,
                    mc.getModelManager(),
                    mc.renderBuffers().bufferSource(),
                    mc.getAtlasManager(),
                    mc.renderBuffers().outlineBufferSource(),
                    mc.renderBuffers().crumblingBufferSource(),
                    mc.font,
                    mc.gameRenderer.getGameRenderState()
            );
        }
    }

    private static void release() {
        if (ghostTarget != null) {
            ghostTarget.destroyBuffers();
            ghostTarget = null;
        }

        if (configBuffer != null) {
            configBuffer.close();
            configBuffer = null;
        }

        if (features != null) {
            features.close();
            features = null;
        }

        nodeStorage = null;
        unusedFrames = 0;
    }

    @SuppressWarnings("rawtypes")
    private static @Nullable Vibration getVibration(EntityRenderState entityState) {
        float progress = 0.0F;
        int samples = 0;

        for (Object entry : entityState.getRenderDataOrDefault(PalladiumRenderStateKeys.RENDER_LAYERS, Collections.emptyMap()).entrySet()) {
            Map.Entry layerEntry = (Map.Entry) entry;

            if (layerEntry.getKey() instanceof VibrationPackRenderLayer layer
                    && layerEntry.getValue() instanceof VibrationPackRenderLayer.State layerState
                    && layer.shouldRender(layerState, Perspective.THIRD_PERSON)) {
                progress = Math.max(progress, layerState.getProgress(entityState.partialTick));
                samples = Math.max(samples, layer.intensity);
            }
        }

        if (progress <= 0.0F || samples <= 0) {
            return null;
        }

        return new Vibration(progress / 300.0F, Mth.clamp(samples, 1, MAX_SAMPLES));
    }

    private record Vibration(float radius, int samples) {
    }
}
