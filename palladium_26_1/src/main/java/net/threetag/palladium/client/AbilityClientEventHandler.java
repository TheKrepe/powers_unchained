package net.threetag.palladium.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.attachment.PalladiumAttachments;
import net.threetag.palladium.client.renderer.entity.layer.pack.PackRenderLayer;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.client.renderer.power.PowerRendererLayer;
import net.threetag.palladium.client.util.PerspectiveAwareConditions;
import net.threetag.palladium.entity.sizechange.EntitySizeChangeHandler;
import net.threetag.palladium.mixin.client.LivingEntityRendererInvoker;
import net.threetag.palladium.network.ComboAttackMissPacket;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.ability.ForcedPerspectiveAbility;
import net.threetag.palladium.power.ability.IntangibilityAbility;
import net.threetag.palladium.util.PlayerUtil;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;

import static net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class AbilityClientEventHandler {

    public static float OVERRIDDEN_OPACITY = 1F;
    public static int OVERRIDDEN_TINT = -1;
    public static boolean AFTER_IMAGE = false;
    public static boolean AFTER_IMAGE_GHOST = false;
    public static boolean AFTER_IMAGE_GHOST_EDGES = false;
    private static int WALL_JUMP_TICKS = 0;
    private static BlockPos WALL_JUMP_DIRECTION = null;
    private static boolean CACHED_JUMP_KEY_PRESSED = false;
    @Nullable
    private static CameraType PERSPECTIVE_BEFORE_FORCED = null;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @SubscribeEvent
    static void preRenderLiving(RenderLivingEvent.Pre<?, ?, ?> e) {
        var state = e.getRenderState();
        float opacity = state.getRenderDataOrDefault(PalladiumRenderStateKeys.OPACITY, 1F);

        if (opacity <= 0F) {
            e.setCanceled(true);
            resetColorOverrides();
            return;
        }

        var poseStack = e.getPoseStack();
        var renderer = e.getRenderer();
        var prState = state.getRenderData(PalladiumRenderStateKeys.POWER_RENDERER);
        if (prState != null && prState.progress() >= 1.0f && prState.model() != null) {
            e.setCanceled(true);
            poseStack.pushPose();
            resetColorOverrides();

            if (state.hasPose(Pose.SLEEPING)) {
                Direction bedOrientation = state.bedOrientation;
                if (bedOrientation != null) {
                    float headOffset = state.eyeHeight - 0.1F;
                    poseStack.translate((float) (-bedOrientation.getStepX()) * headOffset, 0.0F, (float) (-bedOrientation.getStepZ()) * headOffset);
                }
            }

            var rendererInvoker = (LivingEntityRendererInvoker) renderer;
            float scale = state.scale;
            poseStack.scale(scale, scale, scale);
            rendererInvoker.invokeSetupRotations(state, poseStack, state.bodyRot, scale);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            PowerRendererLayer.applyBodyBone(poseStack, state);
            rendererInvoker.invokeScale(state, poseStack);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            boolean isBodyVisible = rendererInvoker.invokeIsBodyVisible(state);
            boolean forceTransparent = !isBodyVisible && !state.isInvisibleToPlayer;
            RenderType renderType = PowerRendererLayer.getRenderType(prState, isBodyVisible, forceTransparent, state.appearsGlowing());
            if (renderType != null) {
                int overlayCoords = getOverlayCoords(state, rendererInvoker.invokeGetWhiteOverlayProgress(state));
                int baseColor = forceTransparent ? 654311423 : -1;
                int tintedColor = ARGB.multiply(baseColor, rendererInvoker.invokeGetModelTint(state));
                PowerRendererLayer.renderPowerModel(prState, poseStack, e.getSubmitNodeCollector(), renderType, e.getRenderState().lightCoords, overlayCoords, tintedColor, e.getRenderState());
            }

            if (rendererInvoker.invokeShouldRenderLayers(state)) {
                PowerRendererLayer.renderHeldItems(prState, poseStack, e.getSubmitNodeCollector(), e.getRenderState().lightCoords, state);
                PowerRendererLayer.renderHeadItem(prState, poseStack, e.getSubmitNodeCollector(), e.getRenderState().lightCoords, state);
                EntityModel parentModel = new EntityModel(prState.model().root()) {
                };
                e.getRenderState().getRenderDataOrDefault(PalladiumRenderStateKeys.RENDER_LAYERS, Collections.emptyMap()).forEach((layer, layerState) -> {
                    PackRenderLayer.State s = layerState;
                    if (layer.shouldRender(s, PerspectiveAwareConditions.Perspective.THIRD_PERSON)) {
                        layer.submit(e.getPoseStack(), e.getSubmitNodeCollector(), parentModel,
                                e.getRenderState(), s,
                                e.getRenderState().lightCoords, e.getRenderState().partialTick, 0f, 0f);
                    }
                });
            }

            poseStack.popPose();
            return;
        }

        OVERRIDDEN_OPACITY = opacity;
        OVERRIDDEN_TINT = e.getRenderState().getRenderDataOrDefault(PalladiumRenderStateKeys.TINT, -1);
    }

    @SubscribeEvent
    static void postRenderLiving(RenderLivingEvent.Post<?, ?, ?> e) {
        resetColorOverrides();
    }

    @SubscribeEvent
    static void renderHand(RenderHandEvent e) {
        float opacity = 1F - AbilityUtil.getHighestAnimationTimerProgress(Minecraft.getInstance().player, AbilitySerializers.INVISIBILITY.get(), e.getPartialTick());

        if (opacity <= 0F) {
            e.setCanceled(true);
            resetColorOverrides();
        } else {
            OVERRIDDEN_OPACITY = opacity;
        }
    }

    public static void resetColorOverrides() {
        OVERRIDDEN_OPACITY = 1F;
        OVERRIDDEN_TINT = -1;
    }

    @SubscribeEvent
    static void clientTickPre(ClientTickEvent.Pre e) {
        EntitySizeChangeHandler.RENDER_PARTIAL_TICK = 1.0F;
    }

    @SubscribeEvent
    static void computeCameraAngles(ViewportEvent.ComputeCameraAngles e) {
        EntitySizeChangeHandler.RENDER_PARTIAL_TICK = (float) e.getPartialTick();
    }

    @SubscribeEvent
    static void renderFog(ViewportEvent.RenderFog e) {
        if (e.getCamera().entity() instanceof LivingEntity living) {
            if (AbilityUtil.isTypeEnabled(living, AbilitySerializers.INTANGIBILITY.get())) {
                if (IntangibilityAbility.getInWallBlockState(living) != null) {
                    e.setFarPlaneDistance(5F);
                    e.setNearPlaneDistance(1F);
                }
            }
        }
    }

    @SubscribeEvent
    static void fogColor(ViewportEvent.ComputeFogColor e) {
        if (e.getCamera().entity() instanceof LivingEntity living) {
            if (AbilityUtil.isTypeEnabled(living, AbilitySerializers.INTANGIBILITY.get())) {
                if (IntangibilityAbility.getInWallBlockState(living) != null) {
                    e.setRed(0F);
                    e.setGreen(0F);
                    e.setBlue(0F);
                }
            }
        }
    }

    @SubscribeEvent
    static void comboAttackMiss(PlayerInteractEvent.LeftClickEmpty e) {
        if (AbilityUtil.isTypeEnabled(e.getEntity(), AbilitySerializers.COMBO_ATTACK.get())) {
            ClientPacketDistributor.sendToServer(new ComboAttackMissPacket());
        }
    }

    @SubscribeEvent
    static void forcedPerspective(ClientTickEvent.Post e) {
        var mc = Minecraft.getInstance();

        if (mc.player == null) {
            PERSPECTIVE_BEFORE_FORCED = null;
            return;
        }

        var forced = AbilityUtil.getEnabledInstances(mc.player, AbilitySerializers.FORCED_PERSPECTIVE.get()).stream().findFirst();

        if (forced.isPresent()) {
            if (PERSPECTIVE_BEFORE_FORCED == null) {
                PERSPECTIVE_BEFORE_FORCED = mc.options.getCameraType();
            }

            mc.options.setCameraType(switch (forced.get().getAbility().perspective) {
                case FIRST_PERSON -> CameraType.FIRST_PERSON;
                case THIRD_PERSON_BACK -> CameraType.THIRD_PERSON_BACK;
                case THIRD_PERSON_FRONT -> CameraType.THIRD_PERSON_FRONT;
            });
        } else if (PERSPECTIVE_BEFORE_FORCED != null) {
            mc.options.setCameraType(PERSPECTIVE_BEFORE_FORCED);
            PERSPECTIVE_BEFORE_FORCED = null;
        }
    }

    @SubscribeEvent
    static void wallClimbing(ClientTickEvent.Post e) {
        var mc = Minecraft.getInstance();

        if (mc.player != null) {
            if (mc.player.getData(PalladiumAttachments.IS_CLIMBING.get()) && WALL_JUMP_TICKS <= 0) {
                mc.player.level().findSupportingBlock(mc.player, mc.player.getBoundingBox().inflate(0.2F, -0.2F, 0.2F)).ifPresent(blockPos -> {
                    var direction = mc.player.blockPosition().subtract(blockPos);
                    var toWallRot = (float) -Mth.wrapDegrees(Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())) + 180F);
                    mc.player.setYBodyRot(toWallRot);

                    float f = Mth.wrapDegrees(mc.player.getYRot() - toWallRot);
                    float f1 = Mth.clamp(f, -105.0F, 105.0F);
                    mc.player.yRotO += f1 - f;
                    mc.player.setYRot(mc.player.getYRot() + f1 - f);
                    mc.player.setYHeadRot(mc.player.getYRot());
                });
            }

            if (CACHED_JUMP_KEY_PRESSED != mc.options.keyJump.isDown()) {
                if (!CACHED_JUMP_KEY_PRESSED && mc.player.getData(PalladiumAttachments.IS_CLIMBING.get())) {
                    float f = mc.player.getJumpPower(1F);
                    if (!(f <= 1.0E-5F)) {
                        mc.player.level().findSupportingBlock(mc.player, mc.player.getBoundingBox().inflate(0.2F, -0.2F, 0.2F)).ifPresent(blockPos -> {
                            WALL_JUMP_DIRECTION = mc.player.blockPosition().subtract(blockPos);
                            WALL_JUMP_TICKS = 10;
                            var jumpAngle = (float) -Mth.wrapDegrees(Math.toDegrees(Math.atan2(WALL_JUMP_DIRECTION.getX(), WALL_JUMP_DIRECTION.getZ())));

                            mc.player.setYRot(jumpAngle);
                            mc.options.keyUp.consumeClick();
                            mc.player.needsSync = true;
                            CommonHooks.onLivingJump(mc.player);
                        });
                    }
                }
                CACHED_JUMP_KEY_PRESSED = !CACHED_JUMP_KEY_PRESSED;
            }


            if (!PlayerUtil.isFlying(mc.player) && WALL_JUMP_TICKS > 0) {
                float f = mc.player.getJumpPower(1F);
                float scale = WALL_JUMP_TICKS / 80F;
                mc.player.addDeltaMovement(new Vec3(WALL_JUMP_DIRECTION.getX() * f * scale, f * scale, WALL_JUMP_DIRECTION.getZ() * f * scale));
                WALL_JUMP_TICKS--;
            } else {
                WALL_JUMP_TICKS = 0;
                WALL_JUMP_DIRECTION = null;
            }
        }
    }

}
