package net.threetag.palladium.client.model.animation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.entity.PlayerModelCacheExtension;
import net.threetag.palladium.event.PalladiumClientEvents;

import java.util.*;
import java.util.function.BiConsumer;

@Environment(EnvType.CLIENT)
public class PalladiumAnimationRegistry extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private final Map<ResourceLocation, PalladiumAnimation> animations = new LinkedHashMap<>();
    private final List<PalladiumAnimation> animationsSorted = new LinkedList<>();
    public static boolean SKIP_ANIMATIONS = false;
    public static final PalladiumAnimationRegistry INSTANCE = new PalladiumAnimationRegistry();

    public PalladiumAnimationRegistry() {
        super(GSON, "palladium/animations");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.animations.clear();
        this.animationsSorted.clear();

        PalladiumClientEvents.REGISTER_ANIMATIONS.invoker().register(this::registerAnimation);

        this.animationsSorted.addAll(this.animations.values());
        this.animationsSorted.sort(Comparator.comparingInt(PalladiumAnimation::getPriority));
    }

    public void registerAnimation(ResourceLocation id, PalladiumAnimation animation) {
        animations.put(id, animation);
    }

    public static void resetPoses(Iterable<ModelPart> headParts, Iterable<ModelPart> bodyParts) {
        for (ModelPart bodyPart : headParts) {
            bodyPart.resetPose();
        }

        for (ModelPart bodyPart : bodyParts) {
            bodyPart.resetPose();
        }
    }

    public static void forEach(AbstractClientPlayer player, HumanoidModel<?> model, PalladiumAnimation.FirstPersonContext firstPersonContext, float partialTicks, BiConsumer<PalladiumAnimation.PlayerModelPart, PalladiumAnimation.PartAnimationData> consumer) {
        Map<PalladiumAnimation.PlayerModelPart, PalladiumAnimation.PartAnimationData> gathered = new HashMap<>();

        for (PalladiumAnimation animation : INSTANCE.animationsSorted) {
            PalladiumAnimation.Builder builder = new PalladiumAnimation.Builder();
            animation.animate(builder, player, model, firstPersonContext, partialTicks);
            for (Map.Entry<PalladiumAnimation.PlayerModelPart, PalladiumAnimation.PartAnimationData> entry : builder.getAnimationData().entrySet()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void forEach(AbstractClientPlayer player, HumanoidModel<?> model, PalladiumAnimation.FirstPersonContext firstPersonContext, float partialTicks, BiConsumer<PalladiumAnimation.PlayerModelPart, PalladiumAnimation.PartAnimationData> consumer, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for (PalladiumAnimation animation : INSTANCE.animationsSorted) {
            PalladiumAnimation.Builder builder = new PalladiumAnimation.Builder(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            animation.animate(builder, player, model, firstPersonContext, partialTicks);
            for (Map.Entry<PalladiumAnimation.PlayerModelPart, PalladiumAnimation.PartAnimationData> entry : builder.getAnimationData().entrySet()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void applyAnimations(HumanoidModel<?> model, LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof AbstractClientPlayer player) {
            forEach(player, model, PalladiumAnimation.FirstPersonContext.NONE, Minecraft.getInstance().getFrameTime(), (part, data) -> {
                part.applyToModelPart(model, data);
            }, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }

    public static void applyFirstPersonAnimations(PoseStack poseStack, AbstractClientPlayer player, HumanoidModel<?> model, boolean rightArm) {
        PalladiumAnimation.PoseStackResult result = new PalladiumAnimation.PoseStackResult();
        forEach(player, model, rightArm ? PalladiumAnimation.FirstPersonContext.RIGHT_ARM : PalladiumAnimation.FirstPersonContext.LEFT_ARM, Minecraft.getInstance().getFrameTime(), (part, data) -> {
            if (rightArm && part == PalladiumAnimation.PlayerModelPart.RIGHT_ARM) {
                data.apply(result);
            }

            if (!rightArm && part == PalladiumAnimation.PlayerModelPart.LEFT_ARM) {
                data.apply(result);
            }
        });
        result.apply(poseStack);
    }

    public static PalladiumAnimation.PoseStackResult setupRotations(PlayerRenderer playerRenderer, AbstractClientPlayer player, PoseStack poseStack, float partialTicks) {
        var result = getBodyAnimation(player, playerRenderer.getModel(), partialTicks);
        result.apply(poseStack);
        return result;
    }

    public static PalladiumAnimation.PoseStackResult getBodyAnimation(AbstractClientPlayer player, HumanoidModel<?> model, float partialTicks) {
        PalladiumAnimation.PoseStackResult result = new PalladiumAnimation.PoseStackResult();
        forEach(player, model, PalladiumAnimation.FirstPersonContext.NONE, partialTicks, (part, data) -> {
            if (part == PalladiumAnimation.PlayerModelPart.BODY) {
                data.apply(result);
            }
        });
        return result;
    }

    public static void updateCachedModel(AbstractClientPlayer player, float partialTicks) {
        var minecraft = Minecraft.getInstance();

        if (player != minecraft.player || !minecraft.options.getCameraType().isFirstPerson()
                || !(player instanceof PlayerModelCacheExtension ext)) {
            return;
        }

        var model = ext.palladium$getCachedModel();
        float bodyRot = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float headRot = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
        float limbSwing = 0.0F;
        float limbSwingAmount = 0.0F;

        if (!player.isPassenger() && player.isAlive()) {
            limbSwingAmount = Math.min(player.walkAnimation.speed(partialTicks), 1.0F);
            limbSwing = player.walkAnimation.position(partialTicks);

            if (player.isBaby()) {
                limbSwing *= 3.0F;
            }
        }

        if (player.isPassenger() && player.getVehicle() instanceof LivingEntity vehicle) {
            bodyRot = Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot);
            float clamped = Mth.clamp(Mth.wrapDegrees(headRot - bodyRot), -85.0F, 85.0F);
            bodyRot = headRot - clamped;

            if (clamped * clamped > 2500.0F) {
                bodyRot += clamped * 0.2F;
            }
        }

        float netHeadYaw = headRot - bodyRot;
        float headPitch = Mth.lerp(partialTicks, player.xRotO, player.getXRot());

        if (LivingEntityRenderer.isEntityUpsideDown(player)) {
            netHeadYaw *= -1.0F;
            headPitch *= -1.0F;
        }

        float ageInTicks = player.tickCount + partialTicks;
        model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (!SKIP_ANIMATIONS) {
            applyAnimations(model, player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }

        ext.palladium$setBodyAnimationResult(getBodyAnimation(player, model, partialTicks));
    }

}
