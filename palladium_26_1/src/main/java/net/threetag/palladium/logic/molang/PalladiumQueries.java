package net.threetag.palladium.logic.molang;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.attachment.PalladiumAttachments;
import net.threetag.palladium.documentation.MoLangQueryDocumentationBuilder;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.entity.flight.SwingingFlightType;
import net.threetag.palladium.event.RegisterMoLangQueriesEvent;
import net.threetag.palladium.power.ability.AbilityReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import team.unnamed.mocha.runtime.binding.Binding;
import team.unnamed.mocha.runtime.value.Function;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.threetag.palladium.logic.molang.VanillaQueries.HEAD_X_ROTATION;
import static net.threetag.palladium.logic.molang.VanillaQueries.HEAD_Y_ROTATION;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class PalladiumQueries implements ObjectValue {

    @SubscribeEvent
    static void registerMoLang(RegisterMoLangQueriesEvent e) {
        e.register(Palladium.MOD_ID, PalladiumQueries.class, PalladiumQueries::new, PalladiumQueries::addDocumentation);
    }

    static void addDocumentation(MoLangQueryDocumentationBuilder builder) {
        builder.setDescription("Queries added by Palladium, for its own features and for values that are useful in player animations.")
                .add(HEAD_X_ROTATION, "The head pitch in degrees. Negative when looking up.")
                .add(HEAD_Y_ROTATION, "The head yaw relative to the body, in degrees from -180 to 180. Unlike query.head_y_rotation this is 0 when the player looks straight ahead, which is what a head bone in an animation needs.")
                .add("tick_count", "The entity's age in ticks, including the progress to the next tick for smooth motion.")
                .add("get_animation_timer_eased", "The eased animation timer of an ability, from 0 to 1. 0 if the ability is not found.",
                        MoLangQueryDocumentationBuilder.argument("ability", "Ability reference in the form \"namespace:power#ability\"."))
                .add("horizontal_speed", "The horizontal distance the entity moved since the last tick, in blocks. Vertical movement is ignored.")
                .add("model_property", "A value of a model part. Only works inside the overrides of a render layer, returns 0 everywhere else.",
                        MoLangQueryDocumentationBuilder.argument("part", "Name of the model part, like \"head\" or \"right_arm\"."),
                        MoLangQueryDocumentationBuilder.argument("property", "One of x, y, z, x_rot, y_rot, z_rot (radians), x_rot_degrees, y_rot_degrees, z_rot_degrees, x_scale, y_scale, z_scale."))
                .add("flight_roll", "The roll of the current flight animation, in degrees. 0 when not flying.")
                .add("flight_pitch", "The pitch of the current flight animation, in degrees. 0 when not flying.")
                .add("flight_yaw", "The yaw of the current flight animation, in degrees. 0 when not flying.")
                .add("flight_limb_pitch", "The pitch the arms and legs should follow during flight, in degrees.")
                .add("flight_limb_roll", "The roll the arms and legs should follow during flight, in degrees.")
                .add("flight_propulsion_scale", "How far the propelled (fast) flight pose is blended in, from 0 to 1.")
                .add("flight_yaw_delta", "How fast the flying entity is turning, smoothed. Useful to lean into curves.")
                .add("flight_speed_forward", "Forward speed during flight relative to the flight speed, from -1 (backwards) to 1.")
                .add("flight_speed_sideways", "Sideways speed during flight relative to the flight speed, from -1 to 1.")
                .add("flight_phase", "The phase of the active flight type. Default flight: 0 none, 1 hovering, 2 moving, 3 propelled, 4 descending, 5 landing. Swinging: 0 none, 1 launching, 2 swinging, 3 released, 4 diving, 5 landing.")
                .add("swinging_phase", "The phase of swinging flight: 0 none, 1 launching, 2 swinging, 3 released, 4 diving, 5 landing. 0 for other flight types.")
                .add("swinging_is_hanging", "Whether the player hangs still on a web line.")
                .add("swinging_on_right_arm", "Whether the current web line is attached to the right arm.")
                .add("swinging_on_left_arm", "Whether the current web line is attached to the left arm.")
                .add("swinging_right_arm_pitch", "A physics based pitch for the right arm while swinging, in degrees. Points up at the anchor when the arm holds the line.")
                .add("swinging_left_arm_pitch", "A physics based pitch for the left arm while swinging, in degrees. Points up at the anchor when the arm holds the line.")
                .add("swinging_limb_vel", "How fast the swinging limbs are currently moving. Useful for secondary motion like legs trailing behind.")
                .add("any_animation_finished", "Whether the animation of the current animation controller state has finished. Only works in animation controller transitions.")
                .add("gliding_tick_count", "Ticks spent gliding with an elytra. 0 when not gliding.")
                .add("is_wall_climbing", "Whether the player is climbing a wall with Palladium's wall climbing ability.")
                .add("pos_x", "The entity's X position in the world, smoothed between ticks.")
                .add("pos_y", "The entity's Y position in the world, smoothed between ticks.")
                .add("pos_z", "The entity's Z position in the world, smoothed between ticks.")
                .add("attack_time", "Progress of the current attack swing, from 0 to 1. 0 when not attacking.")
                .add("attack_on_right_arm", "Whether an attack swing is playing and the right arm is the main arm.")
                .add("attack_on_left_arm", "Whether an attack swing is playing and the left arm is the main arm.")
                .add("attack_swing_x", "The X rotation in degrees the attacking arm gets in the vanilla swing animation. Lets animations recreate the swing.")
                .add("attack_swing_y", "The Y rotation in degrees the attacking arm gets in the vanilla swing animation.")
                .add("attack_swing_z", "The Z rotation in degrees the attacking arm gets in the vanilla swing animation.")
                .add("attack_body_yaw", "The body twist in degrees during the vanilla attack swing.");
    }

    private final EntityContext context;
    private final Map<String, Supplier<Object>> functions = new HashMap<>();

    public PalladiumQueries(EntityContext context) {
        this.context = context;
        this.functions.put(HEAD_X_ROTATION, this::head_x_rotation);
        this.functions.put(HEAD_Y_ROTATION, this::head_y_rotation);
        this.functions.put("tick_count", this::tick_count);
        this.functions.put("horizontal_speed", this::horizontal_speed);
        this.functions.put("flight_roll", this::flight_roll);
        this.functions.put("flight_pitch", this::flight_pitch);
        this.functions.put("flight_yaw", this::flight_yaw);
        this.functions.put("flight_limb_pitch", this::flight_limb_pitch);
        this.functions.put("flight_limb_roll", this::flight_limb_roll);
        this.functions.put("flight_propulsion_scale", this::flight_propulsion_scale);
        this.functions.put("flight_yaw_delta", this::flight_yaw_delta);
        this.functions.put("flight_speed_forward", this::flight_speed_forward);
        this.functions.put("flight_speed_sideways", this::flight_speed_sideways);
        this.functions.put("flight_phase", this::flight_phase);
        this.functions.put("swinging_phase", this::swinging_phase);
        this.functions.put("swinging_is_hanging", this::swinging_is_hanging);
        this.functions.put("swinging_on_right_arm", this::swinging_on_right_arm);
        this.functions.put("swinging_on_left_arm", this::swinging_on_left_arm);
        this.functions.put("swinging_right_arm_pitch", this::swinging_right_arm_pitch);
        this.functions.put("swinging_left_arm_pitch", this::swinging_left_arm_pitch);
        this.functions.put("swinging_limb_vel", this::swinging_limb_vel);
        this.functions.put("any_animation_finished", this::any_animation_finished);
        this.functions.put("gliding_tick_count", this::gliding_tick_count);
        this.functions.put("is_wall_climbing", this::is_wall_climbing);
        this.functions.put("pos_x", this::pos_x);
        this.functions.put("pos_y", this::pos_y);
        this.functions.put("pos_z", this::pos_z);
        this.functions.put("attack_time", this::attack_time);
        this.functions.put("attack_on_right_arm", this::attack_on_right_arm);
        this.functions.put("attack_on_left_arm", this::attack_on_left_arm);
        this.functions.put("attack_swing_x", this::attack_swing_x);
        this.functions.put("attack_swing_y", this::attack_swing_y);
        this.functions.put("attack_swing_z", this::attack_swing_z);
        this.functions.put("attack_body_yaw", this::attack_body_yaw);
    }

    @Override
    public @NotNull Value get(@NonNull String name) {
        if (name.equals("get_animation_timer_eased")) {
            return (Function<?>) (ctx, args) -> Value.of(get_animation_timer_eased(args.next().toString()));
        } else if (name.equals("model_property")) {
            return (Function<?>) (ctx, args) -> Value.of(model_property(args.next().toString(), args.next().toString()));
        } else if (this.functions.containsKey(name)) {
            return Value.of(this.functions.get(name).get());
        }
        return Value.nil();
    }

    @Binding(HEAD_X_ROTATION)
    public double head_x_rotation() {
        return context.entity().getViewXRot(context.partialTick());
    }

    @Binding(HEAD_Y_ROTATION)
    public double head_y_rotation() {
        if (context.entity() instanceof LivingEntity living) {
            float f = Mth.rotLerp(context.partialTick(), living.yHeadRotO, living.yHeadRot);
            float bodyRot = LivingEntityRenderer.solveBodyRot(living, f, context.partialTick());
            return Mth.wrapDegrees(f - bodyRot);
        }

        return context.entity().getViewYRot(context.partialTick());
    }

    @Binding("tick_count")
    public double tick_count() {
        return context.entity().tickCount + context.partialTick();
    }

    @Binding("get_animation_timer_eased")
    public double get_animation_timer_eased(String abilityRef) {
        if (context.entity() instanceof LivingEntity living && abilityRef != null) {
            var ability = AbilityReference.parse(abilityRef).getInstance(living);

            if (ability != null) {
                return ability.getAnimationTimerProgressEased(context.partialTick());
            }
        }

        return 0F;
    }

    @Binding("horizontal_speed")
    public double horizontal_speed() {
        return context.entity().position().subtract(context.entity().oldPosition()).horizontalDistance();
    }

    @Binding("model_property")
    public double model_property(String bone, String type) {
        return this.context.getModelValue(bone, type);
    }

    @Binding("flight_roll")
    public double flight_roll() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getRoll(context.partialTick()) : 0;
    }

    @Binding("flight_pitch")
    public double flight_pitch() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getPitch(context.partialTick()) : 0;
    }

    @Binding("flight_yaw")
    public double flight_yaw() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getYaw(context.partialTick()) : 0;
    }

    @Binding("flight_limb_pitch")
    public double flight_limb_pitch() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getLimbPitch(context.partialTick()) : 0;
    }

    @Binding("flight_limb_roll")
    public double flight_limb_roll() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getLimbRoll(context.partialTick()) : 0;
    }

    @Binding("flight_propulsion_scale")
    public double flight_propulsion_scale() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getPropulsionScale(context.partialTick()) : 0;
    }

    @Binding("flight_yaw_delta")
    public double flight_yaw_delta() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getYawDelta(context.partialTick()) : 0;
    }

    @Binding("flight_speed_forward")
    public double flight_speed_forward() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getForwardMotion(context.partialTick()) : 0;
    }

    @Binding("flight_speed_sideways")
    public double flight_speed_sideways() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation != null ? animation.getSidewaysMotion(context.partialTick()) : 0;
    }

    @Binding("flight_phase")
    public double flight_phase() {
        if (context.entity() instanceof LivingEntity living) {
            var controller = EntityFlightHandler.get(living).getController();

            if (controller != null) {
                return controller.getPhaseId();
            }
        }
        return 0;
    }

    @Binding("swinging_phase")
    public double swinging_phase() {
        if (context.entity() instanceof LivingEntity living
                && EntityFlightHandler.get(living).getController() instanceof SwingingFlightType.Controller controller) {
            return controller.getPhase().getId();
        }
        return 0;
    }

    @Binding("swinging_is_hanging")
    public boolean swinging_is_hanging() {
        if (context.entity() instanceof LivingEntity living
                && EntityFlightHandler.get(living).getController() instanceof SwingingFlightType.Controller controller
                && controller.getPhase() == SwingingFlightType.Phase.SWINGING) {
            return controller.isHanging()
                    || context.entity().position().subtract(context.entity().oldPosition()).lengthSqr() < 0.01;
        }
        return false;
    }

    @Binding("swinging_on_right_arm")
    public boolean swinging_on_right_arm() {
        if (context.entity() instanceof LivingEntity living
                && EntityFlightHandler.get(living).getController() instanceof SwingingFlightType.Controller controller) {
            return controller.rightArm;
        }
        return false;
    }

    @Binding("swinging_on_left_arm")
    public boolean swinging_on_left_arm() {
        if (context.entity() instanceof LivingEntity living
                && EntityFlightHandler.get(living).getController() instanceof SwingingFlightType.Controller controller) {
            return !controller.rightArm;
        }
        return false;
    }

    @Binding("swinging_right_arm_pitch")
    public double swinging_right_arm_pitch() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation instanceof SwingingFlightType.AnimationHandler swinging
                ? swinging.getRightArmPitch(context.partialTick())
                : 0;
    }

    @Binding("swinging_left_arm_pitch")
    public double swinging_left_arm_pitch() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation instanceof SwingingFlightType.AnimationHandler swinging
                ? swinging.getLeftArmPitch(context.partialTick())
                : 0;
    }

    @Binding("swinging_limb_vel")
    public double swinging_limb_vel() {
        var animation = context.entity() instanceof LivingEntity living
                ? EntityFlightHandler.get(living).getAnimationHandler()
                : null;
        return animation instanceof SwingingFlightType.AnimationHandler swinging
                ? swinging.getLimbPitchVel(context.partialTick())
                : 0;
    }

    @Binding("any_animation_finished")
    public boolean any_animation_finished() {
        return this.context.hasAnimationFinished();
    }

    @Binding("gliding_tick_count")
    public double gliding_tick_count() {
        return this.context.entity() instanceof LivingEntity living ? living.getFallFlyingTicks() : 0;
    }

    @Binding("is_wall_climbing")
    public boolean is_wall_climbing() {
        return this.context.entity() instanceof Player player && player.getData(PalladiumAttachments.IS_CLIMBING.get());
    }

    @Binding("pos_x")
    public double pos_x() {
        return Mth.lerp(this.context.partialTick(), this.context.entity().xo, this.context.entity().getX());
    }

    @Binding("pos_y")
    public double pos_y() {
        return Mth.lerp(this.context.partialTick(), this.context.entity().yo, this.context.entity().getY());
    }

    @Binding("pos_z")
    public double pos_z() {
        return Mth.lerp(this.context.partialTick(), this.context.entity().zo, this.context.entity().getZ());
    }

    @Binding("attack_time")
    public double attack_time() {
        return context.entity() instanceof LivingEntity living ? living.getAttackAnim(context.partialTick()) : 0;
    }

    @Binding("attack_on_right_arm")
    public boolean attack_on_right_arm() {
        return attack_time() > 0D && context.entity() instanceof LivingEntity living && living.getMainArm() == HumanoidArm.RIGHT;
    }

    @Binding("attack_on_left_arm")
    public boolean attack_on_left_arm() {
        return attack_time() > 0D &&  context.entity() instanceof LivingEntity living && living.getMainArm() == HumanoidArm.LEFT;
    }

    private float attackBodyYawRad() {
        if (!(context.entity() instanceof LivingEntity living)) {
            return 0F;
        }

        float attackTime = living.getAttackAnim(context.partialTick());

        if (attackTime <= 0F) {
            return 0F;
        }

        float yaw = Mth.sin(Mth.sqrt(attackTime) * (float) (Math.PI * 2)) * 0.2F;
        return living.getMainArm() == HumanoidArm.LEFT ? -yaw : yaw;
    }

    @Binding("attack_body_yaw")
    public double attack_body_yaw() {
        return Math.toDegrees(this.attackBodyYawRad());
    }

    @Binding("attack_swing_x")
    public double attack_swing_x() {
        if (!(context.entity() instanceof LivingEntity living)) {
            return 0;
        }

        float attackTime = living.getAttackAnim(context.partialTick());

        if (attackTime <= 0F) {
            return 0;
        }

        float inverse = 1F - attackTime;
        float eased = 1F - inverse * inverse * inverse * inverse;
        float headPitch = (float) Math.toRadians(living.getViewXRot(context.partialTick()));
        float swing = Mth.sin(eased * (float) Math.PI) * 1.2F + Mth.sin(attackTime * (float) Math.PI) * -(headPitch - 0.7F) * 0.75F;
        return Math.toDegrees(-swing);
    }

    @Binding("attack_swing_y")
    public double attack_swing_y() {
        return Math.toDegrees(this.attackBodyYawRad() * 2F);
    }

    @Binding("attack_swing_z")
    public double attack_swing_z() {
        if (!(context.entity() instanceof LivingEntity living)) {
            return 0;
        }

        float attackTime = living.getAttackAnim(context.partialTick());
        return attackTime <= 0F ? 0 : Math.toDegrees(Mth.sin(attackTime * (float) Math.PI) * -0.4F);
    }

    @Override
    public @Nullable ObjectProperty getProperty(@NotNull String name) {
        return null;
    }
}
