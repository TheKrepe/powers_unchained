package net.threetag.palladium.client.renderer.entity.layer.pack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.client.animation.ModelAnimationDefinition;
import net.threetag.palladium.client.model.ModelLayerLocationCodec;
import net.threetag.palladium.client.model.ModelLayerManager;
import net.threetag.palladium.client.renderer.RenderTypeFunction;
import net.threetag.palladium.client.renderer.RenderTypeRegistry;
import net.threetag.palladium.client.util.ModelUtil;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.entity.SkinTypedValue;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class DefaultPackRenderLayer extends PackRenderLayer<DefaultPackRenderLayer.LayerState> {

    public static final MapCodec<DefaultPackRenderLayer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SkinTypedValue.codec(ModelLayerLocationCodec.CODEC).optionalFieldOf("model_layer", new SkinTypedValue<>(ModelLayerLocationCodec.parse("minecraft:player#main"), ModelLayerLocationCodec.parse("minecraft:player_slim#main"))).forGetter(l -> l.modelLayers),
            SkinTypedValue.codec(PackRenderLayerTexture.CODEC).fieldOf("texture").forGetter(l -> l.textures),
            RenderTypeRegistry.CODEC.optionalFieldOf("render_type", RenderTypeRegistry.ENTITY_TRANSLUCENT).forGetter(l -> l.renderType),
            Value.CODEC.optionalFieldOf("light_emission", new StaticValue(0)).forGetter(l -> l.lightEmission),
            ModelAnimationDefinition.CODEC.optionalFieldOf("overrides").forGetter(l -> Optional.ofNullable(l.overrides)),
            propertiesCodec()
    ).apply(instance, (modelLayers, textures, renderType, lightEmission, overrides, properties) -> {
        return new DefaultPackRenderLayer(modelLayers, textures, renderType, lightEmission, overrides.orElse(null), properties);
    }));

    private final SkinTypedValue<ModelLayerLocationCodec> modelLayers;
    private SkinTypedValue<Model.Simple> model;
    private boolean modelsFailed;
    private final SkinTypedValue<PackRenderLayerTexture> textures;
    private final RenderTypeFunction renderType;
    private final Value lightEmission;
    private final ModelAnimationDefinition overrides;

    public DefaultPackRenderLayer(SkinTypedValue<ModelLayerLocationCodec> modelLayers, SkinTypedValue<PackRenderLayerTexture> textures, RenderTypeFunction renderType, Value lightEmission, ModelAnimationDefinition overrides, PackRenderLayerProperties properties) {
        super(properties);

        this.modelLayers = modelLayers;
        this.textures = textures;
        this.renderType = renderType;
        this.lightEmission = lightEmission;
        this.overrides = overrides;
    }

    @Override
    public PackRenderLayerSerializer<?> getSerializer() {
        return PackRenderLayerSerializers.DEFAULT;
    }

    public SkinTypedValue<ModelLayerLocationCodec> getModelLayers() {
        return this.modelLayers;
    }

    // Helper used to decide whether to build the models. Takes failing models into account
    private boolean ensureModels() {
        if (this.model != null) {
            return true;
        }
        if (this.modelsFailed) {
            return false;
        }
        this.buildModels();
        return this.model != null;
    }

    private void buildModels() {
        if (!ModelLayerManager.hasLayers(this.modelLayers)) {
            AddonPackLog.error("No model for layer {}. Skipping render layer.", ModelLayerManager.missingLayerNames(this.modelLayers));
            this.modelsFailed = true;
            return;
        }

        var entityModels = Minecraft.getInstance().getEntityModels();
        this.model = new SkinTypedValue<>(
                new Model.Simple(entityModels.bakeLayer(this.modelLayers.getWide().get()), RenderTypes::entityTranslucent),
                new Model.Simple(entityModels.bakeLayer(this.modelLayers.getSlim().get()), RenderTypes::entityTranslucent)
        );
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, EntityModel<LivingEntityRenderState> parentModel, LivingEntityRenderState state, LayerState layerState, int packedLight, float partialTick, float xRot, float yRot) {
        if (!this.ensureModels()) {
            return;
        }

        Model.Simple model = this.model.get(layerState.isSlim);
        for (ModelPart modelPart : model.allParts()) {
            modelPart.resetPose();
            modelPart.visible = true;
            modelPart.skipDraw = false;
        }

        AvatarAnimManager emote = null;
        if (state instanceof IAvatarAnimationState animState) {
            AvatarAnimManager manager = animState.playerAnimLib$getAnimManager();
            if (manager != null && manager.isActive()) {
                emote = manager;
            }
        }
        mimicModelParts("root", parentModel.root(), model.root(), state, emote);

        List<ModelPartPose> snapshotPoses = new ArrayList<>();
        for (ModelPart part : model.allParts()) {
            snapshotPoses.add(new ModelPartPose(part));
        }
        CustomModelWrapper wrapper = new CustomModelWrapper(model, snapshotPoses, RenderTypes::entityTranslucent);

        submitNodeCollector.submitModel(
                wrapper,
                Unit.INSTANCE,
                poseStack,
                this.renderType.getRenderType(layerState.texture),
                LightCoordsUtil.lightCoordsWithEmission(packedLight, layerState.lightEmission),
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null);

        if (layerState.hasFoil) {
            submitNodeCollector.submitModel(
                    wrapper,
                    Unit.INSTANCE,
                    poseStack,
                    RenderTypes.armorEntityGlint(),
                    LightCoordsUtil.lightCoordsWithEmission(packedLight, layerState.lightEmission),
                    OverlayTexture.NO_OVERLAY,
                    state.outlineColor,
                    null);
        }
    }

    public static void animate(ModelPart modelPart, String partName, AvatarAnimManager emote) {
        PlayerAnimBone bone = emote.get3DTransform(UniversalAnimLoader.getCorrectPlayerBoneName(partName));
        RenderUtil.copyVanillaPart(modelPart, bone);
        bone.rotation.y -= modelPart.getInitialPose().yRot();
        emote.updatePart(modelPart, bone);

        for (String name : modelPart.children.keySet()) {
            ModelPart child = modelPart.getChild(name);
            animate(child, name, emote);
        }
    }

    public List<String> getPartNames(Entity entity) {
        if (!this.ensureModels()) {
            return List.of();
        }

        List<String> list = new ArrayList<>();
        this.addPartNames(this.model.getWide().root(), list);
        return list;
    }

    private void addPartNames(ModelPart m, List<String> list) {
        for (String name : m.children.keySet()) {
            list.add(name);
            addPartNames(m.getChild(name), list);
        }
    }

    @Override
    public void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, HumanoidArm arm, ModelPart armPart, AvatarRenderer<?> playerRenderer, LayerState layerState, int packedLight) {
        if (!this.ensureModels()) {
            return;
        }

        Model.Simple model = this.model.get(layerState.isSlim);
        for (ModelPart modelPart : model.allParts()) {
            modelPart.resetPose();
            modelPart.visible = true;
            modelPart.skipDraw = false;
        }
        var partName = arm == HumanoidArm.RIGHT ? "right_arm" : "left_arm";

        if (model.root().hasChild(partName)) {
            var foundPart = model.root().getChild(partName);
            ModelUtil.copyProperties(armPart, foundPart);
            armPart = foundPart;
        } else {
            return;
        }

        submitNodeCollector.submitModelPart(armPart, poseStack,
                this.renderType.getRenderType(layerState.texture),
                LightCoordsUtil.lightCoordsWithEmission(packedLight, layerState.lightEmission),
                OverlayTexture.NO_OVERLAY,
                null);

        if (layerState.hasFoil) {
            submitNodeCollector.submitModelPart(armPart, poseStack,
                    RenderTypes.armorEntityGlint(),
                    LightCoordsUtil.lightCoordsWithEmission(packedLight, layerState.lightEmission),
                    OverlayTexture.NO_OVERLAY,
                    null);
        }
    }

    private static void mimicModelParts(String partName, @Nullable ModelPart source, ModelPart target, LivingEntityRenderState state, @Nullable AvatarAnimManager emote) {
        if (source != null) {
            ModelUtil.copyProperties(source, target);
        } else if (emote != null) {
            animate(target, partName.substring(partName.lastIndexOf('.') + 1), emote);
            if (state instanceof AvatarRenderState avatar && partName.equalsIgnoreCase("root.body.cape")) {
                target.rotateBy((new Quaternionf()).rotateX((6.0F + avatar.capeLean / 2.0F + avatar.capeFlap) * ((float) Math.PI / 180F)).rotateZ(avatar.capeLean2 / 2.0F * ((float) Math.PI / 180F)).rotateY((180.0F - avatar.capeLean2 / 2.0F) * ((float) Math.PI / 180F)));
            }
            return;
        }

        if (state instanceof AvatarRenderState avatar && partName.equalsIgnoreCase("root.body.cape")) {
            target.rotateBy((new Quaternionf()).rotateX((6.0F + avatar.capeLean / 2.0F + avatar.capeFlap) * ((float) Math.PI / 180F)).rotateZ(avatar.capeLean2 / 2.0F * ((float) Math.PI / 180F)).rotateY((180.0F - avatar.capeLean2 / 2.0F) * ((float) Math.PI / 180F)));
        }

        for (Map.Entry<String, ModelPart> e : target.children.entrySet()) {
            String childName = partName + "." + e.getKey();
            ModelPart sourceChild = source != null && source.hasChild(e.getKey()) ? source.getChild(e.getKey()) : null;
            mimicModelParts(childName, sourceChild, e.getValue(), state, emote);
        }
    }

    @Override
    public LayerState createState(DataContext context) {
        return new LayerState(this, context);
    }

    public static class LayerState extends PackRenderLayer.State {

        public Identifier texture = null;
        public int lightEmission = 0;
        public boolean hasFoil = false;

        public LayerState(DefaultPackRenderLayer layer, DataContext context) {
            super(layer, context);
        }

        @Override
        public void extractRenderState(PackRenderLayer<?> layer, DataContext context) {
            super.extractRenderState(layer, context);
            if (!(layer instanceof DefaultPackRenderLayer defaultLayer)) return;
            this.texture = defaultLayer.textures.get(this.isSlim).getTexture(context);
            this.lightEmission = defaultLayer.lightEmission.getAsInt(context);
            this.hasFoil = context.getItem().hasFoil();
        }
    }

    public static class Serializer extends PackRenderLayerSerializer<DefaultPackRenderLayer> {

        @Override
        public MapCodec<DefaultPackRenderLayer> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<PackRenderLayer<? extends State>, DefaultPackRenderLayer> builder, HolderLookup.Provider provider) {
            builder.setName("Default Render Layer")
                    .setDescription("Default render layer that renders a model with a texture.")
                    .add("model_layer", TYPE_IDENTIFIER, "The model layer to render.")
                    .add("texture", TYPE_ANY_TEXTURE, "The texture to render the model with.")
                    .addOptional("render_type", SettingType.enumList(RenderTypeRegistry.types().stream().map(Identifier::toString).toList()), "The render type to render the model with.", RenderTypeRegistry.getKey(RenderTypeRegistry.ENTITY_TRANSLUCENT))
                    .addOptional("light_emission", TYPE_VALUE, "The light emission of the model. Must be within 0 - 15", 0)
//                    .addOptional("overrides", TYPE_IDENTIFIER, "ID of an animations file")
                    .addExampleObject(new DefaultPackRenderLayer(
                            new SkinTypedValue<>(ModelLayerLocationCodec.parse("example:wide_model"), ModelLayerLocationCodec.parse("example:slim_model")),
                            new SkinTypedValue<>(new PackRenderLayerTexture(Identifier.fromNamespaceAndPath("example", "wide_texture")), new PackRenderLayerTexture(Identifier.fromNamespaceAndPath("example", "slim_texture"))),
                            RenderTypeRegistry.ENTITY_TRANSLUCENT,
                            new StaticValue(5),
                            null,
                            PackRenderLayerProperties.DEFAULT
                    ));
        }
    }

    private record ModelPartPose(float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale, boolean visible, boolean skipDraw) {
        public ModelPartPose(ModelPart part) {
            this(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot, part.xScale, part.yScale, part.zScale, part.visible, part.skipDraw);
        }

        public void apply(ModelPart part) {
            part.x = this.x;
            part.y = this.y;
            part.z = this.z;
            part.xRot = this.xRot;
            part.yRot = this.yRot;
            part.zRot = this.zRot;
            part.xScale = this.xScale;
            part.yScale = this.yScale;
            part.zScale = this.zScale;
            part.visible = this.visible;
            part.skipDraw = this.skipDraw;
        }
    }

    private static class CustomModelWrapper extends Model<Unit> {
        private final Model.Simple originalModel;
        private final List<ModelPartPose> snapshotPoses;

        public CustomModelWrapper(Model.Simple originalModel, List<ModelPartPose> snapshotPoses, Function<Identifier, RenderType> renderTypeGetter) {
            super(originalModel.root(), renderTypeGetter);
            this.originalModel = originalModel;
            this.snapshotPoses = snapshotPoses;
        }

        @Override
        public void setupAnim(Unit state) {
            super.setupAnim(state);
            List<ModelPart> parts = this.originalModel.allParts();
            for (int i = 0; i < parts.size(); i++) {
                this.snapshotPoses.get(i).apply(parts.get(i));
            }
        }
    }

}
