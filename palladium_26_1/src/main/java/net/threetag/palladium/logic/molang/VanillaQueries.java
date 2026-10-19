package net.threetag.palladium.logic.molang;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.documentation.MoLangQueryDocumentationBuilder;
import net.threetag.palladium.event.RegisterMoLangQueriesEvent;
import net.threetag.palladium.util.EntityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.binding.Binding;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class VanillaQueries implements ObjectValue {

    public static final String ACTOR_COUNT = "actor_count";
    public static final String BLOCKING = "blocking";
    public static final String BODY_X_ROTATION = "body_x_rotation";
    public static final String BODY_Y_ROTATION = "body_y_rotation";
    public static final String CARDINAL_FACING = "cardinal_facing";
    public static final String CARDINAL_FACING_2D = "cardinal_facing_2d";
    public static final String CARDINAL_PLAYER_FACING = "cardinal_player_facing";
    public static final String DAY = "day";
    public static final String DEATH_TICKS = "death_ticks";
    public static final String DISTANCE_FROM_CAMERA = "distance_from_camera";
    public static final String EQUIPMENT_COUNT = "equipment_count";
    public static final String FRAME_ALPHA = "frame_alpha";
    public static final String GET_ACTOR_INFO_ID = "get_actor_info_id";
    public static final String GROUND_SPEED = "ground_speed";
    public static final String HAS_CAPE = "has_cape";
    public static final String HAS_COLLISION = "has_collision";
    public static final String HAS_GRAVITY = "has_gravity";
    public static final String HAS_HEAD_GEAR = "has_head_gear";
    public static final String HAS_OWNER = "has_owner";
    public static final String HAS_PLAYER_RIDER = "has_player_rider";
    public static final String HAS_RIDER = "has_rider";
    public static final String HEAD_X_ROTATION = "head_x_rotation";
    public static final String HEAD_Y_ROTATION = "head_y_rotation";
    public static final String HEALTH = "health";
    public static final String HURT_TIME = "hurt_time";
    public static final String INVULNERABLE_TICKS = "invulnerable_ticks";
    public static final String IS_ALIVE = "is_alive";
    public static final String IS_ANGRY = "is_angry";
    public static final String IS_BABY = "is_baby";
    public static final String IS_BREATHING = "is_breathing";
    public static final String IS_FIRE_IMMUNE = "is_fire_immune";
    public static final String IS_FIRST_PERSON = "is_first_person";
    public static final String IS_IN_CONTACT_WITH_WATER = "is_in_contact_with_water";
    public static final String IS_IN_LAVA = "is_in_lava";
    public static final String IS_IN_WATER = "is_in_water";
    public static final String IS_IN_WATER_OR_RAIN = "is_in_water_or_rain";
    public static final String IS_INVISIBLE = "is_invisible";
    public static final String IS_LEASHED = "is_leashed";
    public static final String IS_MOVING = "is_moving";
    public static final String IS_ON_FIRE = "is_on_fire";
    public static final String IS_ON_GROUND = "is_on_ground";
    public static final String IS_RIDING = "is_riding";
    public static final String IS_SADDLED = "is_saddled";
    public static final String IS_SILENT = "is_silent";
    public static final String IS_SLEEPING = "is_sleeping";
    public static final String IS_SNEAKING = "is_sneaking";
    public static final String IS_SPRINTING = "is_sprinting";
    public static final String IS_SWIMMING = "is_swimming";
    public static final String IS_USING_ITEM = "is_using_item";
    public static final String IS_WALL_CLIMBING = "is_wall_climbing";
    public static final String LIFE_TIME = "life_time";
    public static final String LIMB_SWING = "limb_swing";
    public static final String LIMB_SWING_AMOUNT = "limb_swing_amount";
    public static final String MAIN_HAND_ITEM_MAX_DURATION = "main_hand_item_max_duration";
    public static final String MAIN_HAND_ITEM_USE_DURATION = "main_hand_item_use_duration";
    public static final String MAX_HEALTH = "max_health";
    public static final String MOON_BRIGHTNESS = "moon_brightness";
    public static final String MOON_PHASE = "moon_phase";
    public static final String MOVEMENT_DIRECTION = "movement_direction";
    public static final String PLAYER_LEVEL = "player_level";
    public static final String RIDER_BODY_X_ROTATION = "rider_body_x_rotation";
    public static final String RIDER_BODY_Y_ROTATION = "rider_body_y_rotation";
    public static final String RIDER_HEAD_X_ROTATION = "rider_head_x_rotation";
    public static final String RIDER_HEAD_Y_ROTATION = "rider_head_y_rotation";
    public static final String SCALE = "scale";
    public static final String SLEEP_ROTATION = "sleep_rotation";
    public static final String TIME_OF_DAY = "time_of_day";
    public static final String TIME_STAMP = "time_stamp";
    public static final String VERTICAL_SPEED = "vertical_speed";
    public static final String YAW_SPEED = "yaw_speed";

    @SubscribeEvent
    static void registerMoLang(RegisterMoLangQueriesEvent e) {
        e.register("query", VanillaQueries.class, VanillaQueries::new, VanillaQueries::addDocumentation);
    }

    static void addDocumentation(MoLangQueryDocumentationBuilder builder) {
        builder.setDescription("Queries modelled after the ones in Bedrock Edition. Also available as \"q\".")
                .add(ACTOR_COUNT, "Always 1. Exists for compatibility with Bedrock animations.")
                .add(BLOCKING, "Whether the entity is blocking with a shield.")
                .add(BODY_X_ROTATION, "The entity's pitch in degrees.")
                .add(BODY_Y_ROTATION, "The body yaw in degrees. For entities without a separate body rotation this is the view yaw.")
                .add(CARDINAL_FACING, "The direction the entity faces as a number: 0 down, 1 up, 2 north, 3 south, 4 west, 5 east.")
                .add(CARDINAL_FACING_2D, "Like cardinal_facing, but up and down are reported as 6, so only the four horizontal directions remain.")
                .add(CARDINAL_PLAYER_FACING, "The direction the entity faces, using the same numbers as cardinal_facing.")
                .add(DAY, "The number of in-game days that have passed, with the time of day as the fraction.")
                .add(DEATH_TICKS, "Ticks since the entity died. 0 while alive.")
                .add(DISTANCE_FROM_CAMERA, "Always 0. Exists for compatibility with Bedrock animations.")
                .add(EQUIPMENT_COUNT, "How many armor slots are filled.")
                .add(FRAME_ALPHA, "The progress between the last and the next tick, from 0 to 1. Useful for smooth motion.")
                .add(GET_ACTOR_INFO_ID, "The entity's numeric ID in the current world.")
                .add(GROUND_SPEED, "The horizontal speed in blocks per tick.")
                .add(HAS_CAPE, "Whether the player has a cape that is visible.")
                .add(HAS_COLLISION, "Whether the entity collides with blocks.")
                .add(HAS_GRAVITY, "Whether the entity is affected by gravity.")
                .add(HAS_HEAD_GEAR, "Whether something is worn in the head slot.")
                .add(HAS_OWNER, "Whether the entity is tamed or otherwise owned.")
                .add(HAS_PLAYER_RIDER, "Whether a player is riding the entity.")
                .add(HAS_RIDER, "Whether anything is riding the entity.")
                .add(HEAD_X_ROTATION, "The head pitch in degrees. Negative when looking up.")
                .add(HEAD_Y_ROTATION, "The head yaw in degrees, in world space.")
                .add(HEALTH, "The current health.")
                .add(HURT_TIME, "Counts down from 10 after the entity took damage. 0 when not hurt.")
                .add(INVULNERABLE_TICKS, "Remaining ticks of damage immunity after getting hurt.")
                .add(IS_ALIVE, "Whether the entity is alive.")
                .add(IS_ANGRY, "Whether a neutral mob, like a wolf or enderman, is angry.")
                .add(IS_BABY, "Whether the entity is a baby.")
                .add(IS_BREATHING, "Whether the entity's air supply is full.")
                .add(IS_FIRE_IMMUNE, "Whether the entity type is immune to fire.")
                .add(IS_FIRST_PERSON, "Whether the entity is the local player viewed in first person.")
                .add(IS_IN_CONTACT_WITH_WATER, "Whether the entity is in water or standing in rain.")
                .add(IS_IN_LAVA, "Whether the entity is in lava.")
                .add(IS_IN_WATER, "Whether the entity is in water.")
                .add(IS_IN_WATER_OR_RAIN, "Whether the entity is in water or standing in rain.")
                .add(IS_INVISIBLE, "Whether the entity is invisible.")
                .add(IS_LEASHED, "Whether the entity is on a lead.")
                .add(IS_MOVING, "Whether the entity moved since the last tick.")
                .add(IS_ON_FIRE, "Whether the entity is burning.")
                .add(IS_ON_GROUND, "Whether the entity stands on the ground.")
                .add(IS_RIDING, "Whether the entity is riding something.")
                .add(IS_SADDLED, "Whether the entity wears a saddle.")
                .add(IS_SILENT, "Whether the entity is silent.")
                .add(IS_SLEEPING, "Whether the entity is sleeping in a bed.")
                .add(IS_SNEAKING, "Whether the entity is crouching.")
                .add(IS_SPRINTING, "Whether the entity is sprinting.")
                .add(IS_SWIMMING, "Whether the entity is swimming.")
                .add(IS_USING_ITEM, "Whether the entity is using an item, like eating or drawing a bow.")
                .add(IS_WALL_CLIMBING, "Whether the entity is on a ladder, vine or another climbable block. For Palladium's wall climbing ability use palladium.is_wall_climbing.")
                .add(LIFE_TIME, "Always 0. Exists for compatibility with Bedrock animations.")
                .add(LIMB_SWING, "How far the legs have swung in the walking animation. Grows continuously while walking.")
                .add(LIMB_SWING_AMOUNT, "How strongly the walking animation plays, from 0 when standing still to about 1 when running.")
                .add(MAIN_HAND_ITEM_MAX_DURATION, "How many ticks the item in the main hand can be used for, like a bow's draw time.")
                .add(MAIN_HAND_ITEM_USE_DURATION, "How long the item in the main hand has been used, in seconds. 0 when it is not being used.")
                .add(MAX_HEALTH, "The maximum health.")
                .add(MOON_BRIGHTNESS, "The current brightness of the stars, from 0 during the day to 1 at night.")
                .add(MOON_PHASE, "The moon phase, from 0 (full moon) to 7.")
                .add(MOVEMENT_DIRECTION, "The direction the entity is moving in, using the same numbers as cardinal_facing. 6 when not moving.")
                .add(PLAYER_LEVEL, "The player's experience level. 0 for other entities.")
                .add(RIDER_BODY_X_ROTATION, "The pitch of the first rider in degrees. 0 without a rider.")
                .add(RIDER_BODY_Y_ROTATION, "The body yaw of the first rider in degrees. 0 without a rider.")
                .add(RIDER_HEAD_X_ROTATION, "The head pitch of the first rider in degrees. 0 without a rider.")
                .add(RIDER_HEAD_Y_ROTATION, "The head yaw of the first rider in degrees. 0 without a rider.")
                .add(SCALE, "The entity's scale, for example from the scale attribute. 1 by default.")
                .add(SLEEP_ROTATION, "The yaw of the bed the entity sleeps in, in degrees. 0 when not sleeping.")
                .add(TIME_OF_DAY, "The time of day from 0 to 1. 0 is sunrise, 0.25 noon, 0.5 sunset and 0.75 midnight.")
                .add(TIME_STAMP, "The total game time in ticks.")
                .add(VERTICAL_SPEED, "The vertical speed in blocks per tick. Positive when moving up.")
                .add(YAW_SPEED, "How far the yaw changed since the last tick, in degrees.");
    }

    private final EntityContext context;
    private final Map<String, Supplier<Object>> functions = new HashMap<>();

    public VanillaQueries(EntityContext context) {
        this.context = context;

        this.functions.put(ACTOR_COUNT, this::actor_count);
        this.functions.put(BLOCKING, this::blocking);
        this.functions.put(BODY_X_ROTATION, this::body_x_rotation);
        this.functions.put(BODY_Y_ROTATION, this::body_y_rotation);
        this.functions.put(CARDINAL_FACING, this::cardinal_facing);
        this.functions.put(CARDINAL_FACING_2D, this::cardinal_facing_2d);
        this.functions.put(CARDINAL_PLAYER_FACING, this::cardinal_player_facing);
        this.functions.put(DAY, this::day);
        this.functions.put(DEATH_TICKS, this::death_ticks);
        this.functions.put(DISTANCE_FROM_CAMERA, this::distance_from_camera);
        this.functions.put(EQUIPMENT_COUNT, this::equipment_count);
        this.functions.put(FRAME_ALPHA, this::frame_alpha);
        this.functions.put(GET_ACTOR_INFO_ID, this::get_actor_info_id);
        this.functions.put(GROUND_SPEED, this::ground_speed);
        this.functions.put(HAS_CAPE, this::has_cape);
        this.functions.put(HAS_COLLISION, this::has_collision);
        this.functions.put(HAS_GRAVITY, this::has_gravity);
        this.functions.put(HAS_HEAD_GEAR, this::has_head_gear);
        this.functions.put(HAS_OWNER, this::has_owner);
        this.functions.put(HAS_PLAYER_RIDER, this::has_player_rider);
        this.functions.put(HAS_RIDER, this::has_rider);
        this.functions.put(HEAD_X_ROTATION, this::head_x_rotation);
        this.functions.put(HEAD_Y_ROTATION, this::head_y_rotation);
        this.functions.put(HEALTH, this::health);
        this.functions.put(HURT_TIME, this::hurt_time);
        this.functions.put(INVULNERABLE_TICKS, this::invulnerable_ticks);
        this.functions.put(IS_ALIVE, this::is_alive);
        this.functions.put(IS_ANGRY, this::is_angry);
        this.functions.put(IS_BABY, this::is_baby);
        this.functions.put(IS_BREATHING, this::is_breathing);
        this.functions.put(IS_FIRE_IMMUNE, this::is_fire_immune);
        this.functions.put(IS_FIRST_PERSON, this::is_first_person);
        this.functions.put(IS_IN_CONTACT_WITH_WATER, this::is_in_contact_with_water);
        this.functions.put(IS_IN_LAVA, this::is_in_lava);
        this.functions.put(IS_IN_WATER, this::is_in_water);
        this.functions.put(IS_IN_WATER_OR_RAIN, this::is_in_water_or_rain);
        this.functions.put(IS_INVISIBLE, this::is_invisible);
        this.functions.put(IS_LEASHED, this::is_leashed);
        this.functions.put(IS_MOVING, this::is_moving);
        this.functions.put(IS_ON_FIRE, this::is_on_fire);
        this.functions.put(IS_ON_GROUND, this::is_on_ground);
        this.functions.put(IS_RIDING, this::is_riding);
        this.functions.put(IS_SADDLED, this::is_saddled);
        this.functions.put(IS_SILENT, this::is_silent);
        this.functions.put(IS_SLEEPING, this::is_sleeping);
        this.functions.put(IS_SNEAKING, this::is_sneaking);
        this.functions.put(IS_SPRINTING, this::is_sprinting);
        this.functions.put(IS_SWIMMING, this::is_swimming);
        this.functions.put(IS_USING_ITEM, this::is_using_item);
        this.functions.put(IS_WALL_CLIMBING, this::is_wall_climbing);
        this.functions.put(LIFE_TIME, this::life_time);
        this.functions.put(LIMB_SWING, this::limb_swing);
        this.functions.put(LIMB_SWING_AMOUNT, this::limb_swing_amount);
        this.functions.put(MAIN_HAND_ITEM_MAX_DURATION, this::main_hand_item_max_duration);
        this.functions.put(MAIN_HAND_ITEM_USE_DURATION, this::main_hand_item_use_duration);
        this.functions.put(MAX_HEALTH, this::max_health);
        this.functions.put(MOON_BRIGHTNESS, this::moon_brightness);
        this.functions.put(MOON_PHASE, this::moon_phase);
        this.functions.put(MOVEMENT_DIRECTION, this::movement_direction);
        this.functions.put(PLAYER_LEVEL, this::player_level);
        this.functions.put(RIDER_BODY_X_ROTATION, this::rider_body_x_rotation);
        this.functions.put(RIDER_BODY_Y_ROTATION, this::rider_body_y_rotation);
        this.functions.put(RIDER_HEAD_X_ROTATION, this::rider_head_x_rotation);
        this.functions.put(RIDER_HEAD_Y_ROTATION, this::rider_head_y_rotation);
        this.functions.put(SCALE, this::scale);
        this.functions.put(SLEEP_ROTATION, this::sleep_rotation);
        this.functions.put(TIME_OF_DAY, this::time_of_day);
        this.functions.put(TIME_STAMP, this::time_stamp);
        this.functions.put(VERTICAL_SPEED, this::vertical_speed);
        this.functions.put(YAW_SPEED, this::yaw_speed);
    }

    @Override
    public @NotNull Value get(@NotNull String name) {
        if (this.functions.containsKey(name)) {
            return Value.of(this.functions.get(name).get());
        }
        return Value.nil();
    }

    @Override
    public @Nullable ObjectProperty getProperty(@NotNull String name) {
        return null;
    }

    @Binding(ACTOR_COUNT)
    public double actor_count() {
        return 1;
    }

    @Binding(CARDINAL_PLAYER_FACING)
    public double cardinal_player_facing() {
        return context.entity().getDirection().ordinal();
    }

    @Binding(DAY)
    public double day() {
        return context.level().getGameTime() / 24000D;
    }

    @Binding(FRAME_ALPHA)
    public double frame_alpha() {
        return context.partialTick();
    }

    @Binding(HAS_CAPE)
    public boolean has_cape() {
        return Palladium.PROXY.hasCape(context.entity());
    }

    @Binding(IS_FIRST_PERSON)
    public boolean is_first_person() {
        return Palladium.PROXY.isFirstPerson(context.entity());
    }

    @Binding(LIFE_TIME)
    public double life_time() {
        return 0D;
    }

    @Binding(MOON_BRIGHTNESS)
    public double moon_brightness() {
        return context.level().environmentAttributes().getValue(EnvironmentAttributes.STAR_BRIGHTNESS,
                context.entity().position());
    }

    @Binding(MOON_PHASE)
    public double moon_phase() {
        return context.level().environmentAttributes()
                .getValue(EnvironmentAttributes.MOON_PHASE, context.entity().position()).index();
    }

    @Binding(PLAYER_LEVEL)
    public double player_level() {
        return context.entity() instanceof Player player ? player.experienceLevel : 0D;
    }

    @Binding(TIME_OF_DAY)
    public double time_of_day() {
        return context.level().getDefaultClockTime() / 24000D;
    }

    @Binding(TIME_STAMP)
    public double time_stamp() {
        return context.level().getGameTime();
    }

    @Binding(BODY_X_ROTATION)
    public double body_x_rotation() {
        return context.entity().getViewXRot(context.partialTick());
    }

    @Binding(BODY_Y_ROTATION)
    public double body_y_rotation() {
        return context.entity() instanceof LivingEntity living
                ? Mth.lerp(context.partialTick(), living.yBodyRotO, living.yBodyRot)
                : context.entity().getViewYRot(context.partialTick());
    }

    @Binding(CARDINAL_FACING)
    public double cardinal_facing() {
        return context.entity().getDirection().get3DDataValue();
    }

    @Binding(CARDINAL_FACING_2D)
    public double cardinal_facing_2d() {
        int direction = context.entity().getDirection().get3DDataValue();
        return direction < 2 ? 6 : direction;
    }

    @Binding(DISTANCE_FROM_CAMERA)
    public double distance_from_camera() {
        return 0D;
    }

    @Binding(GET_ACTOR_INFO_ID)
    public double get_actor_info_id() {
        return context.entity().getId();
    }

    @Binding(EQUIPMENT_COUNT)
    public double equipment_count() {
        return context.entity() instanceof EquipmentUser armorable ? Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmor).filter(slot -> !armorable.getItemBySlot(slot).isEmpty()).count() : 0;
    }

    @Binding(HAS_COLLISION)
    public boolean has_collision() {
        return !context.entity().noPhysics;
    }

    @Binding(HAS_GRAVITY)
    public boolean has_gravity() {
        return !context.entity().isNoGravity();
    }

    @Binding(HAS_OWNER)
    public boolean has_owner() {
        return context.entity() instanceof OwnableEntity ownable && ownable.getOwnerReference() != null;
    }

    @Binding(HAS_PLAYER_RIDER)
    public boolean has_player_rider() {
        return context.entity().hasPassenger(Player.class::isInstance);
    }

    @Binding(HAS_RIDER)
    public boolean has_rider() {
        return context.entity().isVehicle();
    }

    @Binding(IS_ALIVE)
    public boolean is_alive() {
        return context.entity().isAlive();
    }

    @Binding(IS_ANGRY)
    public boolean is_angry() {
        return context.entity() instanceof NeutralMob neutralMob && neutralMob.isAngry();
    }

    @Binding(IS_BREATHING)
    public boolean is_breathing() {
        return context.entity().getAirSupply() >= context.entity().getMaxAirSupply();
    }

    @Binding(IS_FIRE_IMMUNE)
    public boolean is_fire_immune() {
        return context.entity().getType().fireImmune();
    }

    @Binding(IS_INVISIBLE)
    public boolean is_invisible() {
        return context.entity().isInvisible();
    }

    @Binding(IS_IN_CONTACT_WITH_WATER)
    public boolean is_in_contact_with_water() {
        return context.entity().isInWaterOrRain();
    }

    @Binding(IS_IN_LAVA)
    public boolean is_in_lava() {
        return context.entity().isInLava();
    }

    @Binding(IS_IN_WATER)
    public boolean is_in_water() {
        return context.entity().isInWater();
    }

    @Binding(IS_IN_WATER_OR_RAIN)
    public boolean is_in_water_or_rain() {
        return context.entity().isInWaterOrRain();
    }

    @Binding(IS_LEASHED)
    public boolean is_leashed() {
        return context.entity() instanceof Leashable leashable && leashable.isLeashed();
    }

    @Binding(IS_MOVING)
    public boolean is_moving() {
        return EntityUtil.isMoving(context.entity());
    }

    @Binding(IS_ON_FIRE)
    public boolean is_on_fire() {
        return context.entity().isOnFire();
    }

    @Binding(IS_ON_GROUND)
    public boolean is_on_ground() {
        return context.entity().onGround();
    }

    @Binding(IS_RIDING)
    public boolean is_riding() {
        return context.entity().isPassenger();
    }

    @Binding(IS_SADDLED)
    public boolean is_saddled() {
        return context.entity() instanceof EquipmentUser equipmentUser
                && !equipmentUser.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
    }

    @Binding(IS_SILENT)
    public boolean is_silent() {
        return context.entity().isSilent();
    }

    @Binding(IS_SNEAKING)
    public boolean is_sneaking() {
        return context.entity().isCrouching();
    }

    @Binding(IS_SPRINTING)
    public boolean is_sprinting() {
        return context.entity().isSprinting();
    }

    @Binding(IS_SWIMMING)
    public boolean is_swimming() {
        return context.entity().isSwimming();
    }

    @Binding(MOVEMENT_DIRECTION)
    public double movement_direction() {
        return EntityUtil.isMoving(context.entity())
                ? Direction.getApproximateNearest(context.entity().getDeltaMovement()).get3DDataValue()
                : 6;
    }

    @Binding(RIDER_BODY_X_ROTATION)
    public double rider_body_x_rotation() {
        return context.entity().isVehicle() ? context.entity().getFirstPassenger() instanceof LivingEntity ? 0
                : Objects.requireNonNull(context.entity().getFirstPassenger()).getViewXRot(context.partialTick()) : 0;
    }

    @Binding(RIDER_BODY_Y_ROTATION)
    public double rider_body_y_rotation() {
        return context.entity().isVehicle() ? context.entity().getFirstPassenger() instanceof LivingEntity living
                ? Mth.lerp(context.partialTick(), living.yBodyRotO, living.yBodyRot)
                : Objects.requireNonNull(context.entity().getFirstPassenger()).getViewYRot(context.partialTick()) : 0;
    }

    @Binding(RIDER_HEAD_X_ROTATION)
    public double rider_head_x_rotation() {
        return context.entity().getFirstPassenger() instanceof LivingEntity living
                ? living.getViewXRot(context.partialTick())
                : 0;
    }

    @Binding(RIDER_HEAD_Y_ROTATION)
    public double rider_head_y_rotation() {
        return context.entity().getFirstPassenger() instanceof LivingEntity living
                ? living.getViewYRot(context.partialTick())
                : 0;
    }

    @Binding(VERTICAL_SPEED)
    public double vertical_speed() {
        return context.entity().getDeltaMovement().y();
    }

    @Binding(YAW_SPEED)
    public double yaw_speed() {
        return context.entity().getYRot(context.partialTick()) - context.entity().yRotO;
    }

    @Binding(BLOCKING)
    public boolean blocking() {
        return context.entity() instanceof LivingEntity living && living.isBlocking();
    }

    @Binding(DEATH_TICKS)
    public double death_ticks() {
        return context.entity() instanceof LivingEntity living
                ? living.deathTime == 0 ? 0 : living.deathTime + context.partialTick()
                : 0;
    }

    @Binding(GROUND_SPEED)
    public double ground_speed() {
        return context.entity().getDeltaMovement().horizontalDistance();
    }

    @Binding(HAS_HEAD_GEAR)
    public boolean has_head_gear() {
        return context.entity() instanceof EquipmentUser equipmentUser
                && !equipmentUser.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
    }

    @Binding(HEAD_X_ROTATION)
    public double head_x_rotation() {
        return context.entity().getViewXRot(context.partialTick());
    }

    @Binding(HEAD_Y_ROTATION)
    public double head_y_rotation() {
        return context.entity().getViewYRot(context.partialTick());
    }

    @Binding(HEALTH)
    public double health() {
        return context.entity() instanceof LivingEntity living ? living.getHealth() : 0;
    }

    @Binding(HURT_TIME)
    public double hurt_time() {
        return context.entity() instanceof LivingEntity living
                ? living.hurtTime == 0 ? 0 : living.hurtTime - context.partialTick()
                : 0;
    }

    @Binding(INVULNERABLE_TICKS)
    public double invulnerable_ticks() {
        return context.entity() instanceof LivingEntity living
                ? living.invulnerableTime == 0 ? 0 : living.invulnerableTime - context.partialTick()
                : 0;
    }

    @Binding(IS_BABY)
    public boolean is_baby() {
        return context.entity() instanceof LivingEntity living && living.isBaby();
    }

    @Binding(IS_SLEEPING)
    public boolean is_sleeping() {
        return context.entity() instanceof LivingEntity living && living.isSleeping();
    }

    @Binding(IS_USING_ITEM)
    public boolean is_using_item() {
        return context.entity() instanceof LivingEntity living && living.isUsingItem();
    }

    @Binding(IS_WALL_CLIMBING)
    public boolean is_wall_climbing() {
        return context.entity() instanceof LivingEntity living && living.onClimbable();
    }

    @Binding(LIMB_SWING)
    public double limb_swing() {
        return context.entity() instanceof LivingEntity living ? living.walkAnimation.position(context.partialTick()) : 0;
    }

    @Binding(LIMB_SWING_AMOUNT)
    public double limb_swing_amount() {
        return context.entity() instanceof LivingEntity living ? living.walkAnimation.speed(context.partialTick()) : 0;
    }

    @Binding(MAIN_HAND_ITEM_MAX_DURATION)
    public double main_hand_item_max_duration() {
        return context.entity() instanceof LivingEntity living ? living.getMainHandItem().getUseDuration(living) : 0;
    }

    @Binding(MAIN_HAND_ITEM_USE_DURATION)
    public double main_hand_item_use_duration() {
        return context.entity() instanceof LivingEntity living && living.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? (living.getTicksUsingItem() / 20D) + context.partialTick()
                : 0;
    }

    @Binding(MAX_HEALTH)
    public double max_health() {
        return context.entity() instanceof LivingEntity living ? living.getMaxHealth() : 0D;
    }

    @Binding(SCALE)
    public double scale() {
        return context.entity() instanceof LivingEntity living ? living.getScale() : 1D;
    }

    @Binding(SLEEP_ROTATION)
    public double sleep_rotation() {
        return Optional.ofNullable(context.entity() instanceof LivingEntity living ? living.getBedOrientation() : null)
                .map(Direction::toYRot).orElse(0f);
    }
}
