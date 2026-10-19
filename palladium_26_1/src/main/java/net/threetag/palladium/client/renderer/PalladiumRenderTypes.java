package net.threetag.palladium.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.threetag.palladium.Palladium;

import java.util.function.Function;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public abstract class PalladiumRenderTypes  {

    public static final RenderType LASER = RenderType.create(Palladium.MOD_ID + ":laser", RenderSetup.builder(Pipelines.ADD)
            .setOutputTarget(OutputTarget.WEATHER_TARGET).sortOnUpload().createRenderSetup());

    public static final RenderType LASER_TRANSLUCENT = RenderType.create(Palladium.MOD_ID + ":laser_translucent", RenderSetup.builder(Pipelines.LASER_TRANSLUCENT)
            .setOutputTarget(OutputTarget.WEATHER_TARGET).sortOnUpload().createRenderSetup());

    private static final Function<Identifier, RenderType> ENTITY_ADDITIVE = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_ADDITIVE)
                .withTexture("Sampler0", texture)
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":entity_additive", state);
    });

    public static RenderType entityAdditive(Identifier texture) {
        return ENTITY_ADDITIVE.apply(texture);
    }

    private static final Function<Identifier, RenderType> ENTITY_GLOW = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_GLOW)
                .withTexture("Sampler0", texture)
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":entity_glow", state);
    });

    private static final Function<Identifier, RenderType> ARMOR_GLOW = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_GLOW)
                .withTexture("Sampler0", texture)
                .useOverlay()
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":armor_glow", state);
    });

    public static RenderType entityGlow(Identifier texture) {
        return ENTITY_GLOW.apply(texture);
    }

    public static RenderType armorGlow(Identifier texture) {
        return ARMOR_GLOW.apply(texture);
    }

    private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_STRICT = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_TRANSLUCENT_STRICT)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":entity_translucent_strict", state);
    });

    private static final Function<Identifier, RenderType> ENTITY_TRANSLUCENT_NO_DEPTH_WRITE = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_TRANSLUCENT_NO_DEPTH_WRITE)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":entity_translucent_no_depth_write", state);
    });

    private static final Function<Identifier, RenderType> ARMOR_TRANSLUCENT_STRICT = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ARMOR_TRANSLUCENT_STRICT)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":armor_translucent_strict", state);
    });

    private static final Function<Identifier, RenderType> ARMOR_TRANSLUCENT_NO_DEPTH_WRITE = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ARMOR_TRANSLUCENT_NO_DEPTH_WRITE)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":armor_translucent_no_depth_write", state);
    });

    private static final Function<Identifier, RenderType> ENTITY_GHOST = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_GHOST)
                .withTexture("Sampler0", texture)
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":entity_ghost", state);
    });

    private static final Function<Identifier, RenderType> ARMOR_GHOST = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_GHOST)
                .withTexture("Sampler0", texture)
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":armor_ghost", state);
    });

    public static RenderType entityTranslucentStrictDepth(Identifier texture) {
        return ENTITY_TRANSLUCENT_STRICT.apply(texture);
    }

    public static RenderType entityTranslucentNoDepthWrite(Identifier texture) {
        return ENTITY_TRANSLUCENT_NO_DEPTH_WRITE.apply(texture);
    }

    public static RenderType armorTranslucentStrictDepth(Identifier texture) {
        return ARMOR_TRANSLUCENT_STRICT.apply(texture);
    }

    public static RenderType armorTranslucentNoDepthWrite(Identifier texture) {
        return ARMOR_TRANSLUCENT_NO_DEPTH_WRITE.apply(texture);
    }

    private static final Function<Identifier, RenderType> ENTITY_GHOST_EDGES = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_GHOST_EDGES)
                .withTexture("Sampler0", texture)
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":entity_ghost_edges", state);
    });

    private static final Function<Identifier, RenderType> ARMOR_GHOST_EDGES = Util.memoize(texture -> {
        RenderSetup state = RenderSetup.builder(Pipelines.ENTITY_GHOST_EDGES)
                .withTexture("Sampler0", texture)
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderType.create(Palladium.MOD_ID + ":armor_ghost_edges", state);
    });

    public static RenderType entityGhost(Identifier texture) {
        return ENTITY_GHOST.apply(texture);
    }

    public static RenderType entityGhostEdges(Identifier texture) {
        return ENTITY_GHOST_EDGES.apply(texture);
    }

    public static RenderType armorGhostEdges(Identifier texture) {
        return ARMOR_GHOST_EDGES.apply(texture);
    }

    public static RenderType armorGhost(Identifier texture) {
        return ARMOR_GHOST.apply(texture);
    }

    public static class Pipelines {

        public static final RenderPipeline ADD = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withCull(false)
                .withLocation(Palladium.id("add"))
                .withVertexShader("core/rendertype_lightning")
                .withFragmentShader("core/rendertype_lightning")
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS)
                .build();

        public static final RenderPipeline LASER_TRANSLUCENT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withCull(false)
                .withLocation(Palladium.id("laser_translucent"))
                .withVertexShader("core/rendertype_lightning")
                .withFragmentShader("core/rendertype_lightning")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS)
                .build();

        public static final RenderPipeline ENTITY_ADDITIVE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(Palladium.id("pipeline/entity_additive"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("PER_FACE_LIGHTING")
                .withSampler("Sampler1")
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
                .build();

        public static final RenderPipeline ENTITY_GLOW = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(Palladium.id("pipeline/entity_glow"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withSampler("Sampler1")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
                .build();

        public static final RenderPipeline ENTITY_TRANSLUCENT_STRICT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Palladium.id("pipeline/entity_translucent_strict"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("PER_FACE_LIGHTING")
                .withSampler("Sampler1")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, true))
                .build();

        public static final RenderPipeline ENTITY_TRANSLUCENT_NO_DEPTH_WRITE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Palladium.id("pipeline/entity_translucent_no_depth_write"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("PER_FACE_LIGHTING")
                .withSampler("Sampler1")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .build();

        public static final RenderPipeline ARMOR_TRANSLUCENT_STRICT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Palladium.id("pipeline/armor_translucent_strict"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("NO_OVERLAY")
                .withShaderDefine("PER_FACE_LIGHTING")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, true))
                .build();

        public static final RenderPipeline ARMOR_TRANSLUCENT_NO_DEPTH_WRITE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Palladium.id("pipeline/armor_translucent_no_depth_write"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("NO_OVERLAY")
                .withShaderDefine("PER_FACE_LIGHTING")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .build();

        public static final RenderPipeline ENTITY_GHOST = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(Palladium.id("pipeline/entity_ghost"))
                .withFragmentShader(Palladium.id("core/entity_ghost"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withShaderDefine("NO_OVERLAY")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .build();

        public static final RenderPipeline ENTITY_GHOST_EDGES = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(Palladium.id("pipeline/entity_ghost_edges"))
                .withFragmentShader(Palladium.id("core/entity_ghost"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withShaderDefine("NO_OVERLAY")
                .withShaderDefine("EDGES")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .build();

        public static final RenderPipeline VIBRATION = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
                .withLocation(Palladium.id("pipeline/vibration"))
                .withVertexShader("core/screenquad")
                .withFragmentShader(Palladium.id("post/vibration"))
                .withSampler("GhostSampler")
                .withSampler("GhostDepthSampler")
                .withSampler("SceneDepthSampler")
                .withUniform("VibrationConfig", UniformType.UNIFORM_BUFFER)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .build();

    }

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent e) {
        e.registerPipeline(Pipelines.ADD);
        e.registerPipeline(Pipelines.LASER_TRANSLUCENT);
        e.registerPipeline(Pipelines.ENTITY_ADDITIVE);
        e.registerPipeline(Pipelines.ENTITY_GLOW);
        e.registerPipeline(Pipelines.ENTITY_TRANSLUCENT_STRICT);
        e.registerPipeline(Pipelines.ENTITY_TRANSLUCENT_NO_DEPTH_WRITE);
        e.registerPipeline(Pipelines.ARMOR_TRANSLUCENT_STRICT);
        e.registerPipeline(Pipelines.ARMOR_TRANSLUCENT_NO_DEPTH_WRITE);
        e.registerPipeline(Pipelines.ENTITY_GHOST);
        e.registerPipeline(Pipelines.ENTITY_GHOST_EDGES);
        e.registerPipeline(Pipelines.VIBRATION);
    }
}
