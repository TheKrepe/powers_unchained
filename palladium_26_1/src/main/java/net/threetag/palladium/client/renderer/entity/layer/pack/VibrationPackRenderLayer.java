package net.threetag.palladium.client.renderer.entity.layer.pack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.renderer.VibrationRenderer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PalladiumCodecs;

public class VibrationPackRenderLayer extends PackRenderLayer<VibrationPackRenderLayer.State> {

    public static final MapCodec<VibrationPackRenderLayer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("intensity").forGetter(l -> l.intensity),
            PalladiumCodecs.TIME.optionalFieldOf("fade_ticks", 20).forGetter(l -> l.fadeTicks),
            propertiesCodec()
    ).apply(instance, VibrationPackRenderLayer::new));

    public final int intensity;
    public final int fadeTicks;

    public VibrationPackRenderLayer(int intensity, int fadeTicks, PackRenderLayerProperties properties) {
        super(properties);
        this.intensity = intensity;
        this.fadeTicks = fadeTicks;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, EntityModel<LivingEntityRenderState> parentModel, LivingEntityRenderState entityState, State layerState, int packedLight, float partialTick, float xRot, float yRot) {
        // nothing
    }

    @Override
    public void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, HumanoidArm arm, ModelPart armPart, AvatarRenderer<?> playerRenderer, State layerState, int packedLight) {

    }

    @Override
    public PackRenderLayer.State createState(DataContext context) {
        return new State(this, context);
    }

    @Override
    public PackRenderLayerSerializer<?> getSerializer() {
        return PackRenderLayerSerializers.VIBRATION;
    }

    public static class State extends PackRenderLayer.State {

        private final VibrationPackRenderLayer renderLayer;
        private int progress;
        private int prevProgress;

        public State(VibrationPackRenderLayer renderLayer, DataContext context) {
            super(renderLayer, context);
            this.renderLayer = renderLayer;
        }

        @Override
        public void tick(LivingEntity entity) {
            super.tick(entity);
            this.prevProgress = this.progress;

            if (this.markedForRemoval && this.progress > 0) {
                this.progress--;
            } else if (!this.markedForRemoval && this.progress < this.renderLayer.fadeTicks) {
                this.progress++;
            }
        }

        @Override
        public boolean isMarkedForRemoval() {
            return super.isMarkedForRemoval() && this.progress == 0 && this.prevProgress == 0;
        }

        public float getProgress(float partialTick) {
            return Mth.lerp(partialTick, this.prevProgress, this.progress);
        }
    }

    public static class Serializer extends PackRenderLayerSerializer<VibrationPackRenderLayer> {

        @Override
        public MapCodec<VibrationPackRenderLayer> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<PackRenderLayer<? extends PackRenderLayer.State>, VibrationPackRenderLayer> builder, HolderLookup.Provider provider) {
            builder.setName("Vibration")
                    .setDescription("Renders a vibration effect on the entity.")
                    .add("intensity", TYPE_INT, "The intensity of the vibration, i.e. how many offset copies of the entity get blended in. Values above " + VibrationRenderer.MAX_SAMPLES + " are capped.")
                    .add("fade_ticks", TYPE_TIME, "The time/ticks it needs for the vibration to fade in/out.")
                    .addExampleObject(new VibrationPackRenderLayer(
                            10, 20,
                            PackRenderLayerProperties.DEFAULT
                    ));
        }
    }

}
