package net.threetag.palladium.client.renderer.entity.layer.pack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.client.renderer.power.ClientPowerRendererHandler;
import net.threetag.palladium.client.util.PerspectiveAwareConditions.Perspective;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PlayerUtil;

import java.util.List;

public abstract class PackRenderLayer<T extends PackRenderLayer.State> {

    protected final PackRenderLayerProperties properties;

    protected PackRenderLayer(PackRenderLayerProperties properties) {
        this.properties = properties;
    }

    public abstract void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, EntityModel<LivingEntityRenderState> parentModel, LivingEntityRenderState entityState, T layerState, int packedLight, float partialTick, float xRot, float yRot);

    public abstract void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, HumanoidArm arm, ModelPart armPart, AvatarRenderer<?> playerRenderer, T layerState, int packedLight);

    public boolean shouldRender(State state, Perspective perspective) {
        return state.isCachedVisible(perspective);
    }

    public State createState(DataContext context) {
        return new State(this, context);
    }

    public State createExtractedState(DataContext context) {
        State state = this.createState(context);
        state.extractRenderState(this, context);
        return state;
    }

    public boolean isOrContains(PackRenderLayer<?> layer) {
        return this == layer;
    }

    public final PackRenderLayerProperties getProperties() {
        return this.properties;
    }

    public abstract PackRenderLayerSerializer<?> getSerializer();

    protected static <T extends PackRenderLayer.State, B extends PackRenderLayer<T>> RecordCodecBuilder<B, PackRenderLayerProperties> propertiesCodec() {
        return PackRenderLayerProperties.CODEC.forGetter(l -> l.properties);
    }

    public static class State {

        public final PackRenderLayer<?> renderLayer;
        private DataContext context;
        public int ticks = 0;
        protected boolean markedForRemoval = false;

        public boolean isSlim = false;
        private boolean visibleThirdPerson = true;
        private boolean visibleFirstPerson = true;

        public State(PackRenderLayer<?> renderLayer, DataContext context) {
            this.renderLayer = renderLayer;
            this.context = context;
        }

        public void tick(LivingEntity entity) {
            this.ticks++;
        }

        public void updateContext(DataContext context) {
            this.context = context;
        }

        public DataContext getContext() {
            return this.context;
        }

        public void markForRemoval() {
            this.markedForRemoval = true;
        }

        public boolean isMarkedForRemoval() {
            return this.markedForRemoval;
        }

        public void extractRenderState(PackRenderLayer<?> layer, DataContext context) {
            var conditions = layer.getProperties().conditions();
            this.isSlim = context.getEntity() instanceof Player player && PlayerUtil.hasSmallArms(player);
            boolean matches = matchesActiveRenderer(layer, context);
            this.visibleThirdPerson = matches && conditions.test(context, Perspective.THIRD_PERSON);
            this.visibleFirstPerson = matches && conditions.test(context, Perspective.FIRST_PERSON);
        }

        private static boolean matchesActiveRenderer(PackRenderLayer<?> layer, DataContext context) {
            List<Identifier> renderers = layer.getProperties().renderers();
            Identifier active = null;
            if (context.getEntity() != null) {
                ClientPowerRendererHandler handler = ClientPowerRendererHandler.get(context.getEntity());
                if (handler != null) {
                    active = handler.getVisibleModelRendererPowerId();
                }
            }
            return active == null ? renderers.isEmpty() : renderers.contains(active);
        }

        public boolean isCachedVisible(Perspective perspective) {
            return perspective == Perspective.FIRST_PERSON
                    ? this.visibleFirstPerson
                    : this.visibleThirdPerson;
        }

    }

    public static class Codecs {

        public static final Codec<PackRenderLayer<?>> DIRECT_CODEC = PackRenderLayerSerializer.TYPE_CODEC.dispatch(PackRenderLayer::getSerializer, PackRenderLayerSerializer::codec);

        public static Codec<PackRenderLayer<?>> SIMPLE_CODEC = Codec.withAlternative(DIRECT_CODEC, DefaultPackRenderLayer.CODEC.codec());

    }

}
