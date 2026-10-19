package net.threetag.palladium.client.renderer.entity.state;

import com.google.common.reflect.TypeToken;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.animation.ModelAnimationDefinition;
import net.threetag.palladium.client.renderer.entity.layer.pack.ClientEntityRenderLayers;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.client.renderer.entity.layer.pack.VibrationPackRenderLayer;
import net.threetag.palladium.client.renderer.power.ClientPowerRendererHandler;
import net.threetag.palladium.client.renderer.power.PowerRendererRenderState;
import net.threetag.palladium.client.trail.ClientEntityTrailHandler;
import net.threetag.palladium.client.trail.Trail;
import net.threetag.palladium.client.util.ModelUtil;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.customization.EntityCustomizationHandler;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.flight.DefaultFlightType;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AimAbility;
import net.threetag.palladium.power.ability.OpacityChanging;

import java.util.*;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class PalladiumRenderStateKeys {

    public static ContextKey<Map<PackRenderLayer<PackRenderLayer.State>, PackRenderLayer.State>> RENDER_LAYERS = create("render_layers");
    public static ContextKey<Set<String>> HIDDEN_MODEL_PARTS = create("hidden_model_parts");
    public static ContextKey<Float[]> AIM = create("aim");
    public static ContextKey<Map<PlayerModelPart, Float>> SHRINK_OVERLAY = create("shrink_player_overlay");
    public static ContextKey<Map<DataContext, ModelAnimationDefinition>> ANIMATIONS = create("animations");
    public static ContextKey<Long> EYE_SELECTION = create("eye_selection");
    public static ContextKey<Float> IN_FLIGHT = create("in_flight");
    public static ContextKey<Float> FLIGHT_PROPULSION_SCALE = create("flight_propulsion_scale");
    public static ContextKey<Float> OPACITY = create("opacity");
    public static ContextKey<Integer> TINT = create("tint");
    public static ContextKey<Map<Trail, ClientEntityTrailHandler.TrailInstance>> TRAILS = create("trails");
    public static ContextKey<PowerRendererRenderState> POWER_RENDERER = create("power_renderer");

    private static <T> ContextKey<T> create(String name) {
        return new ContextKey<>(Palladium.id(name));
    }

    @SubscribeEvent
    static void add(RegisterRenderStateModifiersEvent e) {
        e.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {
        }, (entity, state) -> {
            Map<PackRenderLayer<PackRenderLayer.State>, PackRenderLayer.State> layerStates = new LinkedHashMap<>();
            ClientEntityRenderLayers layers = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.RENDER_LAYERS.get()) instanceof ClientEntityRenderLayers l ? l : null;
            if (layers != null) {
                layerStates.putAll(layers.getLayerStates());
            }

            ClientPowerRendererHandler prHandler = ClientPowerRendererHandler.get(entity);
            if (prHandler != null && prHandler.isActive()) {
                state.setRenderData(POWER_RENDERER, prHandler.createRenderState(entity, state.partialTick));
            }

            layerStates.forEach((layer, layerState) -> layerState.extractRenderState(layer, layerState.getContext()));
            state.setRenderData(RENDER_LAYERS, layerStates);

            Set<String> hiddenParts = new HashSet<>(ModelUtil.getHiddenModelPartNames(entity));
            if (prHandler != null && prHandler.isActive() && prHandler.getActiveRenderer() != null) {
                DataContext context = DataContext.forEntity(entity);
                prHandler.getActiveRenderer().hiddenModelParts().forEach((partName, condition) -> {
                    if (condition.test(context)) hiddenParts.add(partName);
                });
            }
            state.setRenderData(HIDDEN_MODEL_PARTS, hiddenParts);
            state.setRenderData(AIM, AimAbility.getTimer(entity, state.partialTick));
            Map<PlayerModelPart, Float> shrinkProgress = layers != null ? layers.getShrinkProgress() : Collections.emptyMap();
            state.setRenderData(SHRINK_OVERLAY, shrinkProgress.isEmpty() ? Collections.emptyMap() : new EnumMap<>(shrinkProgress));
            state.setRenderData(EYE_SELECTION, EntityCustomizationHandler.get(entity).getEyeSelection());
            state.setRenderData(TINT, -1);
            var trails = ClientEntityTrailHandler.get(entity).getTrails();
            if (entity instanceof AbstractClientPlayer player && !ClientEntityTrailHandler.COMPUTING_LIVE_ANCHOR) {
                ClientEntityTrailHandler.COMPUTING_LIVE_ANCHOR = true;
                try {
                    trails.values().forEach(instance -> instance.updateLiveAnchor(player, state.partialTick));
                } finally {
                    ClientEntityTrailHandler.COMPUTING_LIVE_ANCHOR = false;
                }
            }
            state.setRenderData(TRAILS, trails);

            // Flight
            var flightHandler =  EntityFlightHandler.get(entity);
            state.setRenderData(IN_FLIGHT, flightHandler.getInFlightTimer(state.partialTick));
            state.setRenderData(FLIGHT_PROPULSION_SCALE, flightHandler.getAnimationHandler() instanceof DefaultFlightType.AnimationHandler a ? a.getPropulsionScale(state.partialTick) : 0F);

            // Opacity
            float opacity = OpacityChanging.getOpacity(entity, entity instanceof LocalPlayer, state.partialTick);
            state.setRenderData(OPACITY, opacity);

            if (opacity < 1.0F) {
                List<EntityRenderState.ShadowPiece> newShadows = new ArrayList<>();
                for (EntityRenderState.ShadowPiece shadow : state.shadowPieces) {
                    newShadows.add(new EntityRenderState.ShadowPiece(shadow.relativeX(), shadow.relativeY(), shadow.relativeZ(), shadow.shapeBelow(), shadow.alpha() * opacity));
                }
                state.shadowPieces.clear();
                state.shadowPieces.addAll(newShadows);
            }

            // Remove Item on head rendering when render layers are active
            ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
            boolean hasHeadLayers = headItem.has(PalladiumDataComponents.Items.RENDER_LAYERS)
                    || MultiversalItemVariant.hasRenderLayers(entity.registryAccess(), headItem, PlayerSlot.get(EquipmentSlot.HEAD));
            if (!(headItem.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof AbstractSkullBlock) && hasHeadLayers) {
                state.wornHeadType = null;
                state.wornHeadProfile = null;
                state.headItem.clear();
            }

            // Animations
            Map<DataContext, ModelAnimationDefinition> animations = new HashMap<>();
            state.setRenderData(ANIMATIONS, animations);
        });
    }

    public static <AvatarLikeEntity extends Avatar & ClientAvatarEntity> void manipulatePlayerState(AvatarLikeEntity player, AvatarRenderState state, float partialTick) {
        var flightHandler = EntityFlightHandler.get(player);
        var flightProgress = flightHandler.getInFlightTimer(partialTick);

        if (flightProgress > 0.0F) {
            state.walkAnimationSpeed = Mth.lerp(flightProgress, state.walkAnimationSpeed, 0F);
        }

        if (flightHandler.getAnimationHandler() instanceof DefaultFlightType.AnimationHandler flight) {
            var scale = flight.getPropulsionScale(partialTick);

            if (scale > 0F) {
                ClientAvatarState clientavatarstate = player.avatarState();
                double d0 = clientavatarstate.getInterpolatedCloakX(partialTick) - Mth.lerp(partialTick, player.xo, player.getX());
                double d2 = clientavatarstate.getInterpolatedCloakZ(partialTick) - Mth.lerp(partialTick, player.zo, player.getZ());
                float f = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
                double d3 = Mth.sin(f * (float) (Math.PI / 180.0));
                double d4 = -Mth.cos(f * (float) (Math.PI / 180.0));
                state.capeLean = (float) (d0 * d3 + d2 * d4) * 100.0F;
                state.capeLean = state.capeLean * (1.0F - scale);
                state.capeLean = Mth.clamp(state.capeLean, 0.0F, 150.0F);
            }
        }
    }

}
