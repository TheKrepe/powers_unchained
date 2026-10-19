package net.threetag.palladium.client.renderer.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.HumanoidArm;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.animation.PalladiumAnimationLayer;
import net.threetag.palladium.client.gui.screen.abilitybar.AbilityBarSprites;
import net.threetag.palladium.client.model.ModelLayerLocationCodec;
import net.threetag.palladium.client.model.PowerRendererModel;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.entity.SkinTypedValue;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public record PowerRenderer(
        @Nullable EntityRendererSettings entityRenderer,
        Optional<Identifier> screenLayout,
        Map<Identifier, Condition> renderLayers,
        Map<Identifier, Condition> trails,
        Map<Identifier, Condition> hudLayouts,
        Map<String, Condition> hiddenModelParts,
        Map<PalladiumAnimationLayer, Identifier> animationControllers,
        AbilityBarSprites abilityBarSprites
) {

    public static final Identifier DEFAULT_SCREEN_LAYOUT = Palladium.id("power/default");

    public static final Codec<PowerRenderer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityRendererSettings.MAP_CODEC.codec().optionalFieldOf("entity_renderer").forGetter(pr -> Optional.ofNullable(pr.entityRenderer())),
            Identifier.CODEC.optionalFieldOf("screen_layout").forGetter(PowerRenderer::screenLayout),
            PalladiumCodecs.CONDITIONAL_IDENTIFIER_MAP.optionalFieldOf("render_layers", Collections.emptyMap()).forGetter(PowerRenderer::renderLayers),
            PalladiumCodecs.CONDITIONAL_IDENTIFIER_MAP.optionalFieldOf("trails", Collections.emptyMap()).forGetter(PowerRenderer::trails),
            PalladiumCodecs.CONDITIONAL_IDENTIFIER_MAP.optionalFieldOf("hud_layouts", Collections.emptyMap()).forGetter(PowerRenderer::hudLayouts),
            Codec.unboundedMap(Codec.STRING, Condition.CODEC).optionalFieldOf("hidden_model_parts", Collections.emptyMap()).forGetter(PowerRenderer::hiddenModelParts),
            Codec.unboundedMap(PalladiumAnimationLayer.CODEC, Identifier.CODEC).optionalFieldOf("animation_controller", Collections.emptyMap()).forGetter(PowerRenderer::animationControllers),
            AbilityBarSprites.CODEC.optionalFieldOf("ability_bar_textures", AbilityBarSprites.DEFAULT).forGetter(PowerRenderer::abilityBarSprites)
    ).apply(instance, (entityRenderer, screenLayout, renderLayers, trails, hudLayouts, hiddenModelParts, animationControllers, abilityBarSprites) ->
            new PowerRenderer(entityRenderer.orElse(null), screenLayout, renderLayers, trails, hudLayouts, hiddenModelParts, animationControllers, abilityBarSprites)));

    public @Nullable Identifier getAnimationController(PalladiumAnimationLayer layer) {
        return this.animationControllers.get(layer);
    }

    public record FirstPersonSettings(
            String right,
            String left,
            float scale
    ) {
        public static final FirstPersonSettings DEFAULT = new FirstPersonSettings(PowerRendererModel.RIGHT_ARM, PowerRendererModel.LEFT_ARM, 1F);

        public static final Codec<FirstPersonSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("right_bone", PowerRendererModel.RIGHT_ARM).forGetter(FirstPersonSettings::right),
                Codec.STRING.optionalFieldOf("left_bone", PowerRendererModel.LEFT_ARM).forGetter(FirstPersonSettings::left),
                ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("scale", 1F).forGetter(FirstPersonSettings::scale)
        ).apply(instance, FirstPersonSettings::new));

        public @Nullable String get(HumanoidArm arm) {
            return arm == HumanoidArm.RIGHT ? this.right() : this.left();
        }
    }

    public record ItemBones(
            String rightArm,
            String leftArm,
            String head
    ) {
        public static final ItemBones DEFAULT = new ItemBones(PowerRendererModel.RIGHT_ARM, PowerRendererModel.LEFT_ARM, PowerRendererModel.HEAD);

        public static final Codec<ItemBones> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("right_arm", PowerRendererModel.RIGHT_ARM).forGetter(ItemBones::rightArm),
                Codec.STRING.optionalFieldOf("left_arm", PowerRendererModel.LEFT_ARM).forGetter(ItemBones::leftArm),
                Codec.STRING.optionalFieldOf("head", PowerRendererModel.HEAD).forGetter(ItemBones::head)
        ).apply(instance, ItemBones::new));
    }

    public record TransitionSettings(
            int inDuration,
            int outDuration,
            @Nullable Identifier inAnimation,
            @Nullable Identifier outAnimation,
            boolean showDuringTransition,
            int animationOutBlend
    ) {
        public static final MapCodec<TransitionSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                PalladiumCodecs.TIME.optionalFieldOf("in_duration", 0).forGetter(TransitionSettings::inDuration),
                PalladiumCodecs.TIME.optionalFieldOf("out_duration", 0).forGetter(TransitionSettings::outDuration),
                Identifier.CODEC.optionalFieldOf("in_animation").forGetter(t -> Optional.ofNullable(t.inAnimation())),
                Identifier.CODEC.optionalFieldOf("out_animation").forGetter(t -> Optional.ofNullable(t.outAnimation())),
                Codec.BOOL.optionalFieldOf("show_during_transition", false).forGetter(TransitionSettings::showDuringTransition),
                PalladiumCodecs.TIME.optionalFieldOf("animation_out_blend", 0).forGetter(TransitionSettings::animationOutBlend)
        ).apply(instance, (in, out, inAnim, outAnim, show, outBlend) -> new TransitionSettings(in, out, inAnim.orElse(null), outAnim.orElse(null), show, outBlend)));
    }

    public record EntityRendererSettings(
            SkinTypedValue<ModelLayerLocationCodec> modelLayer,
            TextureReference texture,
            FirstPersonSettings firstPersonSettings,
            ItemBones itemBones,
            @Nullable TransitionSettings transition,
            Condition conditions,
            boolean applyVanillaMovement,
            Map<String, String> boneMapping,
            boolean enableHandItems,
            boolean enableHeadItem
    ) {
        public static final MapCodec<EntityRendererSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SkinTypedValue.codec(ModelLayerLocationCodec.CODEC).fieldOf("model_layer").forGetter(EntityRendererSettings::modelLayer),
                TextureReference.CODEC.fieldOf("texture").forGetter(EntityRendererSettings::texture),
                FirstPersonSettings.CODEC.optionalFieldOf("first_person", FirstPersonSettings.DEFAULT).forGetter(EntityRendererSettings::firstPersonSettings),
                ItemBones.CODEC.optionalFieldOf("item_bones", ItemBones.DEFAULT).forGetter(EntityRendererSettings::itemBones),
                TransitionSettings.MAP_CODEC.codec().optionalFieldOf("transition").forGetter(s -> Optional.ofNullable(s.transition())),
                Condition.CODEC.optionalFieldOf("conditions", TrueCondition.INSTANCE).forGetter(EntityRendererSettings::conditions),
                Codec.BOOL.optionalFieldOf("apply_vanilla_movement", false).forGetter(EntityRendererSettings::applyVanillaMovement),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("bone_mapping", Collections.emptyMap()).forGetter(EntityRendererSettings::boneMapping),
                Codec.BOOL.optionalFieldOf("enable_hand_items", true).forGetter(EntityRendererSettings::enableHandItems),
                Codec.BOOL.optionalFieldOf("enable_head_item", true).forGetter(EntityRendererSettings::enableHeadItem)
        ).apply(instance, (modelLayer, texture, arms, itemBones, transition, conditions, applyVanillaMovement, boneMapping, enableHandItems, enableHeadItem) ->
                new EntityRendererSettings(modelLayer, texture, arms, itemBones, transition.orElse(null), conditions, applyVanillaMovement, boneMapping, enableHandItems, enableHeadItem)));
    }
}
